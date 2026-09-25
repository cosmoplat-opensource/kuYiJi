<template>
  <date-time :date-time="dayjs().format('YYYY-MM-DD HH:mm:ss')" />
  <default-answer showIcon text="您好，海云客服为您服务！" />
  <default-answer :text="adList[randomIndex]" />
  <common-problem />
</template>

<script setup lang="ts">
import DateTime from './date-time.vue'
import DefaultAnswer from './default-answer.vue'
import CommonProblem from './common-problem.vue'
import dayjs from 'dayjs'

const adList = [
  '在制品库存清晰明了，生产调度有依据',
  '员工线上实时记工，告别线下纸质单据',
  '生产数据实时汇总，数据统计不再费时费力',
  '不良品管理闭环，质量提升有抓手'
]
// 随机数
const randomIndex = Math.floor(Math.random() * 4)
</script>
