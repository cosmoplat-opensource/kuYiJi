<template>
  <view class="bg-fff flex flex-col pt-32 box mb-8" v-for="(item, index) in dataList" :key="index">
    <!--头部人物信息区域-->
    <view class="flex align-center pr-32 box overflow-hidden" :class="[item.listShow ? '' : 'pb-32']">
      <view class="block" />
      <view class="color-333 font-28 bold ml-16">{{ item.employeeName }}</view>
      <view class="flex-1" />
      <view class="flex-center h-32 px-16 bg-EBF0F5 rounded-16">
        <text class="font-24 color-5a6f82">待结算</text>
        <text class="color-ff0000 ml-4 bold font-24">
          {{ String(item.totalAdjustedNum).replace(/^(.*\..{4}).*$/, '$1') }}
        </text>
      </view>
      <image
        src="/static/images/icon_unfold.svg"
        class="icon-32 ml-32"
        :class="{ 'icon-rotate-y': item.listShow }"
        @tap="handleListShow(item)"
      />
    </view>
    <template v-if="item.listShow">
      <view
        class="pt-32 pb-24 mx-32 box flex flex-col"
        :class="[pi + 1 === item.detailList.length ? '' : ' border-bottom-dashed']"
        v-for="(p, pi) in item.detailList.slice(0, item.showNum)"
        :key="pi"
      >
        <!--产品名称/编码-->
        <view class="font-28 bold flex align-center">
          <h-text-display :text="p.productName" :width="288" class="font-28" />
          <view class="color-999 flex-1">({{ p.productCode }})</view>
          <image
            :src="formatImage('icon_settlement_edit', 'svg')"
            class="icon-32"
            @tap="changeEditAble(item.employeeId, item.employeeName, p)"
          />
        </view>
        <!--工序-->
        <view class="mt-24 flex align-center flex-wrap color-5a6f82 font-28">
          <h-text-display :text="p.operateProcessName" :width="288" />
          <view v-if="!item.editStatus && p.remark" class="mark-ebf0f5 ml-32">{{ p.remark }}</view>
        </view>
        <!--数量-->
        <view class="flex justify-between mt-24">
          <view class="mark-ebf0f5">数量</view>
          <view class="flex-1" />
          <view class="color-333 bold font-28">{{ p.adjustedNum }}</view>
          <view class="color-333 bold font-28 ml-8">{{ p.productUnit }}</view>
        </view>
      </view>
      <view
        v-if="item.detailList?.length > 5 && item.showNum < item.detailList.length"
        @tap="itemShowMore(item)"
        class="more h-80 font-28 color-0066ff bg-f3f3f5 rounded-16 flex justify-center align-center my-32 mx-32"
      >
        查看更多
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import HTextDisplay from '@/components/h-text-display.vue'
import { $store, formatImage } from '@/utils/common'
import { computed, ref } from 'vue'
import HooksPopup from '@/hooks/popup'
const emits = defineEmits(['openEditPopup'])
const dataList = computed(() => {
  const arr = $store.state.settlement.dataList
  arr.forEach((v: DataItem) => {
    let count = 0
    v.detailList?.forEach((p) => {
      !p.hasOwnProperty('oldNum') && (p.oldNum = p.adjustedNum)
      !p.hasOwnProperty('remark') && (p.remark = '')
      count += Number(p.adjustedNum)
      return p
    })
    v.totalAdjustedNum = count
    v.listShow = true
    if (v.detailList?.length > 5) {
      v.showNum = 5
    } else {
      v.showNum = v.detailList.length
    }
    return v
  })
  return arr
})

type DataItem = {
  employeeName: string
  editStatus: boolean
  totalAdjustedNum: number
  showNum: number
  listShow: boolean
  detailList: {
    oldNum: number
    adjustedNum: number
    operateProcessCode: string
    operateProcessName: string
    operateProcessSeq: string
    productCode: string
    productName: string
    productSeq: string
    remark: string
  }[]
}

function changeEditAble(employeeId, employeeName, item) {
  let currentItem = {
    employeeId: employeeId,
    employeeName: employeeName,
    ...item
  }
  currentItem.oldNum = currentItem.adjustedNum
  emits('openEditPopup', currentItem)
}

const { popupOpen } = HooksPopup()
function handleProductNumCheck(p) {
  // 正则判断数字
  const reg = /^[0-9\.]*$/
  if (!reg.test(p.adjustedNum)) {
    popupOpen('请输入正确的数量')
    p.adjustedNum = p.oldNum
    return
  }
  if (p.adjustedNum > p.oldNum) {
    popupOpen('结算数量不能大于已完工数量')
    p.adjustedNum = p.oldNum
    return
  }
}
//列表展开
function handleListShow(item) {
  item.listShow = !item.listShow
  if (item.listShow) {
    if (item.detailList?.length > 5) {
      item.showNum = 5
    } else {
      item.showNum = item.detailList.length
    }
  }
}
function itemShowMore(item) {
  item.showNum += 10
}
</script>

<style lang="scss" scoped>
.more {
  &:active {
    background-color: #dbeaff;
  }
}
.index-mark {
  width: px2vw(38);
  height: px2vw(32);
  text-align: center;
  line-height: px2vw(32);
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  color: #5a6f82;
  font-size: px2vw(20);
}
.adjustment-area:active {
  .adjustment-text {
    color: #0066ff;
  }
}
.close {
  position: absolute;
  top: px2vw(24);
  right: px2vw(24);
}
</style>
