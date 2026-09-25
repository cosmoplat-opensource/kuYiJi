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
