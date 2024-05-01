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
                    resource = javaClass.getResource("/pythonTemplateProject/gradle/wrapper/gradle-wrapper.jar.bin")!!
                ),
                GeneratorResourceFile(
                    relativePath = "gradle/wrapper/gradle-wrapper.properties",
                    resource = javaClass.getResource("/pythonTemplateProject/gradle/wrapper/gradle-wrapper.properties")!!
                ),
                GeneratorResourceFile(
                    relativePath = ".gitignore",
                    resource = javaClass.getResource("/pythonTemplateProject/gradle.gitignore.txt")!!
                ),
                GeneratorResourceFile(
                    relativePath = "build.gradle.kts",
                    resource = javaClass.getResource("/pythonTemplateProject/build.gradle.kts")!!
                ),
                GeneratorResourceFile(
                    permissions = setOf(
                        PosixFilePermission.OWNER_EXECUTE,
                        PosixFilePermission.GROUP_EXECUTE,
                        PosixFilePermission.OTHERS_EXECUTE
                    ),
                    relativePath = "gradlew",
                    resource = javaClass.getResource("/pythonTemplateProject/gradlew.bin")!!
                ),
                GeneratorResourceFile(
                    relativePath = "gradlew.bat",
                    resource = javaClass.getResource("/pythonTemplateProject/gradlew.bat.bin")!!
                ),
                GeneratorResourceFile(
                    relativePath = "README.md",
                    resource = javaClass.getResource("/pythonTemplateProject/README.md")!!
                ),
                GeneratorResourceFile(
                    relativePath = "requirements.txt",
                    resource = javaClass.getResource("/pythonTemplateProject/requirements.txt")!!
                ),
                GeneratorResourceFile(
                    relativePath = "script.py",
                    resource = javaClass.getResource("/pythonTemplateProject/script.py")!!
                ),
            )
        }
    }

    class Builder : GeneratorNewProjectWizardBuilderAdapter(GradleNewProjectWizard()) {
        override fun getWeight(): Int = PYTHON_WEIGHT + 100
    }

}
