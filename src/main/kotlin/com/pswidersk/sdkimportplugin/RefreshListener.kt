package com.pswidersk.sdkimportplugin

import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType.RESOLVE_PROJECT
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class RefreshListener(private val cs: CoroutineScope) : ExternalSystemTaskNotificationListener {

    @Override
    override fun onSuccess(projectPath: String, id: ExternalSystemTaskId) {
        if (id.type == RESOLVE_PROJECT) {
            val project = id.findProject()
            if (project == null) return

            cs.launch {
                withBackgroundProgress(project, "Reimporting SDKs ...") {
                    delay(1.seconds) // wait until Gradle action completes
                    project.service<SdkImportService>().runImport()
                }
            }
        }
    }

}
