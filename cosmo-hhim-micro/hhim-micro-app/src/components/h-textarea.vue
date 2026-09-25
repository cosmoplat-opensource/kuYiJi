<template>
  <view class="relative">
    <view class="flex">
      <textarea
        class="h-textarea box flex-1 align-top"
        :class="[{ border: props.border, padding: props.padding }]"
        :style="[getStyle]"
        :value="newValue"
        :placeholder="props.placeholder"
        :maxlength="props.maxlength"
        disable-default-padding
        hold-keyboard
        placeholder-style="color:#b8b8b8"
        cursor-spacing="200"
        :show-confirm-bar="false"
        auto-height
        :disabled="props.disabled"
        @input="handleInput"
      />
      <template v-if="showClear">
        <view
          v-show="newValue"
          @click="clearValue"
          class="w-56 flex justify-end align-top clear"
          :style="{ top: padding ? (24 / 720) * 100 + 'vw' : '0' }"
        >
          <image src="/static/images/icon_del.svg" class="icon-48" />
        </view>
      </template>
    </view>
    <view class="input-length" v-if="props.maxlength && showLength">
      <text class="current">{{ CurrentLength }}</text>
      <text class="mx-4">/</text>
      <text>{{ props.maxlength }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  border: {
    type: Boolean,
    default: true
  },
  padding: {
    type: Boolean,
    default: true
  },
  customStyle: {
    type: Object,
    default() {
      return {}
    }
  },
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入...'
  },
  showLength: {
    type: Boolean,
    default: true
  },
  maxlength: {
    type: Number,
    default: 0
  },
  showClear: {
    type: Boolean,
    default: false
  },
  minH: {
    type: Number,
    default: 144
  }
})

const CurrentLength = computed(() => newValue.value?.length ?? 0)
const getStyle = computed(() => {
  const style = {
    pointerEvents: props.disabled ? 'none' : 'auto',
    minHeight: props.minH + 'rpx'
  }
  Object.assign(style, props.customStyle)
  return style
})
const newValue = computed({
  get: function () {
    return props.modelValue
  },
  set: function (value) {
    emits('update:modelValue', value)
  }
})
const emits = defineEmits(['update:modelValue', 'input'])
function handleInput(e) {
  newValue.value = e.detail.value
  emits('input', e.detail.value)
}
function clearValue() {
  newValue.value = ''
  emits('input', '')
}
</script>

<style lang="scss" scoped>
.h-textarea {
  // width: 100%;
  // min-height: px2vw(184);
  font-size: px2vw(28);
  white-space: pre-wrap;
  text-align: justify;
  text-justify: inter-ideograph;
}
.min-h-84 {
  min-height: px2vw(84);
}

.h-textarea.padding {
  padding: px2vw(24) px2vw(16) px2vw(30);
}

.h-textarea.border {
  border: px2vw(2) solid #f5f5f5;
  border-radius: px2vw(16);
}

.input-length {
  font-size: px2vw(24);
  padding: px2vw(10);
  text-align: right;
  color: #999;
  position: absolute;
  right: 0;
  bottom: 0;
}

.input-length .current {
  color: #999999;
}

.clear {
  // position: absolute;
  // right: 0;
  // z-index: 2;
  // top: px2vw(24);
}
</style>
