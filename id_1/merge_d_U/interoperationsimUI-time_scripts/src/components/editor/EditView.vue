<template>
  <pre style="height: 100%">{{ JSON.stringify(editInfoStore.format(), null, 2) }}</pre>
</template>

<script>
import {useEditInfoStore} from "@/stores/EditInfoStore";

export default {
  name: "EditView",
  data() {
    return {
      editInfoStore: useEditInfoStore(),
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
