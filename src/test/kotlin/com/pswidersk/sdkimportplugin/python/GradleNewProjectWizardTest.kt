package com.pswidersk.sdkimportplugin.python

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.rules.ProjectModelExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@TestApplication
@RunInEdt
class GradleNewProjectWizardTest {

    @JvmField
    @RegisterExtension
    val projectModel: ProjectModelExtension = ProjectModelExtension()

    private val project: Project
        get() = projectModel.project

    @Test
    fun `new Gradle Python project is created`() {
        // given
        val wizard = GradleNewProjectWizard()
        val context = WizardContext(project) {}

        // when
        runWriteAction {
            context.setProjectFileDirectory(project.guessProjectDir()!!.path)
            wizard.createStep(context).setupProject(project)
        }

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
