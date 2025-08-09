import com.pswidersk.gradle.python.VenvTask

plugins {
    id("com.pswidersk.python-plugin") version "2.9.0"
}

pythonPlugin {
    pythonVersion = "3.13.0"
    condaInstaller = "Miniconda3"
    condaVersion = "py312_24.9.2-0"
    useHomeDir = true
}

tasks {

    register<VenvTask>("condaInfo") {
        venvExec = "conda"
        args = listOf("info")
    }

    val condaInstall by registering(VenvTask::class) {
        venvExec = "conda"
        args = listOf("install", "--file", "requirements.txt", "-y")
    }

    register<VenvTask>("runScript") {
        args = listOf("script.py")
        dependsOn(condaInstall)
    }

}
