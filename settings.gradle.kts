dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
    }
}

rootProject.name = "netty-application"

include("common-dep", "netty-tcp-server")