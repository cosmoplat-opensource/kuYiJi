<template>
  <input
    v-model.trim="newValue"
    :placeholder-style="$attrs['placeholder-style']"
    :placeholder="$attrs.placeholder"
    :focus="$attrs.focus"
    @input="handleInput"
  />
</template>

<script setup>
import { computed } from 'vue'

const emits = defineEmits(['input', 'update:modelValue'])

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const newValue = computed({
  get: function () {
    return this.modelValue
  },
  set: function (value) {
    emits('update:modelValue', value)
  }
})

function handleInput(e) {
  emits('input', e)
}
</script>

<style lang="scss"></style>
