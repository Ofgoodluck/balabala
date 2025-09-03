<template>
  <div class="welcome" style="width: 100%; height: 100%">
    <div class="content">
      <h1>测试验证与指标评估应用</h1>
      <br>

      <div class="button-between">
        <a-button class="page-button" type="primary" @click="editorBtnClick">场景想定编辑与方案生成</a-button>
        <a-button class="page-button" type="primary" @click="infoBtnClick">方案推演加载与结果展示</a-button>
      </div>

    </div>

  </div>

  <a-modal :footer="null" v-model:open=openProjectModalOpen>
    <OpenProjects></OpenProjects>
  </a-modal>

  <a-modal :footer="null" width="90%" v-model:open=uploadSimFileOpen>
    <EditPage></EditPage>
  </a-modal>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {useRouter} from "vue-router";
import OpenProjects from "@/components/editor/modal/OpenProjects.vue";
import EditPage from "@/components/editor/EditPage.vue";

let openProjectModalOpen = ref(false)
let uploadSimFileOpen = ref(false)
let nextBtn = ref(false)
let router = null
onMounted(()=>{
  nextBtn.value=false
  router = useRouter()

})

function editorBtnClick(){
  // router.push('./editor')
  uploadSimFileOpen.value=true
}

function infoBtnClick(){
  openProjectModalOpen.value =true
}


</script>

<style scoped>
.welcome {
  background-image: url('@/assets/back_blue.JPG');
  background-size: 100% 100%;
  background-repeat: repeat;
}

h1 {
  font-size: 48px;
  margin-bottom: 16px;
  color: white;
  font-weight: bold;
  text-shadow: 3px 3px 6px #888888;
}

.content {
  position: absolute;
  top: 30%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.button-between {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  margin-top: 20px;
}
.page-button{
  width: 200px;
  height: 50px;
}
</style>
