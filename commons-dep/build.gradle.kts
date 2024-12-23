plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.10"
}

group = "io.intellij.netty.commons"
version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
}

dependencies {
    api("org.apache.commons:commons-lang3:3.14.0")
    api("commons-io:commons-io:2.16.1")

    api("io.netty:netty-all:4.1.116.Final")

    api("org.slf4j:slf4j-api:2.0.12")
    api("ch.qos.logback:logback-classic:1.5.6")
    api("ch.qos.logback:logback-core:1.5.6")

    api("com.alibaba.fastjson2:fastjson2:2.0.52")

    api("org.jetbrains:annotations:24.0.1")

}


tasks.jar {
    archiveFileName = "netty-application-commons-dep.jar"
}
