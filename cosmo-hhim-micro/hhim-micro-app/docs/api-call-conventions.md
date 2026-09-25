# 小程序 API 调用约定

> decouple-from-ops-platform-cleanup 决策 (2026-06-05)：
> 任何需要租户上下文的接口，**必须**先有 `micro_token` + `micro_tenantCode` 才能调用。

## 背景

旧架构下，gateway 会给所有请求注入默认的 `target_schema / target_customer` header（`M000008 / im-micro`），所以即使前端没登录也能"跑通"业务接口——但数据是脏的，用户无感知。

新架构下，gateway 没了，`CertificateInterceptor` 只对 `includedPaths` 生效；其他接口的 `ThreadContext` 依赖前端显式通过 token 提供 schema/customer。**前端必须显式承担"登录 → 选租户 → 调接口"的链路责任**。

## 规则

### 1. 哪些接口需要租户上下文？

| 接口类别 | 需要 token | 需要 tenantCode | 例子 |
|----------|-----------|----------------|------|
| 公开接口 | ❌ | ❌ | `POST /login/wxminiapp/login`、`POST /login/register-tenant`、`GET /login/codeToPhone` |
| 租户级业务接口 | ✅ | ✅ | `/setting/getIndividuationConfig`、`/user/completeUserInfo`、`/import/**` |
| 临时存储 / 上传 | ✅ | ❌（被 `includedPaths` 拦截器处理）| `/import/importTemplate` |
| 跨租户 / 系统级 | ❌ | ❌ | 几乎不存在 |

### 2. 强制规则

- ❌ **禁止**在 `onLoad` / `onShow` / `<script setup>` 顶层 module-load 时调用租户级接口（即使有 token，也要等用户**明确选完租户**后）
- ✅ **必须**在调用前用 `requireAuth()` 守卫；守卫失败时给出明确引导（提示登录或跳登录页）

### 3. 守卫模式

```typescript
import { requireAuth } from '@/utils/auth-guard'

// 写法 A：函数式守卫（推荐用于 onShow / event handler）
async function fetchSettings() {
  if (!requireAuth({ redirect: '/pages-login/index' })) return
  _get({ url: '/setting/getIndividuationConfig' })
    .then(/* ... */)
}

// 写法 B：包装器（推荐用于 hook 顶层）
function getIndividuationConfig() {
  return withAuth(
    () => _get({ url: '/setting/getIndividuationConfig' }),
    { onFail: 'redirect' }
  ).then(/* ... */)
}
```

`requireAuth` 返回 `boolean`；`withAuth` 包装整个调用。两种方式任选。

### 4. 入口与登录流程

- 应用 entry 是 `pages/main`（在 `pages.json`）
- `App.vue` 的 `initData` 负责 token 检测和 `initUserInfo`
- `pages/main` 的 `onShow` 必须做 token guard：没 token → 跳登录；有 token 但没 tenantCode → 跳登录或自动选主租户
- 登录成功后，通过 `wx.navigateBack` / `uni.reLaunch` 回到 `pages/main`

## 违规检查（CI / 评审）

提交前在 PR description 里确认：
- [ ] 没有 `<script setup>` / `onLoad` 顶层 module-load 调用租户接口（除非已 `requireAuth`）
- [ ] 所有 `_get / _post / _put / _delete` 调租户接口的位置，前 1-3 行有守卫

## 何时打破规则

没有。所有租户级接口**必须**走守卫。如果有"看似简单"但想跳过的场景，**先**加 token + tenantCode 设置逻辑，**再**调接口。绝不允许"我先发出去看看后端给什么"。

## 后端对应约束

`CertificateInterceptor` 只对 `certificate.includedPaths` 列表生效（不在 `excludedPaths` 内的请求会被拦截）。后端对没 token 的请求返回 401（`CustomException("请求凭证无效或已过期！")`）。**后端不提供任何租户默认值**，前端必须显式提供。

## 参考

- 决策记录：`openspec/changes/decouple-from-ops-platform-cleanup/tasks.md` §"架构决策：租户接口必须先登录"
- 工具函数：`hhim-micro-app/src/utils/auth-guard.ts`
- Dogfooding：`hhim-micro-app/src/hooks/setting-config.ts`

---

> 创建于 2026-06-05 · decouple-from-ops-platform-cleanup C.9
