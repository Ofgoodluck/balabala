<script setup>

import * as echarts from 'echarts';
import {onMounted, reactive, watch} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

let darkColors = [
  '#325B74', // 蓝色
  '#D4C45D', // 卡其色
  '#2F8E5B', // 草绿色
  '#A95696', // 淡紫色
  '#D98C00', // 橙色
  '#D9809E', // 粉红色
  '#555555', // 灰色
  '#0D8C8E', // 深蓝绿色
  '#B63A31', // 红色
  '#6A1C76'  // 紫色
];

let lightColors = [
  '#92B9D9', // 蓝色
  '#F4EAA5', // 卡其色
  '#77C4A5', // 草绿色
  '#EBAED7', // 淡紫色
  '#FFC566', // 橙色
  '#FFB1C5', // 粉红色
  '#AAAAAA', // 灰色
  '#59C2C4', // 深蓝绿色
  '#E5736C', // 红色
  '#B35B92'  // 紫色
];

const app = {};
let myChart = null;

let store = useSimulationProjectInfoStore()

//获取拓扑所有边信息
let edgesSet = reactive({})
edgesSet = store.getGraphEdgesSet

let keys = []
getEdges()
let chooseEdges = keys

let chooseLegends = []
getChooseLegends()
let selected = {}
getSelected()

let option = {
  animation: false,
  title: {
    text: '网络拓扑带宽使用实时展示',
    left: 'center',
    fontSize: '16px'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: [
    {
      type: 'value',
      name: '带宽占用大小(Bps)',
    },
    {
      type: 'value',
      name: '带宽占用率(%)',
      alignTicks: true,
      min: 0,
      max: 100,
      interval: 20,
      axisLabel: {
        formatter: '{value} %'
      }
    }
  ],
  legend: {
    x: 'center',
    y: 'bottom',
    data: chooseLegends,
    selected: selected,
    animation: false,
    type: 'scroll'
  },
  series: []
};

watch(() => store.getChooseEdge.length, () => {
  chooseEdges = store.getChooseEdge
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

let allUse = {}
let newData = {}
for (let key of keys) {
  allUse[key] = edgesSet[key]['bandWidth'] / 8 * 1000
  newData[key] = 0
}

onMounted(() => {
  let charDom = document.getElementById('widthofedge')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  let i = 0
  let barData = {}
  let lineData = {}

  for (let key in newData) {
    barData[key] = []
    barData[key].push(newData[key])
    lineData[key] = []
    lineData[key].push(newData[key] / allUse[key] * 100)
  }

  option && myChart.setOption(option);

  delete option.legend;

  setInterval(function () {
    let xData = []
    let if_i_add = false

    for (let j = 0; j < 15; j++) {
      xData.push(i + j)
    }

    for (let key in newData) {
      let keyTmp
      if (barData.hasOwnProperty(key)) {
        keyTmp = key;
      } else {
        keyTmp = convertEdge(key);
      }
      let len = barData[keyTmp].length;
      if (len >= 15) if_i_add = true

      if (len < 15) {
        barData[keyTmp].push(newData[key])
      } else {
        barData[keyTmp].shift()
        barData[keyTmp].push(newData[key])
      }

      if (len < 15) {
        lineData[keyTmp].push(newData[key] / allUse[key] * 100)
      } else {
        lineData[keyTmp].shift()
        lineData[keyTmp].push(newData[key] / allUse[key] * 100)
      }
    }

    let chartData = []
    let index = 0
    for (let key in barData) {
      chartData.push({
        name: key + '带宽统计',
        type: 'bar',
        smooth: true,
        data: barData[key],
        itemStyle: {
          color: lightColors[index]
        }
      })
      index = index + 1
    }

    index = 0
    for (let key in lineData) {
      chartData.push({
        name: key + '带宽占用率',
        yAxisIndex: 1,
        type: 'line',
        smooth: true,
        data: lineData[key],
        itemStyle: {
          color: darkColors[index]
        }
      })
      index = index + 1
    }

    option.series = chartData
    option.xAxis.data = xData
    myChart.setOption(option)

    if (if_i_add) i++

  }, 1000)

  window.electronAPI.receiveEdgeBandWidthData(data => {
    for (let key in data) {
      if (newData.hasOwnProperty(key)) {
        newData[key] = data[key];
      } else {
        newData[convertEdge(key)] = data[key];
      }
    }
  });

  window.addEventListener('resize', () => {
    myChart.resize()
  })

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

function getChooseLegends() {
  chooseLegends = []
  for (let key of chooseEdges) {
    chooseLegends.push(key + '带宽统计');
  }
  for (let key of chooseEdges) {
    chooseLegends.push(key + '带宽占用率');
  }
}

function getSelected() {
  for (let key of chooseEdges) {
    selected[key + '带宽统计'] = true;
    selected[key + '带宽占用率'] = true;
  }
}


</script>

<template>
  <div id="widthofedge" style="height: 100%; width: 100%; margin-top: 5px">
  </div>


</template>

<style scoped>

</style>
