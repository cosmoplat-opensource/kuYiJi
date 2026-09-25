import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'
import fs from 'fs'

/**
 * 仅开发环境生效：vite 2.9 的 dev server 无法按路径提供中文文件名的静态资源（返回 404），
 * 这里拦截 /static/micro_app/ 请求，解码中文路径后从 public（视频）或 src（图片）目录读文件返回。
 * 生产构建不走 configureServer，因此对构建产物与线上 nginx 无任何影响。
 */
function serveChineseStatic() {
  return {
    name: 'serve-chinese-static',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = (req.url || '').split('?')[0]
        if (url.startsWith('/static/micro_app/')) {
          let decoded = url
          try {
            decoded = decodeURIComponent(url)
          } catch (e) {
            // 解码失败按原路径处理，交给后续中间件
            return next()
          }
          // 允许访问的根目录：public/static/micro_app（视频+大图）与 src/static/micro_app（包内 css 图）
          const allowedRoots = [
            path.resolve(__dirname, 'public', 'static', 'micro_app'),
            path.resolve(__dirname, 'src', 'static', 'micro_app')
          ]
          // path.resolve 会展开 ../ 等相对段，规范化后再做前缀校验，防止路径穿越（/static/micro_app/../../../ 读取任意文件）
          const candidates = allowedRoots.map(root =>
            path.resolve(root, '.' + decoded.replace(/^\/static\/micro_app/, ''))
          )
          const filePath = candidates.find(p => {
            const root = allowedRoots.find(r => p === r || p.startsWith(r + path.sep))
            return !!root && fs.existsSync(p) && fs.statSync(p).isFile()
          })
          if (!filePath) {
            return next()
          }
          if (filePath) {
            const ext = path.extname(filePath).toLowerCase()
            const mime = ext === '.mp4' ? 'video/mp4'
              : ext === '.png' ? 'image/png'
              : ext === '.jpg' || ext === '.jpeg' ? 'image/jpeg'
              : ext === '.gif' ? 'image/gif'
              : ext === '.svg' ? 'image/svg+xml'
              : 'application/octet-stream'
            const size = fs.statSync(filePath).size

            // 支持 HTTP Range 请求：浏览器 <video> 播放会发 Range 头探测/分片拉取，
            // 期望 206 Partial Content + Content-Range，否则报 MEDIA_ERR_SRC_NOT_SUPPORTED
            res.setHeader('Accept-Ranges', 'bytes')
            res.setHeader('Content-Type', mime)

            const range = req.headers.range
            if (range) {
              const m = /^bytes=(\d*)-(\d*)$/.exec(range)
              if (m) {
                let start = m[1] ? parseInt(m[1], 10) : 0
                let end = m[2] ? parseInt(m[2], 10) : size - 1
                if (isNaN(start) || start < 0) start = 0
                if (isNaN(end) || end >= size) end = size - 1
                if (start > end || start >= size) {
                  res.statusCode = 416
                  res.setHeader('Content-Range', `bytes */${size}`)
                  res.end()
                  return
                }
                res.statusCode = 206
                res.setHeader('Content-Range', `bytes ${start}-${end}/${size}`)
                res.setHeader('Content-Length', end - start + 1)
                fs.createReadStream(filePath, { start, end }).pipe(res)
                return
              }
            }

            // 无 Range 头：返回完整文件
            res.setHeader('Content-Length', size)
            fs.createReadStream(filePath).pipe(res)
            return
          }
        }
        next()
      })
    }
  }
}

/**
 * 仅开发环境生效：uni-app 的 H5 dev server 不提供 vite 标准 public 目录，
 * 这里把 public/ 根级文件（如 cover-support.js，index.html 以 /xxx.js 绝对路径引用）
 * 在 dev 模式下直接返回，与生产构建的 copyPublicStaticToH5 复制逻辑对应。
 * 安全：仅匹配 public/ 根级单层文件名（不含 / 与 ..），无路径穿越面。
 */
function servePublicRootFiles() {
  return {
    name: 'serve-public-root-files',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = (req.url || '').split('?')[0]
        const publicRoot = path.join(__dirname, 'public')
        if (!fs.existsSync(publicRoot)) {
          return next()
        }
        const fileName = url.replace(/^\//, '')
        if (fileName && !fileName.includes('/') && !fileName.includes('..')) {
          const filePath = path.join(publicRoot, fileName)
          if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
            res.setHeader('Content-Type', 'application/javascript')
            fs.createReadStream(filePath).pipe(res)
            return
          }
        }
        next()
      })
    }
  }
}

/**
 * 仅 H5 生产构建生效：uni-app 不使用 vite 的 public 目录，
 * 这里在构建结束后把 public/ 下的文件复制到 H5 产物：
 *  - public/ 根级文件（如 cover-support.js）→ 产物根
 *  - public/static/micro_app（视频 + 大图）→ 产物 /static/micro_app
 * 使 H5 部署后资源完整（小程序包不包含这些文件，走远程加载）。
 */
function copyPublicStaticToH5() {
  return {
    name: 'copy-public-static-to-h5',
    apply: 'build',
    closeBundle() {
      if (process.env.UNI_PLATFORM !== 'h5') {
        return
      }
      const publicRoot = path.join(__dirname, 'public')
      const outRoot = path.join(__dirname, 'dist', 'build', 'h5')
      if (!fs.existsSync(publicRoot)) {
        return
      }
      // 1) public/ 根级文件 → 产物根（cover-support.js 等，index.html 以 /xxx.js 绝对路径引用）
      for (const name of fs.readdirSync(publicRoot)) {
        const src = path.join(publicRoot, name)
        if (fs.statSync(src).isFile()) {
          fs.copyFileSync(src, path.join(outRoot, name))
          console.log('[copy-public-static-to-h5] copied root file:', name)
        }
      }
      // 2) public/static/micro_app（视频+大图）→ 产物 /static/micro_app
      const srcDir = path.join(publicRoot, 'static', 'micro_app')
      const outDir = path.join(outRoot, 'static', 'micro_app')
      if (!fs.existsSync(srcDir)) {
        return
      }
      fs.mkdirSync(outDir, { recursive: true })
      for (const name of fs.readdirSync(srcDir)) {
        fs.copyFileSync(path.join(srcDir, name), path.join(outDir, name))
      }
      console.log('[copy-public-static-to-h5] copied', fs.readdirSync(srcDir).length, 'files ->', outDir)
    }
  }
}

/**
 * 仅开发环境生效：HMR WebSocket 的健壮性兜底。
 *
 * 背景：vite 2.9 内置的 ws 服务在收到"非法帧"时会把 error 抛成未捕获异常，Node 进程直接退出
 * （RangeError: Invalid WebSocket frame: RSV2 and RSV3 must be clear → WS_ERR_UNEXPECTED_RSV_2_3，
 * 表现为 `npm run dev:h5` 莫名其妙自己停）。触发源通常是：局域网/虚拟网卡上的扫描器、代理、
 * 带 WebSocket 调试能力的浏览器插件、手机调试客户端等 —— 它们完成握手后发了非 WebSocket 数据。
 *
 * 这里挂上 error 监听把异常"吃掉"（只打警告，不崩进程），dev 体验不受影响。
 * 想彻底隔绝来源，可用 DEV_HOST=127.0.0.1 只监听本机（见下方 server.host）。
 */
function guardHmrWebSocket() {
  return {
    name: 'guard-hmr-websocket',
    configureServer(server: any) {
      try {
        // 1) server 级 error
        server.ws?.on?.('error', (e: any) => {
          console.warn('[hmr-ws] 忽略异常连接（不影响服务）:', e?.message || e)
        })
      } catch (e) {
        // 挂不上就算了，不阻断启动
      }
      try {
        // 2) 升级阶段的 socket 错误同样不能让进程退出
        server.httpServer?.on?.('upgrade', (_req: any, socket: any) => {
          socket?.on?.('error', () => {})
        })
      } catch (e) {
        // ignore
      }
      try {
        // 3) **每一条连接**都要挂 error 监听：
        //    崩溃栈是 "Emitted 'error' event on WebSocket$1 instance" —— 说明是"单条连接"
        //    的 ws 实例抛错且无人处理 → Node 直接退出。server 级监听管不到它。
        server.ws?.on?.('connection', (socket: any) => {
          socket?.on?.('error', (e: any) => {
            console.warn('[hmr-ws] 连接异常已忽略:', e?.message || e)
          })
          socket?.on?.('close', () => {})
        })
      } catch (e) {
        // ignore
      }
    }
  }
}

export default defineConfig({
  plugins: [uni(), serveChineseStatic(), servePublicRootFiles(), copyPublicStaticToH5(), guardHmrWebSocket()],
  resolve: {
    alias: [
      { find: '@', replacement: path.resolve(__dirname, 'src') },
      // 分包页面静态资源：/pages-xxx/static → src/pages-xxx/static（H5 构建需要，小程序不受影响）
      { find: /^\/pages-([a-zA-Z0-9-]+)\/static/, replacement: path.resolve(__dirname, 'src/pages-$1/static') }
    ]
  },
  server: {
    // 是否自动打开浏览器
    open: false,
    // 服务器端口号
    port: 8082,
    // 设为 true ,若端口已被占用则会直接退出，而不是尝试下一个可用端口
    strictPort: false,
    // 为开发服务器配置 CORS（H5 直连后端，需要后端放开 CORS 或开发期由前端代理，这里仅留开关）
    cors: true,
    // 设置为 true 强制使依赖预构建
    force: false,
    // 监听地址：**默认只监听本机 127.0.0.1** —— 局域网里的扫描器/插件会向 0.0.0.0 的
    // HMR 端口发非法 WebSocket 帧，把 dev server 打挂（RSV2/RSV3 崩溃，已发生多次）。
    // 需要用手机/其他设备调试时再放开：
    //   Windows: set DEV_HOST=0.0.0.0 && npm run dev:h5
    host: process.env.DEV_HOST || '127.0.0.1',
    // 开发代理 —— 仅 dev 生效；若后端已放开 CORS 可直连，此配置不会命中
    // H5 请求相对路径（VITE_API_BASE=/api）时才走代理；直连绝对地址时不经过
    proxy: {
      '/api': {
        target: 'http://localhost:9010',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
