<template>
  <a-tabs v-model:activeKey="platformInfo.choosePlatform" type="card" size="small" @change="tabChangeHandler"
          style="height: 8%">

    <a-tab-pane v-for="item in platformInfo.getPlatformIdList" :key="item" :tab="item">

    </a-tab-pane>

  </a-tabs>

  <el-dialog v-model="dialogTableVisible" title="日志" width="80%">
    <el-table :data="platformInfo.getPlatformErrorLog">
      <el-table-column property="time" label="时间" width="200"/>
      <el-table-column property="logLevel" label="日志等级" width="180"/>
      <el-table-column property="logContent" label="日志内容"/>
    </el-table>
  </el-dialog>


  <el-scrollbar height="92%">


    <contextHolder></contextHolder>
    <a-typography style="margin-bottom: 5px; margin-left: 5px">仿真节点容器 {{ platformInfo.getPlatformName }}
      配置信息:
    </a-typography>
    <el-descriptions
        class="margin-top"
        :column="2"
        size="default"
        border

    >

      <el-descriptions-item label="节点容器编号">{{ platformInfo.getPlatformId }}</el-descriptions-item>
      <el-descriptions-item label="节点角色">{{ platformInfo.getPlatformRole }}</el-descriptions-item>
      <el-descriptions-item label="节点IP地址" :span="1">{{ platformInfo.getPlatformIP }}</el-descriptions-item>
      <el-descriptions-item label="计算资源配额" :span="1">{{
          platformInfo.getPlatformComputationSetting
        }}
      </el-descriptions-item>
      <el-descriptions-item label="内存资源配额" :span="1">{{ platformInfo.getPlatformMemorySetting }}
      </el-descriptions-item>
      <el-descriptions-item label="容器资源管理中间件状态" :span="2">
        <a-badge dot :offset="[3,0]">
          <a href="#" @click.prevent="viewLogHandler">查看日志</a>
        </a-badge>
      </el-descriptions-item>
      <el-descriptions-item label="容器状态" :span="2">
        <a-badge status="processing"/>
        {{ platformInfo.getRunningState }}
      </el-descriptions-item>

      <el-descriptions-item label="当前计算资源占用率" span="2">
        <a-progress :percent="platformInfo.getPlatformComputationUsage" size="small"/>
      </el-descriptions-item>

      <el-descriptions-item label="当前存储资源占用率" span="2">
        <a-progress :percent="platformInfo.getPlatformMemoryUsage" size="small"/>
      </el-descriptions-item>

    </el-descriptions>

    <div>
      <a-typography style="margin-top: 5px; margin-left: 5px; margin-right: 5px; display: inline-block">
        仿真节点应用/装备信息:
      </a-typography>
      <a-cascader style=" display: inline-block" v-model:value="equipmentSelected" :options="selectMenu"
                  @change="selectedItemChangedHandler">
        <a href="#">选择</a>
      </a-cascader>
    </div>

    <div style="padding: 10px">

      <el-descriptions
          class="margin-top"
          :column="1"
          size="default"
          border
      >

        <el-descriptions-item width="110px" :label="infoType+'名称'">{{ equipmentInfo.name }}
        </el-descriptions-item>
        <el-descriptions-item :label="infoType+'软件目录'">{{ equipmentInfo.softwareIndex }}</el-descriptions-item>

        <el-descriptions-item :label="infoType+'软件路径'" :span="1">{{ equipmentInfo.softwarePath }}
        </el-descriptions-item>
        <el-descriptions-item :label="infoType+'资源子树描述文件'" :span="1">{{ equipmentInfo.descriptionPath }}
        </el-descriptions-item>
        <el-descriptions-item label="软件存活状态" :span="1">
          <a-badge status="processing" v-if="platformInfo.getEquipmentState"/>
          {{ platformInfo.getEquipmentState }}
        </el-descriptions-item>
        <el-descriptions-item :label="infoType+'软件参数'" :span="1">{{ equipmentInfo.args }}
        </el-descriptions-item>


      </el-descriptions>
    </div>

  </el-scrollbar>
</template>
<script setup>

import {onBeforeMount, onMounted, reactive, ref} from "vue";
import {Modal} from 'ant-design-vue';
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

onBeforeMount(() => {
  //启动数据监听
  window.electronAPI.receiveDockerCpuAndMemData(data => {
    useSimulationProjectInfoStore().updateUsageData(data)
  });

  window.electronAPI.receiveDockerSoftwareStateData(data => {
    useSimulationProjectInfoStore().updateSoftwareState(data)
  });

  window.electronAPI.receiveDockerDeviceStateData(data => {
    useSimulationProjectInfoStore().updateEquipmentState(data)
  });

  window.electronAPI.receiveDockerLogData(data => {
    useSimulationProjectInfoStore().updateErrorLog(data)
  });
  window.electronAPI.receiveDockerIdAndState(data => {
    useSimulationProjectInfoStore().updatePlatformState(data)
  });

  window.electronAPI.receiveDockerCpuAndMemCfgData(data => {
    useSimulationProjectInfoStore().updateResourceSetting(data)
  });

})

let platformInfo = useSimulationProjectInfoStore()
let selectMenu = ref([])
let dialogTableVisible = ref(false)

let equipmentSelected = ref([])
let infoType = ref('装备')


let equipmentInfo = reactive({
  name: "请选择装备/应用",
  softwareIndex: "",
  softwarePath: "",
  descriptionPath: "",
  state: '',
  args: ''
})

function initSelectMenu() {
  equipmentInfo.name = '请选择装备/应用'
  equipmentInfo.softwareIndex = ''
  equipmentInfo.softwarePath = ''
  equipmentInfo.descriptionPath = ''
  equipmentInfo.state = ''
  let equipmentItems = []
  let appItems = []
  selectMenu.value = []


  platformInfo.getPlatformEquipments.forEach((item) => {
    equipmentItems.push({
      value: item,
      label: item
    })
  })
  platformInfo.getPlatformApplist.forEach((item) => {
    appItems.push({
      value: item,
      label: item
    })
  })

  selectMenu.value.push({
    value: "装备",
    label: '装备',
    children: equipmentItems
  })
  selectMenu.value.push({
    value: "应用",
    label: '应用',
    children: appItems
  })

  let info = []
  if (equipmentItems.length > 0) {
    info.push('装备')
    info.push(equipmentItems[0].value)
  } else if (appItems.length > 0) {
    info.push('应用')
    info.push(appItems[0].value)
  } else {
    return
  }
  selectedItemChangedHandler(info)
}

initSelectMenu()


function selectedItemChangedHandler(info) {
  infoType.value = info[0]
  let itemName = info[1]
  // console.log("itemName"+itemName+JSON.stringify(platformInfo.getEquipmentState))

  if (info[0] === '装备') {
    platformInfo.getEquipmentsSet.forEach((item) => {
      if (item.name === itemName) {
        equipmentInfo.name = item.name
        equipmentInfo.softwareIndex = item.softwareIndex
        equipmentInfo.softwarePath = item.softwarePath
        equipmentInfo.descriptionPath = item.descriptionPath
        equipmentInfo.args = platformInfo.simulationProjectInfo[platformInfo.chooseProject].nodesSet[platformInfo.choosePlatform].equipmentState[info[1]].args
      }
    })
  } else {
    platformInfo.getAppSet.forEach((item) => {
      if (item.name === itemName) {
        equipmentInfo.name = item.name
        equipmentInfo.softwareIndex = item.softwareIndex
        equipmentInfo.softwarePath = item.softwarePath
        equipmentInfo.descriptionPath = item.descriptionPath
        equipmentInfo.args = platformInfo.simulationProjectInfo[platformInfo.chooseProject].nodesSet[platformInfo.choosePlatform].softwareState[info[1]].args
      }
    })
  }
  platformInfo.setChooseEquipment(info[0], itemName)
}

// setInterval(function (){
//   console.log(equipmentInfo.state)
// },2000)
function tabChangeHandler(info) {
  useSimulationProjectInfoStore().setChoosePlatform(info)
  initSelectMenu()

}

const [modal, contextHolder] = Modal.useModal();

function viewLogHandler() {
  // modal.confirm({
  //   width:1000,
  //   title: '日志信息',
  //   content: '错误信息',
  //   class:'test'
  // })
  dialogTableVisible.value = true
}


let timeNum = 0.0

const dockerSettingLabelStyle = {
  width: '130px',
  textAlign: 'center',
  fontSize: `${window.innerWidth * 0.005}px`
}
const dockerSettingContentStyle = {
  textAlign: 'left',
  fontSize: `${window.innerWidth * 0.005}px`
}
const equipmentLabelStyle = {
  width: '140px',
  textAlign: 'center',
  fontSize: `${window.innerWidth * 0.005}px`
}
const equipmentContentStyle = {
  textAlign: 'left',
  fontSize: `${window.innerWidth * 0.005}px`
}
const titleTextStyle = {
  marginLeft: '5px',
  fontSize: `${window.innerWidth * 0.006}px`
}

const equipmentTitleTextStyle = {
  marginBottom: 0,
  marginLeft: '5px',
  marginRight: '5px',
  display: 'inline-block',
  fontSize: `${window.innerWidth * 0.006}px`
}

</script>

<style>


</style>

