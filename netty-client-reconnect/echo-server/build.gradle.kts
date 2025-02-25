plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.6"
}

group = "io.intellij.netty.frp"

version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(project(":commons-dependencies"))
}
