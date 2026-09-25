<template>
  <view class="color-fff font-64 rounded-16-top step1-wrapper">
    <view class="color-000b29 font-48 bold">欢迎来到KU易记</view>
    <view class="color-000b29 font-28 mt-24">选择感兴趣的功能，为你快速视频演示</view>
    <!--功能区块-->
    <view class="mt-64 flex flex-wrap gap-24">
      <view class="p-32 rounded-16 bg-fff" v-for="(item, index) in typeArr" :key="index" @tap="handleChecked(index)">
        <view class="flex align-center">
          <!--标题-->
          <view class="flex-1 color-333 font-30 bold">{{ item.title }}</view>
          <!--已选中-->
          <view class="flex-center icon-40 rounded-full bg-0066ff" v-if="item.checked">
            <img :src="formatImage('icon_checked_20_fff', 'svg')" class="icon-20" />
          </view>
          <!--未选中-->
          <view class="icon-40 rounded-full border-2 border-cad7eb box" v-else />
        </view>
        <!--内容描述-->
        <view class="mt-44 w-236 desc color-999 pr-16 box">{{ item.desc }}</view>
      </view>
    </view>
    <view class="mt-64">
      <h-button width="100%" height="88" text="我已选好，观看视频" @tap="handleNext" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatImage } from '@/utils/common'

/** 功能区块 **/
const typeArr = ref([
  {
    title: '在制品库存',
    desc: '在制品库存了如指掌， 生产调度更轻松',
    video: '1、在制品库存了如执掌，生产调度更轻松-m',
    checked: true
  },
  {
    title: '智能记工',
    desc: '智能推荐，工人更好记工',
    video: '3、标准工艺-智能推荐-工人更好记工-m',
    checked: true
  },
  { title: '审产防错', desc: '审产有防错，可预防超报、漏报', video: '3、审产防错-m', checked: true },
  { title: '完工计件', desc: '记工数据快速导出，计件统计效率更高', video: '4、完工入库-m', checked: true },
  { title: '入库结算', desc: '产品库存清晰可见，入库结算有依据', video: '5、记工记录结算-m', checked: true },
  { title: '不良品分析', desc: '不良品可掌控、可分析、可改善', video: '6、质量趋势，良品率分析合一-m', checked: true }
])
function handleChecked(index) {
  typeArr.value[index].checked = !typeArr.value[index].checked
}
const emits = defineEmits(['stepChange'])
function handleNext() {
  const arr = typeArr.value.filter((v) => v.checked)
  if (!arr.length) {
    uni.showToast({
      title: '请选择要观看的视频',
      icon: 'none'
    })
  } else {
    emits('stepChange', 2, { arr })
  }
}
</script>

<style scoped lang="scss">
.step1-wrapper {
  background-image: url('/static/micro_app/bg_head_video.png');
  background-repeat: no-repeat;
  background-size: 100%;
  background-color: #f2f7ff;
  padding: px2vw(80) px2vw(48);
  .desc {
    font-size: px2vw(22);
    line-height: px2vw(33);
  }
}
</style>
