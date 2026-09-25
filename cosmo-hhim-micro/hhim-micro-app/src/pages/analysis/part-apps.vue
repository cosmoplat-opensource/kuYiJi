<template>
  <view class="flex flex-wrap">
    <view v-for="(item, index) in formatAppList" :key="index" class="card-item" @tap="handleJumpDetail(item.name)">
      <image :src="formatImage(item.icon)" class="icon-96" />
      <view class="ml-8 pt-16 flex-1 flex flex-col box">
        <view class="font-28 color-333 bold">{{ item.name }}</view>
        <view class="font-24 mt-12 flex flex-wrap line-height-36 color-333">
          <rich-text :nodes="item.nodeName" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { formatImage, Role_Admin, Role_Experience, Role_Review } from '@/utils/common'
import { _get } from '@/utils/common-request'
import dayjs from 'dayjs'

const appList = ref<AppItem[]>([])
onMounted(() => {
  appList.value = [
    {
      name: '不良品管理',
      nodeName: `待处理 <span class="color-333">${0}</span>， 涉及 <span class="color-333">${0}</span>人`,
      icon: 'icon_work_1',
      show: true
    },
    {
      name: '完工报告',
      nodeName: '',
      icon: 'icon_work_2',
      show: true
    },
    {
      name: '产成品库存',
      nodeName: '',
      icon: 'icon_work_3',
      show: Role_Review.value || Role_Admin.value || Role_Experience.value
    },
    {
      name: '计件结算',
      nodeName: `待结算 <span class="color-f333">${0}</span>， 涉及 <span class="color-333">${0}</span>款产品`,
      icon: 'icon_work_4',
      show: Role_Review.value || Role_Admin.value || Role_Experience.value
    }
  ]
  uni.$on('obtainedNgProductTotalNumAndUserRefresh', (options) => {
    getObtainedNgProductTotalNumAndUser()
  })
  uni.$on('finishedProductStatisticsRefresh', (options) => {
    getFinishedProductStatistics()
  })
  uni.$on('storageFinishStatisticRefresh', (options) => {
    getStorageFinishStatistic()
  })
  uni.$on('settlementStatisticRefresh', (options) => {
    getSettlementStatistic()
  })
  getObtainedNgProductTotalNumAndUser()
  getFinishedProductStatistics()
  getStorageFinishStatistic()
  getSettlementStatistic()
})
onUnmounted(() => {
  uni.$off('finishedProductStatisticsRefresh')
  uni.$off('storageFinishStatisticRefresh')
})
const formatAppList = computed(() => {
  return appList.value.filter((v) => v.show)
})

function getObtainedNgProductTotalNumAndUser() {
  _get({ url: '/ngProduct/manage/obtainedNgProductTotalNumAndUser' }).then((res: any) => {
    const { totalNgNum, userNum } = res.data
    appList.value.find((v) => v.name === '不良品管理').nodeName = `待处理 <span class="color-333">${Math.floor(
      totalNgNum || 0
    )}</span>， 涉及 <span class="color-333">${Math.floor(userNum || 0)}</span> 人`
  })
}
function getFinishedProductStatistics() {
  _get({ url: '/complete/report/waitDeal/statistics' }).then((res: any) => {
    const { waitDealSubmitTotalNum, productCategoryNum } = res
    appList.value.find((v) => v.name === '完工报告').nodeName = `待确认 <span class="color-333">${Math.floor(
      waitDealSubmitTotalNum || 0
    )}</span>， 涉及 <span class="color-333">${Math.floor(productCategoryNum || 0)}</span> 款产品`
  })
}

function getStorageFinishStatistic() {
  const time = dayjs().format('YYYY-MM-DD')
  _get({ url: '/storage/finish/history/statistic', data: { startDate: time, endDate: time } }).then(
    (res: IResponseType<any>) => {
      const { inBoundNum, inBoundProductCategoryNum } = res.data
      appList.value.find((v) => v.name === '产成品库存').nodeName = `今日入库 <span class="color-333">${Math.floor(
        inBoundNum || 0
      )}</span>， 涉及 <span class="color-333">${Math.floor(inBoundProductCategoryNum || 0)}</span> 款产品`
    }
  )
}
function getSettlementStatistic() {
  _get({ url: '/settlement/home/index' }).then((res: any) => {
    const { openSettlementNum, productNum } = res.data
    appList.value.find((v) => v.name === '计件结算').nodeName = `待结算 <span class="color-333">${Math.floor(
      openSettlementNum || 0
    )}</span>， 涉及 <span class="color-333">${Math.floor(productNum || 0)}</span> 款产品`
  })
}

function handleJumpDetail(name) {
  switch (name) {
    case '完工报告':
      uni.navigateTo({ url: '/pages-work-center/completion-report/index' })
      break
    case '计件结算':
      uni.navigateTo({ url: '/pages-settlement/index' })
      break
    case '产成品库存':
      uni.navigateTo({ url: '/pages-work-center/finish-product-storage/index' })
      break
    case '不良品管理':
      uni.navigateTo({ url: '/pages-rejects/index' })
      break
    default:
      break
  }
}
</script>

<style lang="scss" scoped>
.card-item {
  box-sizing: border-box;
  border-radius: px2vw(16);
  background-color: #ffffff;
  margin-top: px2vw(16);
  width: px2vw(336);
  height: px2vw(180);
  padding: px2vw(16);
  display: flex;
  align-items: flex-start;
  &:nth-child(odd) {
    margin-right: px2vw(16);
  }
  &:active {
    background: #f3f3f5;
  }
}
</style>
