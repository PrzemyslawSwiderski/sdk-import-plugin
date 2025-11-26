package com.pswidersk.sdkimportplugin

import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@TestApplication
@RunInEdt(writeIntent = true)
class LoadSdkFileTest {

    @CsvSource(
        "/opt/projBasePath/,/opt/projBasePath/test-sdk-dir/python",
        "/opt/projBasePath//,/opt/projBasePath/test-sdk-dir/python",
        "/opt/projBasePath,/opt/projBasePath/test-sdk-dir/python",
        "/opt,/opt/test-sdk-dir/python",
        ",/test-sdk-dir/python",
    )
    @ParameterizedTest
    fun `project dir is properly resolved`(inputPath: String?, expectedResolved: String) {
        // given
        val testProj = mock<Project>()
        val projDirVar = $$"$PROJECT_DIR$"
        val testEntry = SdkImportConfigEntry().apply {
            path = "$projDirVar/test-sdk-dir/python"
        }
        `when`(testProj.basePath).thenReturn(inputPath)

        // when / then
        assertThatThrownBy { testEntry.loadSdkFile(testProj) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("SDK can not be found with by the path: `$expectedResolved`")
    }

}