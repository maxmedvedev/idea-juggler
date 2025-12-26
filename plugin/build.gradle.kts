plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":core"))
    intellijPlatform {
        intellijIdea("2025.3")
    }
}

tasks {
    patchPluginXml {
        sinceBuild.set("253")
        untilBuild.set("261.*")
    }
}

kotlin {
    jvmToolchain(17)
}
