<template>
  <view class="h-full bg-data-governance box flex flex-col overflow-hidden">
    <uni-nav-bar backgroundColor="#00000000" />
    <h-status-header :title="getTitle" />
    <!--初始圆环-->
    <view class="flex-1 flex flex-col align-center pt-240" v-if="!listShow">
      <circle-progress :value="100" :widths="widths" :breadth="breadth">
        <view class="color-ff0000 font-56 bold">{{ totalCount }}</view>
        <view class="color-5a6f82 font-28 mt-32">{{ getNotice }}</view>
      </circle-progress>
      <h-button
        v-if="!btnNoticeShow"
        width="296"
        height="88"
        text="检测垃圾数据"
        class="mt-96"
        @tap.stop="handleInitList"
      />
      <view v-else class="btn-checking flex-center bg-fff mt-96 rounded-44">
        <text class="color-5a6f82 font-32">检测中...</text>
      </view>
    </view>
    <!--检测列表-->
    <view class="flex-1 flex flex-col overflow-hidden" v-else>
      <view class="py-24 px-48 flex align-center">
        <circle-progress :value="circleSmallPer" :widths="widthsSmall" :breadth="breadthSmall">
          <view class="color-333 font-32 bold">{{ circleSmallPer }}%</view>
        </circle-progress>
        <view class="ml-32">
          <!--数据健康度-->
          <view class="flex align-center font-28">
            <image src="/static/images/icon_health_green.svg" class="icon-32" />
            <text class="color-5a6f82 font-28 ml-8">数据健康度</text>
            <text class="color-333 font-32 ml-8 bold">{{ circleSmallPer }}%</text>
          </view>
          <!--产品总数-->
          <view class="flex align-center mt-16">
            <view class="mark-ffffff">产品总数</view>
            <view class="ml-16 font-28">
              <text class="color-333 bold">{{ totalCount }}</text>
              <text class="ml-8 color-333 bold">条</text>
            </view>
          </view>
          <!--垃圾数据-->
          <view class="flex align-center mt-16">
            <view class="mark-ffffff">垃圾数据 </view>
            <view class="ml-16 font-28">
              <text class="color-ff0000 bold">{{ pageTotal }}</text>
              <text class="ml-8 color-333 bold">条</text>
            </view>
          </view>
        </view>
      </view>
      <view class="flex align-center h-84 rounded-16-top mx-16 box bg-fff b-b-1 border-f5f5f5">
        <text class="color-5a6f82 font-28 ml-32 bold">垃圾数据</text>
      </view>
      <scroll-view
        @refresherrefresh="pageListRefresh"
        @scrolltolower="pageListLoadMore"
        :refresher-triggered="refreshTag"
        scroll-y
        refresher-enabled
        enable-flex
        class="flex-1 overflow-hidden rounded-16-bottom"
      >
        <template v-if="pageEmpty">
          <h-empty tipsWord="无垃圾数据" class-name="pt-240" />
        </template>
        <template v-else>
          <product-item
            v-for="(item, index) in dataList"
            :key="item.id"
            :dataItem="item"
            :index="index + 1"
            :product="checkProduct"
            @handleItemCheck="handleItemCheck"
          />
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
      <view class="flex align-center mt-16 px-32">
        <view class="font-24">
          <text class="color-333">已选</text>
          <text class="color-ff0000 ml-4 bold">{{ getCheckedCount }}</text>
        </view>
        <view class="flex-1" />
        <view class="font-24 ml-16 px-16 py-10 rounded-24 color-5a6f82 bg-f3f3f5" @tap="handleCheckAll">全选</view>
        <h-checkbox :checked="checkAll" @checkedChange="handleCheckAll" class="mr-16" />
      </view>
      <view class="flex justify-center py-32 box">
        <h-button width="624" height="72" text="一键清理" @tap.stop="handleDeleteBatch" />
      </view>
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CircleProgress from '../circle-progress.vue'
import { onLoad } from '@dcloudio/uni-app'
import { _delete, _get } from '@/utils/common-request'
import HooksPageList from '@/hooks/page-list'
import { formatPx2Vw } from '@/utils/common'
import { BigNumber } from 'bignumber.js'
import ProductItem from '@/pages-data-governance/main/product-item.vue'
import HEmpty from '@/components/h-empty.vue'

const totalCount = ref(100)
const widths = ref(formatPx2Vw(400))
const breadth = ref(formatPx2Vw(64))
const widthsSmall = ref(formatPx2Vw(160))
const breadthSmall = ref(formatPx2Vw(48))

const type = ref('product')
onLoad((options) => {
  type.value = options.type
})

onMounted(() => {
  getTotalCount()
  pageListSetParams({ masterType: getMasterType.value })
  pageListRefresh()
})

function getTotalCount() {
  _get({ url: '/warn/masterDataCount', data: { masterType: getMasterType.value } }).then((res) => {
    totalCount.value = res.data
  })
}

const getTitle = computed(() => (type.value === 'product' ? '产品数据清理' : '工序数据清理'))
const getNotice = computed(() => (type.value === 'product' ? '产品总数' : '工序总数'))
const getMasterType = computed(() => (type.value === 'product' ? 'MASTER_PRODUCT' : 'MASTER_PROCESS'))
const checkProduct = computed(() => type.value === 'product')

const listShow = ref(false)
const btnNoticeShow = ref(false)
const { dataList, pageListRefresh, pageTotal, pageListLoadMore, pageListSetParams, dataNoMore, refreshTag, pageEmpty } =
  HooksPageList('/warn/masterData', {
    data: { masterType: getMasterType.value },
    run: false,
    pageSize: 99999
  })
function handleInitList() {
  btnNoticeShow.value = true
  setTimeout(() => {
    listShow.value = true
    btnNoticeShow.value = false
  }, 1000)
}

const circleSmallPer = computed(() => {
  if (pageTotal.value === 0) {
    return 100
  }
  const num = Math.floor(new BigNumber(totalCount.value - pageTotal.value).div(totalCount.value).times(100).toNumber())
  return Math.min(100, num)
})

// 全选操作
const checkAll = ref(false)
const getCheckedCount = computed(() => {
  return dataList.value.filter((v) => v.checked).length
})
function handleCheckAll() {
  checkAll.value = !checkAll.value
  dataList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
function handleItemCheck(item) {
  const obj = dataList.value.find((v) => (v.id && v.id === item.id) || (v.ids && v.ids === item.ids))
  obj && (obj.checked = !obj.checked)
  checkAll.value = dataList.value.every((v) => v.checked)
}
//删除产品
function handleDeleteBatch() {
  let ids = dataList.value.reduce((pre, cur) => {
    if (cur.checked) {
      pre.push(cur.id)
    }
    return pre
  }, [])
  if (!ids.length) {
    uni.showToast({ title: `请先选择需要清理的${checkProduct.value ? '产品' : '工序'}`, icon: 'none' })
    return
  }
  uni.showModal({
    title: '提示',
    content: '是否确认清理？',
    success: async function (res) {
      if (res.confirm) {
        if (checkProduct.value) {
          await _delete({ url: '/product/' + ids.toString() })
        } else {
          await _delete({ url: '/process/' + ids.toString() })
        }
        checkAll.value = false
        getTotalCount()
        pageListRefresh()
        if (checkProduct.value) {
          uni.$emit('refreshProductList')
        } else {
          uni.$emit('refreshList')
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.bg-data-governance {
  background-image: url('/static/micro_app/img_cleardata_bg.jpg');
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
.btn-checking {
  width: px2vw(296);
  height: px2vw(88);
}

::-webkit-scrollbar {
  width: px2vw(8) !important;
  height: px2vw(8) !important;
  background-color: #b8b8b9;
}

/*定义滚动条轨道 内阴影+圆角*/
::-webkit-scrollbar-track {
  border-radius: px2vw(10);
  background-color: #fff;
}

/*定义滑块 内阴影+圆角*/
::-webkit-scrollbar-thumb {
  border-radius: px2vw(10);
  -webkit-box-shadow: inset 0 0 px2vw(6) rgba(0, 0, 0, 0.3);
  background-color: #b8b8b9;
}

.mark {
  width: px2vw(16);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
</style>
