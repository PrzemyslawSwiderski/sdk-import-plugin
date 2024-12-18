import org.gradle.internal.jvm.Jvm
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.pythonPlugin) // Python Plugin (https://github.com/PrzemyslawSwiderski/python-gradle-plugin)
}

val javaVersion: String by project
val pluginGroup: String by project
val pluginVersion: String by project
val platformType: String by project
val platformVersion: String by project
val platformBundledPlugins: String by project
val platformPlugins: String by project
val pluginRepositoryUrl: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

group = pluginGroup
version = pluginVersion

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    testImplementation(libs.assertj)
    testImplementation(libs.jupiterApi)

    testRuntimeOnly(libs.jupiterEngine)

    // Temp workaround suggested in https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4
    // Can be removed when IJPL-159134 is fixed
    testImplementation("org.junit.vintage:junit-vintage-engine:${libs.versions.junit}")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(platformType, platformVersion)

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(platformBundledPlugins.split(','))

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(platformPlugins.split(','))

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = pluginVersion

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = with(changelog) {
            renderItem(
                (getOrNull(pluginVersion) ?: getUnreleased())
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        }


        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild = pluginUntilBuild
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = listOf(pluginVersion.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups = listOf("Added", "Changed", "Removed")
    repositoryUrl = pluginRepositoryUrl
}

pythonPlugin {
    pythonVersion = "3.12.2"
    condaInstaller = "Miniconda3"
    condaVersion = "py312_24.1.2-0"
    useHomeDir = true
}

tasks {
    wrapper {
        val gradleVersion: String by project
        this.gradleVersion = gradleVersion
    }

    test {
        jvmArgs = listOf(
            "-Didea.force.default.filesystem=true",
            "-Dwsl.use.remote.agent.for.nio.filesystem=false"
        )
        useJUnitPlatform()
        // Creating `.idea` directory so that `saveSdkImportConfig` task will be executed
        file(".idea").mkdir()
        systemProperty("PROJECT_DIR", projectDir.path)
        systemProperty("JDK_PATH", Jvm.current().javaHome)
        dependsOn(envSetup)
    }

    publishPlugin {
        dependsOn("patchChangelog")
    }

}
