<template>
  <view
    class="icon-32 flex-center box rounded-8 border-2 border-0066ff"
    @tap.stop="handleChecked"
    :class="checked ? 'bg-0066ff' : 'bg-fff'"
  >
    <image src="/static/images/icon_checked_20.svg" class="icon-20" v-if="checked" />
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
