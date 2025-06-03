plugins {
    id("java-library")
    alias(libs.plugins.freefair.lombok)
}

group = "io.intellij.netty.commons"
version = "1.0"

val javaVersion = libs.versions.java.get().toInt()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
}

dependencies {

    api(libs.commons.lang3)
    api(libs.commons.io)

    api(libs.netty.all)

    api(libs.slf4j.api)
    api(libs.logback.classic)
    api(libs.logback.core)
    api(libs.fastjson2)
    api(libs.jetbrains.annotations)
}

tasks.jar {
    archiveFileName = "netty-application-commons.jar"
}
