<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">选择出库产品</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="flex-center px-32 mt-32">
          <h-search class="flex-1" placeholder="输入产品编码/名称" bgClass="bg-f3f3f5" @searchInput="handleInput" />
        </view>
        <view class="flex flex-col h-500 overflow-auto" v-if="productList.length || !searchInput">
          <view class="font-24 pt-32 pb-16 border-bottom-f5f5f5 pl-32 box"
            >已选 <text class="color-ff0000">{{ checkCheckedNum }}</text></view
          >
          <scroll-view
            @refresherrefresh="pageListRefresh"
            @scrolltolower="pageListLoadMore"
            :refresher-triggered="refreshTag"
            enable-flex
            refresher-enabled
            scroll-y
            class="box flex-1 overflow-hidden"
          >
            <view
              v-for="item in productList"
              :key="item.itemSeq"
              class="mx-32 py-32 box flex align-center font-28 bold b-b-1 border-f5f5f5"
              @tap="handleSelectProduct(item)"
            >
              <h-text-display
                :text="item.productName"
                :width="212"
                :class="item.checked ? 'color-0066ff' : 'color-333'"
              />
              <h-text-display
                :width="212"
                :text="`(${item.productCode})`"
                :class="item.checked ? 'color-0066ff' : 'color-999'"
              />
              <view class="flex-1" />
              <h-checkbox :checked="item.checked" @checkedChange="handleSelectProduct(item)" class="ml-16" />
            </view>
          </scroll-view>
        </view>

        <view v-else class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查询到相关产品，请重新搜索" />
        </view>
      </view>
      <view class="bg-fff py-32 box flex justify-center align-center font-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { $store } from '@/utils/common'
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import HStatusFooter from '@/components/h-status-footer.vue'
import WordIcon from '@/components/word-icon.vue'
import HCheckBox from '@/components/h-checkbox.vue'
import HooksPageList from '@/hooks/page-list'
onMounted(() => {
  uni.$on('cancelPopup', (options) => {
    handleCancel()
  })
})
//分页列表模块
const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/storage/finish/list', {
    run: false,
    cb: dataListCallBack
  })
/** 分页列表模块 */
function dataListCallBack(res) {
  dataList.value.filter((item: any) => item.num)
  dataList.value =
    dataList?.value.map((v) => {
      if (!v.checked) {
        v.checked = false
      }
      return v
    }) ?? []
  productList.value = dataList.value
  popupStaff.value.open('bottom')
}
const popupStaff = ref(null)
const searchInput = ref('')
const productList = ref([])
const itemSelect = ref([])
const checkCheckedNum = computed(() => {
  return productList.value.filter((item: any) => item.checked).length
})

const props = defineProps({})
const emits = defineEmits(['productSelected'])
function open(nameOrCode) {
  itemSelect.value = []
  searchInput.value = nameOrCode
  getList(nameOrCode)
}
function getList(nameOrCode) {
  pageListSetParams(
    {
      productCodeOrName: nameOrCode
    },
    '/storage/finish/list'
  )
}
function handleInput(e) {
  searchInput.value = e.detail.value
  getList(searchInput.value)
  // if (searchInput.value) {
  //   productList.value = dataList.value.filter(
  //     (v) => v.productName.includes(searchInput.value) || v.productSeq.includes(searchInput.value)
  //   )
  // } else {
  //   handleInputClear()
  // }
}
function handleInputClear() {
  searchInput.value = ''
  getList(searchInput.value)
  // productList.value = dataList.value
}
function handleCancel() {
  popupStaff.value?.close('bottom')
}

function handleSelectProduct(val) {
  val.checked = !val.checked
  //可取消选中
  if (val.checked) {
    itemSelect.value.push(val)
  } else {
    for (let i in itemSelect.value) {
      if (val.id === itemSelect.value[i].id) {
        itemSelect.value.splice(i, 1)
        return
      }
    }
  }
}

function confirmPopup() {
  emits('productSelected', itemSelect.value)
  $store.commit('outProductStorage/updateOutList', itemSelect.value)
  uni.navigateTo({
    url: '/pages-work-center/finish-product-storage/index-out-product'
  })
}

function initData() {}

defineExpose({ open })
</script>
