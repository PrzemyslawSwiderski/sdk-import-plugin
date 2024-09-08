package com.pswidersk.sdkimportplugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import icons.SdkImportIcons.ToolIcon

class SdkImportAction : DumbAwareAction(
    SdkImportBundle.message("action.text"),
    SdkImportBundle.message("action.description"),
    ToolIcon
) {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<SdkImportService>()?.runImport()
    }

}
