<template>
  <view class="uni-stat__select">
    <span v-if="label" class="uni-label-text hide-on-phone">{{ label + '：' }}</span>
    <view class="uni-select">
      <view class="uni-select__input-box" @click.stop="toggleSelector">
        <!-- 显示插槽内容 -->
        <slot></slot>
      </view>
      <view class="uni-select--mask" v-if="showSelector" @click.stop="toggleSelector" />
      <view class="uni-select__selector" v-if="showSelector">
        <view class="uni-popper__arrow"></view>
        <scroll-view scroll-y="true" class="uni-select__selector-scroll">
          <view class="uni-select__selector-empty" v-if="mixinDatacomResData.length === 0">
            <text>{{ emptyTips }}</text>
          </view>
          <view
            v-else
            class="uni-select__selector-item"
            v-for="(item, index) in mixinDatacomResData"
            :key="index"
            @click.stop="change(item)"
          >
            <image :src="item.icon" class="icon-32 mr-16" v-if="item.icon" />
            <text :class="{ 'uni-select__selector__disabled': item.disable }">{{ formatItemName(item) }}</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
/**
 * DataChecklist 数据选择器
 * @description 通过数据渲染的下拉框组件
 * @tutorial https://uniapp.dcloud.io/component/uniui/uni-data-select
 * @property {String} value 默认值
 * @property {Array} localdata 本地数据 ，格式 [{text:'',value:''}]
 * @property {Boolean} clear 是否可以清空已选项
 * @property {Boolean} emptyText 没有数据时显示的文字 ，本地数据无效
 * @property {String} label 左侧标题
 * @property {String} placeholder 输入框的提示文字
 * @event {Function} change  选中发生变化触发
 */

export default {
  name: 'uni-stat-select',
  mixins: [uniCloud.mixinDatacom || {}],
  data() {
    return {
      showSelector: false,
      current: '',
      mixinDatacomResData: [],
      apps: [],
      channels: []
    }
  },
  props: {
    localdata: {
      type: Array,
      default() {
        return []
      }
    },
    value: {
      type: [String, Number],
      default: ''
    },
    modelValue: {
      type: [String, Number],
      default: ''
    },
    label: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '...'
    },
    emptyTips: {
      type: String,
      default: '无选项'
    },
    clear: {
      type: Boolean,
      default: true
    },
    defItem: {
      type: Number,
      default: 0
    }
  },
  created() {
    this.last = `${this.collection}_last_selected_option_value`
    if (this.collection && !this.localdata.length) {
      this.mixinDatacomEasyGet()
    }
  },
  computed: {
    typePlaceholder() {
      const text = {
        'opendb-stat-app-versions': '版本',
        'opendb-app-channels': '渠道',
        'opendb-app-list': '应用'
      }
      const common = '请选择'
      const placeholder = text[this.collection]
      return placeholder ? common + placeholder : common
    }
  },
  watch: {
    localdata: {
      immediate: true,
      handler(val, old) {
        if (Array.isArray(val)) {
          this.mixinDatacomResData = val
        }
      }
    },
    // #ifndef VUE3
    value() {
      this.initDefVal()
    },
    // #endif
    // #ifdef VUE3
    modelValue() {
      this.initDefVal()
    },
    // #endif
    mixinDatacomResData: {
      immediate: true,
      handler(val) {
        if (val.length) {
          this.initDefVal()
        }
      }
    }
  },
  methods: {
    initDefVal() {
      let defValue = ''
      if ((this.value || this.value === 0) && !this.isDisabled(this.value)) {
        defValue = this.value
      } else if ((this.modelValue || this.modelValue === 0) && !this.isDisabled(this.modelValue)) {
        defValue = this.modelValue
      } else {
        let strogeValue
        if (this.collection) {
          strogeValue = uni.getStorageSync(this.last)
        }
        if (strogeValue || strogeValue === 0) {
          defValue = strogeValue
        } else {
          let defItem = ''
          if (this.defItem > 0 && this.defItem < this.mixinDatacomResData.length) {
            defItem = this.mixinDatacomResData[this.defItem - 1].value
          }
          defValue = defItem
        }
        this.emit(defValue)
      }
      const def = this.mixinDatacomResData.find((item) => item.value === defValue)
      this.current = def ? this.formatItemName(def) : ''
    },

    /**
     * @param {[String, Number]} value
     * 判断用户给的 value 是否同时为禁用状态
     */
    isDisabled(value) {
      let isDisabled = false

      this.mixinDatacomResData.forEach((item) => {
        if (item.value === value) {
          isDisabled = item.disable
        }
      })

      return isDisabled
    },

    clearVal() {
      this.emit('')
      if (this.collection) {
        uni.removeStorageSync(this.last)
      }
    },
    change(item) {
      if (!item.disable) {
        this.showSelector = false
        this.current = this.formatItemName(item)
        this.emit(item.value)
      }
    },
    emit(val) {
      this.$emit('change', val)
      this.$emit('input', val)
      this.$emit('update:modelValue', val)
      if (this.collection) {
        uni.setStorageSync(this.last, val)
      }
    },
    closeDrop() {
      this.showSelector = false
    },
    toggleSelector() {
      this.showSelector = !this.showSelector
    },
    formatItemName(item) {
      let { text, value, channel_code } = item
      channel_code = channel_code ? `(${channel_code})` : ''
      return this.collection.indexOf('app-list') > 0 ? `${text}(${value})` : text ? text : `未命名${channel_code}`
    }
  }
}
</script>

<style lang="scss">
$uni-base-color: #6a6a6a !default;
$uni-main-color: #3a3a3a !default;
$uni-secondary-color: #909399 !default;
$uni-border-3: #dcdcdc;

.uni-stat__select {
  display: flex;
  align-items: center;
}

.uni-label-text {
  font-size: px2vw(14);
  font-weight: bold;
  color: $uni-base-color;
  margin: auto 0;
  margin-right: px2vw(5);
}

.uni-select {
  font-size: px2vw(14);
  // border: px2vw(1) solid $uni-border-3;
  box-sizing: border-box;
  border-radius: px2vw(1);
  padding: 0 px2vw(5);
  position: relative;
  /* #ifndef APP-NVUE */
  display: flex;
  user-select: none;
  /* #endif */
  flex-direction: row;
  align-items: center;
  // border-bottom: solid px2vw(1) $uni-border-3;
}

.uni-select__label {
  font-size: px2vw(16);
  line-height: px2vw(22);
  padding-right: px2vw(10);
  color: $uni-secondary-color;
}

.uni-select__input-box {
  min-height: px2vw(20);
  position: relative;
  /* #ifndef APP-NVUE */
  display: flex;
  /* #endif */
  flex: 1;
  flex-direction: row;
  align-items: center;
}

.uni-select__input {
  flex: 1;
  font-size: px2vw(14);
  height: px2vw(22);
  line-height: px2vw(22);
}

.uni-select__input-plac {
  font-size: px2vw(14);
  color: $uni-secondary-color;
}

.uni-select__selector {
  /* #ifndef APP-NVUE */
  box-sizing: border-box;
  /* #endif */
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: auto;
  background-color: #ffffff;
  border: px2vw(1) solid #ebeef5;
  border-radius: px2vw(6);
  box-shadow: 0 px2vw(2) px2vw(12) 0 rgba(0, 0, 0, 0.1);
  z-index: 2;
  padding: px2vw(4) 0;
}

.uni-select__selector-scroll {
  padding-top: px2vw(8);
  padding-bottom: px2vw(8);
  /* #ifndef APP-NVUE */
  max-height: px2vw(260);
  box-sizing: border-box;
  /* #endif */
}

.uni-select__selector-empty,
.uni-select__selector-item {
  line-height: px2vw(20);
  font-size: px2vw(28);
  color: #333;
  text-align: center;
  padding: px2vw(26) px2vw(64) px2vw(26) px2vw(40);
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  white-space: nowrap;
  flex-wrap: nowrap;
}

.uni-select__selector-item:active {
  background-color: #f3f3f5;
}

.uni-select__selector-empty:last-child,
.uni-select__selector-item:last-child {
  border-bottom: none;
}

.uni-select__selector__disabled {
  opacity: 0.4;
  cursor: default;
}

/* picker 弹出层通用的指示小三角 */
.uni-popper__arrow,
.uni-popper__arrow::after {
  position: absolute;
  display: block;
  width: 0;
  height: 0;
  border-color: transparent;
  border-style: solid;
  border-width: px2vw(12);
}

.uni-popper__arrow {
  filter: drop-shadow(0 4px 24px rgba(0, 0, 0, 0.03));
  top: px2vw(-12);
  right: px2vw(19);
  margin-right: px2vw(6);
  border-top-width: 0;
  border-bottom-color: #ebeef5;
}

.uni-popper__arrow::after {
  content: ' ';
  top: px2vw(1);
  margin-left: px2vw(-6);
  border-top-width: 0;
  border-bottom-color: #fff;
}

.uni-select__input-text {
  color: $uni-main-color;
  white-space: nowrap;
  text-overflow: ellipsis;
  -o-text-overflow: ellipsis;
  overflow: hidden;
  width: auto;
}

.uni-select__input-placeholder {
  color: $uni-base-color;
}

/* #ifdef H5 */
/* H5：下拉选项强制横排 + 宽度自适应内容，避免 flex 压缩导致选项文字竖排/重叠 */
.uni-select__selector {
  width: max-content;
  min-width: px2vw(200);
}

.uni-select__selector-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  white-space: nowrap;
  flex-wrap: nowrap;
}
/* #endif */

.uni-select--mask {
  position: fixed;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
}
</style>
