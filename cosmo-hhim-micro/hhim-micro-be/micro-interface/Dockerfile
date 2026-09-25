#FROM centos-jdk8
FROM eclipse-temurin:8-jdk
MAINTAINER cosmoplat
ADD ./target/micro-interface.jar ./app.jar
EXPOSE 80
ENTRYPOINT ["sh","-c","java  -XX:+HeapDumpOnOutOfMemoryError  -Dproject.name=hhim-micro-app   -Duser.country=cn -Duser.language=zh -jar app.jar -c"]
