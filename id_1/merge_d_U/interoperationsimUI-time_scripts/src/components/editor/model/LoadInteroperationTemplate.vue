<template>
  <a-tabs v-model:active-key="loadMode">
    <a-tab-pane key="old" tab="选取已有文件">
      <a-upload :before-upload="beforeUploadHandle" :max-count="1">
        <a-button>
          <FileTextOutlined/>
        </a-button>
      </a-upload>
    </a-tab-pane>

    <a-tab-pane key="new" tab="新建文件">
      <a-space direction="vertical">
        <a-textarea v-model:value="interoperationTemplateContent" placeholder="example" style="width: 100%"/>
        <a-space align="center" size="large">
          <a-button content="清空文本内容" danger @click="editClearHandle">清空</a-button>
          <a-button content="保存文本内容并配置到装备" @click="editSaveHandle">保存</a-button>
        </a-space>
      </a-space>
    </a-tab-pane>
  </a-tabs>

  <a-divider style="height: 2px; background-color: #05b390" dashed/>

  <a-button type="primary" html-type="submit" @click="onSubmit">提交</a-button>
</template>

<script>

import {useEditInfoStore} from "@/stores/EditInfoStore";
import {FileTextOutlined} from "@ant-design/icons-vue"
import {saveAs} from "file-saver";

export default {
  name: "LoadInteroperationTemplate",
  components: {FileTextOutlined},
  data() {
    return {
      editInfoStore: useEditInfoStore(),
      loadMode: 'old',
      interoperationTemplateContent: '',

      interoperationTemplate: {
        name: '',
        path: ''
      }
    }
  },
  methods: {
    beforeUploadHandle(file) {
      file.text().then(res => {
        const fileData = JSON.parse(res.toString())
        this.interoperationTemplate.name = fileData['模板名称']
        this.interoperationTemplate.path = file.path
      })
      return false
    },
    editClearHandle() {
      this.interoperationTemplateContent = ''
    },
    editSaveHandle() {
      console.log(this.interoperationTemplateContent)
      // if (this.equipment.name === '') {
      //   return false
      // }
      // this.equipment.treeFile.path = this.equipment.name + '_tree.json'
      // console.log(this.equipment.treeFile.path)
      // const blob = new Blob([this.equipment.treeFile.data], {type: "text/plain;charset=utf-8"});
      // saveAs(blob, this.equipment.treeFile.path)
    },
    onSubmit() {
      this.editInfoStore.loadInteroperationTemplate(this.interoperationTemplate)
    }
  }
}
</script>

<style scoped>

</style>