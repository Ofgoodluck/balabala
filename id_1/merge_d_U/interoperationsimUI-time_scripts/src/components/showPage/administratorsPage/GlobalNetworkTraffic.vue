<script setup>
import * as echarts from 'echarts';
import {nextTick, onMounted, ref, watch} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

const store = useSimulationProjectInfoStore()
//获取拓扑节点信息
let nodesSet = store.getGraphNodesSet

let allNewData = {}
let platsData = {}
for (let key in nodesSet) {
  allNewData[key] = {
    '出口流量': [],
    '入口流量': []
  };
  platsData[key] = {
    '出口流量': 0,
    '入口流量': 0
  };
}
let plat = store.getChoosePlatform

let duration = 30;
let xData = Array.from({length: duration}, (_, i) => i);
let option = {
  title: {
    text: plat + '仿真节点网络流量展示',
    left: 'center',
    fontSize: '16px'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#6a7985'
      }
    }
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
  legend: {
    data: ['入口流量', '出口流量'],
    x: 'right',
    y: 25
  },
  dataZoom: [
    {
      type: 'slider',
      startValue: 0,
      endValue: 29,
      height: 20,
    }
  ],
  series: [
    {
      name: '入口流量',
      type: 'line',
      areaStyle: {},
      emphasis: {
        focus: 'series'
      },
      data: allNewData[plat]['入口流量']
    },
    {
      name: '出口流量',
      type: 'line',
      areaStyle: {},
      emphasis: {
        focus: 'series'
      },
      data: allNewData[plat]['出口流量']
    }
  ]
};
let myChart = null;
const updating = ref(true);

onMounted(() => {
  let charDom = document.getElementById('global_network_traffic')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  watch(() => store.choosePlatform, (newPlat) => {
    plat = newPlat;
    option.title.text = plat + '仿真节点网络流量展示';
    option.xAxis.data = xData;
    option.series[0].data = allNewData[plat]['入口流量'];
    option.series[1].data = allNewData[plat]['出口流量'];
    myChart.setOption(option);
  });

  myChart.on('datazoom', function (param) {
    updating.value = myChart.getOption().dataZoom[0].endValue === xData[xData.length - 1];
  });

  let i = 30

  myChart.setOption(option);


  setInterval(function () {
    if (allNewData[plat]['入口流量'].length >= duration) {
      pushData(xData, i);
      i++;
    }
    for (let key in allNewData) {
      pushData(allNewData[key]['入口流量'], platsData[key]['入口流量']);
      pushData(allNewData[key]['出口流量'], platsData[key]['出口流量']);
      platsData[key]['入口流量'] = 0;
      platsData[key]['出口流量'] = 0;
    }
    if (!updating.value) {
      option.dataZoom[0].startValue = myChart.getOption().dataZoom[0].startValue;
      option.dataZoom[0].endValue = myChart.getOption().dataZoom[0].endValue;
      myChart.setOption(option);
      return;
    }
    option.dataZoom[0].endValue = xData[xData.length - 1];
    option.dataZoom[0].startValue = option.dataZoom[0].endValue - duration;
    myChart.setOption(option);

  }, 1000)

  option && myChart.setOption(option);

  window.addEventListener('resize', () => {
    myChart.resize()
  })

  window.electronAPI.receivePlatIOBandWidthData(data => {
    for (let key in data) {
      platsData[key]['入口流量'] = data[key]['入口流量'];
      platsData[key]['出口流量'] = data[key]['出口流量'];
    }
  })
})

function pushData(list, data) {
  if (list.length >= 1000) {
    list.shift();
  }
  list.push(data);
}
</script>

<template>
  <div id="global_network_traffic" style="height: 100%; width: 100%"></div>
</template>

<style scoped>

</style>
