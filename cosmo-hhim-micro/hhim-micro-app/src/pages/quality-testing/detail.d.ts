/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
interface ngInfoItem {
  ngType: string
  ngNum: string
}

interface IQualityTestingItem {
  negativeStockFlag: string
  lowPassRateFlag: string
  overProductiveCapacityFlag: string
  passNum: number
  ngNum: number
  remark: string
  productName: string
  productCode: string
  productUnit: string
  warnFlagStaff: string
  submitNickName: string
  expiredRecordFlag: number
  submitDay: string
  operateProcessName: string
  preProcessName: string
  createdDate: string
  id: number
  checkNickName: string
  checkDate: string
  checkPassNum: number
  checkNgNum: number
  isLastProcess: string
  isFirstProcess: string
  checked: boolean
  submitStatus: number
  unit: string
  ngProductDetailInfoList: ngInfoItem[]
  checkStatus: number
  qcNickName: string
  qcDate: string
  submitDate: string
  processName: string
}
