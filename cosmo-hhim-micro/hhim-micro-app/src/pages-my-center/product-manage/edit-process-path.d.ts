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
