/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const HooksShare = () => {
  const title = '全厂就差你一个了，一键记工，快来！'
  const path = '/pages/audit/list'
  const imageUrl = '/static/images/img_wechat_share.jpg'
  const shareAppMessage = { title, path, imageUrl }
  const shareTimeline = { title, imageUrl }

  return {
    shareAppMessage,
    shareTimeline
  }
}

export default HooksShare
