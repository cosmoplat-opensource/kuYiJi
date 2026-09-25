<template>
  <view class="mx-16 flex-1 flex flex-col mt-16">
    <report-input-select
      @itemSelected="handleProductSelect"
      :processMark="REPORT_WORK.PRODUCT"
      :focus="autoFocus"
      :inputData="productItem"
      :showAdd="!checkWorkerBaseDataConfine"
    >
      <template #mark>
        <view class="process-mark blue-deep">
          <text>产品</text>
        </view>
      </template>
      <template #recommend>
        <recommend-list @itemSelected="handleProductSelect" :recommendList="recommendList" />
      </template>
    </report-input-select>
    <view class="flex-1 flex align-end justify-center pb-32">
      <h-button width="624" height="88" text="下一步" class="ml-32" @tap.stop="handleBtnNext" />
    </view>
  </view>
</template>

<script setup lang="ts">
import ReportInputSelect from '@/components/report-input-select.vue'
import RecommendList from '@/pages/report-work/components/recommend-list.vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { _post } from '@/utils/common-request'
import { REPORT_WORK } from '@/utils/common'
import HooksSettingConfig from '@/hooks/setting-config'
const { checkWorkerBaseDataConfine } = HooksSettingConfig()
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  }
})

// 推荐的产品集合（F02 已下线，返回空数组）
const recommendList = ref([])
function getRecommendList() {
  _post({ url: '/submit/multiRecommend', data: [] }).then((res) => {
    recommendList.value = res.data['HIGH_FREQUENCY_PRODUCT'] || []
  })
}

// 搜索的产品集合
const productList = ref([])
const searchValue = ref('')
const autoFocus = ref(false)

function handleCloseAllList() {
  autoFocus.value = false
}
const emit = defineEmits(['productSelected', 'tabActiveChange'])
// 产品选中
const productItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
function checkSameName(cb) {
  if (productList.value.length && props.dataItem.itemName && !props.dataItem.itemCode) {
    if (productList.value.find((item) => item.itemName === props.dataItem.itemName)) {
      uni.showModal({
        title: '提示',
        showCancel: false,
        content: `已存在与 ${props.dataItem.itemName} 名称相同的产品，请选择已存在的同名产品`,
        success: (res) => {
          if (res.confirm) {
            autoFocus.value = true
          } else {
            cb()
          }
        }
      })
    } else {
      cb()
    }
  } else {
    cb()
  }
}
defineExpose({ handleCloseAllList, checkSameName })

function handleProductSelect(item) {
  item.t = new Date().getTime()
  Object.assign(productItem, item)
  emit('productSelected', item)
}
function handleBtnNext() {
  if (!productItem.itemName) {
    uni.showToast({ title: '请选择产品', icon: 'none' })
    return
  }
  emit('tabActiveChange', 2)
}

watch(
  props.dataItem,
  (val) => {
    Object.assign(productItem, val)
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  getRecommendList()
})
</script>
<style lang="scss" scoped>
.process-mark {
  position: absolute;
  left: 0;
  top: 0;
  padding: px2vw(4) px2vw(8);
  font-size: px2vw(24);
  border-radius: 0 0 px2vw(8) 0;
  background-color: #ebf0f5;
  color: #5a6f82;
}
</style>
