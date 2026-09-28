/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
interface IRecommendType {
  productName: string
  productCode: string
  preProcessName: string
  preProcessCode: string
  operateProcessName: string
  operateProcessCode: string
  standard: boolean
}

type selectStock = {
  unapprovedNum: number
  totalStockNum: number
  itemSeq: string
}
