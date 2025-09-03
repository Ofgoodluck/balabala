<script setup>

import {ref} from "vue";
import {UploadOutlined} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue';
import {getFileContent} from "@/components/Util/SimulationDescriptionFileUtil";

const emit = defineEmits(['UploadReady'])

let resultSoftwarePath = ref('')

let interoperateTemplate = ref('')

let templateName = ref('')
let templatePath = ref('')



const handleChange = async info => {
  let a = await getFileContent(info.file.originFileObj)
  console.log("file" + a)
  interoperateTemplate.value = a

};

function uploadCommit(){
  emit("UploadReady",[resultSoftwarePath.value,templateName.value,templatePath.value])
  resultSoftwarePath.value=''
  interoperateTemplate.value=''
}



</script>

<template>



  <div  >
    <a-typography class="input" >互操作结果评价软件路径:</a-typography>
    <a-input class="input" v-model:value="resultSoftwarePath" placeholder="请输入软件路径" style="width: 100%" block></a-input>
  </div>

  <div  >

    <a-typography >互操作模板定义:</a-typography>

<!--    <a-upload-->
<!--        :show-upload-list = 'false'-->
<!--        name="file"-->
<!--        @change="handleChange"-->
<!--    >-->
<!--      <a-button type="dashed">-->
<!--        <upload-outlined></upload-outlined>-->
<!--        选择-->
<!--      </a-button>-->
<!--    </a-upload>-->

    <div class="button-container">
      <a-typography >名称:</a-typography>
      <a-input v-model:value="templateName" placeholder="请输入模板名称" style="width: 90% " block></a-input>
    </div>

    <div class="button-container">
      <a-typography >路径:</a-typography>
      <a-input v-model:value="templatePath" placeholder="请输入模板路径" style="width: 90%" block></a-input>
    </div>



  </div>



<!--  <a-textarea v-model:value="interoperateTemplate" placeholder="请输入互操作模板内容" :rows="4" />-->


  <div class="right">
    <a-button type="primary" @click="uploadCommit">确定</a-button>
  </div>



</template>


<style scoped>
.input{
  width: 100%;
  margin-bottom: 20px;
}

.right {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;


}
.button-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  margin-top: 20px;
  padding-left: 10px;
}
</style>