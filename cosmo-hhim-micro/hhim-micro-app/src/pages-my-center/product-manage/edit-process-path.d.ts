/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
interface IProcessItem {
  id?: number
  processId?: string
  processName?: string
  processCode?: string
  processSeq?: string
  parentId?: string
  parentProcessId?: number
  parentProcessSeq?: string
  isLastProcess?: string
  isFirstProcess?: string
  checked?: boolean
}

interface IProcessPath extends IProcessItem {
  targetArr: IProcessItem[]
}

type TProcessItemBase = {
  itemId: string
  itemCode: string
  itemName: string
  itemSeq: string
}

type TProductItem = {
  id: string
  productSeq: string
}
