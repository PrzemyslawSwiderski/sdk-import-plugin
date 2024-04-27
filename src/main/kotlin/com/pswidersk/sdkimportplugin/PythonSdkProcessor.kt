package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.SdkImportBundle.message


class PythonSdkProcessor : SdkProcessor {

    private val logger = thisLogger()

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == SdkType.PYTHON) {
            addPythonSdk(project, sdkConfig)
        }
    }

    private fun addPythonSdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        val sdkHome = sdkConfig.loadSdkFile()
        val sdkTable = ProjectJdkTable.getInstance()
        val pythonSdkName = "Python env: ${sdkConfig.path}"
        val tableSdk = sdkTable.findJdk(pythonSdkName)
        val sdk = if (tableSdk != null) tableSdk else {
            val pythonSdk = SdkConfigurationUtil.setupSdk(
                emptyArray(), sdkHome, PythonSdkType.getInstance(), true, null, pythonSdkName
            )!!
            withWriteAction {
                sdkTable.addJdk(pythonSdk)
            }
            notifyAboutNewSdk(project, pythonSdkName)
            pythonSdk
        }
        project.withModule(sdkConfig.module) {
            setModuleSdk(it, sdk)
        }
    }

    private fun setModuleSdk(module: Module, sdk: Sdk) {
        logger.info("Setting Python SDK to module: ${module.name}")
        if (module.pythonSdk?.name != sdk.name) {
            module.pythonSdk = sdk
            notifyAboutSdkChange(module, sdk.name)
        }
    }

    private fun notifyAboutNewSdk(project: Project, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            message("notification.newPythonSdk.title"),
            message("notification.newPythonSdk.content", sdkName),
            NotificationType.INFORMATION
        ).notify(project)
    }

    private fun notifyAboutSdkChange(module: Module, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            message("notification.newModulePythonSdk.title"),
            message("notification.newModulePythonSdk.content", module.name, sdkName),
            NotificationType.INFORMATION
        ).notify(module.project)
    }
}