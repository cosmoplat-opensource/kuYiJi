/**
 * decouple-from-ops-platform-cleanup (C.9): 严格契约守卫
 *
 * 用于确保"需要租户上下文"的 API 调用在用户登录 + 选完租户后才执行。
 *
 * 背景：旧架构下 gateway 给所有请求注入默认 target_schema / target_customer，
 *       所以前端可以"无脑"发请求；新架构下后端拦截器只对 includedPaths 生效，
 *       其他接口依赖前端显式通过 token + tenantCode 注入。
 *
 * 用法（任选其一）：
 *
 *   // A) 函数式守卫：调用方负责条件分支
 *   if (!requireAuth({ redirect: '/pages-login/index' })) return
 *   _get({ url: '/setting/getIndividuationConfig' })
 *
 *   // B) 包装器：把整个请求包起来，失败时自动处理
 *   withAuth(() => _get({ url: '/setting/getIndividuationConfig' }))
 *
 * 任选其一，团队按习惯统一。
 */

export interface RequireAuthOptions {
  /**
   * 失败时的行为：
   *  - 'return'  : 静默 return false，调用方自行处理（默认）
   *  - 'redirect': 跳登录页
   *  - 'toast'   : 仅弹 toast 提示
   */
  onFail?: 'return' | 'redirect' | 'toast'
  /**
   * 失败时 toast 文案（onFail='toast' 或 onFail='redirect' 时生效）
   */
  message?: string
  /**
   * 跳转目标（onFail='redirect' 时生效）
   */
  redirectTo?: string
}

/**
 * 守卫检查：返回 true = 通过；返回 false = 不通过
 */
export function requireAuth(options: RequireAuthOptions = {}): boolean {
  const token = uni.getStorageSync('micro_token')
  const tenantCode = uni.getStorageSync('micro_tenantCode')

  if (token && tenantCode) {
    return true
  }

  // 失败路径
  const onFail = options.onFail ?? 'return'
  const message =
    options.message ??
    (token
      ? '请先选择租户'
      : '请先登录')

  if (onFail === 'toast' || onFail === 'redirect') {
    uni.showToast({ title: message, icon: 'none' })
  }

  if (onFail === 'redirect') {
    const target = options.redirectTo ?? '/pages-login/index'
    setTimeout(() => {
      uni.reLaunch({ url: target })
    }, 1000) // 留出 toast 显示时间
  }

  return false
}

/**
 * 包装器：把整个 API 调用包起来，守卫失败时返回 null
 *
 * 用法：
 *   const data = await withAuth(
 *     () => _get({ url: '/setting/getIndividuationConfig' }),
 *     { onFail: 'redirect' }
 *   )
 *   if (!data) return  // 守卫失败
 */
export async function withAuth<T>(
  apiCall: () => Promise<T>,
  options: RequireAuthOptions = {}
): Promise<T | null> {
  if (!requireAuth(options)) {
    return null
  }
  return apiCall()
}

/**
 * 给 onShow / event handler 用的"登录后跳回"工具：
 *   onShow(() => {
 *     if (!token) { ensureLoginAndBack() }  // 跳登录页，登录成功后会自动跳回
 *   })
 */
export function ensureLoginAndBack(redirectTo: string = '/pages-login/index') {
  if (!uni.getStorageSync('micro_token')) {
    uni.reLaunch({ url: redirectTo })
    return false
  }
  return true
}
