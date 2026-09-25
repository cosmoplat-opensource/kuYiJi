<template>
  <view class="py-16 flex bg-f3f3f5 flex gap-16 z-index-2 sticky overflow-x-auto" style="top: 0">
    <view class="select-item" v-for="(item, index) in selectList" :key="index" @tap="handleKeyWordSelect(item)">
      {{ item }}
    </view>
  </view>
  <view class="py-16">
    <view class="w-560 pt-24 px-24 pb-40 rounded-16 bg-fff">
      <view class="flex justify-between align-center">
        <view class="flex align-center">
          <image :src="formatImage('icon_tips_ai', 'svg')" class="icon-64" />
          <view class="font-32 color-333 ml-16">猜你想问</view>
        </view>
        <view class="flex align-center" @tap="refreshGuess">
          <view class="font-24 color-5a6f82 mr-8">换一批</view>
          <image :src="formatImage('icon_refresh', 'svg')" class="icon-32" />
        </view>
      </view>
      <view class="flex flex-col gap-24 pt-32">
        <view
          v-for="(item, index) in problemList"
          :key="index"
          class="flex align-center problem-title"
          @tap="handleSelect(item)"
        >
          <view class="flex-1 pr-32 font-28 color-333 text-ellipsis-other title">{{ item.question }}</view>
          <image :src="formatImage('icon_arr_5a6f82', 'svg')" class="icon-48" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { $store, formatImage } from '@/utils/common'
import { onReady } from '@dcloudio/uni-app'
import { _get } from '@/utils/common-request'

type TProblem = {
  question?: string
  answer?: string
}

const selectList = ref([])
const problemList = ref<TProblem[]>([])

onReady(() => {
  _get({ url: '/faq/keyword' }).then((res: IResponseType<[]>) => {
    selectList.value = res.data
  })
  refreshGuess()
})

function refreshGuess() {
  _get({ url: '/faq/guess', data: { keyword: '' } }).then((res: IResponseType<TProblem[]>) => {
    problemList.value = res.data
  })
}
function handleKeyWordSelect(item: string) {
  uni.$emit('getSegmentList', item)
}

function handleSelect(item) {
  $store.commit('service/chatAdd', item)
  uni.$emit('scrollToBottom')
}
</script>

<style lang="scss" scoped>
.select-item {
  padding: px2vw(34) px2vw(24);
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
  box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 0.3);
  border-radius: px2vw(16);
  font-size: px2vw(28);
  line-height: px2vw(28);
  color: #333;
  font-weight: 500;
  flex-shrink: 0;
  &:active {
    background: #0066ff;
    color: #fff;
  }
}

.select-item-last {
  padding: 0 px2vw(8);
  background: transparent;
  box-shadow: none;
  &:active {
    background: transparent;
  }
}

.problem-title {
  &:active {
    .title {
      color: #0066ff;
    }
  }
}
</style>
