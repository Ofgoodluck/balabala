<template>
  <div class="nodeDescription">
    <div class="titleNode">仿真节点信息监控</div>
    <div class="nodeInfo">
    <a-tabs v-model:activeKey="platformInfo.choosePlatform" type="card" size="small" @change="tabChangeHandler" style="height: 8%;border-top-left-radius: 8px;border-top-right-radius: 8px">
      <a-tab-pane v-for="item in platformInfo.getPlatformIdList" :key="item" :tab="item"></a-tab-pane>
    </a-tabs>

    <el-dialog v-model="dialogTableVisible" title="日志" width="80%">
      <el-table :data="platformInfo.getPlatformErrorLog">
        <el-table-column property="time" label="时间" width="200" />
        <el-table-column property="logLevel" label="日志等级" width="180" />
        <el-table-column property="logContent" label="日志内容" />
      </el-table>
    </el-dialog>

    <el-scrollbar height="92%">
      <el-descriptions
          class="margin-top"
          :column="2"
          size="default"
          border
      >
        <el-descriptions-item label="节点容器编号" >{{platformInfo.getPlatformId}}</el-descriptions-item>
        <el-descriptions-item label="节点容器名称" >{{platformInfo.getPlatformRole}}</el-descriptions-item>
        <el-descriptions-item label="节点IP地址" :span="1">{{platformInfo.getPlatformIP}}</el-descriptions-item>
        <el-descriptions-item label="计算资源配额" :span="1">{{ platformInfo.getPlatformComputationSetting }}</el-descriptions-item>
        <el-descriptions-item label="内存资源配额" :span="1">{{platformInfo.getPlatformMemorySetting}}</el-descriptions-item>
        <el-descriptions-item label="容器资源管理中间件状态" :span="2">
          <a-badge dot :offset="[3,0]">
            <a href="#" @click.prevent="viewLogHandler" >查看日志</a>
          </a-badge>
        </el-descriptions-item>
        <el-descriptions-item label="容器状态" :span="2">
          <a-badge status="processing"/>
          {{platformInfo.getRunningState}}
        </el-descriptions-item>
        <el-descriptions-item label="当前计算资源占用率" span="2">
          <a-progress :percent="platformInfo.getPlatformComputationUsage" size="small" />
        </el-descriptions-item>
        <el-descriptions-item label="当前存储资源占用率" span="2">
          <a-progress :percent="platformInfo.getPlatformMemoryUsage" size="small" />
        </el-descriptions-item>
      </el-descriptions>
    </el-scrollbar>
    </div>
  </div>
</template>
<script setup>

import {onBeforeMount, ref} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";
onBeforeMount(()=>{
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
let   dialogTableVisible= ref(false)

function tabChangeHandler(info){
  useSimulationProjectInfoStore().setChoosePlatform(info)
}

function viewLogHandler(){
  dialogTableVisible.value=true
}

const dockerSettingLabelStyle={
  width:'130px',
  textAlign:'center',
  fontSize: `${window.innerWidth * 0.005}px`
}
const dockerSettingContentStyle={
  textAlign:'left',
  fontSize: `${window.innerWidth * 0.005}px`
}
const equipmentLabelStyle={
  width:'140px',
  textAlign:'center',
  fontSize: `${window.innerWidth * 0.005}px`
}
const equipmentContentStyle={
  textAlign:'left',
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
.titleNode {
  height: 7%;
  width: 100%;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
}

.nodeDescription{
  height: 100%;
  width: 100%;
  overflow: auto;
}

.nodeInfo{
  margin-left: 10px;
  margin-right: 10px;
}
</style>
