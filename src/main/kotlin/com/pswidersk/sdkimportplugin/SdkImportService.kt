package com.pswidersk.sdkimportplugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.project.stateStore

@Service(Service.Level.PROJECT)
class SdkImportService(private val project: Project) {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    fun runImport() {
        ApplicationManager.getApplication().invokeAndWait {
            val sdkImportConfig = loadConfig()

            sdkImportConfig.import.forEach { sdkConfig ->
                SdkProcessor.EP_NAME.extensionList.forEach { it.applySdk(project, sdkConfig) }
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
        notifyAboutMissingConfig()
        return SdkImportConfig()
    }

    private fun notifyAboutMissingConfig() {
        Notification(
            NOTIFICATION_GROUP,
            SdkImportBundle.message("notification.missingConfig.title"),
            SdkImportBundle.message("notification.missingConfig.content"),
            NotificationType.WARNING
        ).notify(project)
    }

}
