/**
 * decouple-from-ops-platform-cleanup (B.2): 租户选择 picker
 * 基于 uni.showActionSheet API 的工具模块（无 UI）。
 *
 * 使用方式：
 *   import { showTenantPicker, ITenantOption } from '@/components/tenant-picker'
 *   showTenantPicker(tenantList).then(tenant => ...)
 */

export interface ITenantOption {
  tenantCode: string
  tenantName: string
  customerName?: string
  isPrimary?: number
}

export function showTenantPicker(tenantList: ITenantOption[]): Promise<ITenantOption | null> {
  return new Promise((resolve) => {
    if (!Array.isArray(tenantList) || tenantList.length === 0) {
      resolve(null)
      return
    }
    if (tenantList.length === 1) {
      resolve(tenantList[0])
      return
    }
    const itemList = tenantList.map((t) =>
      t.customerName ? `${t.tenantName}（${t.customerName}）` : t.tenantName
    )
    uni.showActionSheet({
      itemList,
      success: (res) => {
        resolve(tenantList[res.tapIndex])
      },
      fail: () => {
        resolve(null)
      }
    })
  })
}
