package com.pswidersk.sdkimportplugin

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.pswidersk.sdkimportplugin.SdkImportBundle.message
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.io.File.separatorChar
import java.util.function.Consumer

const val PLUGIN_NAME = "SDK-Import"

fun withWriteAction(action: Runnable) {
    WriteAction.run<Throwable> {
        action.run()
    }
}

fun Project.withModule(moduleName: String, moduleConsumer: Consumer<Module>) {
    val module = ModuleManager.getInstance(this).findModuleByName(moduleName)
    if (module == null) {
        missingModuleNotif(this, moduleName)
    } else {
        moduleConsumer.accept(module)
    }
}

fun SdkImportConfigEntry.loadSdkFile(project: Project): VirtualFile {
    val projectBasePath = project.basePath?.trimEnd(separatorChar) ?: ""
    val resolvedPath = path.replace($$"$PROJECT_DIR$", projectBasePath)
    val sdkHome = WriteAction.compute<VirtualFile, RuntimeException> {
        LocalFileSystem.getInstance().refreshAndFindFileByPath(resolvedPath)
    }

    require(sdkHome != null && sdkHome.exists()) {
        message("validation.missingSdkFile", resolvedPath)
    }

    return sdkHome
}

fun File.loadAsYamlImportConfig(): SdkImportConfig =
    Yaml(Constructor(SdkImportConfig::class.java, LoaderOptions()))
        .load(this.inputStream())

fun execSafe(project: Project, execution: () -> Unit) {
    try {
        return execution()
    } catch (exception: Exception) {
        errorNotif(project, exception)
    }
}
