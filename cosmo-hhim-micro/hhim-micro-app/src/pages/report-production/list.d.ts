/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
type TTopData = {
  full: string
  pass: number
  ng: number
  day: string
}
type TDatesItem = {
  day: string
  full: string
  value: string | number
  pass: number
  ng: number
  id: string
  week: string
}

type TDatesRes = {
  submitDay: string
  totalCounts: string
  totalPassNum: number
  totalNgNum: number
}
