<template>
  <view class="flex align-center overflow-hidden bg-fff" id="operation-show">
    <view class="block" />
    <view class="flex-1 ml-16">
      <text class="color-333 font-28 bold">操作日志</text>
    </view>
    <view class="icon-96 flex-center" @tap="handleListShow">
      <image src="/static/images/icon_unfold.svg" class="icon-32" :class="{ 'icon-rotate-y': listShow }" />
    </view>
  </view>
  <view id="operation-hide" />
  <view v-if="listShow" class="b-t-1 border-f5f5f5 bg-fff overflow-hidden">
    <view class="py-32 box relative" v-for="(item, index) in dataList" :key="item.id">
      <view class="item-line" />
      <view class="item-line-white" v-if="index === dataList.length - 1" />
      <!--名字-->
      <view class="flex align-center pt-16 pl-16 box">
        <view class="circle" :class="{ first: index === 0 }" />
        <view class="ml-32">
          <text class="color-333 font-32 bold">{{ item.operateNode }}</text>
        </view>
      </view>
      <!--操作员-->
      <view class="mt-24 pl-76 color-5a6f82 font-24 flex align-center">
        <view v-if="item.operateNode === '不良返修'" class="flex"
          >返修人：<h-text-display :text="item.operatorName" :width="150"
        /></view>
        <view v-else-if="item.operateNode === '记工结算'" class="flex"
          >结算人：<h-text-display :text="item.operatorName" :width="150"
        /></view>
        <view v-else class="flex">操作员：<h-text-display :text="item.operatorName" :width="150" /></view>
        <view class="ml-16">
          <text>{{ item.createdDate }}</text>
        </view>
      </view>
      <!--产品-->
      <view class="mt-24 pl-76 color-333 font-28 flex align-center">
        <h-text-display class="bold" :text="item.productName" :width="200" />
        <text class="bold">({{ item.productCode }})</text>
      </view>
      <!--工序-->
      <view class="mt-8 pl-76 font-28 flex align-center flex-wrap">
        <view class="flex align-center mr-32 mt-16">
          <view class="mark-ebf0f5-rounded mr-16">报</view>
          <h-text-display class="bold" :text="item.operateProcessName" :width="320" />
        </view>
        <view class="flex align-center font-28 mt-16" v-if="item.preProcessName">
          <view class="mark-ebf0f5-rounded mr-16">前</view>
          <h-text-display class="bold" :text="item.preProcessName" :width="320" />
        </view>
      </view>
      <!-- 记工结算 -->
      <view class="mt-16 pl-76 flex align-center font-28" v-if="item.operateNode === '记工结算'">
        <view class="bg-EBF0F5 rounded-8 color-5a6f82 font-24 px-4 h-32 flex-center mr-16">本次结算数量</view>
        <h-text-display class="bold" :text="item.settledNum" :width="320" />
      </view>
      <!--不良返修-->
      <view v-if="item.operateNode === '不良返修'" class="mt-24 pl-76 flex align-center flex-wrap">
        <view class="flex align-center mr-32">
          <word-icon text="修" class="mr-16" />
          <text class="font-28">{{ item.repairNum }}</text>
        </view>
        <view class="flex align-center mr-32">
          <word-icon text="让" class="mr-16" />
          <text class="font-28">{{ item.concessionNum }}</text>
        </view>
        <view class="flex align-center mr-32">
          <word-icon text="废" class="mr-16" />
          <text class="font-28">{{ item.abandonedNum }}</text>
        </view>
      </view>
      <!-- 记工结算 -->
      <view v-else-if="item.operateNode === '记工结算'"></view>
      <!--良/不良-->
      <view v-else class="mt-24 pl-76">
        <area-date-info :dataItem="{ pass: item.passNum, ng: item.ngNum }" />
      </view>
    </view>
    <view class="font-24 color-b6c0c9 py-48 text-center">已到底部</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import { _get } from '@/utils/common-request'
import WordIcon from '@/components/word-icon.vue'
const emits = defineEmits(['update:modelValue'])
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  submitNo: {
    type: String,
    default: ''
  }
})
const listShow = ref(true)
const dataList = ref([])
function handleListShow() {
  listShow.value = !listShow.value
  emits('update:modelValue', listShow.value ? 'operation-show' : 'operation-hide')
}

function getList(submitNo) {
  _get({ url: '/submit/history/recordDetail', data: { submitNo } }).then((res) => {
    dataList.value = res.data
  })
}

onMounted(() => {
  getList(props.submitNo)
})

defineExpose({ getList })
</script>

<style lang="scss" scoped>
.hot-area {
  left: px2vw(-16);
  top: px2vw(-16);
}
.circle {
  width: px2vw(24);
  height: px2vw(24);
  border-radius: 50%;
  box-sizing: border-box;
  background: #f5f5f5;
  border: px2vw(2) solid #e5e5e5;
  z-index: 3;
  &.first {
    background: #ffffff;
    border: px2vw(2) solid #0066ff;
  }
}
.item-line {
  position: absolute;
  left: px2vw(27);
  top: px2vw(32);
  height: 100%;
  width: px2vw(1);
  background-color: #f5f5f5;
  z-index: 1;
}
.item-line-white {
  position: absolute;
  left: px2vw(27);
  top: px2vw(82);
  height: 100%;
  width: px2vw(1);
  background-color: #fff;
  z-index: 2;
}
.mark {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: px2vw(24);
  border-radius: px2vw(4);
  height: px2vw(32);
}
.mark-product {
  width: px2vw(56);
  background: #64b5ea;
  color: #fff;
}
.mark-process {
  width: px2vw(104);
  color: #c682ee;
  background: #efe2f7;
}
.mark-process-pre {
  width: px2vw(80);
  color: #418cbd;
  background: #def2ff;
}
</style>
