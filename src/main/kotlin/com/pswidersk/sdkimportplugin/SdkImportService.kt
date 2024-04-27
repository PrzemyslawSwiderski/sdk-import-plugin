package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.project.stateStore

@Service(Service.Level.PROJECT)
class SdkImportService(private val project: Project) {

    fun runImport() {
        ApplicationManager.getApplication().invokeAndWait {
            val sdkImportConfig = loadConfig()

            sdkImportConfig.import.forEach { sdkConfig ->
                SdkProcessor.EP_NAME.extensionList.forEach {
                    tryToApply(it, sdkConfig)
                }
            }
        }
    }

    private fun tryToApply(
        processor: SdkProcessor,
        sdkConfig: SdkImportConfigEntry
    ) {
        try {
            processor.applySdk(project, sdkConfig)
        } catch (exception: Exception) {
            val exceptionMessage = exception.message ?: "N/A"
            val msg = SdkImportBundle.message("notification.exception.content", exceptionMessage)
            notifyAboutException(msg)
        }
    }

    private fun loadConfig(): SdkImportConfig {
        val dotIdeaDir = project.stateStore.directoryStorePath
        dotIdeaDir?.let {
            var sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yml")
            if (!sdkImportFile.exists()) sdkImportFile = dotIdeaDir.toFile().resolve("sdk-import.yaml")
            if (sdkImportFile.exists()) return sdkImportFile.loadAsYamlImportConfig()
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

    private fun notifyAboutException(msg: String) {
        Notification(
            NOTIFICATION_GROUP,
            SdkImportBundle.message("notification.exception.title"),
            msg,
            NotificationType.ERROR
        ).notify(project)
    }
}
