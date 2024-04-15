package com.pswidersk.sdkimportplugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.jetbrains.python.psi.icons.PythonPsiApiIcons

class SdkImportAction : DumbAwareAction(
    SdkImportBundle.message("action.text"),
    SdkImportBundle.message("action.description"),
    PythonPsiApiIcons.Python
) {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<SdkImportService>()?.runImport()
    }

}