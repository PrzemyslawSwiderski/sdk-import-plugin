package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.pswidersk.sdkimportplugin.SdkImportBundle.message

private const val NOTIFICATION_GROUP = "SDK-Import"

// General Notifications

fun errorNotif(project: Project, throwable: Throwable) {
    val msg = throwable.message ?: "N/A"
    Notification(
        NOTIFICATION_GROUP,
        message("notification.exception.title"),
        message("notification.exception.content", msg),
        NotificationType.ERROR
    ).notify(project)
}

fun missingModuleNotif(project: Project, moduleName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.missingModule.title"),
        message("notification.missingModule.content", moduleName),
        NotificationType.WARNING
    ).notify(project)
}

fun missingConfigNotif(project: Project) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.missingConfig.title"),
        message("notification.missingConfig.content"),
        NotificationType.WARNING
    ).notify(project)
}

// Python Notifications

fun newPythonSdkNotif(project: Project, sdkName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.newPythonSdk.title"),
        message("notification.newPythonSdk.content", sdkName),
        NotificationType.INFORMATION
    ).notify(project)
}

fun changedModulePythonSdkNotif(module: Module, sdkName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.newModulePythonSdk.title"),
        message("notification.newModulePythonSdk.content", module.name, sdkName),
        NotificationType.INFORMATION
    ).notify(module.project)
}

// Java Notifications

fun newJavaSdkNotif(project: Project, sdkName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.newJavaSdk.title"),
        message("notification.newJavaSdk.content", sdkName),
        NotificationType.INFORMATION
    ).notify(project)
}

fun changedModuleJavaSdkNotif(module: Module, sdkName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.newModuleJavaSdk.title"),
        message("notification.newModuleJavaSdk.content", module.name, sdkName),
        NotificationType.INFORMATION
    ).notify(module.project)
}

fun changedProjectJavaSdkNotif(project: Project, sdkName: String) {
    Notification(
        NOTIFICATION_GROUP,
        message("notification.newProjectJavaSdk.title"),
        message("notification.newProjectJavaSdk.content", project.name, sdkName),
        NotificationType.INFORMATION
    ).notify(project)
}