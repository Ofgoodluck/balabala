<script setup>

import * as echarts from 'echarts';
import {onMounted, reactive, watch, ref} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

let info = useSimulationProjectInfoStore()

//获取所有的边信息
let edgesSet = reactive({})
edgesSet = info.getGraphEdgesSet

let keys = []
getEdges()

let chooseEdges = keys
let chooseLegends = keys
let selected = {}
getSelected()

watch(() => info.getChooseEdge.length, () => {
  chooseEdges = info.getChooseEdge
  for (let key in selected) {
    selected[key] = false
  }
  getSelected()
  myChart.setOption({
    legend: {
      selected: selected,
    }
  })
});


let newData = {}
for (let key of keys) {
  newData [key] = 0
}


let option = {
  title: {
    text: '网络拓扑实时时延展示',
    left: 'center',
    fontSize: '16px'
  },
  xAxis: {
    name: '时间(秒)',
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    name: '时延(毫秒)',
  },
  legend: {
    data: chooseLegends,
    x: 'center',
    y: 'bottom',
    type: 'scroll',
    selected: selected,
    animation: false
  },
  series: []
};
let myChart = null;

onMounted(() => {
  let charDom = document.getElementById('flowofedge')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  let i = 0
  let legends = []
  let yData = {}
  for (let key in newData) {
    yData[key] = []
    yData[key].push(newData[key])
    legends.push(key)
  }

  option && myChart.setOption(option);

  delete option.legend;

  setInterval(function () {
    let xData = []
    let if_i_add = false
    for (let j = 0; j < 15; j++) {
      xData.push(i + j)
    }

    for (let key in yData) {
      let len = yData[key].length
      if (len >= 15) if_i_add = true

      if (len < 15) {
        yData[key].push(newData[key])
      } else {
        yData[key] = yData[key].slice(1, len)
        yData[key].push(newData[key])
      }
    }

    let chartData = []

    for (let key in yData) {
      chartData.push({
        name: key,
        type: 'line',
        smooth: true,
        data: yData[key]
      })
    }

    option.series = chartData
    option.xAxis.data = xData
    myChart.setOption(option)

    if (if_i_add) i++

    for (let key in newData) {
      newData[key] = 0
    }
  }, 1000)

  window.addEventListener('resize', () => {
    myChart.resize()
  })

  window.electronAPI.receiveEdgeDelayData(data => {
    for (let key in data) {
      if (newData.hasOwnProperty(key)) {
        newData[key] = data[key];
      } else {
        newData[convertEdge(key)] = data[key];
      }
    }
  });


})

function convertEdge(edge) {
  const pos = edge.lastIndexOf('-');
  return edge.slice(pos + 1) + '-' + edge.slice(0, pos);
}

function getEdges() {
  for (let key in edgesSet) {
    keys.push(key)
  }
}

function getSelected() {
  for (let key of chooseEdges) {
    selected[key] = true;
  }
}

</script>

<template>
  <div id="flowofedge" style="height: 100%; width: 100%; margin-top: 5px">
  </div>
</template>

<style scoped>

</style>
