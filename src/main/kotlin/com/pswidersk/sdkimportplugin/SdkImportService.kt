package com.pswidersk.sdkimportplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.project.stateStore

@Service(Service.Level.PROJECT)
class SdkImportService(private val project: Project) {

    fun runImport() = ApplicationManager.getApplication().invokeAndWait {
        execSafe(project) {
            val sdkImportConfig = loadConfig()

            sdkImportConfig.import.forEach { sdkConfig ->
                SdkProcessor.EP_NAME.extensionList.forEach {
                    execSafe(project) { it.applySdk(project, sdkConfig) }
                }
            }
        }
    }

    private fun loadConfig(): SdkImportConfig {
        val dotIdeaDir = project.stateStore.directoryStorePath
        dotIdeaDir?.let {
            var sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yml")
            if (!sdkImportFile.exists()) sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yaml")
            if (sdkImportFile.exists()) return sdkImportFile.loadAsYamlImportConfig()
        }
        missingConfigNotif(project)
        return SdkImportConfig()
    }

}
