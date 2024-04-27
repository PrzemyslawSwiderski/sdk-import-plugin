package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.pswidersk.sdkimportplugin.SdkImportBundle.message
import java.util.function.Consumer

const val NOTIFICATION_GROUP = "SDK-Import"

fun withWriteAction(action: Runnable) {
    WriteAction.run<Throwable> {
        action.run()
    }
}

fun Project.withModule(moduleName: String, moduleConsumer: Consumer<Module>) {
    val module = ModuleManager.getInstance(this).findModuleByName(moduleName)
    if (module == null) {
        notifyAboutMissingModule(this, moduleName)
    } else {
        moduleConsumer.accept(module)
    }
}

fun notifyAboutMissingModule(project: Project, moduleName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.missingModule.title"),
        message("notification.missingModule.content", moduleName),
        NotificationType.WARNING
    ).notify(project)
}


fun SdkImportConfigEntry.loadSdkFile(): VirtualFile {
    val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
        LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    }

    require(sdkHome != null && sdkHome.exists()) {
        message("validation.missingSdkFile", path)
    }

    return sdkHome;
}
