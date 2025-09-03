<template>
  <div>
    <a-steps :current="countStore.count" :items="steps" :direction="'vertical'"></a-steps>
    <div class="steps-action">
      <a-button
          v-if="countStore.count < steps.length - 1"
          type="primary"
          :disabled="nextDisabled"
          @click="nextStep"
      >下一步
      </a-button>
      <a-button
          v-if="countStore.count === steps.length - 1"
          type="primary"
          @click="onComplete"
      >完成
      </a-button>
      <a-button
          v-if="countStore.count > 0"
          style="margin-left: 8px"
          @click="prevStep"
      >上一步
      </a-button>
    </div>
  </div>
</template>
<script>

import {useEditStepsCountStore, useEditInfoStore} from "@/stores/EditInfoStore";

export default {
  data() {
    return {
      countStore: useEditStepsCountStore(),
      editInfoStore: useEditInfoStore(),
      nextDisabled: false,

      steps: [
        {
          key: '1',
          title: '填写测试名称',
        },
        {
          key: '2',
          title: '加载装备库',
        },
        {
          key: '3',
          title: '加载应用库',
        },
        {
          key: '4',
          title: '添加节点和链路',
        },
        {
          key: '5',
          title: '添加互操作模板',
        },
        {
          key: '6',
          title: '添加评估软件',
        }
      ]
    }
  },
  methods: {
    nextStep() {
      this.countStore.increment()
    },
    prevStep() {
      this.countStore.decrement()
    },
    onComplete() {
      const fileName = this.editInfoStore.testTaskName + '.json'
      const blob = new Blob([JSON.stringify(this.editInfoStore.format(), null, 2)], {type: "text/plain;charset=utf-8"});
      saveAs(blob, fileName)
    }
  }
}
</script>
<style scoped>

.steps-action {
  margin-top: 24px;
}

</style>