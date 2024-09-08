package com.pswidersk.sdkimportplugin

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

fun interface SdkProcessor {

    fun applySdk(project: Project, sdkConfig: SdkImportConfigEntry)

    companion object {
        @JvmStatic
        val EP_NAME = ExtensionPointName<SdkProcessor>("com.pswidersk.sdkimportplugin.sdkProcessor")
    }

}
