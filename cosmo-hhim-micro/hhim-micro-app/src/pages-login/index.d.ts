/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
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
