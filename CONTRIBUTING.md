# 贡献指南

感谢您对本项目的兴趣！欢迎提交 Issue 和 Pull Request。

---

## 行为准则

请尊重他人，保持友善和专业的沟通态度。

---

## 提 Bug

在 [GitHub Issues](https://github.com/your-org/cosmo-hhim-open/issues) 中提交 Bug，附上：

- **环境信息**：JDK 版本、操作系统、数据库版本
- **复现步骤**：清晰的步骤说明
- **期望行为** vs **实际行为**
- **相关日志或截图**（如有）

---

## 贡献代码

### 1. Fork 本仓库

点击 GitHub 页面的 `Fork` 按钮。

### 2. 克隆代码

```bash
git clone https://github.com/your-username/cosmo-hhim-open.git
cd cosmo-hhim-open
```

### 3. 创建功能分支

```bash
git checkout -b feature/your-feature-name
# 或修复 bug
git checkout -b fix/your-bug-fix
```

### 4. 开发

- 按照代码规范编写代码
- 添加必要的单元测试
- 确保所有测试通过

### 5. 提交代码

```bash
git add .
git commit -m "feat: 添加新功能"
git push origin feature/your-feature-name
```

#### 提交信息规范

```
<type>: <subject>

<body>
```

type 类型：
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档变更
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建或辅助工具变更

### 6. 提 Pull Request

1. 推送分支到你的 Fork
2. 在 GitHub 上创建 Pull Request
3. 描述清楚改动内容和原因
4. 等待代码审查

---

## 开发环境

### 前置条件

- JDK 1.8
- Maven 3.6+
- MySQL 8.0+
- Redis
- Nacos（可选，用于服务发现）

### 本地运行

```bash
# 编译 + 安装全部内部模块（务必在仓库根目录执行，一次 reactor 构建产出全部内部依赖）
mvn clean install -DskipTests

# 运行测试
mvn test

# 启动服务
mvn spring-boot:run
```

### 配置

复制并修改配置文件（示例文件 → 本地文件，均已在 .gitignore 中）：

```bash
# 后端② thirdplat-web
cp hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml.example \
   hhim-third-platform/thirdplat-web/src/main/resources/application-local.yml

# 后端① micro-interface
cp cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml.example \
   cosmo-hhim-micro/hhim-micro-be/micro-interface/src/main/resources/application-local.yml
```

填写必要的配置（数据库、Redis、第三方服务密钥等），详细步骤见 [README](./README.md)「快速开始」。

### 前端（uni-app，H5 + 微信小程序）

```bash
cd cosmo-hhim-micro/hhim-micro-app
npm install --legacy-peer-deps   # 规避 uni-app 官方 peer 依赖冲突

npm run dev:h5                   # H5 开发（http://localhost:8082）
npm run dev:mp-weixin            # 小程序开发（微信开发者工具导入 dist/dev/mp-weixin）
```

注意：小程序端图表依赖 canvas 2d 同层渲染，**游客模式（AppID 占位 touristappid）下图表会空白**——本地调试图表时在 `src/manifest.json` 填入你自己的 AppID 后重新编译，提交前改回占位符（详见 README「配置说明」）。

---

## 代码规范

- 使用 IDEA 默认代码格式化（`Ctrl+Alt+L`）
- 类和方法要有 Javadoc 注释
- 变量命名要清晰、有意义
- 禁止在代码中硬编码敏感信息，使用配置文件或环境变量
- 所有 `System.out.println` 改用日志框架

---

## 测试要求

- 新功能必须包含单元测试
- 修复 Bug 时添加回归测试
- 确保 `mvn test` 全部通过后再提 PR

---

## 分支管理

- `main`: 主分支，稳定版本
- `feature/*`: 功能分支
- `fix/*`: 修复分支

---

## 许可证

提交代码即表示您同意您的代码以 [MIT License](./LICENSE) 许可证开源。

---

## 联系方式

- GitHub Issues: [提交 Issue](https://github.com/your-org/cosmo-hhim-open/issues)
- 邮箱: your-email@example.com