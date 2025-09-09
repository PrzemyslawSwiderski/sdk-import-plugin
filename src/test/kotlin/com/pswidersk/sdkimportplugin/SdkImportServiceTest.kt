package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.project.stateStore
import com.intellij.testFramework.common.ThreadLeakTracker
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.python.sdk.pythonSdk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

private const val TEST_MODULE_NAME = "sample-python-module"

@TestApplication
@RunInEdt(writeIntent = true)
class SdkImportServiceTest {

    private val projectModel = projectFixture()
    private val project: Project
        get() = projectModel.get()

    private val moduleModel = projectModel.moduleFixture(TEST_MODULE_NAME)
    private val module: Module
        get() = moduleModel.get()

    private val ideaDir: File
        get() = project.stateStore.directoryStorePath!!.toFile().also { it.mkdir() }

    private val sdkImportFile: File
        get() = ideaDir.resolve("sdk-import.yml")

    private val pluginProjectDir: String
        get() = System.getProperty("PROJECT_DIR")

    private val pythonPath: String
        get() = findPythonPath(pluginProjectDir)

    private val jdkPath: String
        get() = System.getProperty("JDK_PATH")

    private val app: Application
        get() = ApplicationManager.getApplication()

    @AfterEach
    fun ignoreThreads() {
        ThreadLeakTracker.longRunningThreadCreated(app, "SystemPropertyWatcher")
    }

    @Test
    fun `new Python SDK is imported`() {
        // given
        mockPythonSdk()
        val projectService = project.service<SdkImportService>()

        // when
        projectService.runImport()

        // then
        assertThat(ProjectJdkTable.getInstance().allJdks).hasSize(1)
        assertThat(module.pythonSdk?.name).isEqualTo("Python env: $pythonPath")
    }

    @Test
    fun `new Java SDK is imported`() {
        // given
        mockJdk()
        val projectService = project.service<SdkImportService>()

        // when
        projectService.runImport()

        // then
        val allJdks = ProjectJdkTable.getInstance().allJdks
        assertThat(allJdks).hasSize(1)
        assertThat(allJdks.first().name).isEqualTo("JDK: $jdkPath")
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

    private fun mockJdk() {
        runWriteAction {
            sdkImportFile.writeText(
                """
                import:
                  - type: JAVA
                    path: $jdkPath
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

