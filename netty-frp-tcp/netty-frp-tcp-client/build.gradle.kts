plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
}

group = "io.intellij.netty.client"

version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":netty-frp-tcp:frp-tcp-commons"))
    // junit-jupiter-engine 用于运行JUnit 5 引擎测试
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = "io.intellij.netty.tcpfrp.client.FrpClientMain"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

}

tasks.build {
    dependsOn(tasks.named("fatJar"))
}
