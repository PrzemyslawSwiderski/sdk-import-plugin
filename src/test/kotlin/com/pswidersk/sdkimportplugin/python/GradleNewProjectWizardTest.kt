package com.pswidersk.sdkimportplugin.python

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.pswidersk.sdkimportplugin.customProjectFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
@RunInEdt(writeIntent = true)
class GradleNewProjectWizardTest {

    private val projectModel = customProjectFixture()
    private val project: Project
        get() = projectModel.get()

    @Test
    fun `new Gradle Python project is created`() {
        // given
        val wizard = GradleNewProjectWizard()
        val context = WizardContext(project) {}

        // when
        context.setProjectFileDirectory(project.guessProjectDir()!!.path)
        wizard.createStep(context).setupProject(project)

        // then
        assertThat(project.guessProjectDir()!!.children).hasSize(9)
        assertThat(project.guessProjectDir()!!.findChild("settings.gradle.kts")!!.readText())
            .isEqualTo(
                """
                        rootProject.name = "${project.name}"
                        
                    """.trimIndent()
            )
    }

}
