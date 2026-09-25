<template>
  <view class="compress__canvas">
    <canvas
      canvas-id="compress_canvas2"
      id="compress_canvas2"
      :style="{ width: width + 'px', height: height + 'px' }"
    />
  </view>
</template>

<script setup>
import compress from './compress.js'
import { getCurrentInstance, ref } from 'vue'
const width = ref(200)
const height = ref(200)

function start(imgUrl, options = {}) {
  const instance = getCurrentInstance()
  return new Promise(async (resolve, reject) => {
    if (imgUrl instanceof Array) {
      try {
        let arr = []
        for (let i = 0; i < imgUrl.length; i++) {
          let url = await compress(imgUrl[i], instance, options)
          arr.push(url)
        }
        resolve(arr)
      } catch (e) {
        reject(e)
      }
    } else {
      compress(imgUrl, this, options).then(resolve).catch(reject)
    }
  })
}
defineExpose({ start })
</script>

<style>
.compress__canvas {
  position: absolute;
  left: 10000px;
  visibility: hidden;
  height: 0;
  overflow: hidden;
}
</style>
