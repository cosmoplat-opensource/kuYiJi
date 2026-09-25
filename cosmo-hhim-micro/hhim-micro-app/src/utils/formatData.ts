export const productTypeList: IProductType[] = [
  {
    code: 'CP',
    label: '成品'
  },
  {
    code: 'BCP',
    label: '半成品'
  },
  {
    code: 'YCL',
    label: '原材料'
  }
]
export const getProductType = (value) => {
  const actions = []
  Object.keys(productTypeList).map((key) => {
    if (productTypeList[key].code === value) {
      actions.push(productTypeList[key].label)
      return false
    }
  })
  return actions.join('')
}

export const techVideoList: IVideoType[] = [
  { name: '员工如何记工——单条记工？', video: '1、工人如何单个报工-m', index: 1 },
  { name: '员工如何记工——批量记工？', video: '2、工人如何批量记工-m', index: 2 },
  { name: '智能推荐，员工更好记工', video: '3、标准工艺-智能推荐-工人更好记工-m', index: 3 },
  { name: '记工有误如何编辑？', video: '4、记工后如何修改编辑-m', index: 4 },
  { name: '如何删除记工数据？', video: '5、记工后如何删除-m', index: 5 },
  { name: '记工日历对员工的价值？', video: '6、如何查看月度工作量及当月结算情况-m', index: 6 },
  { name: '审核时发现记工错误如何修正？', video: '7、审产修正工人记工-m', index: 7 },
  { name: '审产有防错，可预防超报、漏报', video: '8、审产防错-m', index: 8 },
  { name: '在制品库存了如执掌，生产调度更轻松', video: '9、在制品库存了如执掌-生产调度更轻松-m', index: 9 },
  { name: '管理在制品库存有何意义', video: '10、在制品库存-m', index: 10 },
  { name: '生产完工后如何入库变为成品？', video: '11、完工入库-m', index: 11 },
  { name: '按员工记工数量进行工资结算', video: '13、记工记录结算-m', index: 13 },
  { name: '按产品完工数量进行工资结算', video: '14、计件结算——完工产品结算-m', index: 14 },
  { name: '不良品清单如何查看', video: '15、不良清单-m', index: 15 },
  { name: '不良品管理全流程', video: '16、不良品处理全流程-m', index: 16 },
  { name: '不良品返修如何操作', video: '17、返修复核-m', index: 17 },
  { name: '不良品返修后可以如何查看', video: '18、不良返修业务说明-m', index: 18 },
  { name: '质检员如何质检', video: '19、质检详细操作-m', index: 19 },
  { name: '生产良品率的分析', video: '20、良品率分析-m', index: 20 },
  { name: '质量趋势分析', video: '21、良品率趋势-m', index: 21 },
  { name: '不良品管理的意义', video: '24、质量趋势-良品率分析合一-m', index: 24 },
  { name: '工艺路线如何标准化', video: '22、根据推荐工艺标准化工艺-m', index: 22 },
  { name: '如何新增产品，新增工艺', video: '23、新增产品-维护标准工艺-m', index: 23 },
  { name: '生产日报', video: '25、生产日报-m', index: 25 },
  { name: '如何邀请员工一起使用', video: '26、邀请同事一起体验-m', index: 26 }
]
