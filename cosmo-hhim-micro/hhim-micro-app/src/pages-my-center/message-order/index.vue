<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header title="短信订阅" />
    <view class="flex-1">
      <view class="rounded-16 bg-fff mx-16 p-32 box">
        <view class="flex-ac justify-between mb-24">
          <view class="bg-f3f3f5 font-24 color-5a6f82 px-4 h-32 flex-center rounded-8">生产周报</view>
          <view class="font-24 color-0066ff" @tap="show = !show">{{ show ? '收起' : '预览' }}</view>
        </view>
        <!-- 预览短信文本 -->
        <view v-if="show" class="bg-f7f8e7 p-16 box font-28 color-333 line-height-42"
          >【生产周报】您企业上一周的生产周报已统计完成，上周共生产 3 款产品，已完工确认 128，总完工待确认
          32，生产平均良品率 96.8%，上周共 8 名员工记工，本周记工总数 688，未审核记工总数 316，审核及时率
          90%【KU易记】</view
        >
        <view v-else class="line w-100"></view>
        <!-- 通知人员(2/3) -->
        <view class="flex-ac justify-between mt-32">
          <view class="font-24 color-333">通知人员({{ personList.length }}/3)</view>
          <image
            v-if="personList.length < 3"
            :src="formatImage('icon_add_0066ff', 'svg')"
            class="icon-48"
            @tap="addPerson"
          ></image>
        </view>
        <view class="flex-ac flex-wrap">
          <view
            v-for="item in personList"
            :key="item.id"
            class="flex-ac rounded-8 mr-16 box bg-f3f3f5 font-28 color-333 h-48 flex-ac px-8 mt-16"
            ><text>{{ item.nickName }}</text
            ><image :src="formatImage('icon_del_32_ff0000')" class="icon-32 ml-16" @tap="delPerson(item)"></image></view
        ></view>
      </view>
    </view>
    <view class="py-32 box flex justify-center font-28">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button width="296" height="72" text="保存" class="ml-32" @tap.stop="submit" />
    </view>
    <input-staff ref="selectStaff" title="选择通知人员" @staffSelected="staffSelected" />
  </view>
</template>
<script setup lang="ts">
import { _get, _put } from '@/utils/common-request'
import { ref } from 'vue'
import { formatImage } from '@/utils/common'
import InputStaff from '@/pages-my-center/components/input-staff.vue'
import { onLoad } from '@dcloudio/uni-app'
onLoad(() => {
  getConfig()
})
const show = ref(false)
const personList = ref([])
const selectStaff = ref(null)
function addPerson() {
  selectStaff.value.open(personList.value)
}
function staffSelected(itemSelectList) {
  personList.value = [...itemSelectList]
}
function delPerson(item) {
  const index = personList.value.findIndex((itemSelect) => itemSelect.id === item.id)
  personList.value.splice(index, 1)
}
const configInfo = ref({})
function getConfig() {
  let mockParams = {
    url: '/notice/config/detail',
    data: {
      businessSign: '20'
    }
  }

  _get(mockParams).then((res) => {
    configInfo.value = res.data || {}
    personList.value = (res.data && res.data.noticeUserList) || []
  })
}
function submit() {
  let idList = []
  personList.value.forEach((item) => {
    idList.push(item.id)
  })
  let mockParams = {
    url: '/notice/config',
    data: {
      businessSign: 'PRODUCE_WEEK_REPORT',
      noticeUserIdList: idList,
      noticeChannelList: ['SMS']
    }
  }

  _put(mockParams).then((res) => {
    uni.showToast({
      title: '提交成功',
      icon: 'none',
      duration: 1500
    })
    setTimeout(() => {
      cancel()
    }, 1500)
  })
}
function cancel() {
  uni.navigateBack()
}
</script>
<style lang="scss" scoped>
.line {
  height: px2vw(2);
  background: #f5f5f5;
}
.line-height-42 {
  line-height: px2vw(42);
}
</style>
