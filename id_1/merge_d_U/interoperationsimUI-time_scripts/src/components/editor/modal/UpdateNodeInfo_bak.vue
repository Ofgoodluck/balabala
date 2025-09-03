<script setup>


import {reactive, ref} from "vue";
import {PlusOutlined} from "@ant-design/icons-vue";
const emit = defineEmits(['updateNodeInfoFinish','addEquipmentFinish'])
const props = defineProps({
  nodeId:String,
  existedEquipments:Array
})

let existedEquipments = props.existedEquipments
let nodeId= props.nodeId


let nodeInfoState = reactive({
  nodeId:'',
  role: '',
  equipments:[],
  allEquipments:[]
});

let toAddEquipmentsShow = ref(false)
let toAddEquipments = reactive({
  name:'',
  softwareIndex:'',
  softwarePath:'',
  descriptionPath:''
})
function  addEquipment (){
  existedEquipments.push({
    name:toAddEquipments.name,
    softwareIndex:toAddEquipments.softwareIndex,
    softwarePath:toAddEquipments.softwarePath,
    descriptionPath:toAddEquipments.descriptionPath
  })
  console.log("now"+JSON.stringify(existedEquipments))
  toAddEquipmentsShow.value = false
  toAddEquipments.name = ''
  toAddEquipments.softwareIndex=''
  toAddEquipments.softwarePath = ''
  toAddEquipments.descriptionPath = ''
}

function initForm(){
  // nodeInfoState.edgeId=props.nodeId
  // nodeInfoState.allEquipments = props.existedEquipments
  nodeInfoState.role= ''
  nodeInfoState.equipments= []
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

function addNewEquipmentBtnClick(){
    toAddEquipmentsShow.value=!(toAddEquipmentsShow.value)
}



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

      <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
        <a-button type="dashed" block @click="addNewEquipmentBtnClick">
          <PlusOutlined />
          添加新装备
        </a-button>
      </a-form-item>

      <a-form
          v-show="toAddEquipmentsShow"
          :model="toAddEquipments"
          name="basic"
          :label-col="{ span: 10 }"
          :wrapper-col="{ span: 12 }"
          autocomplete="off"
      >
        <a-form-item
            label="装备名称"
            :rules="[{ required: false, message: '请输入装备名称' }]"
        >
          <a-input v-model:value="toAddEquipments.name"/>
        </a-form-item>

        <a-form-item
            label="装备软件目录"
            :rules="[{ required: false, message: '请输入装备软件目录' }]"
        >
          <a-input v-model:value="toAddEquipments.softwareIndex"/>
        </a-form-item>


        <a-form-item
            label="装备数据发生器软件路径"
            :rules="[{ required: false, message: '请输入装备数据发生器软件路径' }]"
        >
          <a-input v-model:value="toAddEquipments.softwarePath"/>
        </a-form-item>

        <a-form-item
            label="装备资源子树描述文件路径"
            :rules="[{ required: false, message: '请输入装备资源子树描述文件路径' }]"
        >
          <a-input v-model:value="toAddEquipments.descriptionPath"/>
        </a-form-item>


        <a-form-item :wrapper-col="{ offset: 16, span: 16 }">
          <a-button @click = 'addEquipment'>添加装备</a-button>
        </a-form-item>

      </a-form>

      <a-form-item :wrapper-col="{ offset: 16, span: 16 }">
        <a-button type="primary" html-type="submit">确定设置</a-button>
      </a-form-item>

    </a-form>

  </div>
</template>

<style scoped>

</style>