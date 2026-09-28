@echo off
REM Copyright (c) 2026 海尔卡奥斯物联科技有限公司
REM Licensed under the Apache License, Version 2.0 (the "License");

REM Install private jars from lib\ into local Maven repo (~/.m2/repository).
REM Run once after cloning, before the first mvn install / build.
REM Requires `mvn` on PATH (open in a shell where Maven is configured, e.g. IDEA Terminal).

setlocal enabledelayedexpansion
cd /d "%~dp0"
set LIB_DIR=%CD%\lib

if not exist "%LIB_DIR%" (
  echo ERROR: %LIB_DIR% not found
  exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
  echo ERROR: mvn not on PATH. Run this in a shell where Maven is configured.
  exit /b 1
)

call :install "%LIB_DIR%\hhim-common-core-4.1.jar"      com.cosmo.plugins hhim-common-core      4.1
call :install "%LIB_DIR%\hhim-common-redis-4.1.jar"     com.cosmo.plugins hhim-common-redis     4.1
call :install "%LIB_DIR%\hhim-common-security-4.1.jar"  com.cosmo.plugins hhim-common-security  4.1
call :install "%LIB_DIR%\hhim-common-log-4.1.jar"       com.cosmo.plugins hhim-common-log       4.1
call :install "%LIB_DIR%\hhim-common-datasource-4.1.jar" com.cosmo.plugins hhim-common-datasource 4.1
call :install "%LIB_DIR%\hhim-common-datascope-4.1.jar" com.cosmo.plugins hhim-common-datascope 4.1
call :install "%LIB_DIR%\hhim-common-cache-4.1.jar"     com.cosmo.plugins hhim-common-cache     4.1
call :install "%LIB_DIR%\hhim-common-easypoi-4.1.jar"   com.cosmo.plugins hhim-common-easypoi   4.1
call :install "%LIB_DIR%\hhim-common-event-4.1.jar"     com.cosmo.plugins hhim-common-event     4.1
call :install "%LIB_DIR%\hhim-common-mail-4.1.jar"      com.cosmo.plugins hhim-common-mail      4.1
call :install "%LIB_DIR%\hhim-common-ioss-4.1.jar"      com.cosmo.plugins hhim-common-ioss      4.1

call :install "%LIB_DIR%\thirdplat-core-1.0.jar"             com.cosmo.hhim.thirdplat thirdplat-core             1.0
call :install "%LIB_DIR%\thirdplat-common-1.0.jar"           com.cosmo.hhim.thirdplat thirdplat-common           1.0
call :install "%LIB_DIR%\thirdplat-api-unipush-1.0.jar"      com.cosmo.hhim.thirdplat thirdplat-api-unipush      1.0
call :install "%LIB_DIR%\thirdplat-api-wechatmp-1.0.jar"     com.cosmo.hhim.thirdplat thirdplat-api-wechatmp     1.0
call :install "%LIB_DIR%\thirdplat-api-wechatminiapp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-api-wechatminiapp 1.0
call :install "%LIB_DIR%\thirdplat-api-cosmosupport-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-api-cosmosupport 1.0
call :install "%LIB_DIR%\thirdplat-api-thirdclient-1.0.jar"  com.cosmo.hhim.thirdplat thirdplat-api-thirdclient  1.0
call :install "%LIB_DIR%\im-api-operation-1.0.jar"           com.cosmo.hhim.thirdplat im-api-operation           1.0
call :install "%LIB_DIR%\thirdplat-modules-unipush-1.0.jar"  com.cosmo.hhim.thirdplat thirdplat-modules-unipush  1.0
call :install "%LIB_DIR%\thirdplat-modules-wechatmp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-modules-wechatmp 1.0
call :install "%LIB_DIR%\thirdplat-modules-wechatminiapp-1.0.jar" com.cosmo.hhim.thirdplat thirdplat-modules-wechatminiapp 1.0
call :install "%LIB_DIR%\thirdplat-modules-thirdclient-1.0.jar"   com.cosmo.hhim.thirdplat thirdplat-modules-thirdclient   1.0

rem micro-interface 为应用入口模块（Spring Boot fat jar，含本地配置），不作为依赖安装
call :install "%LIB_DIR%\micro-application-1.0.jar"           com.cosmo.hhim.micro micro-application           1.0
call :install "%LIB_DIR%\micro-infrastructure-1.0.jar"        com.cosmo.hhim.micro micro-infrastructure        1.0
call :install "%LIB_DIR%\micro-base-domain-1.0.jar"           com.cosmo.hhim.micro micro-base-domain           1.0
call :install "%LIB_DIR%\micro-storage-domain-1.0.jar"        com.cosmo.hhim.micro micro-storage-domain        1.0
call :install "%LIB_DIR%\micro-planning-domain-1.0.jar"       com.cosmo.hhim.micro micro-planning-domain       1.0
call :install "%LIB_DIR%\micro-submit-domain-1.0.jar"         com.cosmo.hhim.micro micro-submit-domain         1.0
call :install "%LIB_DIR%\micro-complete-domain-1.0.jar"       com.cosmo.hhim.micro micro-complete-domain       1.0
call :install "%LIB_DIR%\micro-integration-domain-1.0.jar"    com.cosmo.hhim.micro micro-integration-domain    1.0
call :install "%LIB_DIR%\micro-ng-domain-1.0.jar"             com.cosmo.hhim.micro micro-ng-domain             1.0

echo === Done: 33 jars installed to local repo ===
exit /b 0

:install
if not exist "%~1" (
  echo [SKIP] %~1 not found
  exit /b 0
)
REM 若 lib 目录存在同名 .pom（如 hhim-common-ioss-4.1.pom），一并传入，
REM 避免 install:install-file 生成无依赖信息的极简 pom 破坏传递依赖链
if exist "%~dpn1.pom" (
  echo [INST] %~3-%~4 ^(附带 pom 依赖信息^)
  call mvn install:install-file -Dfile="%~1" -DgroupId="%~2" -DartifactId="%~3" -Dversion="%~4" -Dpackaging=jar -DpomFile="%~dpn1.pom" -q
) else (
  echo [INST] %~3-%~4
  call mvn install:install-file -Dfile="%~1" -DgroupId="%~2" -DartifactId="%~3" -Dversion="%~4" -Dpackaging=jar -q
)
exit /b 0
