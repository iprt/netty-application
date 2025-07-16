# Netty Sock5

## reference

https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/socksproxy

## how to run

find `netty-socks-server-1.0-all.jar` from directory `build/libs/`

start on default port `1080`

```shell
java -jar netty-socks-server-1.0-all.jar
```

or specify port

```shell
java -jar -Dport=1080 netty-socks-server-1.0-all.jar
```

or with authentication

```shell
export SOCKS5_USERNAME=your_username
export SOCKS5_PASSWORD=your_password
java -jar netty-socks-server-1.0-all.jar
```
