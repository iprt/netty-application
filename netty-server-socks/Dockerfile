FROM openjdk:17-jdk

MAINTAINER "tech@intellij.io"

LABEL email="tech@intellij.io" \
      author="zhenjie zhu"

# random_file
RUN dd if=/dev/urandom of=/random_file bs=1K count=1

WORKDIR /opt/app

ADD build/libs/netty-server-socks-1.0-all.jar netty-server-socks-1.0-all.jar

EXPOSE 1080

CMD java $JAVA_OPTIONS -jar netty-server-socks-1.0-all.jar
