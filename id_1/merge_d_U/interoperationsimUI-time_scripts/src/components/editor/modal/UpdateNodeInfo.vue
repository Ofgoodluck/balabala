<script setup>


import {reactive, ref} from "vue";
import {PlusOutlined} from "@ant-design/icons-vue";
const emit = defineEmits(['updateNodeInfoFinish'])
const props = defineProps({
  nodeId:String,
  existedEquipments:Array,
  existedApplist:Array
})

let existedEquipments = props.existedEquipments
let nodeId= props.nodeId
let existedApplist = props.existedApplist


let nodeInfoState = reactive({
  nodeId:'',
  role: '',
  applist:[],
  equipments:[],
  allEquipments:[]
});

let toAddEquipmentsShow = ref(false)



function initForm(){
  // nodeInfoState.edgeId=props.nodeId
  // nodeInfoState.allEquipments = props.existedEquipments
  nodeInfoState.role= ''
  nodeInfoState.equipments= []
  nodeInfoState.applist=[]
}

const onFinish = values=>{
  nodeInfoState.nodeId  = nodeId
  nodeInfoState.allEquipments = existedEquipments
  console.log("finish"+JSON.stringify(values)+"  "+JSON.stringify(nodeInfoState))
  emit('updateNodeInfoFinish',nodeInfoState)
  initForm()
}
const onFinishFailed = errorInfo => {
  console.log('Failed:', errorInfo);
};





</script>

<template>


  <div>

    <a-form
        :model="nodeInfoState"
        name="basic"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 10 }"
        autocomplete="off"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
    >



      <a-form-item
          label="角色"
          name="role"
          :rules="[{ required: false, message: '请输入节点角色定义' }]"
      >
        <a-input v-model:value="nodeInfoState.role"/>
      </a-form-item>



      <a-form-item label="装备配置" >

        <a-checkbox-group  v-model:value="nodeInfoState.equipments">
          <a-checkbox v-for="item in existedEquipments" :value='item.name' >
            {{item.name}}
          </a-checkbox>
        </a-checkbox-group>

      </a-form-item>


      <a-form-item label="应用配置" >

        <a-checkbox-group  v-model:value="nodeInfoState.applist">
          <a-checkbox v-for="item in existedApplist" :value='item.name' >
            {{item.name}}
          </a-checkbox>
        </a-checkbox-group>

      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 16, span: 16 }">
        <a-button type="primary" html-type="submit">确定设置</a-button>
      </a-form-item>

    </a-form>

  </div>
</template>

<style scoped>

</style>