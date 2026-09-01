package com.pswidersk.sdkimportplugin.python

import com.intellij.openapi.projectRoots.Sdk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.nio.file.Files
import java.nio.file.Path

class PythonSdkProcessorTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `direct interpreter path remains unchanged`() {
        val python = Files.createFile(tempDir.resolve("python"))

        assertThat(resolvePythonExecutable(python)).isEqualTo(python)
    }

    @Test
    fun `virtualenv directory resolves its interpreter`() {
        val python = createVirtualenv(tempDir.resolve("environment"))

        assertThat(resolvePythonExecutable(python.parent.parent)).isEqualTo(python)
    }

    @Test
    fun `virtualenv directory resolves a python3 interpreter`() {
        val python = createUnixVirtualenv(tempDir.resolve("environment"), "python3")

        assertThat(resolvePythonExecutable(tempDir.resolve("environment"))).isEqualTo(python)
    }

    @Test
    fun `Windows virtualenv directory resolves its interpreter`() {
        val python = createWindowsVirtualenv(tempDir.resolve("environment"))

        assertThat(resolvePythonExecutable(tempDir.resolve("environment"))).isEqualTo(python)
    }

    @Test
    fun `project directory resolves dot venv interpreter`() {
        val python = createVirtualenv(tempDir.resolve("project/.venv"))

        assertThat(resolvePythonExecutable(tempDir.resolve("project"))).isEqualTo(python)
    }

    @Test
    fun `project directory resolves Windows dot venv interpreter`() {
        val python = createWindowsVirtualenv(tempDir.resolve("project/.venv"))

        assertThat(resolvePythonExecutable(tempDir.resolve("project"))).isEqualTo(python)
    }

    @Test
    fun `virtualenv interpreter takes precedence over project dot venv`() {
        val python = createVirtualenv(tempDir.resolve("project"))
        createVirtualenv(tempDir.resolve("project/.venv"))

        assertThat(resolvePythonExecutable(tempDir.resolve("project"))).isEqualTo(python)
    }

    @Test
    fun `directory without an interpreter is rejected`() {
        val directory = Files.createDirectory(tempDir.resolve("empty"))

        assertThatThrownBy { resolvePythonExecutable(directory) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining(directory.toString())
    }

    @Test
    fun `registered SDK is matched by interpreter path instead of name`() {
        val python = Files.createDirectories(tempDir.resolve("environment/bin"))
            .resolve("python")
            .also(Files::createFile)
        val sdk = mock<Sdk>()
        `when`(sdk.name).thenReturn("IDE generated name")
        `when`(sdk.homePath).thenReturn(python.toString())

        assertThat(findRegisteredPythonSdk(python, listOf(sdk))).isSameAs(sdk)
    }

    private fun createVirtualenv(environment: Path): Path {
        return createUnixVirtualenv(environment, "python")
    }

    private fun createUnixVirtualenv(environment: Path, executableName: String): Path {
        val bin = Files.createDirectories(environment.resolve("bin"))
        return Files.createFile(bin.resolve(executableName))
    }

    private fun createWindowsVirtualenv(environment: Path): Path {
        val scripts = Files.createDirectories(environment.resolve("Scripts"))
        return Files.createFile(scripts.resolve("python.exe"))
    }
}
