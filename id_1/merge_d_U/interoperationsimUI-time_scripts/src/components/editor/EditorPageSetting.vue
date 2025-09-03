<template>
  <div>
    <a-transfer
        v-model:target-keys="targetKeys"
        v-model:selected-keys="selectedKeys"
        :data-source="mockData"
        select-all-labels="  "
        :titles="[' 待选装备库', '参试装备']"
        :render="item => item.title"
        @change="handleChange"
        @selectChange="handleSelectChange"
        @scroll="handleScroll"
    />

  </div>
</template>
<script setup>
import {reactive, ref} from 'vue';

let equipmentSet = reactive([
  {
    name:'radarA',
    softwarePath:'/usr/bin/radarA.so',
    descriptionPath:'/etc/radarA.json'
  },
  {
    name:'radarB',
    softwarePath:'/usr/bin/radarB.so',
    descriptionPath:'/etc/radarB.json'
  },
  {
    name:'radarC',
    softwarePath:'/usr/bin/radarC.so',
    descriptionPath:'/etc/radarC.json'
  }
])

const mockData = [];

equipmentSet.forEach((item)=>{
  mockData.push({
    key: item.name,
    title:item.name
  })
})
// for (let i = 0; i < 20; i++) {
//   mockData.push({
//     key: i.toString(),
//     title: `content${i + 1}`,
//     description: `description of content${i + 1}`,
//   });
// }

const targetKeys = ref([]);
const selectedKeys = ref([]);
const handleChange = (nextTargetKeys, direction, moveKeys) => {
  console.log('targetKeys: ', nextTargetKeys);
  console.log('direction: ', direction);
  console.log('moveKeys: ', moveKeys);
};
const handleSelectChange = (sourceSelectedKeys, targetSelectedKeys) => {
  console.log('sourceSelectedKeys: ', sourceSelectedKeys);
  console.log('targetSelectedKeys: ', targetSelectedKeys);
};
const handleScroll = (direction, e) => {
  console.log('direction:', direction);
  console.log('target:', e.target);
};
</script>