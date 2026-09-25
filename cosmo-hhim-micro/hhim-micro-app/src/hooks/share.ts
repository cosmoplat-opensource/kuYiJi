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
