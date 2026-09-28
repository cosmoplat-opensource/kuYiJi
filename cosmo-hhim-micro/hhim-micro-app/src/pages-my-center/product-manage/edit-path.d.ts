/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
type TChainItem = {
  itemCode: string
  itemName: string
}

type TProcessChain = {
  chain: TChainItem[]
  head: TChainItem[]
  single: TChainItem[]
}
