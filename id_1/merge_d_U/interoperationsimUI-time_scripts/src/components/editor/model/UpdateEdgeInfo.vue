<script setup>


import {reactive, ref} from "vue";
import {useEditInfoStore} from "@/stores/EditInfoStore";

const emit = defineEmits(['updateEdgeInfoFinish'])
const props = defineProps({
  edgeId:String,
  from:String,
  to:String
})
const editInfoStore = useEditInfoStore()
const formState = reactive({
  edgeId:'',
  bandWidth: 0,
  delay:0,
  lossRate:0
});

function initForm(){
  formState.edgeId=''
  formState.bandWidth= 0
  formState.delay= 0
  formState.lossRate= 0
}

const onFinish = values=>{
  values.edgeId  = props.edgeId
  console.log("finishUpdateEdgeInfo"+JSON.stringify(values))
  emit('updateEdgeInfoFinish',values)
  editInfoStore.loadLink(formState, props.from, props.to)
  initForm()
}
const onFinishFailed = errorInfo => {
  console.log('Failed:', errorInfo);
};

</script>

<template>


  <div>

    <a-form
        :model="formState"
        name="link-form"
        :label-col="{ span: 8 }"
        :wrapper-col="{ span: 10 }"
        autocomplete="off"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
    >

      <a-form-item
          label="带宽kb/s"
          name="bandWidth"
          :rules="[{ required: true, message: '请输入带宽' }]"
      >
        <a-input-number v-model:value="formState.bandWidth"/>
      </a-form-item>

      <a-form-item
          label="时延ms"
          name="delay"
          :rules="[{ required: true, message: '请输入时延' }]"
      >
        <a-input-number v-model:value="formState.delay"/>
      </a-form-item>

      <a-form-item
          label="丢包率%"
          name="lossRate"
          :rules="[{ required: true, message: '请输入丢包率' }]"
      >
        <a-input-number v-model:value="formState.lossRate" :min="1" :max="100"/>
      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
        <a-button type="primary" html-type="submit">确定</a-button>
      </a-form-item>

    </a-form>

  </div>
</template>

<style scoped>

</style>