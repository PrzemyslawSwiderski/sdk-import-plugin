package com.pswidersk.sdkimportplugin.python

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.*

class PythonSdkProcessor : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == PYTHON_SDK_TYPE) {
            addPythonSdk(project, sdkConfig)
        }
    }

    private fun addPythonSdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        val sdkHome = sdkConfig.loadSdkFile(project)
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
            newPythonSdkNotif(project, pythonSdkName)
            pythonSdk
        }
        project.withModule(sdkConfig.module) {
            setModuleSdk(it, sdk)
        }
    }

    private fun setModuleSdk(module: Module, sdk: Sdk) {
        if (module.pythonSdk?.name != sdk.name) {
            module.pythonSdk = sdk
            changedModulePythonSdkNotif(module, sdk.name)
        }
    }

}