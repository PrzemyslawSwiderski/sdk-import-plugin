package com.pswidersk.sdkimportplugin

import com.intellij.notification.Notification
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.project.stateStore
import com.intellij.testFramework.junit5.RunMethodInEdt
import com.intellij.testFramework.junit5.RunMethodInEdt.WriteIntentMode
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.pythonSdk
import com.pswidersk.sdkimportplugin.python.PythonSdkProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File

private const val TEST_MODULE_NAME = "sample-python-module"

@TestApplication
class SdkImportServiceTest {

    private val projectPathModel = tempPathFixture()
    private val projectModel = projectFixture(projectPathModel)
    private val project: Project
        get() = projectModel.get()

    private val moduleModel = projectModel.moduleFixture(TEST_MODULE_NAME)
    private val module: Module
        get() = moduleModel.get()

    private val nonProjectModulePathModel = tempPathFixture()
    private val projectRootModuleModel = projectModel.moduleFixture(nonProjectModulePathModel, addPathToSourceRoot = true)
    private val projectRootModule: Module
        get() = projectRootModuleModel.get()

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

    @Test
    fun `existing Python SDK is registered once and assigned`() {
        runBlocking {
            val sdkConfig = SdkImportConfigEntry().apply {
                type = PYTHON_SDK_TYPE
                path = pythonPath
                module = TEST_MODULE_NAME
            }
            val processor = PythonSdkProcessor(this)
            edtWriteAction {
                ModuleRootModificationUtil.addContentRoot(projectRootModule, projectPathModel.get().toString())
            }
            val contentRootPaths = readAction {
                ModuleRootManager.getInstance(projectRootModule).contentRoots.map { it.toNioPath() }
            }
            assertThat(contentRootPaths).containsExactly(nonProjectModulePathModel.get(), projectPathModel.get())

            coroutineScope {
                listOf(module, projectRootModule).map { targetModule ->
                    async { processor.addPythonSdk(project, targetModule, sdkConfig) }
                }.awaitAll()
            }

            val registeredSdk = ProjectJdkTable.getInstance().allJdks.single()
            assertThat(registeredSdk.homePath).isEqualTo(pythonPath)
            assertThat(registeredSdk.sdkType).isSameAs(PythonSdkType.getInstance())
            assertThat(module.pythonSdk).isSameAs(registeredSdk)
            assertThat(projectRootModule.pythonSdk).isSameAs(registeredSdk)
            assertThat(ProjectRootManager.getInstance(project).projectSdk).isSameAs(registeredSdk)

            processor.addPythonSdk(project, projectRootModule, sdkConfig)

            assertThat(ProjectJdkTable.getInstance().allJdks).containsExactly(registeredSdk)
        }
    }

    @RunMethodInEdt(writeIntent = WriteIntentMode.True)
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

    @RunMethodInEdt(writeIntent = WriteIntentMode.True)
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
