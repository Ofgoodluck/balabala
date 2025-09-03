<script setup>

import * as echarts from 'echarts';
import {onMounted, watch} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

const app = {};
let myChart = null;

const store = useSimulationProjectInfoStore()

//获取拓扑节点信息
let nodesSet = store.getGraphNodesSet

let allNewData = {}
let platsData = {}
for (let key in nodesSet) {
  allNewData[key] = [];
  platsData[key] = 0;
}
let xData = Array.from({length: 15}, (_, i) => i);
let plat = store.getChoosePlatform

let option = {
  title: {
    text: plat + '仿真节点网络流量展示',
    left: 'center',
    fontSize: '16px'
  },
  xAxis: {
    type: 'category',
    data: xData,
    name: '时间(秒)',
  },
  yAxis: {
    name: '流量(字节)',
    type: 'value'
  },
  series: [
    {
      name: '平台流量',
      type: 'line',
      smooth: true,
      data: allNewData[plat]
    }
  ]
};

onMounted(() => {
  let charDom = document.getElementById('flowofnode')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  watch(() => store.choosePlatform, (newPlat) => {
    plat = newPlat;
    option.title.text = plat + '仿真节点网络流量展示';
    option.xAxis.data = xData;
    option.series[0].data = allNewData[plat];
    myChart.setOption(option);
  });

  let i = 1

  setInterval(function () {
    for (let j = 0; j < 15; j++) {
      xData[j] = j + i;
    }

    option.title.text = plat + '仿真节点网络流量展示';
    option.xAxis.data = xData;
    option.series[0].data = allNewData[plat];
    myChart.setOption(option)

    if (allNewData[plat].length >= 15) {
      i++;
    }
    for (let key in allNewData) {
      pushData(allNewData[key], platsData[key])
      platsData[key] = 0
    }
  }, 1000)

  option && myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize()
  })

  window.electronAPI.receivePlatBandWidthData(data => {
    platsData[data['平台名称']] = data['平台流量']
  })

})

function pushData(list, data) {
  if (list.length >= 15) {
    list.shift();
  }
  list.push(data);
}


</script>

<template>
  <div id="flowofnode" style="height: 100%; width: 100%">
  </div>
</template>

<style scoped>

</style>
