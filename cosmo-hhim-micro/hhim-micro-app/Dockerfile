# ============================================================
# hhim-h5（前端 H5）镜像
# 构建上下文：cosmo-hhim-micro/hhim-micro-app 目录
#   docker build cosmo-hhim-micro/hhim-micro-app
# 构建参数：
#   VITE_API_BASE  前端 API 请求前缀（默认 /api，由 nginx 反代到后端）
#   VITE_EXPERIENCE_USERNAME / VITE_EXPERIENCE_PASSWORD  体验登录账号（默认通用体验账号）
# ============================================================

# ---------- 阶段 1：Node 构建（uni-app 要求 Node 16/18） ----------
FROM node:16-alpine AS build
WORKDIR /app

# 依赖安装（利用缓存；项目存在 eslint peer 依赖冲突，按 README 用 --legacy-peer-deps）
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps || npm install --legacy-peer-deps

# 源码
COPY . .

# API 地址注入（H5 走 nginx 同域 /api，避免跨域）
ARG VITE_API_BASE=/api
ENV VITE_API_BASE=$VITE_API_BASE

# 体验登录账号注入（可选，默认通用体验账号）
ARG VITE_EXPERIENCE_USERNAME=15888888888
ENV VITE_EXPERIENCE_USERNAME=$VITE_EXPERIENCE_USERNAME
ARG VITE_EXPERIENCE_PASSWORD=cosmoplat
ENV VITE_EXPERIENCE_PASSWORD=$VITE_EXPERIENCE_PASSWORD

RUN npm run build:h5

# ---------- 阶段 2：Nginx 运行 ----------
FROM nginx:alpine
COPY --from=build /app/dist/build/h5 /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 安全响应头（被 nginx.conf include），防点击劫持/MIME嗅探/信息泄露等
COPY security-headers.conf /etc/nginx/security-headers.conf
# CSP 动态源变量（http 上下文 map，自动被主配置 include；文件名 00- 确保先于 default.conf 加载）
COPY csp-map.conf /etc/nginx/conf.d/00-csp-map.conf
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
