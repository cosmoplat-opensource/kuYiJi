type TChainItem = {
  itemCode: string
  itemName: string
}

type TProcessChain = {
  chain: TChainItem[]
  head: TChainItem[]
  single: TChainItem[]
}
