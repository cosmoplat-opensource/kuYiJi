<template>
  <view class="flex flex-wrap">
    <view v-for="(item, index) in dataList" :key="index" class="data-item" @tap="handleJumpDetail(item.type)">
      <image :src="formatImage(item.icon)" class="icon-96 mr-8" />
      <view class="flex flex-col justify-center">
        <text class="font-28 mt-16 bold">{{ item.title }}</text>
        <text class="font-24 mt-24">{{ item.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
import { onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'

const dataList = ref([
  {
    type: 'rank',
    title: '记工排行',
    desc: '人员计件',
    icon: 'icon_analysis_rank'
  },
  {
    type: 'finish',
    title: '完工产品',
    desc: 'Top 7',
    icon: 'icon_analysis_product'
  },
  {
    type: 'stock',
    title: '库存分析',
    desc: 'Top 10',
    icon: 'icon_analysis_stock_analyse'
  },
  {
    type: 'product',
    title: '在制品查询',
    desc: '产品 ',
    icon: 'icon_analysis_producing'
  },
  {
    type: 'quality',
    title: '质量趋势',
    desc: '统计、趋势',
    icon: 'icon_analysis_quality'
  },
  {
    type: 'rate',
    title: '良品率分析',
    desc: '良品率',
    icon: 'icon_analysis_fpy'
  }
])

function handleJumpDetail(type) {
  switch (type) {
    case 'finish':
      // 完工产品
      uni.navigateTo({ url: '/pages-analysis/finish-product' })
      break
    case 'stock':
      // 库存分析
      uni.navigateTo({ url: '/pages-analysis/stock-analysis' })
      break
    case 'rank':
      // 记工排行
      uni.navigateTo({ url: '/pages-analysis/work-rank' })
      break
    case 'product':
      // 在制品库存
      uni.navigateTo({ url: '/pages-analysis/wip-query/index' })
      break
    case 'quality':
      // 质量趋势
      uni.navigateTo({ url: '/pages-analysis/quality/index' })
      break
    case 'rate':
      // 良品率分析
      uni.navigateTo({ url: '/pages-analysis/rate-analysis/index' })
      break
    default:
      break
  }
}

//在制品数量
function getProductList() {
  _get({
    url: '/storage/product/list?productNameOrCode='
  }).then((res: IResponseType<{}>) => {
    dataList.value.find((v) => v.type === 'product').desc = `产品 ${res?.total} 个`
  })
}

onMounted(() => {
  getProductList()
})
</script>

<style lang="scss" scoped>
.data-item {
  box-sizing: border-box;
  width: px2vw(342);
  height: px2vw(160);
  padding: px2vw(24) px2vw(16) px2vw(40) px2vw(22);
  background-color: #ffffff;
  display: flex;
  align-items: flex-start;
  margin-top: px2vw(4);
  &:nth-child(odd) {
    margin-right: px2vw(4);
    padding-left: px2vw(16);
  }
  &:nth-child(1) {
    border-radius: px2vw(16) 0 0 0;
  }
  &:nth-child(2) {
    border-radius: 0 px2vw(16) 0 0;
  }
  &:nth-last-child(2) {
    border-radius: 0 0 0 px2vw(16);
  }
  &:nth-last-child(1) {
    border-radius: 0 0 px2vw(16) 0;
  }
  &:active {
    background: #f3f3f5;
  }
}
</style>
