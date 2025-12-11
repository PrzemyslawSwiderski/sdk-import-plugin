import com.pswidersk.gradle.python.VenvTask

plugins {
    id("com.pswidersk.python-plugin") version "3.1.3"
}

pythonPlugin {
    // find possible options here: https://github.com/PrzemyslawSwiderski/python-gradle-plugin?tab=readme-ov-file#python-plugin-properties
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
