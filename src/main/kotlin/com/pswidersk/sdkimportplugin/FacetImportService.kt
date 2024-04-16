package com.pswidersk.sdkimportplugin

import com.intellij.facet.FacetManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.python.community.plugin.java.facet.JavaPythonFacetType

@Service(Service.Level.PROJECT)
class FacetImportService {

    private val logger = thisLogger()

    private val pythonFacetType
        get() = JavaPythonFacetType.getInstance()

    fun addFacet(module: Module, sdk: Sdk) {
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
