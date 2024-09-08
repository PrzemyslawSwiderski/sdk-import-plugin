package com.pswidersk.sdkimportplugin.python

import com.intellij.ide.projectWizard.generators.AssetsNewProjectWizardStep
import com.intellij.ide.starters.local.GeneratorResourceFile
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.openapi.project.Project
import com.pswidersk.sdkimportplugin.python.GradleTemplateGroup.Companion.SETTINGS_GRADLE
import icons.SdkImportIcons.ToolIcon
import java.nio.file.attribute.PosixFilePermission
import javax.swing.Icon

class GradleNewProjectWizard : GeneratorNewProjectWizard {

    override val icon: Icon
        get() = ToolIcon

    override val id: String
        get() = "GradlePython"

    override val name: String
        get() = "Gradle Python"

    override fun createStep(context: WizardContext): NewProjectWizardStep =
        RootNewProjectWizardStep(context)
            .nextStep(::newProjectWizardBaseStepWithoutGap)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::AssetsStep)

    private class AssetsStep(parent: NewProjectWizardStep) : AssetsNewProjectWizardStep(parent) {
        override fun setupAssets(project: Project) {

            addTemplateAsset("settings.gradle.kts", SETTINGS_GRADLE, "PYTHON_PROJECT_NAME" to project.name)

            addAssets(
                GeneratorResourceFile(
                    relativePath = "gradle/wrapper/gradle-wrapper.jar",
                    resource = resource("/pythonTemplateProject/gradle/wrapper/gradle-wrapper.jar.bin")
                ),
                GeneratorResourceFile(
                    relativePath = "gradle/wrapper/gradle-wrapper.properties",
                    resource = resource("/pythonTemplateProject/gradle/wrapper/gradle-wrapper.properties")
                ),
                GeneratorResourceFile(
                    relativePath = ".gitignore",
                    resource = resource("/pythonTemplateProject/gradle.gitignore.txt")
                ),
                GeneratorResourceFile(
                    relativePath = "build.gradle.kts",
                    resource = resource("/pythonTemplateProject/build.gradle.kts")
                ),
                GeneratorResourceFile(
                    permissions = setOf(
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.GROUP_EXECUTE,
                        PosixFilePermission.OTHERS_EXECUTE
                    ),
                    relativePath = "gradlew",
                    resource = resource("/pythonTemplateProject/gradlew.bin")
                ),
                GeneratorResourceFile(
                    relativePath = "gradlew.bat",
                    resource = resource("/pythonTemplateProject/gradlew.bat.bin")
                ),
                GeneratorResourceFile(
                    relativePath = "README.md",
                    resource = resource("/pythonTemplateProject/README.md")
                ),
                GeneratorResourceFile(
                    relativePath = "requirements.txt",
                    resource = resource("/pythonTemplateProject/requirements.txt")
                ),
                GeneratorResourceFile(
                    relativePath = "script.py",
                    resource = resource("/pythonTemplateProject/script.py")
                ),
            )
        }

        private fun resource(resourceClassPath: String) = javaClass.getResource(resourceClassPath)!!
    }

    class Builder : GeneratorNewProjectWizardBuilderAdapter(GradleNewProjectWizard()) {
        override fun getWeight(): Int = PYTHON_WEIGHT + 100
    }

}
