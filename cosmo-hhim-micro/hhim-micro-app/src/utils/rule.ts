// 角色列表
export function getRoleList(role) {
  switch (role) {
    case '10':
      return '企业管理员'
    case '20':
      return '审产员'
    case '25':
      return '质检员'
    case '30':
      return '员工'
    case '40':
      return '体验角色'
    default:
      return '无'
  }
}
