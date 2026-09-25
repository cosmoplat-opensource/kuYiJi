<template>
  <view class="">
    <view class="box">
      <view class="circle" :style="[circle]">
        <view class="left" :style="[size]">
          <view class="left-circle" :style="[size, leftCircle, leftValue]"> </view>
        </view>
        <view class="right" :style="[size]">
          <view class="right-circle" :style="[size, rightcircle, rightValue]"></view>
        </view>
        <view class="inner flex-center flex-col" :style="[inner]">
          <slot />
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  props: {
    value: {
      type: Number,
      default: 10
    },
    widths: {
      type: Number,
      default: 50
    },
    breadth: {
      type: Number,
      default: 6
    },
    activeColor: {
      type: String,
      default: '#24A8FF'
    },
    defaultColor: {
      type: String,
      default: '#ffffff'
    }
  },
  data() {
    return {}
  },
  mounted() {},
  methods: {},
  computed: {
    circle() {
      const { widths, defaultColor } = this
      return {
        width: `${widths}vw`,
        height: `${widths}vw`,
        background: defaultColor
      }
    },
    size() {
      const { widths } = this
      return {
        width: `${widths / 2}vw`,
        height: `${widths}vw`
      }
    },
    leftCircle() {
      const { widths, activeColor } = this
      return {
        borderTopLeftRadius: `${widths}vw`,
        borderBottomLeftRadius: `${widths}vw`,
        background: activeColor
      }
    },
    rightcircle() {
      const { widths, activeColor } = this
      return {
        borderTopRightRadius: `${widths}vw`,
        borderBottomRightRadius: `${widths}vw`,
        background: activeColor
      }
    },
    leftValue() {
      const { value } = this
      let str = value <= 50 ? 'rotate(180deg)' : `rotate(${((value - 50) / 50) * 180 + 180}deg)`
      let timer = value <= 50 ? 0 : ((value - 50) / 50) * 0.5
      return {
        transform: str,
        transition: `all ${timer}s linear 0.5s`
      }
    },
    rightValue() {
      const { value } = this
      let str = value >= 50 ? 'rotate(0deg)' : `rotate(${(value / 50) * 180 - 180}deg)`
      let timer = value >= 50 ? 0.5 : (value / 50) * 0.5
      return {
        transform: str,
        transition: `all ${timer}s linear`
      }
    },
    inner() {
      const { widths, breadth } = this
      return {
        width: `${widths - breadth}vw`,
        height: `${widths - breadth}vw`
      }
    }
  },
  watch: {}
}
</script>

<style lang="scss" scoped>
.circle {
  border-radius: 50%;
  position: relative;
}

.left,
.right {
  position: absolute;
  overflow: hidden;
}

.left-circle {
  transform-origin: right center;
  transform: rotate(180deg);
}

.right-circle {
  transform-origin: left center;
  transform: rotate(-180deg);
}

.right {
  right: 0;
}

.inner {
  background: #fff;
  position: absolute;
  z-index: 999;
  border-radius: 100%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #a8a8a8;
}
</style>
