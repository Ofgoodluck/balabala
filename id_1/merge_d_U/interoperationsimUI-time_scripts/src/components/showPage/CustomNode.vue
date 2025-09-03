<script setup>
import {Handle} from '@vue-flow/core'

const props = defineProps({
  handles: {
    type: Array,
    required: true,
  },
  label: {
    type: String,
    required: true
  }
})

</script>

<template>
  <div
      style="display: flex; flex-direction: row; flex-wrap: wrap; justify-content: center; max-width: 90%; margin: auto; gap: 3px"
  >
    <div>{{ props.label }}</div>
  </div>
  <template v-for="handle in props.handles" :key="handle.id">
    <Handle :id="handle.id" :type="handle.type" :position="handle.position"></Handle>
  </template>
</template>
