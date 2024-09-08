package com.pswidersk.sdkimportplugin

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager


class JavaSdkProcessor : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == JAVA_SDK_TYPE) {
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
            newJavaSdkNotif(project, jdkName)
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
                changedProjectJavaSdkNotif(project, sdk.name)
            }
        }
    }

    private fun setModuleSdk(module: Module, sdk: Sdk) {
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
        changedModuleJavaSdkNotif(module, sdk.name)
    }

}
