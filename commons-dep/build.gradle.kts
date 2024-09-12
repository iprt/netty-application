plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.10"
}

group = "io.intellij.netty"
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

    api("io.netty:netty-all:4.1.111.Final")

    api("org.slf4j:slf4j-api:2.0.12")
    api("ch.qos.logback:logback-classic:1.5.6")
    api("ch.qos.logback:logback-core:1.5.6")

    api("com.alibaba.fastjson2:fastjson2:2.0.52")

    api("org.jetbrains:annotations:24.0.1")

    // junit-jupiter-engine 用于运行JUnit 5 引擎测试
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

tasks.jar {
    archiveFileName = "commons.jar"
}
