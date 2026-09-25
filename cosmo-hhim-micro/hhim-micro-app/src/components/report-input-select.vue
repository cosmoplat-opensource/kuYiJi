<template>
  <view class="flex flex-col input-select relative">
    <!--左上角角标-->
    <slot name="mark" />
    <!--产品输入区域-->
    <view class="bg-fff px-32 input-wrapper box pt-72 pb-16">
      <view v-if="readOnly" class="flex align-center pb-16">
        <view class="font-28 color-333 bold">{{ formatStr(inputItem.itemName || '无', 12) }}</view>
        <view class="font-28 color-999" v-if="inputItem.itemCode && !isProcess">
          ({{ formatStr(inputItem.itemCode, 9) }})
        </view>
        <word-icon v-if="inputItem.isLastProcess === '0'" class="ml-16" text="尾" />
        <word-icon v-if="inputItem.isFirstProcess === '0'" class="ml-16" text="首" />
      </view>
      <!-- #ifdef H5 -->
      <!-- H5：disabled input 不响应点击（原生 disabled 吞掉事件导致"选择产品/工序"无反应），
           点击穿透到父级 view 触发 handleInputTap（配合样式 pointer-events:none） -->
      <view v-else class="flex align-center relative" @tap="handleInputTap">
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <view v-else class="flex align-center relative">
      <!-- #endif -->
        <view class="flex flex-col flex-1">
          <input
            class="flex-1 mb-24 bold font-28"
            v-model.trim="inputItem.itemName"
            placeholder-style="color:#B8B8B8"
            :placeholder="markItem.placeholder"
            disabled
            @tap="handleInputTap"
          />
          <view v-if="inputItem.itemCode && !isProcess" class="font-24 color-999">{{ inputItem.itemCode }}</view>
          <view v-if="isProcess && stockMaps[inputItem.itemCode]">
            <h-process-stock :dataItem="stockMaps[inputItem.itemCode]" />
          </view>
        </view>
        <template v-if="inputItem.itemName">
          <image src="/static/images/icon_del_b6c0c9.svg" class="icon-48" @tap="handleInputClear" />
          <view class="hot-area__input-clear" @tap="handleInputClear" />
        </template>
      </view>
      <slot name="lastProcess" />
    </view>
    <!--推荐区域-->
    <slot name="recommend" v-if="!inputItem.itemName" />
    <h-popup-process
      v-if="isProcess"
      ref="popupProcess"
      :urlLink="urlLink"
      :productCode="productCode"
      :productSeq="productSeq"
      :productName="productName"
      :isPreProcess="isPreProcess"
      :processItem="processItem"
      :standard="standard"
      :title="markItem.label"
      :showAdd="showAdd"
      @processSelected="(e) => emit('itemSelected', e)"
    />
    <h-popup-product v-else ref="popupProduct" @productSelected="(e) => emit('itemSelected', e)" :showAdd="showAdd" />
  </view>
</template>

<script setup lang="ts">
// 获取推荐产品
import { computed, PropType, reactive, ref, watch } from 'vue'
import { $store, REPORT_WORK } from '@/utils/common'
import { formatStr } from '@/utils/common'
import HProcessStock from '@/components/h-process-stock.vue'
import HPopupProduct from '@/components/h-popup-product.vue'
import HPopupProcess from '@/components/h-popup-process.vue'

const props = defineProps({
  processMark: {
    type: String as PropType<REPORT_WORK>,
    default: REPORT_WORK.BEFORE
  },
  inputData: {
    type: Object,
    default: () => {}
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  isProcess: {
    type: Boolean,
    default: false
  },
  isPreProcess: {
    type: Boolean,
    default: false
  },
  processItem: {
    type: Object,
    default: () => {}
  },
  productName: {
    type: String,
    default: ''
  },
  productCode: {
    type: String,
    default: ''
  },
  productSeq: {
    type: String,
    default: ''
  },
  standard: {
    type: Boolean,
    default: false
  },
  canUse: {
    type: Boolean,
    default: true
  },
  urlLink: {
    type: String,
    default: '/process/selectMixed'
  },
  showAdd: {
    type: Boolean,
    default: true
  },
  cleanWithEmitParent: {
    type: Boolean,
    default: false
  }
})

// 产品选中
const emit = defineEmits(['itemSelected'])
const stockMaps = computed(() => $store.state.process.stockMaps)

const inputItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
function handleInputClear() {
  inputItem.itemName = ''
  inputItem.itemCode = ''
  inputItem.itemSeq = ''
  if (props.isProcess) {
    inputItem.isFirstProcess = '1'
    inputItem.isLastProcess = '1'
  }
  uni.$emit('reportInputSelectClear', { type: !props.isProcess && !props.isPreProcess ? 'product' : 'process' })
  emit('itemSelected', inputItem)
}

const markItem = computed(() => {
  switch (props.processMark) {
    case REPORT_WORK.BEFORE:
      return { mark: 'blue', placeholder: '选择前工序...', inputClass: 'process', label: '前工序' }
    case REPORT_WORK.NOW:
      return { mark: 'purple', placeholder: '选择报工工序...', inputClass: 'process', label: '报工工序' }
    case REPORT_WORK.CURRENT:
      return {
        mark: 'purple',
        placeholder: props.canUse ? '选择工序...' : '请先选择产品',
        inputClass: 'process',
        label: '工序'
      }
    default:
      return { placeholder: '选择产品...', inputClass: 'process', label: '' }
  }
})

watch(
  props.inputData,
  (val) => {
    Object.assign(inputItem, val)
  },
  { deep: true, immediate: true }
)

const popupProduct = ref(null)
const popupProcess = ref(null)
function handleInputTap() {
  if (!props.canUse) return
  if (props.isProcess) {
    popupProcess.value?.open(inputItem)
  } else {
    popupProduct.value?.open(inputItem)
  }
}
</script>

<style lang="scss">
.input-select {
  .input-wrapper {
    &.process {
      padding-top: px2vw(72);
      padding-bottom: px2vw(40);
    }
    &.product {
      height: px2vw(144);
    }
  }
  .hot-area__input-clear {
    position: absolute;
    top: -10px;
    right: -10px;
    width: px2vw(80);
    height: px2vw(80);
    z-index: 999;
  }
  /* #ifdef H5 */
  /* H5：disabled input 不响应点击（事件被原生 disabled 吞掉），
        pointer-events:none 让点击穿透到父级 view 的 @tap（否则"选择产品/工序"无反应） */
  uni-input {
    pointer-events: none;
  }
  /* #endif */
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
}
</style>
