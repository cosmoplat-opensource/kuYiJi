# ============================================================
# hhim-micro-be（后端①）镜像
# 构建上下文：项目根目录（因为要聚合构建多个 maven 模块）
#   docker build -f cosmo-hhim-micro/hhim-micro-be/Dockerfile .
# ============================================================

# ---------- 阶段 1：Maven 编译 ----------
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app

# 1. 拷贝源码（根 pom 聚合了这三个模块，相互有依赖，一次 reactor 构建产出全部内部依赖）
COPY pom.xml ./
COPY hhim-third-platform/ ./hhim-third-platform/
COPY cosmo-himm-commom/ ./cosmo-himm-commom/
COPY cosmo-hhim-micro/hhim-micro-be/ ./cosmo-hhim-micro/hhim-micro-be/

# 2. 使用阿里云 Maven 镜像加速依赖下载（中国大陆网络）
#    必须先于 mvn package：走默认 central（repo.maven.apache.org）会被墙导致 Connection refused
RUN mkdir -p /root/.m2 && \
    printf '%s' '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"><mirrors><mirror><id>aliyun-central</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/central</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 3. 删除本地环境配置文件（含公司内网地址，防止打进 jar），再打包
RUN find . -name "application-local.yml" -delete && \
    mvn clean package -DskipTests

# ---------- 阶段 2：JRE 运行 ----------
FROM eclipse-temurin:8-jre
WORKDIR /app

# 编译产物（.class 字节码，不含源码）
COPY --from=build /app/cosmo-hhim-micro/hhim-micro-be/micro-interface/target/micro-interface.jar /app/app.jar

# 配置模板 → 运行配置（所有值走 ${ENV:默认值} 占位符，由 compose 环境变量注入）
COPY --from=build /app/cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml.example /app/application-local.yml

ENV JAVA_OPTS=""
EXPOSE 9010

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -Dproject.name=hhim-micro-app -Duser.country=cn -Duser.language=zh -jar /app/app.jar"]
