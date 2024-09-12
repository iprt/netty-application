dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
    }
}

rootProject.name = "netty-application"

include("commons-dep")

include("netty-server-socks", "netty-server-tcp", "netty-server-tcp-proxy")
