<script setup>

import {onMounted, ref} from "vue";
import {UploadOutlined} from '@ant-design/icons-vue'

import {getFileContent,readSimulationDescriptionFile} from "@/components/Util/SimulationDescriptionFileUtil";
import {useRouter} from "vue-router";


let router =null
onMounted(()=>{
  router  = useRouter()
})
let allResult={}
let finalInfo = null
const handleChange = async info => {
  console.log(JSON.stringify(info))
  finalInfo =info
  let content = await getFileContent(info.file.originFileObj)
   let name = info.file.name.split('.')[0]
  allResult[name]={
    name:name,
    content:content
  }
};

function uploadCommit(){
  let nameSet = Object.keys(allResult)
  finalInfo.fileList.forEach((item)=>{
    let projectName = item.name.split('.')[0]
    if (nameSet.includes(projectName)){
      readSimulationDescriptionFile(allResult[projectName].content,projectName)
      window.electronAPI.sendStartSimulationMessage(allResult[projectName].content)
    }
  })
  router.push('/info')
}



</script>

<template>


  <div class="button-container" >

    <a-typography>请选择需要打开的推演工程:</a-typography>

    <a-upload
        :show-upload-list = 'true'
        accept=".json"
        name="file"
        @change="handleChange"
    >
      <a-button type="dashed" style="width: 110px">
        <upload-outlined></upload-outlined>
        选择
      </a-button>
    </a-upload>

  </div>


  <div class="right">
    <a-button type="primary" @click="uploadCommit">确定</a-button>
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
  margin-top: 30px;
}
</style>
