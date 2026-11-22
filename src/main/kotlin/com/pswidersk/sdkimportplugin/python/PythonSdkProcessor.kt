package com.pswidersk.sdkimportplugin.python

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.*
import org.jdom.Element
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class PythonSdkProcessor : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == PYTHON_SDK_TYPE) {
            project.withModule(sdkConfig.module) { module ->
                try {
                    addPythonSdk(project, module, sdkConfig)
                } catch (exception: Exception) {
                    errorNotif(project, exception)
                }
            }
        }
    }

    internal fun addPythonSdk(project: Project, module: Module, sdkConfig: SdkImportConfigEntry) {
        val configuredPath = sdkConfig.loadSdkFile(project).toNioPath()
        val pythonSdkName = "Python env: $configuredPath"
        val pythonExecutable = resolvePythonExecutable(configuredPath)

        val (sdk, created) = WriteAction.computeAndWait<Pair<Sdk, Boolean>, RuntimeException> {
            val sdkHome = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(pythonExecutable)
                ?: throw IllegalArgumentException("Python executable can not be loaded: `$pythonExecutable`")
            val sdkTable = ProjectJdkTable.getInstance()
            val pythonSdkType = PythonSdkType.getInstance()

            findRegisteredPythonSdk(
                pythonExecutable,
                sdkTable.allJdks.filter { it.sdkType == pythonSdkType },
            )?.let { it to false } ?: run {
                val newSdk = SdkConfigurationUtil.createSdk(
                    sdkTable.allJdks.asList(),
                    sdkHome,
                    pythonSdkType,
                    null,
                    pythonSdkName,
                )
                newSdk.sdkModificator.apply {
                    sdkAdditionalData = pythonSdkType.loadAdditionalData(newSdk, Element("additional"))
                    commitChanges()
                }
                pythonSdkType.setupSdkPaths(newSdk)
                SdkConfigurationUtil.addSdk(newSdk)
                newSdk to true
            }
        }

        if (created) {
            newPythonSdkNotif(project, pythonSdkName)
        }
        setModuleSdk(project, module, sdk)
    }


    private fun setModuleSdk(project: Project, module: Module, sdk: Sdk) {
        val (moduleSdkChanged, projectSdkChanged) = ReadAction.computeBlocking<Pair<Boolean, Boolean>, RuntimeException> {
            val moduleSdkChanged = module.pythonSdk != sdk
            val isProjectRootModule = project.basePath?.let { projectBasePath ->
                ModuleRootManager.getInstance(module).contentRoots.any {
                    FileUtil.pathsEqual(projectBasePath, it.path)
                }
            } == true
            val projectSdkChanged = isProjectRootModule && project.pythonSdk != sdk
            moduleSdkChanged to projectSdkChanged
        }

        WriteAction.runAndWait<RuntimeException> {
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