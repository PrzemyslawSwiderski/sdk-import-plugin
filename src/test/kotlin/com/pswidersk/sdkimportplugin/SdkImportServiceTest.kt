package com.pswidersk.sdkimportplugin

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.openapi.vfs.writeText
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
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

private const val SDK_IMPORT_REF = ".idea/sdk-import.yml"
private const val TEST_MODULE_NAME = "sample-python-module"

@TestApplication
@RunInEdt
class SdkImportServiceTest {

    @JvmField
    @RegisterExtension
    val projectModel: ProjectModelExtension = ProjectModelExtension()

    private val project: Project
        get() = projectModel.project

    private val buildProjectDir: String
        get() = System.getProperty("PROJECT_DIR")

    private val pythonPath: String
        get() = findPythonPath(buildProjectDir)

    @BeforeEach
    fun setUp() {
        projectModel.createModule(TEST_MODULE_NAME)
        mockPythonSdk()
    }

    @Test
    fun `new SDK is imported`() {
        // given
        val projectService = project.service<SdkImportService>()

        // when
        projectService.runImport()

        // then
        val rootModule = project.modules[0]
        assertThat(ProjectJdkTable.getInstance().allJdks).hasSize(1)
        assertThat(rootModule.pythonSdk?.name).isEqualTo("Python env: $pythonPath")
    }

    @AfterEach
    fun tearDown() {
        clearSdks()
    }

    private fun findPythonPath(buildProjectDir: String): String {
        val testSdkImportConfigFile = File(buildProjectDir).resolve(SDK_IMPORT_REF)
        val testSdkImportConfig: SdkImportConfig = Yaml(Constructor(SdkImportConfig::class.java))
            .load(testSdkImportConfigFile.inputStream())
        return testSdkImportConfig.import.first().path
    }

    private fun mockPythonSdk() {
        VfsRootAccess.allowRootAccess(project, buildProjectDir)
        runWriteAction {
            projectModel.baseProjectDir.newVirtualFile(SDK_IMPORT_REF).writeText(
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

