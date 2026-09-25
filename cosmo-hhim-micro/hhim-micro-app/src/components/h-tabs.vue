<template>
  <view class="_tab-box" :style="{ fontSize: formatPx2Vw(defaultConfig.fontSize) + 'vw', color: defaultConfig.color }">
    <view
      class="_tab-item-box"
      :class="[defaultConfig.itemWidth ? '_clamp' : '_flex', ...(Array.isArray(wrapperClass) ? wrapperClass : wrapperClass ? wrapperClass.split(' ') : [])]"
    >
      <template v-for="(item, index) in tabList" :key="index">
        <view
          class="_item"
          :id="'_tab_' + index"
          :class="{ _active: tagIndex === index, ...itemClass }"
          :style="{
            color: tagIndex === index ? defaultConfig.activeColor : defaultConfig.color,
            width: defaultConfig.itemWidth ? formatPx2Vw(defaultConfig.itemWidth) + 'vw' : '',
            'font-weight': defaultConfig.activeBold && tagIndex === index ? defaultConfig.activeBold : 'normal'
          }"
          @click="tabClick(index)"
        >
          {{ item[defaultConfig.labelKey] || item }}
          <view
            class="_underline"
            :style="{
              width: getUnderlineWidth(index) + 'rpx',
              height: formatPx2Vw(defaultConfig.underLineHeight) + 'vw',
              borderRadius: formatPx2Vw(defaultConfig.underLineHeight / 2) + 'vw',
              backgroundColor: defaultConfig.underLineColor
            }"
          />
        </view>
      </template>
    </view>
  </view>
</template>

<script>
import { formatPx2Vw } from '@/utils/common'

export default {
  name: 'h-tabs',
  props: {
    tabData: {
      type: Array,
      default: () => []
    },
    activeIndex: {
      type: Number,
      default: 0
    },
    config: {
      type: Object,
      default: () => {
        return {}
      }
    },
    itemClass: {
      type: String,
      default: 'px-32'
    },
    wrapperClass: {
      type: [String, Array],
      default: () => []
    }
  },
  data() {
    return {
      tabList: [],
      tagIndex: 0,
      defaultConfig: {
        // 要显示的字段名
        labelKey: 'name',
        // 字体大小 rpx
        fontSize: 26,
        // 字体颜色
        color: '#313131',
        //激活字体加粗数值
        activeBold: 'normal',
        // 激活字体颜色
        activeColor: '#e54d42',
        // item宽度 0为自动
        itemWidth: 0,
        // 下划线左右边距，文字宽度加边距 rpx
        underLinePadding: 0,
        // 下划线宽度 rpx  注意：设置了此值 underLinePadding 失效
        underLineWidth: 0,
        // 下划线高度 rpx
        underLineHeight: 4,
        // 下划线颜色
        underLineColor: '#e54d42'
      }
    }
  },
  watch: {
    tabData(value) {
      this.updateData()
    },
    config(value) {
      this.updateConfig()
    }
  },
  mounted() {
    this.updateConfig()
    this.updateData()
    this.tagIndex = this.activeIndex
  },
  methods: {
    formatPx2Vw,
    updateData() {
      let data = []
      if (typeof this.tabData[0] == 'string') {
        this.tabData.forEach((item, index) => {
          data.push({
            name: item
          })
        })
        this.defaultConfig.labelKey = 'name'
      } else {
        data = JSON.parse(JSON.stringify(this.tabData))
      }

      this.tabList = data
    },
    updateConfig() {
      this.defaultConfig = Object.assign(this.defaultConfig, this.config)
    },
    // 下划线宽度：优先 underLineWidth 配置，否则按文字长度×字号计算（纯计算，不依赖 DOM 测量）
    getUnderlineWidth(index) {
      const item = this.tabList[index]
      if (!item) return 0
      const name = item[this.defaultConfig.labelKey] || item
      if (this.defaultConfig.underLineWidth) return this.defaultConfig.underLineWidth
      return String(name).length * this.defaultConfig.fontSize + this.defaultConfig.underLinePadding * 2
    },
    tabClick(index) {
      if (this.tabData[index].disabled) {
        return
      }
      this.tagIndex = index
      this.$emit('tabClick', index)
    }
  }
}
</script>

<style lang="scss" scoped>
._tab-box {
  width: 100%;
  display: flex;
  font-size: px2vw(28);
  position: relative;
  height: px2vw(80);
  line-height: px2vw(80);
  z-index: 10;
  ._tab-item-box {
    height: 100%;
    flex: 1;
    min-width: 0;
    &._flex {
      display: flex;
      width: 100%;
      ._item {
        flex: 1;
        min-width: 0;
        // tab 文案保持单行完整显示：不折行、不省略（如“调整历史”4 字标题）
        white-space: nowrap;
      }
    }
    &._clamp {
      ._item {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    ._item {
      height: 100%;
      display: inline-block;
      text-align: center;
      padding-top: 0;
      padding-bottom: 0;
      position: relative;
      color: #333;
      &._active {
        color: #e54d42;
      }
      ._underline {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        opacity: 0;
        transition: opacity 0.3s;
      }
      &._active ._underline {
        opacity: 1;
      }
    }
  }
}
</style>
