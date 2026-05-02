plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.marija.aicontext"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

intellij {
    version.set("2024.2.5")
    type.set("IC")
}

tasks {
    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("252.*")
    }

    runIde {
        maxHeapSize = "2g"
        jvmArgs("-Didea.is.internal=true")
    }
}
