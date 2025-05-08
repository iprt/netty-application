dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
    }
}

buildCache {
    local {
        isEnabled = true
        directory = file("${rootDir}/.gradle/build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

rootProject.name = "netty-application"

include("commons-dependencies")

include("example-github")
include("example")

include(
    "netty-client-reconnect:echo-server",
    "netty-client-reconnect:reconnect-client"
)

include(
    "netty-frp-tcp:frp-tcp-commons",
    "netty-frp-tcp:netty-frp-tcp-client",
    "netty-frp-tcp:netty-frp-tcp-server"
)

include(
    "netty-server-dns-proxy-u2t",
    "netty-server-socks",
    "netty-server-tcp",
    "netty-server-tcp-proxy",
    "netty-server-spring-boot",
    "netty-tcp-loadbalancer"
)
