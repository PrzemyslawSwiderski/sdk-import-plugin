import com.pswidersk.gradle.python.VenvTask

plugins {
    id("com.pswidersk.python-plugin") version "2.7.3"
}

pythonPlugin {
    pythonVersion = "3.12.4"
    condaInstaller = "Miniconda3"
    condaVersion = "py312_24.5.0-0"
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
