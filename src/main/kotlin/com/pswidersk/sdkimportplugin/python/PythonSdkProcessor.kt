package com.pswidersk.sdkimportplugin.python

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.jetbrains.python.projectCreation.createVenvAndSdk
import com.jetbrains.python.sdk.ModuleOrProject
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PythonSdkProcessor(private val cs: CoroutineScope) : SdkProcessor {

    override fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        if (sdkConfig.type == PYTHON_SDK_TYPE) {
            cs.launch {
                addPythonSdk(project, sdkConfig)
            }
        }
    }

    private suspend fun addPythonSdk(project: Project, sdkConfig: SdkImportConfigEntry) {
        val sdkHome = sdkConfig.loadSdkFile(project)
        val sdkTable = ProjectJdkTable.getInstance()
        val pythonSdkName = "Python env: ${sdkConfig.path}"
        val tableSdk = sdkTable.findJdk(pythonSdkName)

        val sdk: Sdk = tableSdk ?: run {
            val moduleOrProject: ModuleOrProject = ModuleOrProject.ProjectOnly(project)

            val result = createVenvAndSdk(
                moduleOrProject = moduleOrProject,
                explicitPath = sdkHome,
            )

            val newSdk = result.orThrow {
                IllegalStateException(it.message)
            }

            edtWriteAction {
                sdkTable.addJdk(newSdk)
            }
            newPythonSdkNotif(project, pythonSdkName)
            newSdk
        }

        project.withModule(sdkConfig.module) {
            setModuleSdk(it, sdk)
        }
    }

    private fun setModuleSdk(module: Module, sdk: Sdk) {
        if (module.pythonSdk?.name != sdk.name) {
            ModuleRootModificationUtil.setModuleSdk(module, sdk)
            changedModulePythonSdkNotif(module, sdk.name)
        }
    }
}