#!/bin/bash
# Copyright (c) 2026 海尔卡奥斯物联科技有限公司
# Licensed under the Apache License, Version 2.0 (the "License");

# 把 lib/ 下的公司私服 jar 装到本地 Maven 仓库（~/.m2/repository）
# 仅在 clone 项目后第一次构建前执行一次；之后源码 module 改完直接 mvn install 即可。

set -e
cd "$(dirname "$0")"
LIB_DIR="$(pwd)/lib"

if [ ! -d "$LIB_DIR" ]; then
  echo "ERROR: $LIB_DIR 目录不存在" >&2
  exit 1
fi

install() {
  local file="$1" groupId="$2" artifactId="$3" version="$4"
  if [ ! -f "$file" ]; then
    echo "[SKIP] $file 不存在" >&2
    return
  fi
  # 若 lib 目录存在同名 .pom（如 hhim-common-ioss-4.1.pom），一并传入，
  # 避免 install:install-file 生成无依赖信息的极简 pom 破坏传递依赖链
  local pomFile="${file%.jar}.pom"
  if [ -f "$pomFile" ]; then
    echo "[INST] $artifactId-$version（附带 pom 依赖信息）"
    mvn install:install-file -Dfile="$file" \
      -DgroupId="$groupId" -DartifactId="$artifactId" -Dversion="$version" \
      -Dpackaging=jar -DpomFile="$pomFile" -q
  else
    echo "[INST] $artifactId-$version"
    mvn install:install-file -Dfile="$file" \
      -DgroupId="$groupId" -DartifactId="$artifactId" -Dversion="$version" \
      -Dpackaging=jar -q
  fi
}

# com.cosmo.plugins (12)
install "$LIB_DIR/hhim-common-core-4.1.jar"      com.cosmo.plugins hhim-common-core      4.1
install "$LIB_DIR/hhim-common-redis-4.1.jar"     com.cosmo.plugins hhim-common-redis     4.1
install "$LIB_DIR/hhim-common-security-4.1.jar"  com.cosmo.plugins hhim-common-security  4.1
install "$LIB_DIR/hhim-common-log-4.1.jar"       com.cosmo.plugins hhim-common-log       4.1
install "$LIB_DIR/hhim-common-datasource-4.1.jar" com.cosmo.plugins hhim-common-datasource 4.1
install "$LIB_DIR/hhim-common-datascope-4.1.jar" com.cosmo.plugins hhim-common-datascope 4.1
install "$LIB_DIR/hhim-common-cache-4.1.jar"     com.cosmo.plugins hhim-common-cache     4.1
install "$LIB_DIR/hhim-common-easypoi-4.1.jar"   com.cosmo.plugins hhim-common-easypoi   4.1
install "$LIB_DIR/hhim-common-event-4.1.jar"     com.cosmo.plugins hhim-common-event     4.1
install "$LIB_DIR/hhim-common-mail-4.1.jar"      com.cosmo.plugins hhim-common-mail      4.1
install "$LIB_DIR/hhim-common-ioss-4.1.jar"      com.cosmo.plugins hhim-common-ioss      4.1

# com.cosmo.hhim.thirdplat (12)
install "$LIB_DIR/thirdplat-core-1.0.jar"             com.cosmo.hhim.thirdplat thirdplat-core             1.0
install "$LIB_DIR/thirdplat-common-1.0.jar"           com.cosmo.hhim.thirdplat thirdplat-common           1.0
install "$LIB_DIR/thirdplat-api-unipush-1.0.jar"      com.cosmo.hhim.thirdplat thirdplat-api-unipush      1.0
install "$LIB_DIR/thirdplat-api-wechatmp-1.0.jar"     com.cosmo.hhim.thirdplat thirdplat-api-wechatmp     1.0
install "$LIB_DIR/thirdplat-api-wechatminiapp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-api-wechatminiapp 1.0
install "$LIB_DIR/thirdplat-api-cosmosupport-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-api-cosmosupport 1.0
install "$LIB_DIR/thirdplat-api-thirdclient-1.0.jar"  com.cosmo.hhim.thirdplat thirdplat-api-thirdclient  1.0
install "$LIB_DIR/im-api-operation-1.0.jar"           com.cosmo.hhim.thirdplat im-api-operation           1.0
install "$LIB_DIR/thirdplat-modules-unipush-1.0.jar"  com.cosmo.hhim.thirdplat thirdplat-modules-unipush  1.0
install "$LIB_DIR/thirdplat-modules-wechatmp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-modules-wechatmp 1.0
install "$LIB_DIR/thirdplat-modules-wechatminiapp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-modules-wechatminiapp 1.0
install "$LIB_DIR/thirdplat-modules-thirdclient-1.0.jar"   com.cosmo.hhim.thirdplat thirdplat-modules-thirdclient   1.0

# com.cosmo.hhim.micro (9) —— micro-interface 为应用入口模块（Spring Boot fat jar，含本地配置），不作为依赖安装
install "$LIB_DIR/micro-application-1.0.jar"           com.cosmo.hhim.micro micro-application           1.0
install "$LIB_DIR/micro-infrastructure-1.0.jar"        com.cosmo.hhim.micro micro-infrastructure        1.0
install "$LIB_DIR/micro-base-domain-1.0.jar"           com.cosmo.hhim.micro micro-base-domain           1.0
install "$LIB_DIR/micro-storage-domain-1.0.jar"        com.cosmo.hhim.micro micro-storage-domain        1.0
install "$LIB_DIR/micro-planning-domain-1.0.jar"       com.cosmo.hhim.micro micro-planning-domain       1.0
install "$LIB_DIR/micro-submit-domain-1.0.jar"         com.cosmo.hhim.micro micro-submit-domain         1.0
install "$LIB_DIR/micro-complete-domain-1.0.jar"       com.cosmo.hhim.micro micro-complete-domain       1.0
install "$LIB_DIR/micro-integration-domain-1.0.jar"    com.cosmo.hhim.micro micro-integration-domain    1.0
install "$LIB_DIR/micro-ng-domain-1.0.jar"             com.cosmo.hhim.micro micro-ng-domain             1.0

echo "=== 完成：32 个 jar 已安装到本地仓库 ==="
