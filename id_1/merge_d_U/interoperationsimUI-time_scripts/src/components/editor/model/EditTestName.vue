<template>
  <a-form
      :model="editInfoStore"
  >
    <a-form-item
        label="测试任务名称"
        name="testTaskName"
        style="width: 75%"
        :rules="[{ required: true, message: '请输入本次测试任务的名称' }]"
    >
      <a-input v-model:value="editInfoStore.testTaskName"/>
    </a-form-item>

  </a-form>
</template>

<script>
import {useEditInfoStore} from "@/stores/EditInfoStore";
export default {
  data() {
    return {
      editInfoStore: useEditInfoStore()
    }
  },
  methods: {
    // submit() {
    //   console.log(this.editInfoStore.testTaskName)
    // }
  }
}



</script>

