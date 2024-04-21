package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk


class PythonSdkProcessor : SdkProcessor {

    private val logger = thisLogger()

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        when (sdkConfig.type) {
            SdkType.PYTHON -> {
                addPythonSdk(project, sdkConfig)
            }
        }
    }

    private fun addPythonSdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
            LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkConfig.path)
        }
        val sdkTable = ProjectJdkTable.getInstance()
        val pythonSdkName = "Python env: ${sdkConfig.path}"
        val tableSdk = sdkTable.findJdk(pythonSdkName)
        val sdk = if (tableSdk != null) tableSdk else {
            val pythonSdk = SdkConfigurationUtil.setupSdk(
                emptyArray(), sdkHome, PythonSdkType.getInstance(), true, null, pythonSdkName
            )!!
            WriteAction.run<Throwable> {
                sdkTable.addJdk(pythonSdk)
            }
            notifyAboutNewSdk(project, pythonSdkName)
            pythonSdk
        }
        val module = ModuleManager.getInstance(project).findModuleByName(sdkConfig.module)

        if (module == null) {
            notifyAboutMissingModule(project, sdkConfig.module)
        } else {
            setModuleSdk(module, sdk)
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
            SdkImportBundle.message("notification.newProjectPythonSdk.title"),
            SdkImportBundle.message("notification.newProjectPythonSdk.content", sdkName),
            NotificationType.INFORMATION
        ).notify(project)
    }

    private fun notifyAboutMissingModule(project: Project, moduleName: String) {
        Notification(
            NOTIFICATION_GROUP,
            SdkImportBundle.message("notification.missingModule.title"),
            SdkImportBundle.message("notification.missingModule.content", moduleName),
            NotificationType.WARNING
        ).notify(project)
    }

    private fun notifyAboutSdkChange(module: Module, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            SdkImportBundle.message("notification.newModulePythonSdk.title"),
            SdkImportBundle.message("notification.newModulePythonSdk.content", module.name, sdkName),
            NotificationType.INFORMATION
        ).notify(module.project)
    }
}