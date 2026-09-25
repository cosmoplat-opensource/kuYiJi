# cosmo-hhim-open

**Powered by 卡奥斯 COSMOPlat** | Licensed under MIT License

---

## 项目简介

Ku易记（工程标识 `cosmo-hhim-open` / `cosmo.hhim.*`）是一套面向工业制造现场场景的**微应用管理平台 + 第三方集成平台**，提供与第三方平台（微信、个推、短信等）的集成能力。

### 主要功能

- 第三方平台集成（微信、个推、短信、邮件等）
- 微应用管理（报工、质检、库存等）
- 多租户支持

### 技术栈

- **框架**：Spring Boot 2.3.7 + Spring Cloud Hoxton
- **注册中心**：Nacos（可选，当前未使用，微服务间 Feign 直连）
- **数据库**：MySQL + Druid（db0/db1 双数据源，多租户 Schema）
- **缓存**：Redis
- **消息队列**：Redis Streams（三方接口异步调用，替代 RocketMQ）
- **ORM**：MyBatis Plus

### 系统架构

```
前端 uni-app / Vue3（H5 + 微信小程序，同一套代码）
        │
        ├── /api（nginx 反代）──► 后端① hhim-micro-be :9010（报工/质检/良品率/库存等业务微应用）
        │
        └── wx.request（https 合法域名）──► 后端①（小程序端）
                                            │
        后端① ──Feign 直连──► 后端② hhim-third-platform :8899（微信/个推/短信/邮件/SQM 等三方集成）
        后端② ──Redis Streams──► 三方接口异步调用（失败落库，/external/retryTask 补偿重试）
        中间件：MySQL 双库（db0/db1）/ Redis / MinIO
```

---

## 许可证

本项目采用 **MIT License** 开源。

详细信息请参阅 [LICENSE](./LICENSE) 和 [NOTICE](./NOTICE) 文件。

### 版权声明

Copyright 2026 海尔卡奥斯物联科技有限公司

---

## 快速开始

> 💡 不想手动装环境？直接跳到 [Docker Compose 部署](#docker-compose-部署)，一条命令拉起全部服务（含中间件）。

## Maven 配置建议

首次构建前，建议配置 Maven 镜像以加快依赖下载（特别是在中国大陆）：

**~/.m2/settings.xml**：
```xml
<mirrors>
  <mirror>
    <id>aliyun-central</id>
    <name>Aliyun Maven Central</name>
    <url>https://maven.aliyun.com/repository/central</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

> 如果已有 Maven 配置，使用默认的 Maven Central 也可以，只是下载速度可能较慢。

### 运行环境

- JDK 1.8
- Maven 3.6+
- MySQL 8.0+（im_micro + im-portal 两个库）
- Redis
- MinIO（对象存储，替代 ioss CDN）
- Nacos（可选，用于服务发现和配置管理）

### 第一步：配置数据库

1. 创建两个 MySQL 数据库（MySQL 8.0+）：
   ```sql
   CREATE DATABASE im_micro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   CREATE DATABASE `im-portal` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;  -- 后端②三方对接库
   ```
2. 导入项目根目录的初始化脚本：
   ```bash
   # micro_* 业务表（60 张，含初始数据）
   mysql -h 127.0.0.1 -u root -p im_micro < init.sql
   # hyzz_* 三方对接表（20 张，im-portal 库，后端② db0 数据源用）
   mysql -h 127.0.0.1 -u root -p im-portal < hyzz-schema.sql
   ```
   或使用 Navicat / DBeaver 等工具直接导入。

   > **微信小程序配置（必配）**：`hyzz_micro_miniapp_config` 表（im-portal 库）只建表、不带数据，
   > 部署后必须把微信小程序的 AppId/AppSecret 插入该表（模板见 `hyzz-schema.sql` 末尾的 INSERT，
   > 将占位符替换为你的真实值即可），否则后端②无法解析小程序身份，**微信一键登录、邀请二维码/小程序码生成
   > 等微信功能会失败（接口返回 500）**。
   > 注意：`application_sign` 填 `micro_process`、`platform_type` 填 `wechatMiniApp`（勿填注释里的 `wechat`）、
   > `app_id` 必须与前端 `src/manifest.json` 中 `mp-weixin.appid` 一致。

3. 修改 `cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml` 中
   `db0/db1` 的 `url / username / password` 指向你的数据库。
4. 修改 `hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml` 中
   `db0` 指向 `im-portal` 库、`db1` 指向 `im_micro` 库（用 `cp application-local.yml.example application-local.yml` 复制后填写）。

**初始数据**（init.sql 提供，本地快速体验用）：
- 初始租户：`A9K3Q7`
- 初始管理员：手机号 `13800000000`，昵称"演示管理员"（登录验证码在后端控制台日志中打印）
- 角色：10 企业管理员 / 20 审产员 / 25 质检员 / 30 员工 / 40 试用
- 默认工序：下料 / 车削 / 攻丝

### 第二步：编译项目

> 内部模块（`com.cosmo.plugins` / `com.cosmo.hhim.thirdplat` / `com.cosmo.hhim.micro`）的源码都在本仓库内。
> 请在**仓库根目录**构建：一次 reactor 会按依赖顺序把它们全部产出，无需安装任何离线 jar。
> 单独构建某个子模块会因为兄弟模块尚未安装到 `~/.m2` 而依赖解析失败。

```bash
mvn clean install -DskipTests
```

### 第三步：启动后端

```bash
# 启动微应用后端①（默认端口 9010）
cd cosmo-hhim-micro/hhim-micro-be/micro-interface
mvn spring-boot:run

# 另开终端：启动第三方对接后端②（默认端口 8899）
cd hhim-third-platform/thirdplat-web
mvn spring-boot:run
```

> 或使用 IDEA 打开 `cosmo-hhim-micro/hhim-micro-be` 启动 `LittleGiantsApplication`、打开 `hhim-third-platform/thirdplat-web` 启动 `ThirdpartWebApplication`。
> 启动前请确保已配置两个后端的 `application-local.yml`（数据库 / Redis / 短信）——详见"第一步：配置数据库"。

### 第四步：前端（H5 + 微信小程序）

前端为 uni-app + Vue 3 项目（支持 **H5** 与 **微信小程序**双端）：

```bash
cd cosmo-hhim-micro/hhim-micro-app
npm install --legacy-peer-deps
```

**H5 运行**：
```bash
npm run dev:h5
```
- 本机访问 http://localhost:8082
- 手机（与电脑同局域网）访问 `http://<本机IP>:8082`

**微信小程序运行**：
```bash
npm run dev:mp-weixin
```
用微信开发者工具导入 `dist/dev/mp-weixin` 目录。

**配置说明**：
- 后端地址：H5 端通过环境变量 `VITE_API_BASE` 配置（默认 `/api`，由 vite dev proxy / nginx 反代到后端 9010）；小程序/App 端通过独立环境变量 `VITE_MP_API_BASE` 配置（默认 `http://localhost:9010`，仅限微信开发者工具本地调试）。**真机预览/体验版必须将 `VITE_MP_API_BASE` 改为实际后端地址**（小程序端在 `src/utils/common.ts` 的 `server` 读取该变量），且微信公众平台需配置 request 合法域名（https）。注意：小程序端不能复用 H5 的 `VITE_API_BASE`（其值 `/api` 是相对路径，`wx.request` 不支持）
- 微信小程序 AppID 在 `src/manifest.json` 中修改 `mp-weixin.appid`（仓库默认占位 `touristappid`：微信开发者工具以游客模式运行，**注意：游客模式不支持 canvas 2d 同层渲染，echarts/uCharts 图表会显示空白**——本地调试图表时请在 `manifest.json` 填入你自己的 AppID 后重新编译，测完改回占位符再提交；真机预览/体验版/上传同样需要你的 AppID）

**前端环境变量**（构建时注入，详见 `.env.example`）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_API_BASE` | `/api`（H5 dev 为 `http://localhost:9010`） | 后端接口地址前缀 |
| `VITE_EXPERIENCE_USERNAME` | `15888888888` | 体验登录固定账号 |
| `VITE_EXPERIENCE_PASSWORD` | `cosmoplat` | 体验登录固定密码 |
| `VITE_CDN_BASE` | `/static/micro_app` | 静态图片 CDN 前缀（默认已本地化；如使用自有对象存储可覆盖，如 `https://cdn.example.com`） |
| `VITE_MP_VIDEO_BASE` | `https://your-domain.com/static/micro_app`（占位） | **小程序端静态资源地址（视频 + 图片）**：指向你部署的 H5 静态目录，见下方"小程序端静态资源"说明 |
| `VITE_MP_API_BASE` | `http://localhost:9010` | 小程序/App 端后端地址（微信开发者工具本地调试；真机预览/体验版需改为公网可达 https 域名并在微信公众平台配置 request 合法域名） |
| `VITE_MP_TARGET_APPID` | `wx0000000000000000`（占位） | 小程序跳转目标 AppID（本地开发可在 `.env.development.local` 配置真实值，该文件已 gitignore） |

**H5 端微信专属能力差异**（代码已做多端适配，无需额外配置）：
- 微信一键登录 / 体验登录 → H5 使用**验证码登录**
- 邀请海报 → H5 **直接生成海报**（弹窗显示/下载）
- 分享 → 降级为**复制链接**
- 订阅消息 / 购买跳转 → 属微信专属能力，H5 提示引导

**微信订阅消息模板**：订阅消息模板绑定具体小程序，需在微信公众平台（订阅消息 → 公共模板库）申请"生产日报"类模板，将 `template_id` 更新到数据库 `micro_wechat_msg_template_config`（`WHERE template_type='20' AND service_sign='micro_process'`）。代码逻辑通用，无需修改。

**小程序端静态资源（视频/图片）**：
- 视频（约 62MB）和图片**不会打包进小程序**（小程序主包限制 2MB），而是随 **H5 构建产物**部署（`dist/build/h5/static/micro_app/`），小程序端通过 `VITE_MP_VIDEO_BASE` 从你的 H5 域名加载；
- **依赖关系**：请先完成 H5 部署，再将 `.env.production` 的 `VITE_MP_VIDEO_BASE` 改为你的 H5 域名（如 `https://your-domain.com/static/micro_app`），重新构建小程序；
- **微信合法域名**（真机预览/体验/正式版必须）：在微信公众平台 → 开发设置 → 服务器域名中配置——`request` 合法域名填后端地址、`downloadFile` 合法域名填 H5 静态资源地址（均需 **https + ICP 备案**）；开发调试可先使用开发者工具的"真机调试"（不校验域名）；
- **主包体积**：当前主包约 1.8MB（< 2MB 限制）。后续如新增图片到 `src/static/` 或引入大依赖，请用微信开发者工具检查主包体积，避免超出限制导致无法上传发布。

---

## 项目结构

```
cosmo-hhim-open/
├── hhim-third-platform/          # 第三方对接平台（后端②）
│   ├── thirdplat-api/            # API 模块
│   ├── thirdplat-common/         # 公共模块
│   ├── thirdplat-core/           # 核心模块
│   ├── thirdplat-modules/        # 功能模块
│   └── thirdplat-web/            # Web 层
├── cosmo-himm-commom/            # 通用组件（后端①）
│   └── hhim-common-*             # 各种通用模块
├── cosmo-hhim-micro/             # 微应用
│   ├── hhim-micro-app/           # 微信小程序前端（uni-app）
│   └── hhim-micro-be/            # 微应用后端①
│       ├── micro-application/    # 应用层
│       ├── micro-infrastructure/ # 基础设施
│       └── micro-modules-domain/ # 领域模块
├── .github/                      # CI 工作流
├── docker-compose.yml            # Docker Compose 编排（开源版，一条命令拉起全部服务）
├── .env.example                  # 环境变量模板（复制为 .env 后填写）
├── init.sql                      # micro_* 业务表初始化脚本（60 张表）
├── hyzz-schema.sql               # hyzz_* 三方对接表（im-portal 库，20 张表）
├── LICENSE / NOTICE              # MIT 许可证与第三方组件声明
└── CONTRIBUTING.md               # 贡献指南
```

---

## 配置说明

### 配置文件

主要配置文件位于各模块的 `src/main/resources/` 目录下：

- `application.yml` - 通用配置
- `application-local.yml` - 本地开发配置

### 敏感信息

**重要**：配置文件中的敏感信息（如数据库密码、第三方密钥等）需要替换为实际值或使用环境变量：

```yaml
spring:
  datasource:
    password: ${MYSQL_PASSWORD:your-password}
  redis:
    password: ${REDIS_PASSWORD:your-password}
```

### 第三方服务配置

使用第三方服务（微信、个推、短信等）需要：

1. 注册相应的开发者账号
2. 获取 AppID、AppSecret 等凭据
3. 在配置文件中填写或通过环境变量注入

---

---

## 贡献代码

欢迎提交 Issue 和 Pull Request！

详见 [CONTRIBUTING.md](./CONTRIBUTING.md)

---

## 联系我们

- GitHub Issues: [提交 Issue](https://github.com/your-org/cosmo-hhim-open/issues)

---

## 关于内部模块依赖

本项目内部有 32 个 Maven 模块（groupId 分为 `com.cosmo.plugins`、`com.cosmo.hhim.thirdplat`、`com.cosmo.hhim.micro`），它们**不在 Maven Central**，但**源码全部在本仓库内**，由根 `pom.xml` 的 `<modules>` 聚合：

| groupId | 模块 | 源码目录 |
| --- | --- | --- |
| `com.cosmo.plugins` | `hhim-common-*`（11 个，version 4.1） | `cosmo-himm-commom/` |
| `com.cosmo.hhim.thirdplat` | `thirdplat-*` / `im-api-operation`（12 个，version 1.0） | `hhim-third-platform/` |
| `com.cosmo.hhim.micro` | `micro-*`（9 个，version 1.0） | `cosmo-hhim-micro/hhim-micro-be/` |

因此**不需要任何离线 jar，也不依赖公司私服**：在仓库根目录执行一次 reactor 构建，Maven 会按依赖顺序把这 32 个模块连同两个应用入口一起编译并安装到本地仓库（`~/.m2/repository`）：

```bash
mvn clean install -DskipTests
```

> 注意：请始终从**仓库根目录**构建。直接进入某个子模块执行 `mvn` 时，它的兄弟模块可能尚未安装到 `~/.m2`，会报依赖解析失败。

`pom.xml` 通过 `<dependencyManagement>` 统一声明这些模块的 version，公网依赖走 Maven Central。

---

## 公网依赖

Spring Boot / Spring Cloud / MyBatis / Fastjson 等公网包由 `pom.xml` 的 `<dependencyManagement>` 统一管理版本号，**首次构建时 Maven 会自动从 Maven Central 下载**，不需要任何额外配置。

---

## 配置敏感信息

本项目**不包含**任何明文密码、密钥或内部 IP 地址。所有敏感配置通过 **环境变量占位符**注入。

### 占位符规则

`application-local.yml` 使用 `${ENV_VAR:default}` 格式：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:please_set_db_password}
```

含义：
- `${DB_PASSWORD}` → 读取环境变量 `DB_PASSWORD`
- `:please_set_db_password` → 如果环境变量未设置，使用默认值（带 `please_set_` 前缀）

### 部署方式

#### 方式一：通过环境变量注入（推荐）

```bash
# Linux / macOS
export DB_PASSWORD=your_real_password
export REDIS_PASSWORD=your_redis_password
export NACOS_USERNAME=your_nacos_username
export NACOS_PASSWORD=your_nacos_password
export JWT_SECRET=$(openssl rand -hex 32)
mvn spring-boot:run
```

```powershell
# Windows PowerShell
$env:DB_PASSWORD="your_real_password"
$env:JWT_SECRET=[guid]::NewGuid().ToString().Replace("-","")
mvn spring-boot:run
```

#### 方式二：复制示例文件并修改

```bash
# thirdplat-web
cp hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml.example \
   hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml

# micro-interface
cp cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml.example \
   cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml
```

然后编辑 `application-local.yml`，把所有 `please_set_xxx` 替换为真实值。

### 必填环境变量清单

完整列表请见 `application-local.yml.example` 文件末尾的注释。常用变量：

| 变量名 | 用途 |
|--------|------|
| `DB_PASSWORD` | 数据库密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `NACOS_USERNAME` / `NACOS_PASSWORD` | Nacos 凭证 |
| `NACOS_NAMESPACE` | Nacos 命名空间（默认 `NS04001`） |
| `JWT_SECRET` | JWT 签名密钥（至少 32 字符） |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | MinIO 凭证 |
| `UNIPUSH_APP_KEY` / `UNIPUSH_APP_SECRET` | 个推推送凭证 |
| `WXMP_APP_ID` / `WXMP_APP_SECRET` | 微信公众号凭证 |
| `SQM_CLIENT_ID` / `SQM_CLIENT_SECRET` | SQM 凭证 |
| `MAIL_ACCESS_KEY` | 邮件服务凭证（卡奥斯网关 access-key） |
| `MAIL_UPLOAD_URL` | 邮件附件上传地址 |
| `MAIL_SEND_URL` | 邮件发送地址 |
| `COSMO_SMS_ACCESS_KEY` | 卡奥斯短信网关 AccessKey |
| `COSMO_SMS_URL` | 卡奥斯短信网关 URL |
| `COSMO_SMS_TEMPLATE` | 卡奥斯短信网关模板 ID |
| `SMS_LOGIN_CODE` | 登录验证码短信模板 ID |
| `SMS_WEEK_REPORT` | 周报短信模板 ID |
| `FEEDBACK_DETAIL_URL` | 意见反馈详情页链接（飞书机器人消息附带） |
| `FEISHU_PROD_WEBHOOK` | 飞书生产环境机器人 webhook |
| `FEISHU_TEST_WEBHOOK` | 飞书测试环境机器人 webhook |
| `FEISHU_SUGGEST_BOT_URL` | 飞书意见收集机器人 webhook |

**Docker 部署时在 `.env` 中配置的变量**（`cp .env.example .env` 后有注释说明）：

| 变量名 | 用途 |
|--------|------|
| `MYSQL_ROOT_PASSWORD` / `DB_PASSWORD` / `DB1_PASSWORD` | MySQL root / 应用连接密码（必填） |
| `JWT_SECRET` | JWT 签名密钥，至少 32 位（必填） |
| `THIRDPLAT_DB_HOST` / `THIRDPLAT_DB_PORT` / `THIRDPLAT_DB_NAME` / `THIRDPLAT_DB_USERNAME` / `THIRDPLAT_DB_PASSWORD` | 后端②专用库（默认 compose 内 mysql 的 `im-portal` 库，一般不用改） |
| `SMS_LOGIN_TEMPLATE` / `SMS_WEEK_TEMPLATE` | 后端①短信模板 ID（应与 `SMS_LOGIN_CODE` / `SMS_WEEK_REPORT` 一致） |
| `TODOPUSH_APP_ID` / `TODOPUSH_APP_SECRET` / `TODOPUSH_GATEWAY_URL` / `TODOPUSH_PARENT_ID` | 待办推送（可选） |
| `SYS_URL` / `ORIGINAL_CUSTOMER_CODE` / `APPLICATION_PRODUCT_ID` / `IMPORTEXPORT_IMPORT_LINK` | 系统平台 / 业务默认值（可选） |
| `H5_PORT` | H5 宿主机映射端口（默认 80；服务器要求 8080 时改这里） |

### 代码中的密钥处理

项目使用 Spring `@Value` 占位符注入，不在代码中硬编码真实值：

```java
// 占位符格式：${ENV_VAR:please_set_xxx}
@Value("${mail.access-key:please_set_mail_access_key}")
private String mailAccessKey;

@Value("${notify.feishu.prod-webhook:please_set_feishu_prod_webhook}")
private String prodWebhook;
```

**涉及的 Java 文件**：
- `MailService.java` — 邮件服务凭证和 URL
- `SendIhaierMsg.java` / `TenantWebInterceptorConfig.java` — 飞书 webhook
- `OpenFeishuEventListener.java` — 飞书意见收集 webhook
- `CosmoConfig.java` — 卡奥斯短信网关 URL
- `CustomerSuggestionFacadeService.java` / `MicroFAQFacadeServiceImpl.java` — 意见反馈详情页链接（`feedback.detail-url`）

**用户可覆盖**：通过环境变量（如 `MAIL_ACCESS_KEY=your_key`）或 `application-local.yml` 注入真实值。

### 启动校验

未设置环境变量直接启动时，应用会使用默认值（如 `please_set_db_password`）。  
如果数据库/Redis 连接时仍使用默认值，会**连接失败**，提示用户必须配置真实值。

---

## Docker Compose 部署

项目提供完整的 `docker-compose.yml`，一条命令拉起全部服务（含中间件）。

### 包含的服务

| 服务 | 说明 | 端口 |
|------|------|------|
| `hhim-micro-be` | 微应用后端① | 9010 |
| `hhim-third-platform` | 第三方对接后端② | 8899 |
| `hhim-h5` | H5 前端（nginx） | `${H5_PORT:-80}`（默认 80，.env 可改） |
| `mysql` | 数据库（首次启动自动执行 `init.sql` 建 micro_* 60 张表 + `hyzz-schema.sql` 建 im-portal 库 hyzz_* 20 张表） | 3306 |
| `redis` | 缓存 | 6379 |
| `minio` | 对象存储（替代 ioss CDN） | 9000 / 9001 |

> Nacos 未启用（当前项目未使用服务发现/配置中心），如需启用取消 `docker-compose.yml` 中注释即可。

### 部署步骤

```bash
# 1. 复制环境变量模板并修改
#    必填：MYSQL_ROOT_PASSWORD（数据库密码）、JWT_SECRET（JWT 签名密钥，至少 32 位）
#    其余留空即可启动核心功能（短信/推送/微信等留空 = 对应功能不可用，不阻塞启动）
cp .env.example .env

# 2. 构建镜像并启动全部服务（首次构建需下载 Maven/Node 依赖，约 10-20 分钟）
docker compose up -d --build

# 3. 等待 MySQL 初始化完成（首次自动建 80 张表，最长数分钟，容器状态 healthy 后继续）
docker compose ps

# 4. 查看后端启动日志，出现"微应用服务启动成功"即就绪
docker compose logs -f hhim-micro-be
```

启动后访问：
- H5：http://localhost（`.env` 中 `H5_PORT` 改为 8080 时，访问 http://localhost:8080）
- 后端①：http://localhost:9010
- 后端②：http://localhost:8899
- MinIO 控制台：http://localhost:9001（默认 minioadmin / minioadmin）

### 配置说明

- **全部配置项**在 `.env.example` 中有注释说明；`.env` 不要提交到 git
- **MinIO 图片回显**：`.env` 的 `MINIO_URL` 默认 `http://minio:9000`（容器内互访，后端上传/下载正常）。但浏览器回显图片时解析不了容器名 `minio`——如需图片回显，改为 `http://<宿主机IP>:9000`（compose 已把 9000 映射到宿主机，容器与浏览器都能访问）。桶（hhim / hhim-micro / hyzz-site-test）由 `minio-init` 服务首次启动自动创建
- **后端②专用库**：`THIRDPLAT_DB_*` 默认指向 compose 内 mysql 的 `im-portal` 库（hyzz_* 三方对接表，由 `hyzz-schema.sql` 首次启动自动创建），一般不用改
- **自定义中间件**：若已有自建 MySQL/Redis/MinIO，只需修改 `.env` 中的 `DB_HOST`、`REDIS_HOST`、`MINIO_URL` 等指向你自己的服务即可，无需改动 compose
- **可选功能**（留空 = 功能不可用，不影响其他功能）：
  - 短信：`COSMO_SMS_ACCESS_KEY` / `COSMO_SMS_URL` / `COSMO_SMS_TEMPLATE` / `SMS_LOGIN_CODE` / `SMS_WEEK_REPORT` / `SMS_LOGIN_TEMPLATE` / `SMS_WEEK_TEMPLATE`
  - 个推推送：`UNIPUSH_APP_ID` / `UNIPUSH_APP_KEY` / `UNIPUSH_APP_SECRET` / `UNIPUSH_MASTER_SECRET` / `UNIPUSH_APP_PACKAGE` / `UNIPUSH_BASE_URL`
  - 微信公众号：`WXMP_APP_ID` / `WXMP_APP_SECRET` / `WXMP_SERVER_TOKEN` / `WXNO_SERVER_AES_KEY`
  - 海尔 SQM 开放平台：`SQM_CLIENT_ID` / `SQM_CLIENT_SECRET`
  - 待办推送：`TODOPUSH_APP_ID` / `TODOPUSH_APP_SECRET` / `TODOPUSH_GATEWAY_URL` / `TODOPUSH_PARENT_ID`
- **镜像构建参数**：`VITE_API_BASE`（H5 API 前缀）、`VITE_EXPERIENCE_USERNAME` / `VITE_EXPERIENCE_PASSWORD`（体验登录账号）可在 `.env` 中覆盖

### 常见问题

- **首次构建慢**：首次 `up -d --build` 需下载 Maven / npm 依赖（后端 Dockerfile 已配置阿里云 Maven 镜像、Node 固定 16），约 10-20 分钟，属正常现象
- **MySQL 初始化慢**：首次启动自动建 80 张表（init.sql + hyzz-schema.sql），容器状态 `unhealthy` 期间属正常（healthcheck `start_period` 已放宽到 600s），耐心等待变 `healthy`
- **构建失败（Node 版本）**：uni-app 要求 Node 16/18，Dockerfile 已固定 `node:16-alpine`，不受本机 Node 版本影响
- **`.env` 未创建**：compose 会用默认值启动，但 `MYSQL_ROOT_PASSWORD` / `JWT_SECRET` 为空会导致连接失败，务必先 `cp .env.example .env` 并修改
- **前端端口冲突**：80 被占用时，`.env` 中改 `H5_PORT=8080` 后 `docker compose up -d` 即可
- **中间件端口冲突**：compose 把中间件端口（3306/6379/9000/9001）也映射到宿主机，本机已装 MySQL/Redis/MinIO 会冲突。解决：停掉本机同名服务，或修改 compose 中对应 `ports:` 映射（如 `"3307:3306"`）；若中间件只需容器内互访、不对外，直接删除该服务的 `ports:` 段即可

---

## 默认账号与测试数据

`init.sql` 初始化脚本提供**演示用初始数据**（仅用于本地快速体验，详见"第一步：配置数据库"）：

- **初始管理员**：手机号 `13800000000`，昵称"演示管理员"（角色：10 企业管理员）
- **初始租户**：`A9K3Q7`
- **角色**：10 企业管理员 / 20 审产员 / 25 质检员 / 30 员工 / 40 试用
- **默认工序**：下料 / 车削 / 攻丝

> ⚠️ 生产环境请**删除演示账号**并自行创建管理员，勿使用默认手机号。

---

## AI 问数（问一问）

微信小程序/H5 端新增"问一问"入口（工作台悬浮 AI 按钮），面向管理员/审产员提供**自然语言经营问数**：产量、良品率、不良明细、记工排名、库存、实体清单等，答案附**分级血缘证据**（口径/来源/快照）与图表跳转。

### 快速配置（必读）

```yaml
# micro-interface/src/main/resources/application-local.yml（本文件已被 .gitignore 忽略）
ai:
  base-url: ${AI_BASE_URL:please_set_ai_base_url}     # LLM 网关（OpenAI 兼容，如 DeepSeek / 通义 / OpenAI 等）
  api-key: ${AI_API_KEY:please_set_ai_api_key}        # 真实密钥只存在于本地，勿提交
  chat-model: ${AI_CHAT_MODEL:please_set_ai_chat_model}
```

- 未配置密钥时功能可用（规则意图解析 + 模板答案）；配置后自动升级 LLM 意图解析与答案润色（数字一致性校验，失败回退模板）；
- 会话上限 200 条/会话（约 100 轮），会话持久化于 `micro_ai_chat_session` / `micro_ai_chat_message`（建表脚本见仓库根 `hhim-ai-chat-schema.sql`）。

### 本体资产（单一来源，CI 可挂）

- `ai-ontology/metrics.json`（指标层·9 指标）+ `entities.json`（实体层）+ `relations.json`（关系层）；
- 工具链：`validate.js`（校验）/ `capability.js`（能力清单投影）/ `export-frontend.js`（前端导出）/ `sync.js`（**一键同步**：本体 → 后端 resources + 前端 `src/utils/ai-capability.ts`）；前端投影固定放**主包** `utils/`——主包组件与分包页面都要引用它，微信小程序禁止主包 require 分包模块；
- **新增指标/组合 = 改本体文件 + 登记 SQL（人写）**，运行 `node ai-ontology/sync.js` 生效，而非改 prompt。

### 接口

`POST /ai/ask`（问数）· `POST /ai/chat/session` 等会话 5 接口 · `GET /ai/capability`（能力清单）——详见《问一问接口契约》。

### 覆盖不到怎么办（组合登记与自检）

登记制（人写固定 SQL）的固有风险是"本体承诺"与"执行器实现"漂移。问数不覆盖时**不允许静默失败**：

| 情形 | 处理 | 用户看到 |
| --- | --- | --- |
| 问题超出能力清单（创作/归因/闲聊/未登记指标） | LLM 语义边界 | 原因 + 「你可以试试…」 |
| 粒度在本体 `dims` 内、执行器未登记该组合 | `IntentExecutor` 入口收口 → 确定性错误（UNSUPPORTED）→ STOP | 「这个问法我还没学会。『X』目前不支持按 Y 看（可用粒度：按 Z）；你可以先问「…」」 |
| 登记实现/配置缺陷等确定性错误 | 中性话术，技术原因只进日志（`[Agent/Reflect]`） | 「这个查询我没跑通（已记录）。换个范围或问法再试试」 |

- **组合台账**：`IntentExecutor` 里 `CONSUMED_GROUP_BY`（已消费粒度）/ `TOLERATED_GROUP_BY`（不消费但返回行天然含该维度，近似可接受，仅告警）/ `ENTITY_NARROWED_OK`（已被单个实体约束的冗余分组，如"法兰盘产量"+product）。
- **比率取舍必须与页面一致（数字一致性铁律）**：全站既有口径是 `BigDecimal.divide(..., 3, RoundingMode.DOWN)`（**截断** 3 位），所以登记 SQL 一律用 `truncate(...,3)`（**不要用 `round(...,4)`**），`AnswerComposer.rate()` 也先截断 3 位再 ×100。否则会出现"问数 95.7% / 页面 95.6%"（88/92 的场景）。
- **零 SQL 优先**：同一批登记 SQL 换个排序键/比率键就是新组合——`SUMMARY×{product|employee|process}` 产量排行复用良品率 SQL（按已审记工总数=`checkPassNum+checkNgNum`），`良品率×day` 复用按日聚合 SQL 现算比率。
- **启动自检**：`ExecutionCoverageChecker` 打印本体 `dims` 与执行器台账的差集——`[AI覆盖]`（一致）/ 未登记组合 / 未登记指标 / 死代码；缺口同时以 `[AI缺口] type=...` 出现在运行日志，可直接聚合成"该登记清单"。
- **缺口的三种补法**（先分诊再动手）：① 已有 SQL × 已有维度 → 只补组合/投影（零 SQL）；② 确实缺数据形状 → 在 `MicroAiDailyMapper.xml` 登记一条固定 SQL（必带 `tenant_code` 隔离、口径注释、`nullif` 防除零、`limit`）；③ 本体承诺了但短期不实现 → 从该指标 `questionTemplates` 中移除，别让模板承诺假能力。
- 红线不变：**LLM 只输出语义键，绝不产 SQL/列名/数字**；新 SQL 一律人写、可评审、可回归。

### 前端交互（问一问页面）

- **答案卡片**：关键数字高亮、长答案折叠（>150 字）、复制、**统计范围回显**（登记实现参数回显，用户可确认"问的是不是这段"）、依据展开（指标口径 / 数据来源 / 结果快照，键名中文化、比率转百分比）、查看图表跳转、歧义澄清选项；
- **继续问**：按命中意图取 `FOLLOW_UPS`（本体 `questionTemplates` 投影）生成追问建议，仅最新一条答案展示，避免刷屏；空态为 `QUICK_QUESTIONS` 快捷提问（同为本体投影）；
- **失败可重试**：问数失败在对话内留失败态气泡（本地态，不落库），点"重试"用同一问题重发，不重复上屏问题；
- **会话**：会话记录弹层（搜索 / 重命名 / 删除 / 当前标记）、切换会话有加载态与竞态保护（快速连点丢弃过期响应）、超过 5 个会话出现搜索框；
- **滚动与键盘**：用户上翻查看历史时新答案不强行拽到底（浮出"回到最新"）；H5 端按 `visualViewport` 上移输入栏，避免被输入法遮挡（小程序端由 `textarea` 原生 `adjust-position` 处理）。
