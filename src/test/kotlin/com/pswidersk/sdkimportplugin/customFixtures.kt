package com.pswidersk.sdkimportplugin

import com.intellij.execution.wsl.ijent.nio.toggle.IjentWslNioFsVmOptionsSetter
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import java.nio.file.Files

fun customProjectFixture(): TestFixture<Project> = testFixture {
    IjentWslNioFsVmOptionsSetter.ensureInVmOptionsImpl(
        isEnabled = false,
        forceProductionOptions = true,
        isEnabledByDefault = false,
        getOptionByPrefix = { d -> "-Didea.force.default.filesystem=true" })
    val openProjectTask = OpenProjectTask.build()
    val path = Files.createTempDirectory("IJ")
    val project = ProjectManagerEx.getInstanceEx().newProjectAsync(path, openProjectTask)
    ProjectManagerEx.getInstanceEx().openProject(path, openProjectTask.withProject(project))
    initialized(project) {
        ProjectManagerEx.getInstanceEx().forceCloseProjectAsync(project, save = false)
    }
}
