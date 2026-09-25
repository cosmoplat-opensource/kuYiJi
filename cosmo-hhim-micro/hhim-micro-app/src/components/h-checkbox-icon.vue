<template>
  <view class="h-checkbox-icon" @tap="handleChecked">
    <image src="/static/images/icon_well_ed_chosed.svg" class="icon-32" v-if="checked" />
    <image src="/static/images/icon_well_ed.svg" class="icon-32" v-else />
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  checked: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['checkedChange'])

function handleChecked() {
  emits('checkedChange', !props.checked)
}
</script>

<style lang="scss">
.h-checkbox-icon {
  height: px2vw(32);
}
</style>
