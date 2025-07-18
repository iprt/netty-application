plugins {
    id("java")
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly.configure {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.netty") {
                useVersion("4.1.115.Final")
            }
        }
    }
}

dependencies {
    implementation(project(":commons-dependencies"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

}
