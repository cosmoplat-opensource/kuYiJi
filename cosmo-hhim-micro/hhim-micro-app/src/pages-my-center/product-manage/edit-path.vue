<!--工艺路线-->
<template>
  <view class="flex flex-col h-100">
    <data-notice-area :text="cardNotice" v-if="cardNotice" class="rounded-16" />
    <view class="pt-8 box flex-1 overflow-hidden">
      <view class="flex flex-col pt-32 pb-64 box bg-fff rounded-16 overflow-hidden m-h-100">
        <view class="flex align-center px-32 box" v-if="techData.length">
          <view class="font-24 color-5a6f82 pr-32 flex-1" v-if="cardTime">更新时间：{{ cardTime }}</view>
          <view v-if="techStandard" class="mark-24a8ff border">标准工艺</view>
          <view v-else class="mark-fe9f00 border">推荐工艺</view>
        </view>
        <!--顺序-->
        <view
          class="overflow-auto flex-1 box mt-48 px-32 flex justify-center"
          v-if="techData.length && [10, 30, 35, 40].includes(techPattern)"
        >
          <preview-path :techData="techData" class="py-16 box" />
        </view>
        <!--并序-->
        <view
          class="overflow-auto flex-1 box mt-48 px-32 flex justify-center"
          v-else-if="techData.length && techPattern === 20"
        >
          <juxta-card :techData="techData" />
        </view>
        <view v-else class="h-688 flex-center">
          <h-empty tips-word="暂无维护的工艺路线，可点击编辑进行维护" />
        </view>
      </view>
    </view>
    <view class="py-32 flex-center">
      <h-button width="624" height="72" text="编辑" @tap.stop="handleProcessEdit" />
    </view>
  </view>
  <h-popup />
</template>

<script setup lang="ts">
import DataNoticeArea from '@/pages-my-center/data-notice-area.vue'
import { onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'
import HPopup from '@/components/h-popup.vue'

import PreviewPath from '@/components/preview-path.vue'
import { onShow } from '@dcloudio/uni-app'
import JuxtaCard from '@/pages-my-center/product-manage/edit-process/juxta-card.vue'
import HEmpty from '@/components/h-empty.vue'

const props = defineProps({
  productItem: {
    type: Object,
    default: () => {}
  }
})

const techId = ref(0)
const techData = ref([])
const techStandard = ref(false)
onMounted(() => {
  getInitData()
})

onShow(() => {
  getInitData()
})

const cardNotice = ref('')
const cardTime = ref('')
// 10 顺序 20 并序
const techPattern = ref(0)

function getInitData() {
  techData.value = []
  _get({ url: `/tech/${props.productItem.id}` }).then(
    async (res: IResponseType<{ standard: boolean; techData: unknown[]; techPattern: number }>) => {
      techStandard.value = res.data.standard
      if (res.data.techData) {
        techPattern.value = res.data.standard ? res.data.techPattern : 10
        if (res.data.standard) {
          cardNotice.value = '工艺路线已保存为标准工艺,员工需按照工艺路线进行记工'
        } else {
          cardNotice.value = '工艺路线由工人的记工数据智能分析得出，与实际工艺路线可能存在差异'
        }
        cardTime.value = res.data.techData[0]?.createdDate
        techId.value = res.data.techData[0]?.techId
        techData.value = res.data.techData.map((v) => {
          return {
            ...v,
            isFirstProcess: v.parentProcessId === 0 ? '0' : '1'
          }
        })
      } else {
        cardNotice.value = ''
      }
    }
  )
}

function handleProcessEdit() {
  uni.navigateTo({
    url: `/pages-my-center/product-manage/edit-process-path?productData=${JSON.stringify(props.productItem)}&techId=${
      techId.value
    }&techData=${JSON.stringify(techData.value)}&standard=${techStandard.value}&techPattern=${techPattern.value}`
  })
}
</script>

<style lang="scss" scoped>
.mark {
  width: px2vw(16);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
.card-group {
  width: px2vw(1);
  background-color: #fff;
  height: 70%;
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(px2vw(-1));
  border-left: px2vw(2) dashed #cccccc;
  z-index: 1;
}
.popup-title {
  padding: px2vw(4);
  background: #ebf0f5;
  border-radius: px2vw(4);
  color: #5a6f82;
  font-size: px2vw(24);
}
.popup-close {
  right: px2vw(8);
  top: px2vw(8);
}
</style>
