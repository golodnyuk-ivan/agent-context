plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "dev.agentcontext"
version = "0.3.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val localIdePath = providers.gradleProperty("localIdePath").orNull

dependencies {
    intellijPlatform {
        if (localIdePath != null) {
            local(localIdePath)
        } else {
            intellijIdea("2025.3.4") {
                useInstaller = false
            }
        }
        bundledPlugin("org.jetbrains.plugins.terminal")
    }
}

tasks.withType<JavaCompile> {
    options.release = 21
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
        }
    }
}

tasks.named("instrumentCode") {
    enabled = false
}
