/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
type THealthItem = {
  dataHealth: number
  totalWaitCheckSubmitRecords: number
  abnormalSubmitRecords: number
  negativeStockRecords: number
  overProductiveCapacityRecords: number
  lowPassRateRecords: number
  negativeStockRecordIds: string
  lowPassRateRecordIds: string
  overProductiveCapacityRecordIds: string
}
