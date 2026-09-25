import { ref } from 'vue'

const HooksPageSwiper = (cb) => {
  const touchStartX = ref(0)
  const touchStartY = ref(0)
  function touchStart(e) {
    // 触摸开始
    touchStartX.value = e.changedTouches[0].clientX
    touchStartY.value = e.changedTouches[0].clientY
  }
  function touchEnd(e) {
    const subX = e.changedTouches[0].clientX - touchStartX.value
    const subY = e.changedTouches[0].clientY - touchStartY.value
    if (Math.abs(subY) > 80) return
    if (subX > 50) {
      // console.log('左滑')
      cb('left')
    } else if (subX < -50) {
      // console.log('右滑')
      cb('right')
    } else {
      // console.log('无效')
    }
  }
  return {
    touchStart,
    touchEnd
  }
}

export default HooksPageSwiper
