<script setup>
import {onMounted, onUnmounted, ref} from "vue";

let tableHeight = ref(0)
let interActTable = ref(null)
let rows = ref([])
let i = 0

function resizeTableScroll() {
  tableHeight.value = interActTable.value.$el.parentNode.offsetHeight
  if (tableHeight.value === 0) tableHeight.value = 480
  else tableHeight.value = tableHeight.value - 10
}

function getCommandDataTypeFirst(str) {
  const match = str.match(/^\s*([^ ]+)/);
  if (match && match[1]) {
    return match[1];
  } else {
    return '';
  }
}

function getCommandDataTypeSecond(str) {
  const match = str.match(/^\s*[^ ]+ +([^ ]+)/);
  if (match && match[1]) {
    return match[1];
  } else {
    return '';
  }
}

function rowClass(record) {
  switch (record.type) {
    case 'RETRIEVE': {
      return 'retrieve'
    }
    case 'CREATE': {
      return 'create'
    }
    case 'UPDATE': {
      return 'update'
    }
    case 'SUBSCRIPTION': {
      return 'subscription'
    }
    case 'DELETE': {
      return 'delete'
    }
    case 'FINISHED': {
      return 'finished'
    }
  }
}

function scrollToRow(index) {
  const tableElement = interActTable.value.$el; // 获取表格的根元素
  const rowElement = tableElement.querySelector(`.ant-table-row[data-row-key="${index}"]`); // 获取指定行的元素
  if (rowElement) {
    rowElement.scrollIntoView({behavior: 'smooth', block: 'nearest'}); // 使用 scrollIntoView 方法滚动到指定行
  }
}

onMounted(() => {
  resizeTableScroll()
  window.addEventListener('resize', resizeTableScroll);
  window.electronAPI.receiveTemplatesCfg(data => {
    for (let command of data) {
      let row = {
        key: i,
        time: command['仿真时刻'],
        plat: command['执行平台'],
        action: command['指令内容'],
        type: ''
      }
      i++;
      rows.value.push(row);
    }
  });

  window.electronAPI.receiveCommandData(datas => {
    for (let data of datas) {
      let command = data['指令内容'] + ';';
      let plat = data['执行平台'];
      let find = rows.value.find(obj => obj.plat === plat && obj.action === command);
      if (!find) {
        continue;
      }
      scrollToRow(find.key);
      let commandTypeFirst = getCommandDataTypeFirst(command);
      switch (commandTypeFirst) {
        case 'CREATE': {
          let commandTypeSecond = getCommandDataTypeSecond(command);
          if (commandTypeSecond === 'SUBSCRIPTION') {
            find.type = 'SUBSCRIPTION';
          } else {
            find.type = 'CREATE';
          }
          break;
        }
        case 'DELETE': {
          find.type = 'DELETE';
          break;
        }
        case 'UPDATE': {
          find.type = 'UPDATE';
          break;
        }
        case 'RETRIEVE': {
          find.type = 'RETRIEVE';
          break;
        }
      }
      setTimeout(() => {
        find.type = 'FINISHED'
      }, 3000);
    }
  });
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeTableScroll);
})
const columns = [
  {
    title: '仿真时刻',
    dataIndex: 'time',
    width: 50,
  },
  {
    title: '执行平台',
    dataIndex: 'plat',
    width: 50,
  },
  {
    title: '互操作',
    dataIndex: 'action',
    width: 150,
    ellipsis: true
  }
];
</script>

<template>
  <div class="interactionContainer">
    <div class="titleInteraction">仿真推演资源调度指令执行时序表</div>
    <div class="tableInteraction">
      <a-table
          ref="interActTable"
          :size="'small'"
          :style="{height: `${tableHeight}px`}"
          :columns="columns"
          :data-source="rows"
          :pagination="false"
          :scroll="{ y: tableHeight*0.9}"
          :bordered=true
          :row-class-name="rowClass"
      />
    </div>
  </div>
</template>

<style>
.interactionContainer {
  height: 100%;
  width: 100%;
}

.titleInteraction {
  height: 7%;
  width: 100%;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
}

.tableInteraction {
  height: 93%;
  margin-left: 10px;
  margin-right: 10px;
}

.finished {
  background-color: #9f9f99;
}

.create {
  background-color: #1ab99b;
}

.update {
  background-color: #7360DF;
}

.subscription {
  background-color: #FFCF81;
}

.delete {
  background-color: #BF3131;
}

.retrieve {
  background-color: #7BD3EA;
}

</style>
