# Netty Sock5

## reference

https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/socksproxy

## how to run

find `netty-socks-server-1.0-all.jar` from directory `build/libs/`

start on default port `1080`

```shell
java -jar netty-socks-server-1.0-all.jar
```

or start on port which you want

```shell
java -jar -Dport=1080 netty-socks-server-1.0-all.jar

```

use `epoll`

```shell
java -jar -DuseEpoll=true java -jar -Dport=1088 netty-socks-server-1.0-all.jar

```

