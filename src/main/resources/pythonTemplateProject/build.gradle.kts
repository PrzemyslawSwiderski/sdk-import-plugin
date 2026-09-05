import com.pswidersk.gradle.python.VenvTask

plugins {
    id("com.pswidersk.python-plugin") version "3.2.25"
}

pythonPlugin {
    // find possible options here: https://github.com/PrzemyslawSwiderski/python-gradle-plugin?tab=readme-ov-file#python-plugin-properties
}

tasks {

    register<VenvTask>("condaInfo") {
        description = "List information about Conda"
        venvExec = "conda"
        args = listOf("info")
    }

    val condaInstall = register<VenvTask>("condaInstall") {
        description = "Install Conda"
        venvExec = "conda"
        args = listOf("install", "--file", "requirements.txt", "-y")
    }

    register<VenvTask>("runScript") {
        description = "Run Python script"
        args = listOf("script.py")
        dependsOn(condaInstall)
    }

}
