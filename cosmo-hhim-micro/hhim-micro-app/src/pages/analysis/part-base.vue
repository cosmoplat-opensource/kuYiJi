<template>
  <view class="flex align-center">
    <template v-for="(item, index) in dataList" :key="index">
      <view
        class="modules flex flex-col align-center pt-24 pb-32 bg-fff rounded-16 flex-1 box"
        :class="{ 'ml-16': index > 0 }"
        @tap="handleJump(item.label)"
      >
        <image :src="formatImage(item.icon)" class="icon-64" />
        <view class="mt-16 color-333 font-28 bold">{{ item.title }}</view>
        <view class="mt-24 color-5a6f82 font-24">{{ item.label }} {{ item.num }}</view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'
import { formatImage, openBuyMini, Role_Experience } from '@/utils/common'

function getData() {
  _get({ url: '/user/businessInfo' }).then((res: IResponseType<IBusinessInfo>) => {
    if (res?.data) {
      dataList.value[0].num = res.data.staffNum
      dataList.value[1].num = res.data.productNum
      dataList.value[2].num = res.data.processNum
    }
  })
}

const dataList = ref([
  {
    title: '员工管理',
    icon: 'icon_employee_64',
    num: 0,
    label: '人员'
  },
  {
    title: '产品&工艺',
    icon: 'icon_product_64',
    num: 0,
    label: '产品'
  },
  {
    title: '工序管理',
    icon: 'icon_gongxu_64',
    num: 0,
    label: '工序'
  }
])

onMounted(() => {
  getData()
})

function handleJump(label: string) {
  switch (label) {
    case '人员':
      if (Role_Experience.value) {
        uni.showModal({
          title: '提示',
          content: '购买后才可体验完整功能！',
          showCancel: false,
          confirmText: '去购买',
          success: function (res) {
            if (res.confirm) {
              openBuyMini()
            }
          }
        })
      } else {
        uni.navigateTo({ url: '/pages-my-center/manage-staff/index' })
      }
      break
    case '产品':
      uni.navigateTo({ url: '/pages-my-center/product-manage/list' })
      break
    case '工序':
      uni.navigateTo({ url: '/pages-my-center/manage-process/index' })
      break
  }
}

defineExpose({ getData })
</script>

<style lang="scss" scoped>
.modules {
  &:active {
    background: #f3f3f5;
  }
}
</style>
