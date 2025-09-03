<template>
  <div class="condition-container" style="width: 100%; height: 100%">
    <div v-show="countStore.count === 0" style="width: 100%; height: 100%">
      <EditTestName/>
    </div>
    <div v-show="countStore.count === 1" style="width: 100%; height: 100%">
      <LoadEquipmentLib/>
    </div>
    <div v-show="countStore.count === 2" style="width: 100%; height: 100%">
      <LoadApplicationLib/>
    </div>
    <div v-show="countStore.count === 3" style="width: 100%; height: 100%">
      <EditNodes/>
    </div>
    <div v-show="countStore.count === 4" style="width: 100%; height: 100%">
      <LoadInteroperationTemplate/>
    </div>
    <div v-show="countStore.count === 5" style="width: 100%; height: 100%">
      <LoadEvaluateSoftware/>
    </div>
  </div>
</template>

<script>
import {useEditStepsCountStore} from "@/stores/EditInfoStore";
import EditTestName from "@/components/editor/model/EditTestName.vue";
import LoadEquipmentLib from "@/components/editor/model/LoadEquipmentLib.vue";
import LoadApplicationLib from "@/components/editor/model/LoadApplicationLib.vue";
import LoadInteroperationTemplate from "@/components/editor/model/LoadInteroperationTemplate.vue";
import EditNodes from "@/components/editor/model/EditNodes.vue";
import LoadEvaluateSoftware from "@/components/editor/model/LoadEvaluateSoftware.vue";

export default {
  name: 'EditContent',
  components: {
    EditNodes,
    LoadInteroperationTemplate,
    LoadApplicationLib,
    LoadEquipmentLib,
    EditTestName,
    LoadEvaluateSoftware
  },
  data() {
    return {
      countStore: useEditStepsCountStore()
    }
  }
}

</script>

<style scoped>
.condition-container {
  align-items: flex-start;
}
</style>
