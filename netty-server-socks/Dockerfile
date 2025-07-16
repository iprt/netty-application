FROM registry.cn-shanghai.aliyuncs.com/iproute/openjdk:17-jdk

LABEL org.opencontainers.image.authors="tech@intellij.io"

LABEL email="tech@intellij.io" \
      author="devops"

# random_file
RUN dd if=/dev/urandom of=/random_file bs=1K count=1

WORKDIR /opt/app

ADD build/libs/netty-server-socks-1.0-all.jar socks-server.jar

EXPOSE 1080

CMD java $JAVA_OPTIONS -jar socks-server.jar
