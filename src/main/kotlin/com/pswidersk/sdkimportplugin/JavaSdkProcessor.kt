package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.pswidersk.sdkimportplugin.SdkImportBundle.message


class JavaSdkProcessor : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == SdkType.JAVA) {
            addJdk(project, sdkConfig)
        }
    }

    private fun addJdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        val sdkHome = sdkConfig.loadSdkFile()
        val sdkTable = ProjectJdkTable.getInstance()
        val jdkName = "JDK: ${sdkConfig.path}"
        val tableSdk = sdkTable.findJdk(jdkName)
        val sdk = if (tableSdk != null) tableSdk else {
            val jdk = JavaSdk.getInstance().createJdk(jdkName, sdkHome.path)
            withWriteAction {
                sdkTable.addJdk(jdk)
            }
            notifyAboutNewSdk(project, jdkName)
            jdk
        }
        project.withModule(sdkConfig.module) {
            setModuleSdk(it, sdk)
        }

        adjustProjectJDK(project, sdkConfig, sdk)
    }

    private fun adjustProjectJDK(
        project: Project,
        sdkConfig: SdkImportConfigEntry,
        sdk: Sdk
    ) {
        if (project.name == sdkConfig.module) {
            withWriteAction {
                ProjectRootManager.getInstance(project).projectSdk = sdk
                notifyAboutProjectSdkChange(project, sdk.name)
            }
        }
    }

    private fun setModuleSdk(module: Module, sdk: Sdk) {
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
        notifyAboutModuleSdkChange(module, sdk.name)
    }

    private fun notifyAboutNewSdk(project: Project, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            message("notification.newJavaSdk.title"),
            message("notification.newJavaSdk.content", sdkName),
            NotificationType.INFORMATION
        ).notify(project)
    }

    private fun notifyAboutModuleSdkChange(module: Module, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            message("notification.newModuleJavaSdk.title"),
            message("notification.newModuleJavaSdk.content", module.name, sdkName),
            NotificationType.INFORMATION
        ).notify(module.project)
    }

    private fun notifyAboutProjectSdkChange(project: Project, sdkName: String) {
        Notification(
            NOTIFICATION_GROUP,
            message("notification.newProjectJavaSdk.title"),
            message("notification.newProjectJavaSdk.content", project.name, sdkName),
            NotificationType.INFORMATION
        ).notify(project)
    }
}