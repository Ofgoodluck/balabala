<template>
  <a-form
      :model="formState"
  >
    <a-form-item
        label="测试评价软件路径指定"
        name="path"
        :rules="[{ required: true, message: '请为当前仿真测试指定任务评估软件' }]"
    >
      <a-upload :before-upload="beforeUploadHandle" :max-count="1">
        <a-button>
          <FileTextOutlined/>
        </a-button>
      </a-upload>
    </a-form-item>

    <a-form-item
        label="测试评价软件参数配置"
        name="config"
        :rules="[{ required: false, message: '请为当前软件配置启动参数' }]"
    >
      <a-input v-model:value="formState.config"/>
    </a-form-item>
  </a-form>

  <a-divider style="height: 2px; background-color: #05b390" dashed/>

  <a-button type="primary" html-type="submit" @click="onSubmit">提交</a-button>
</template>

<script>
import {useEditInfoStore} from "@/stores/EditInfoStore";
import {FileTextOutlined} from "@ant-design/icons-vue"
export default {
  name: "LoadEvaluateSoftware",
  components: {FileTextOutlined},
  data() {
    return {
      editInfoStore: useEditInfoStore(),
      formState: {
        path: '',
        config: ''
      }
    }
  },
  methods: {
    beforeUploadHandle(file) {
      this.formState.path = file.path
      console.log(this.formState.path)
      return false
    },
    onSubmit() {
      this.editInfoStore.loadEvaluateSoftware(this.formState)
    }
  }
}
</script>

<style scoped>

</style>