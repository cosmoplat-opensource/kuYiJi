<template>
  <view class="btn-phone">
    <text class="font-28 color-fff btn-phone__text">{{ props.text }}</text>
    <button
      v-if="textChecked"
      class="btn-btn"
      type="primary"
      plain="true"
      open-type="getPhoneNumber"
      @getphonenumber="getPhoneNumber"
    />
    <button v-else class="btn-btn" type="primary" plain="true" @tap="getPhoneNumber"></button>
  </view>
</template>
<script setup lang="ts">
const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  textChecked: {
    type: Boolean,
    default: true
  }
})

const emits = defineEmits(['getPhoneCode'])

function getPhoneNumber(res) {
  if (!props.textChecked) {
    uni.showToast({ title: '请先阅读并同意用户隐私协议', icon: 'none' })
    return
  }
  if (res.detail && res.detail.code) {
    emits('getPhoneCode', res.detail.code)
  } else {
    // 用户取消授权，或授权失败（如开发者工具返回 getPhoneNumber:fail）
    uni.showToast({ title: '手机号授权失败，请重试', icon: 'none' })
  }
}
</script>
<style lang="scss" scoped>
.btn-phone {
  width: px2vw(528);
  height: px2vw(88);
  background-color: #0066ff;
  border-radius: px2vw(44);
  position: relative;
  &:active {
    background-color: #0044e7;
  }

  &__text {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }

  .btn-btn {
    position: absolute;
    left: 0;
    top: 0;
    width: px2vw(528);
    height: px2vw(88);
    opacity: 0;
    z-index: 2;
  }
}
</style>
