<template>
  <div class="mainPage">
    <EditSteps style="height: 600px; width: 20%"/>
    <a-divider type="vertical" style="height: 600px; width: 6px; border-color: #05b390" />
    <EditView style="height: 600px; width: 35%"/>
    <a-divider type="vertical" style="height: 600px; width: 6px; border-color: #05b390" />
    <EditContent style="height: 600px; width: 45%"/>
  </div>
<!--  <a-button type="primary" danger @click="resetCount">重新创建</a-button>-->
</template>
<style scoped>
.mainPage {
  display: flex;
  flex-direction: row;
  gap: 10px;
}
</style>


<script>
import EditSteps from "@/components/editor/EditSteps.vue";
import EditContent from "@/components/editor/EditContent.vue";
import {useEditStepsCountStore} from "@/stores/EditInfoStore";
import EditView from "@/components/editor/EditView.vue";

export default {
  components: {EditView, EditSteps, EditContent},
  data() {
    return {
      countStore: useEditStepsCountStore()
    }
  },
  methods: {
    resetCount() {
      this.countStore.$reset()
    }
  }
}


</script>

