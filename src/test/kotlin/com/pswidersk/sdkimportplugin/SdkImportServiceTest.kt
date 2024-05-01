package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.rules.ProjectModelExtension
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.statistics.modules
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File

private const val TEST_MODULE_NAME = "sample-python-module"

@TestApplication
@RunInEdt
class SdkImportServiceTest {

    @JvmField
    @RegisterExtension
    val projectModel: ProjectModelExtension = ProjectModelExtension()

    private val ideaDir: File
        get() = projectModel.baseProjectDir.root.resolve(".idea").also { it.mkdir() }

    private val sdkImportFile: File
        get() = ideaDir.resolve("sdk-import.yml")

    private val project: Project
        get() = projectModel.project

    private val buildProjectDir: String
        get() = System.getProperty("PROJECT_DIR")

    private val pythonPath: String
        get() = findPythonPath(buildProjectDir)

    @BeforeEach
    fun setUp() {
        projectModel.createModule(TEST_MODULE_NAME)
    }

    @Test
    fun `new Python SDK is imported`() {
        // given
        mockPythonSdk()
        val projectService = project.service<SdkImportService>()

        // when
        projectService.runImport()

        // then
        val rootModule = project.modules[0]
        assertThat(ProjectJdkTable.getInstance().allJdks).hasSize(1)
        assertThat(rootModule.pythonSdk?.name).isEqualTo("Python env: $pythonPath")
    }

    @Test
    fun `error notification if displayed`() {
        // given
        val projectService = project.service<SdkImportService>()
        val notificationsManager = NotificationsManagerImpl.getNotificationsManager()
        saveErroneousConfigFile()

        // when
        projectService.runImport()

        // then
        val notifications = notificationsManager.getNotificationsOfType(Notification::class.java, project)
        assertThat(notifications).hasSize(1)
        with(notifications.first()) {
            assertThat(title).isEqualTo("SDK-Import -> an exception occurred.")
            assertThat(content).startsWith("Exception message: `Cannot create property=import")
        }
    }

    @AfterEach
    fun tearDown() {
        clearSdks()
        sdkImportFile.writeText("")
    }

    private fun saveErroneousConfigFile() {
        runWriteAction {
            sdkImportFile.writeText(
                """
                import:
                  - type: NON_SUPPORTED
                    non-parsable: someValue
                    module: $TEST_MODULE_NAME
                """.trimIndent()
            )
        }
    }

    private fun findPythonPath(buildProjectDir: String): String {
        val testSdkImportConfigFile = File(buildProjectDir).resolve(".idea/sdk-import.yml")
        val testSdkImportConfig = testSdkImportConfigFile.loadAsYamlImportConfig()
        return testSdkImportConfig.import.first().path
    }

    private fun mockPythonSdk() {
        VfsRootAccess.allowRootAccess(project, buildProjectDir)
        runWriteAction {
            sdkImportFile.writeText(
                """
                import:
                  - type: PYTHON
                    path: $pythonPath
                    module: $TEST_MODULE_NAME
                """.trimIndent()
            )
        }
    }

    private fun clearSdks() {
        val sdkTable = ProjectJdkTable.getInstance()
        sdkTable.allJdks.forEach {
            runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(it)
            }
        }
    }
}

