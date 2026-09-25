<template>
  <h-button
    type="border-1 border-0066ff color-0066ff bg-fff"
    font="font-24"
    height="48"
    width="80"
    text="导出"
    @tap="handleClick"
  />
</template>

<script setup lang="ts">
const emits = defineEmits(['exportToMail'])

function handleClick() {
  emits('exportToMail')
}
</script>
