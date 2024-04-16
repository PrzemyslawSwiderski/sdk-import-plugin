package com.pswidersk.sdkimportplugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.stateStore
import com.jetbrains.python.sdk.PythonSdkType

@Service(Service.Level.PROJECT)
class SdkImportService(private val project: Project) {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    private val facetImportService
        get() = project.service<FacetImportService>()

    private val moduleManager
        get() = ModuleManager.getInstance(project)

    fun runImport() {
        ApplicationManager.getApplication().invokeAndWait {
            val sdkImportConfig = loadConfig()

            sdkImportConfig.import.forEach { sdkConfig ->
                when (sdkConfig.type) {
                    SdkType.PYTHON -> {
                        val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
                            LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkConfig.path)
                        }
                        val sdkTable = ProjectJdkTable.getInstance()
                        val pythonSdkName = sdkConfig.module + " Python env"
                        val tableSdk = sdkTable.findJdk(pythonSdkName)
                        val sdk = if (tableSdk != null) tableSdk else {
                            val pythonSdk = SdkConfigurationUtil.setupSdk(
                                emptyArray(), sdkHome, PythonSdkType.getInstance(), true, null, pythonSdkName
                            )!!
                            WriteAction.run<Throwable> {
                                sdkTable.addJdk(pythonSdk)
                            }
                            pythonSdk
                        }
                        moduleManager.findModuleByName(sdkConfig.module)?.let {
                            facetImportService.addFacet(it, sdk)
                        }
                    }
                }
            }
        }
    }

    private fun loadConfig(): SdkImportConfig {
        val dotIdeaDir = project.stateStore.directoryStorePath
        dotIdeaDir?.let {
            var sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yml")
            if (!sdkImportFile.exists()) sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yaml")
            if (sdkImportFile.exists()) return mapper.readValue(sdkImportFile)
        }

        return SdkImportConfig()
    }

}
