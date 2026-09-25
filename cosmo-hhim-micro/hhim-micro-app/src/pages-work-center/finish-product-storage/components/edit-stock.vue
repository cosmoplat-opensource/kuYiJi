<template>
  <uni-popup ref="popup" type="bottom" @change="handlePopupOpen" :safe-area="false">
    <view class="edit-class w-100 bg-ffffff">
      <view class="title w-100 flex align-center relative">
        <text class="color-5a6f82 font-28 flex-1 text-center bold">编辑库存信息</text>
        <image @click="closePopup" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="pl-32 pr-32">
        <view class="flex align-center mid mt-24">
          <text class="color-333 font-32">{{ formatStr(currentItem.productName, 13) }}</text>
          <text class="color-999 font-32 flex-1">({{ formatStr(currentItem.productSeq, 8) }})</text>
        </view>

        <view class="num-line before-00BFA5 mt-32 flex justify-between align-center bg-F3F3F5 pl-24">
          <text class="color-5a6f82 font-28 shrink-1 w-115">良品</text>
          <view v-if="checkPassChange" class="flex align-center font-24 shrink-1 ml-24 mr-24 flex-1">
            <view class="bg-fff pl-4 pr-4 h-32 rounded-4">
              <text class="color-5a6f82">原</text>
              <text class="color-ff0000 ml-8">{{ currentItem.num }}</text>
            </view>
          </view>
          <view class="iptNum flex justify-between align-center">
            <view @tap.stop="minusNum('pass')" class="h-100 w-80 flex justify-center">
              <image src="/static/images/icon_minus.svg" class="icon-48" />
            </view>
            <input
              v-model="currentItem.passToNum"
              @input="inputNum($event, 'pass')"
              class="uni-input flex-1 text-center"
              type="digit"
              placeholder="数量"
            />
            <view @tap.stop="plusNum('pass')" class="h-100 w-80 flex justify-center">
              <image src="/static/images/icon_plus.svg" class="icon-48" />
            </view>
          </view>
        </view>

        <view class="w-100 before-FE9F00 font-28 px-24 py-32 relative box mt-16">
          <view class="color-5a6f82 font-28 pb-24">变动原因</view>
          <view class="w-100">
            <h-textarea
              class="mt-16"
              v-model="currentItem.changeReason"
              @input="reasonInput"
              :maxlength="50"
              :border="false"
              :padding="false"
              :showClear="true"
              :showLength="false"
              :minH="84"
              placeholder="输入变动原因"
            />
          </view>
        </view>
        <view v-if="recommendList.length > 0" class="w-100 bg-fff font-28 pb-32 box mt-8 tagshadow">
          <view class="color-e7a11a bg-fff5e3 font-24 flex-center tips">原因推荐</view>
          <view class="w-100 px-32 box flex flex-wrap"
            ><view
              @tap="selectRecommend(item.itemName)"
              v-for="(item, idx) in recommendList"
              :key="idx"
              class="mark-ebf0f5 mr-16 mt-16 word-tap"
              >{{ formatStr(item.itemName, 11) }}</view
            ></view
          >
        </view>
      </view>

      <view class="bottom-btn w-100 mt-32 flex justify-center align-center font-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="closePopup" />
        <h-button width="296" height="72" text="确认并保存" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import bigNumber, { BigNumber } from 'bignumber.js'
import { $store, formatStr, setGio } from '@/utils/common'
import { computed, ref } from 'vue'
import { _get, _post, _put } from '@/utils/common-request'
import HTextarea from '@/components/h-textarea.vue'

const props = defineProps({
  currentItem: {
    type: Object,
    default: () => {}
  },
  experienceLoading: {
    type: Boolean,
    default: false
  }
})
const checkPassChange = computed(() => {
  return props.currentItem.passToNum !== props.currentItem.num
})

const emits = defineEmits(['handlePopupOpen', 'closePopup', 'confirmPopup', 'changeData'])
function openEdit() {
  popup.value.open('bottom')
  recommendSelect()
}
// 监听弹窗开关
function handlePopupOpen(e) {
  emits('handlePopupOpen', e)
}
const popup = ref(null)
function closePopup() {
  popup.value?.close('bottom')
  setTimeout(() => {
    emits('closePopup')
  }, 200)
}
const submitLoading = ref(false)
function confirmPopup() {
  if (submitLoading.value) {
    return
  }
  if (!checkPassChange.value) {
    uni.showToast({
      title: '只有数据变更才能提交',
      icon: 'none'
    })
    return
  }
  if (props.currentItem.passToNum < 0) {
    uni.showToast({
      title: '库存不可为负数',
      icon: 'none',
      duration: 1500
    })
    return
  }
  if (props.currentItem.passToNum && !/^-?\d+(\.\d{1,4})?$/.test(props.currentItem.passToNum)) {
    uni.showToast({
      title: '数量最多保留4位小数',
      icon: 'none'
    })
    return
  }

  props.currentItem.passToNum = Number(props.currentItem.passToNum)

  let postData = {
    productSeq: props.currentItem.productSeq,
    productName: props.currentItem.productName,
    changeReason: props.currentItem.changeReason,
    changeNum: new bigNumber(props.currentItem.passToNum).minus(props.currentItem.num).toNumber()
  }
  uni.showLoading({
    title: '保存中'
  })
  submitLoading.value = true
  _put({ url: '/storage/finish/edit', data: postData }).then((res) => {
    uni.showToast({
      title: '修改完成',
      icon: 'none',
      duration: 1500
    })

    popup.value?.close('bottom')
    emits('confirmPopup', props.currentItem)
    uni.hideLoading()
    submitLoading.value = false
  })
}
function minusNum(type) {
  if (type === 'pass') {
    if (props.currentItem.passToNum >= 1) {
      emits('changeData', 'passToNum', new BigNumber(props.currentItem.passToNum).minus(1).toNumber())
    } else {
      uni.showToast({
        title: '库存不可为负数',
        icon: 'none',
        duration: 1500
      })
    }
  }
}
function plusNum(type) {
  if (type === 'pass') {
    emits('changeData', 'passToNum', new BigNumber(props.currentItem.passToNum).plus(1).toNumber())
  }
}

function inputNum(e, type) {
  let value = e.detail.value
  if (type === 'pass') {
    if (value) {
      emits('changeData', 'passToNum', value)
    } else {
      emits('changeData', 'passToNum', 0)
    }
  }
}

function selectRecommend(val) {
  props.currentItem.changeReason = val
  tagsSelect()
}
const recommendList = ref([])
function tagsSelect() {
  _get({ url: '/tags/select', data: { key: props.currentItem.changeReason || '', type: 'STORAGE_CHANGE' } }).then(
    (res) => {
      recommendList.value = res.data || []
    }
  )
}
function recommendSelect() {
  _get({
    url: '/storage/recommendChangeTag',
    data: { key: props.currentItem.changeReason || '', type: 'STORAGE_CHANGE' }
  }).then((res) => {
    recommendList.value = res.data.HOT_KEY_TAG_STORAGE_CHANGE || []
  })
}
function reasonInput(val) {
  if (val) {
    tagsSelect()
  } else {
    recommendSelect()
  }
}
defineExpose({ openEdit })
</script>

<style lang="scss" scoped>
.uni-input {
  width: px2vw(208);
}
.num-line {
  height: px2vw(80);
  position: relative;
}
.iptNum {
  width: px2vw(330);
}
.edit-class {
  padding-bottom: env(safe-area-inset-bottom);
  .title {
    height: px2vw(96);
    border-bottom: px2vw(1) solid#F0F0F0;
    .close {
      position: absolute;
      top: px2vw(24);
      right: px2vw(24);
    }
  }
}
.bottom-btn {
  height: px2vw(152);
  border-radius: 0px 0px 0px 0px;
  opacity: 1;
  .cancel {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
  .confirm {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #629bf0 0%, #89b4f5 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
}
.uni-popup {
  z-index: 999;
}
.before-00BFA5:before {
  content: '';
  display: block;
  position: absolute;
  top: px2vw(28);
  left: 0;
  width: px2vw(8);
  height: px2vw(24);
  -webkit-border-radius: 50%;
  -moz-border-radius: 50%;
  border-radius: 0 px2vw(2) px2vw(2) 0;
  background: #00bfa5;
}
.before-9BADBC:before {
  content: '';
  display: block;
  position: absolute;
  top: px2vw(28);
  left: 0;
  width: px2vw(8);
  height: px2vw(24);
  -webkit-border-radius: 50%;
  -moz-border-radius: 50%;
  border-radius: 0 px2vw(2) px2vw(2) 0;
  background: #9badbc;
}
.before-FE9F00:before {
  content: '';
  display: block;
  position: absolute;
  top: px2vw(40);
  left: 0;
  width: px2vw(8);
  height: px2vw(24);
  -webkit-border-radius: 50%;
  -moz-border-radius: 50%;
  border-radius: 0 px2vw(2) px2vw(2) 0;
  background: #fe9f00;
}
.tagshadow {
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 1);
}
.tips {
  width: px2vw(104);
  height: px2vw(32);
  background: #fff5e3;
  border-radius: 0 px2vw(4) px2vw(4) px2vw(4);
}
</style>
