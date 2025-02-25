plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
}

group = "io.intellij.netty.server"

version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":commons-dependencies"))
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
        attributes["Main-Class"] = "io.intellij.netty.server.tcpproxy.HexDumpProxy"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

}

tasks.build {
    dependsOn(tasks.named("fatJar"))
}

// tasks.compileJava {
//     dependsOn(":commons-dep:build")
// }
//
// tasks.jar {
//     manifest {
//         attributes("Main-Class" to "io.intellij.netty.server.tcpproxy.HexDumpProxy")
//     }
//     duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//     configurations["runtimeClasspath"].forEach { file: File ->
//         from(zipTree(file.absoluteFile))
//     }
//
//     archiveFileName = "tcp-proxy.jar"
// }
