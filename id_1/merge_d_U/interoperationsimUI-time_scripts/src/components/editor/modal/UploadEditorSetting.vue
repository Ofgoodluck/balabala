<script setup>

import {onMounted, ref} from "vue";
import {UploadOutlined} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue';
import {getFileContent} from "@/components/Util/SimulationDescriptionFileUtil";
import {
  simulationEquipmentParser,
  simulationAppParser,
  simulationTaskNameParser
} from "@/components/Util/SimulationSettingParser"
import {useRouter} from "vue-router";


let router =null
onMounted(()=>{
  router  = useRouter()
})
let simulationTaskName=ref('')
let allEquipmentResult={}
let allApplistResult={}
let equipmentInfo = null
let applistInfo =null
const handleEquipmentChange = async info => {
  console.log(JSON.stringify(info))
  equipmentInfo =info
  let content = await getFileContent(info.file.originFileObj)
  let name = info.file.name.split('.')[0]
  allEquipmentResult[name]={
    name:name,
    content:content
  }
};

const handleApplistChange = async info => {
  console.log(JSON.stringify(info))
  applistInfo =info
  let content = await getFileContent(info.file.originFileObj)
  let name = info.file.name.split('.')[0]
  allApplistResult[name]={
    name:name,
    content:content
  }
};

function uploadCommit(){
  simulationTaskNameParser(simulationTaskName)

  let equipmentNameSet = Object.keys(allEquipmentResult)
  equipmentInfo.fileList.forEach((item)=>{
    let projectName = item.name.split('.')[0]
    if (equipmentNameSet.includes(projectName)){
      simulationEquipmentParser(allEquipmentResult[projectName].content)
    }
  })

  let applistNameSet = Object.keys(allApplistResult)
  applistInfo.fileList.forEach((item)=>{
    let projectName = item.name.split('.')[0]
    if (applistNameSet.includes(projectName)){
      simulationAppParser(allApplistResult[projectName].content)
    }
  })

  router.push('./editor')

}



</script>

<template>

  <div>
    <a-typography style="width: 100%; margin-bottom: 10px">请设置测试任务名称：</a-typography>
    <a-input class="input" v-model:value="simulationTaskName" placeholder="请输入测试任务名称" style="width: 100%; margin-bottom: 10px" block></a-input>
  </div>

  <div class="button-container" >




    <a-typography>请选择参试装备库</a-typography>

    <a-upload
        :show-upload-list = 'true'
        accept=".json"
        name="file"
        @change="handleEquipmentChange"
    >
      <a-button type="dashed">
        <upload-outlined></upload-outlined>
        选择
      </a-button>
    </a-upload>

  </div>

  <div class="button-container" >

    <a-typography>请选择参试应用库</a-typography>

    <a-upload
        :show-upload-list = 'true'
        accept=".json"
        name="file"
        @change="handleApplistChange"
    >
      <a-button type="dashed">
        <upload-outlined></upload-outlined>
        选择
      </a-button>
    </a-upload>

  </div>

  <div class="right">
    <a-button type="primary" @click="uploadCommit">新建</a-button>
  </div>



</template>


<style scoped>
.right {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;

}
.button-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  margin-top: 20px;
}
</style>