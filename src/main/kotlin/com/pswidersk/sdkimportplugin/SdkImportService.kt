package com.pswidersk.sdkimportplugin

import com.intellij.facet.FacetManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.python.community.plugin.java.facet.JavaPythonFacetType
import com.jetbrains.python.sdk.PythonSdkType

const val SDK_PATH =
    "P:\\GITHUBREPOS\\python-template-project\\.gradle\\python\\Windows\\Miniconda3-py312_24.1.2-0\\envs\\python-3.12.2\\python.exe"

@Service(Service.Level.PROJECT)
class SdkImportService(private val project: Project) {

    private val logger = thisLogger()

    fun runImport() {
        logger.debug("Handle before project loaded event")
        val projectJdkTable = ProjectJdkTable.getInstance()
        logger.debug("All SDK size:${projectJdkTable.allJdks.size}")

        ApplicationManager.getApplication().invokeAndWait {
            logger.debug("Project SDK not configured")
            val sdkHomePath =
                SDK_PATH
            val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath)
            }
            val sdkTable = ProjectJdkTable.getInstance()
            val pythonSdkName = project.name + " Python env"
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
            val pythonFacetType = JavaPythonFacetType.getInstance()

            ModuleManager.getInstance(project).modules.onEach { module ->
                val facetManager = FacetManager.getInstance(module)
                var facet = facetManager.getFacetByType(pythonFacetType.id)
                if (facet == null) {
                    WriteAction.run<Throwable> {
                        val facetModel = facetManager.createModifiableModel()
                        facet = facetManager.createFacet(pythonFacetType, "Python", null)
                        facet!!.configuration.sdk = sdk
                        facetModel.addFacet(facet)
                        facetModel.commit()
                    }
                } else {
                    logger.warn("Python facet already assigned to module: ${module.name}")
                }
            }
        }
    }
}
