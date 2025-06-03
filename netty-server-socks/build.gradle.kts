plugins {
    id("java")
    alias(libs.plugins.freefair.lombok)
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
        attributes["Main-Class"] = "io.intellij.netty.server.socks.SocksServer"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

}

tasks.build {
    dependsOn(tasks.named("fatJar"))
}


// tasks.compileJava {
//    dependsOn(":commons:build")
//}
//
// tasks.jar {
//    manifest {
//        attributes("Main-Class" to "org.iproute.SocksServer")
//    }
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    configurations["runtimeClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//    archiveFileName = "socks.jar"
//}

/*
> Task :netty-socks:compileJava
> Task :netty-socks:processResources
> Task :netty-socks:classes
> Task :netty-socks:jar
> Task :netty-socks:assemble
> Task :netty-socks:compileTestJava NO-SOURCE
> Task :netty-socks:processTestResources NO-SOURCE
> Task :netty-socks:testClasses UP-TO-DATE
> Task :netty-socks:test NO-SOURCE
> Task :netty-socks:check UP-TO-DATE
> Task :netty-socks:build
 */