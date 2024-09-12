FROM openjdk:17-jdk

MAINTAINER "devops@kubectl.net"

LABEL email="devops@kubectl.net" \
      author="zhuzhenjie"

WORKDIR /opt/app

ADD build/libs/netty-tcp-server-1.0-all.jar netty-tcp-server-1.0-all.jar

EXPOSE 8080

CMD java $JAVA_OPTIONS -jar netty-tcp-server-1.0-all.jar
