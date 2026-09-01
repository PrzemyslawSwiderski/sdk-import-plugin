package com.pswidersk.sdkimportplugin.python

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jdom.Element
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

private val pythonSdkRegistrationMutex = Mutex()

class PythonSdkProcessor(private val cs: CoroutineScope) : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == PYTHON_SDK_TYPE) {
            project.withModule(sdkConfig.module) { module ->
                cs.launch {
                    try {
                        addPythonSdk(project, module, sdkConfig)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        errorNotif(project, exception)
                    }
                }
            }
        }
    }

    internal suspend fun addPythonSdk(project: Project, module: Module, sdkConfig: SdkImportConfigEntry) {
        val configuredPath = sdkConfig.loadSdkFile(project).toNioPath()
        val pythonSdkName = "Python env: $configuredPath"
        val pythonExecutable = resolvePythonExecutable(configuredPath)
        val sdkHome = writeAction {
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(pythonExecutable)
        } ?: throw IllegalArgumentException("Python executable can not be loaded: `$pythonExecutable`")
        val (sdk, created) = pythonSdkRegistrationMutex.withLock {
            withContext(Dispatchers.EDT) {
                val sdkTable = ProjectJdkTable.getInstance()
                val pythonSdkType = PythonSdkType.getInstance()
                val registeredSdk = findRegisteredPythonSdk(
                    pythonExecutable,
                    sdkTable.allJdks.filter { it.sdkType == pythonSdkType },
                )

                registeredSdk?.let { it to false } ?: run {
                    val newSdk = SdkConfigurationUtil.createSdk(
                        sdkTable.allJdks.asList(),
                        sdkHome,
                        pythonSdkType,
                        null,
                        pythonSdkName,
                    )
                    val additionalData = pythonSdkType.loadAdditionalData(newSdk, Element("additional"))
                    ApplicationManager.getApplication().runWriteAction {
                        newSdk.sdkModificator.apply {
                            sdkAdditionalData = additionalData
                            commitChanges()
                        }
                    }
                    pythonSdkType.setupSdkPaths(newSdk)
                    SdkConfigurationUtil.addSdk(newSdk)
                    newSdk to true
                }
            }
        }

        if (created) {
            newPythonSdkNotif(project, pythonSdkName)
        }
        setModuleSdk(project, module, sdk)
    }

    private suspend fun setModuleSdk(project: Project, module: Module, sdk: Sdk) {
        val (moduleSdkChanged, projectSdkChanged) = readAction {
            val moduleSdkChanged = module.pythonSdk != sdk
            val isProjectRootModule = project.basePath?.let { projectBasePath ->
                ModuleRootManager.getInstance(module).contentRoots.any {
                    FileUtil.pathsEqual(projectBasePath, it.path)
                }
            } == true
            val projectSdkChanged = isProjectRootModule && project.pythonSdk != sdk
            moduleSdkChanged to projectSdkChanged
        }
        edtWriteAction {
            if (moduleSdkChanged) {
                ModuleRootModificationUtil.setModuleSdk(module, sdk)
            }
            if (projectSdkChanged) {
                ProjectRootManager.getInstance(project).projectSdk = sdk
            }
        }
        if (moduleSdkChanged) {
            changedModulePythonSdkNotif(module, sdk.name)
        }
    }
}

internal fun resolvePythonExecutable(configuredPath: Path): Path {
    val normalizedPath = configuredPath.toAbsolutePath().normalize()
    if (!normalizedPath.isDirectory()) return normalizedPath

    return sequenceOf(normalizedPath, normalizedPath.resolve(".venv"))
        .flatMap { environment ->
            sequenceOf(
                environment.resolve("bin/python"),
                environment.resolve("bin/python3"),
                environment.resolve("Scripts/python.exe"),
            )
        }
        .firstOrNull(Path::isRegularFile)
        ?: throw IllegalArgumentException("Python executable can not be found in: `$normalizedPath`")
}

internal fun findRegisteredPythonSdk(pythonExecutable: Path, sdks: Iterable<Sdk>): Sdk? {
    val expectedPath = FileUtil.toCanonicalPath(pythonExecutable.toAbsolutePath().normalize().toString())
    return sdks.firstOrNull { sdk ->
        sdk.homePath?.let { FileUtil.pathsEqual(FileUtil.toCanonicalPath(it), expectedPath) } == true
    }
}
