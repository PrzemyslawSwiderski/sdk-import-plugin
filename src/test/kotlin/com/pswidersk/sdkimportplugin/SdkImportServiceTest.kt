package com.pswidersk.sdkimportplugin

import com.intellij.facet.FacetManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.rules.ProjectModelExtension
import com.jetbrains.python.statistics.modules
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@TestApplication
@RunInEdt(writeIntent = true)
class SdkImportServiceTest {

    @JvmField
    @RegisterExtension
    val projectModel: ProjectModelExtension = ProjectModelExtension()

    private val project: Project
        get() = projectModel.project

    @BeforeEach
    fun setUp() {
        projectModel.createModule("sample-python-module")
        VfsRootAccess.allowRootAccess(project, SDK_PATH)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    @Test
    fun `new SDK is imported`() {
        // given
        val projectService = project.service<SdkImportService>()

        // when
        projectService.runImport()
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // then
        val rootModule = project.modules[0]
        assertThat(ProjectJdkTable.getInstance().allJdks).hasSize(1)
        assertThat(FacetManager.getInstance(rootModule).allFacets).hasSize(1)
    }

    @AfterEach
    fun tearDown() {
        val sdkTable = ProjectJdkTable.getInstance()
        val pythonSdkName = project.name + " Python env"
        val tableSdk = sdkTable.findJdk(pythonSdkName)

        WriteAction.run<Throwable> {
            ProjectJdkTable.getInstance().removeJdk(tableSdk!!)
        }
    }

}

