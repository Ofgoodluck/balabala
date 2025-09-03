<template>
    <a-table
        ref="interActTable"
        :size="'small'"
        :style="{height: `${tableHeight}px`}"
        :columns="columns"
        :data-source="rows"
        :pagination="false"
        :scroll="{ y: tableHeight }"
        :bordered=true
    />

</template>
<script setup>
import {onMounted, onUnmounted, ref} from "vue";

let tableHeight = ref(0)
let interActTable = ref(null)
let rows = ref([])
let i = 0

function resizeTableScroll(){
  tableHeight.value = interActTable.value.$el.parentNode.offsetHeight
}
onMounted(()=>{
  resizeTableScroll()
  window.addEventListener('resize', resizeTableScroll);
  window.electronAPI.receiveCommandData(data => {
    let row={
      key: i,
      platform: data['执行平台'],
      action: data['指令发起服务名称'],
      time: data['下发时间'],
      content:data['指令内容'],
      delay:data['指令时延'],
      result:data['指令执行结果']
    }
    i++;
    rows.value.push(row);
  });
})

onUnmounted(()=>{
  window.removeEventListener('resize', resizeTableScroll);
})
const columns = [
  {
    title: '执行平台',
    dataIndex: 'platform',
    // width: 150,
  },
  {
    title: '指令发起服务名称',
    dataIndex: 'action',
    // width: 150,
  },
  {
    title: '下发时间',
    dataIndex: 'time',
    // width: 100,
  },
  {
    title: '指令内容',
    dataIndex: 'content',
  },
  {
    title: '指令时延',
    dataIndex: 'delay',
    // width: 150,
  },
  {
    title: '指令执行结果',
    dataIndex: 'result',
    // width: 150,
  },
];
</script>
