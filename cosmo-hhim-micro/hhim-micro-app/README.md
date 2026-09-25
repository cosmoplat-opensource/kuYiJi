### 组件库

> https://vant-ui.github.io/vant-weapp/#/quickstart

> 在制品库存-prod:wx0000000000000000
> 在制品库存-test:wx0000000000000000

---

## 构建和启动

### 环境要求

- **Node.js**: 16.x 或 18.x（不支持 Node.js 22.x）
- **npm**: 8.x+

### 安装依赖

```bash
cd hhim-micro-app
rm -Recurse -Force node_modules, package-lock.json
npm install --legacy-peer-deps
```

> 由于项目依赖的 uni-app 插件版本较旧，与新版 npm 存在 peer dependency 冲突，需要使用 `--legacy-peer-deps` 参数。

### 开发命令

| 平台 | 命令 | 说明 |
|------|------|------|
| H5 | `npm run dev:h5` | 运行在 http://localhost:8082 |
| 微信小程序 | `npm run dev:mp-weixin` | 构建到 `dist/build/mp-weixin` |
| App | `npm run dev:app` | - |
| 生产构建 | `npm run build:h5` / `npm run build:mp-weixin` | - |

### 微信小程序运行步骤

1. 安装依赖：
   ```bash
   npm install --legacy-peer-deps
   ```

2. 构建微信小程序：
   ```bash
   npm run dev:mp-weixin
   ```

3. 打开微信开发者工具，导入项目：
   - 目录选择：`hhim-micro-app/dist/build/mp-weixin`
   - AppID：使用测试号或填入真实 AppID

4. 勾选"不校验合法域名"（开发阶段）

### 常见问题

**Q: npm install 报 ERESOLVE 错误**
```
A: 使用 --legacy-peer-deps 参数
npm install --legacy-peer-deps
```

**Q: Node.js 版本不兼容**
```
A: 项目 uni-app 插件不兼容 Node.js 22.x，降至 16.x 或 18.x
nvm install 18
nvm use 18
```

**Q: 微信小程序 SVG 静态资源路径解析失败**
```
A: 分包中的绝对路径 /pages-xxx/static/ 在某些构建配置下无法解析，
   需要改为相对路径或检查 vite 配置。
```
