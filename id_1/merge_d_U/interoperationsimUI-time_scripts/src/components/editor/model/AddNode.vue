<script setup>


import {reactive, ref} from "vue";
import {useEditInfoStore} from "@/stores/EditInfoStore";
import {PlusOutlined, MinusCircleOutlined} from "@ant-design/icons-vue"

const emit = defineEmits(['addNodeFinish'])
const editInfoStore = useEditInfoStore()
const formState = reactive({
  node_id: '',
  node_name: '',
  node_ip: '',
  node_computationSetting: 0,
  node_memorySetting: 0,
  memorySettingUnit: 'm',
  equipments: [],
  applications: [],
  role: ''
});
const onFinish = values => {

  let commitData = {
    id: values.node_id, // Generate the unique id
    label: values.node_name,
    node_name: values.node_name,
    node_ip: values.node_ip,
    node_computationSetting: values.node_computationSetting,
    node_memorySetting: values.node_memorySetting.toString() + formState.memorySettingUnit,
    node_equipmentsSetting: formState.equipments.slice(0),
    node_applicationsSetting: formState.applications.slice(0),
    node_role: formState.role
  };

  console.log("finish" + commitData)
  emit('addNodeFinish', commitData)
  editInfoStore.loadNode(formState)
  formState.node_id = ''
  formState.node_ip = ''
  formState.node_name = ''
  formState.node_memorySetting = ''
  formState.node_computationSetting = 0
}
const onFinishFailed = errorInfo => {
  console.log('Failed:', errorInfo);
};

const addEquipment = () => {
  formState.equipments.push({
    name: '',
    args: '',
    id: Date.now()
  })
}

const removeEquipment = (item) => {
  const index = formState.equipments.indexOf(item)
  if (index !== -1) {
    formState.equipments.splice(index, 1);
  }
}

const addApplication = () => {
  formState.applications.push({
    name: '',
    args: '',
    id: Date.now()
  })
}

const removeApplication = (item) => {
  const index = formState.applications.indexOf(item)
  if (index !== -1) {
    formState.applications.splice(index, 1);
  }
}

</script>

<template>
  <div>

    <a-form
        :model="formState"
        name="node-from"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 10 }"
        autocomplete="off"
        @finish="onFinish"
        @finishFailed="onFinishFailed"

    >
      <a-form-item
          label="平台ID"
          name="node_id"
          :rules="[{ required: true, message: '请输入平台ID: 仅接受字母数字和下划线', pattern: new RegExp(
          /^[a-zA-Z0-9_]*$/
        ), }]"
      >
        <a-input v-model:value="formState.node_id" placeholder="d1"/>
      </a-form-item>

      <a-form-item
          label="平台名称"
          name="node_name"
          :rules="[{ required: true, message: '请输入平台名称' }]"
      >
        <a-input v-model:value="formState.node_name" placeholder="平台1"/>
      </a-form-item>

      <a-form-item
          label="平台IP"
          name="node_ip"
          :rules="[{ required: true, message: '请输入平台IP: 不是有效的IPV4地址', pattern: new RegExp(
          /^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$/
        ) }]"
      >
        <a-input v-model:value="formState.node_ip" placeholder="10.0.0.251"/>
      </a-form-item>


      <a-form-item
          label="计算资源配额"
          name="node_computationSetting"
          :rules="[{ required: true, message: '请输入计算资源配额' }]"
      >
        <a-input-number :step="0.1" v-model:value="formState.node_computationSetting"/>
      </a-form-item>

      <a-form-item
          label="内存资源配额"
          name="node_memorySetting"
          :rules="[{ required: true, message: '请输入内存资源配额' }]"
      >
        <a-input-number v-model:value="formState.node_memorySetting">

          <template #addonAfter>
            <a-select v-model:value="formState.memorySettingUnit">
              <a-select-option value="b">b</a-select-option>
              <a-select-option value="k">k</a-select-option>
              <a-select-option value="m">m</a-select-option>
              <a-select-option value="g">g</a-select-option>
            </a-select>
          </template>

        </a-input-number>
      </a-form-item>

      <a-form-item
          label="装备配置"
          name="node_equipmentsConfig"
      >
        <a-space v-for="(equipment, index) in formState.equipments" :key="equipment.id"
                 style="display: flex; width: 100%" align="baseline" direction="horizontal"
        >
          <a-form-item
              :name="['equipments', index, 'name']"
              :rules="{
          required: true,
          message: '请从装备库中选择对应的装备',
        }"
          >
            <a-select v-model:value="equipment.name" style="width: 100px">
              <a-select-option v-for="equip in editInfoStore.equipmentsName" :value="equip">{{ equip }}</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item
              :name="['equipments', index, 'args']"
          >
            <a-input
                v-model:value="equipment.args"
                placeholder="--config ..."
                style="width: 100px"
            />
          </a-form-item>
          <MinusCircleOutlined @click="removeEquipment(equipment)"/>
        </a-space>
        <a-form-item>
          <a-button type="dashed" block @click="addEquipment">
            <PlusOutlined/>
            添加装备
          </a-button>
        </a-form-item>
      </a-form-item>

      <a-form-item
          label="应用配置"
          name="node_applicationsConfig"
      >
        <a-space v-for="(application, index) in formState.applications" :key="application.id"
                 style="display: flex; width: 100%" align="baseline" direction="horizontal"
        >
          <a-form-item
              :name="['applications', index, 'name']"
              :rules="{
          required: true,
          message: '请从应用库中选择对应的应用',
        }"
          >
            <a-select v-model:value="application.name" style="width: 100px">
              <a-select-option v-for="app in editInfoStore.applicationsName" :value="app">{{ app }}</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item
              :name="['applications', index, 'args']"
          >
            <a-input
                v-model:value="application.args"
                placeholder="--config ..."
                style="width: 100px"
            />
          </a-form-item>
          <MinusCircleOutlined @click="removeApplication(application)"/>
        </a-space>
        <a-form-item>
          <a-button type="dashed" block @click="addApplication">
            <PlusOutlined/>
            添加应用
          </a-button>
        </a-form-item>
      </a-form-item>

      <a-form-item
          label="互操作角色配置"
          name="role"
          :rules="{
          required: true,
          message: '请为当前平台选定角色',
      }">
        <a-input v-model:value="formState.role" placeholder="主动雷达平台"/>
      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
        <a-button type="primary" html-type="submit">确认</a-button>
      </a-form-item>

    </a-form>

  </div>
</template>

<style scoped>

</style>
