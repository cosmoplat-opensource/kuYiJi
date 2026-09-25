<template>
  <scroll-view v-if="sortType === '顺序'" enable-flex scroll-y class="h-100 flex flex-col px-16 box overflow-hidden">
    <view
      class="flex align-center justify-between mb-16 rounded-16 bg-fff px-32 pb-96 pt-48 box"
      v-for="(item, index) in dataArr"
      :key="index"
    >
      <!--左侧父级节点-->
      <view class="edit-card relative" :class="{ disable: index > 0, empty: !item.processName }">
        <view class="parent-last-icon flex align-center">
          <word-icon
            text="尾"
            v-if="item.isLastProcess === '0' || index === 0"
            :class="{ 'mr-32': item.isFirstProcess === '0' }"
          />
          <word-icon text="首" v-if="item.isFirstProcess === '0'" />
        </view>
        <view :class="item.processName ? 'color-333' : 'color-b6c0c9'" @tap="handleCardSelect(index, 'left')">
          <text class="font-28">{{ item.processName || (index === 0 ? '选择尾序' : '选择工序') }}</text>
        </view>
        <view class="flex-center btn-area">
          <image :src="formatImage('icon_del_circle', 'svg')" class="icon-48" @tap.stop="handleProcessDelete(index)" />
          <image
            v-if="item.isFirstProcess !== '0'"
            :src="formatImage('icon_add_circle', 'svg')"
            class="icon-48 ml-48"
            @tap.stop="handleAddTarget(index)"
          />
        </view>
      </view>
      <!--右侧子级节点-->
      <view class="flex flex-col flex-1">
        <view
          class="flex align-center justify-between"
          :class="{ 'mt-24': tIndex }"
          v-for="(target, tIndex) in item.targetArr"
          :key="tIndex"
        >
          <view class="flex-1 text-center">-></view>
          <view
            class="edit-card relative"
            :class="{ empty: !target.processName }"
            @tap="handleCardSelect(index, 'right', tIndex)"
          >
            <text class="font-28">{{ target.processName || '选择工序' }}</text>
            <view class="parent-last-icon">
              <word-icon text="首" v-if="target.isFirstProcess === '0'" />
            </view>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
import WordIcon from '@/components/word-icon.vue'

const props = defineProps({
  sortType: {
    type: String,
    default: '顺序'
  },
  dataArr: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['cardSelect', 'addTarget', 'deleteProcess'])

function handleCardSelect(index: number, type: string, tIndex?: number) {
  emit('cardSelect', index, type, tIndex)
}
function handleAddTarget(index: number) {
  emit('addTarget', index)
}

function handleProcessDelete(index: number) {
  emit('deleteProcess', index)
}
</script>

<style lang="scss">
/* 工艺节点卡片样式(原在父页面 edit-process-path.vue,因父页面被 scoped 化,H5 下子组件内节点命中不了,改为组件内样式) */
.edit-card {
  width: px2vw(288);
  /* H5 宽视口下 px2vw 按窗口放大,卡片会被撑散; 用固定 px 上限约束(720设计稿的288px≈实际显示上限), 小程序屏窄不受影响 */
  max-width: 288px;
  box-sizing: border-box;
  height: px2vw(80);
  line-height: px2vw(80);
  text-align: center;
  background: #f7f8e7;
  border-radius: px2vw(16);
  border: px2vw(2) dashed #9badbc;
  &.disable {
    background-color: #f5f5f5;
    border: px2vw(2) solid #f5f5f5;
  }
  &.empty {
    color: #b6c0c9;
    background-color: white;
  }
  .btn-area {
    position: absolute;
    bottom: px2vw(-64);
    left: 50%;
    transform: translateX(-50%);
  }
}
.parent-last-icon {
  position: absolute;
  top: px2vw(-20);
  left: 50%;
  transform: translateX(-50%);
}
</style>
