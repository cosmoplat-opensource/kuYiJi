# Copyright (c) 2026 海尔卡奥斯物联科技有限公司
# Licensed under the Apache License, Version 2.0 (the "License");

#FROM centos-jdk8
FROM eclipse-temurin:8-jdk
MAINTAINER cosmoplat
ADD ./target/micro-interface.jar ./app.jar
EXPOSE 80
ENTRYPOINT ["sh","-c","java  -XX:+HeapDumpOnOutOfMemoryError  -Dproject.name=hhim-micro-app   -Duser.country=cn -Duser.language=zh -jar app.jar -c"]
