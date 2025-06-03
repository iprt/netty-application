plugins {
    id("java-library")
    alias(libs.plugins.freefair.lombok)
}

group = "io.intellij.netty.client"

version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(project(":commons-dependencies"))
}
