plugins {
    id("java-library")
}

group = "io.intellij.netty.client"

version = "1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(project(":commons-dep"))

    implementation("javax.activation:activation:1.1.1")
    // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
    implementation("com.google.protobuf:protobuf-java:4.28.2")

}
