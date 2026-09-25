interface IUuc {
  token: string
  userName: string
}

interface IResponseType<T> extends Request {
  code?: number
  data?: T
  msg?: string
  rows?: T[]
  total: number
  tip?: string
}

interface ILogin {
  token: string
  openId: string
  phoneNumber: string
}
