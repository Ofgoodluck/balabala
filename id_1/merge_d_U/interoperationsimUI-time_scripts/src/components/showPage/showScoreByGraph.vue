<script setup>

import * as echarts from 'echarts';
import {onMounted, ref} from "vue";
import {Background} from "@vue-flow/background";
import {useVueFlow, VueFlow} from "@vue-flow/core";

const graphInstance = useVueFlow({
  nodes: [],
  edges: [],
})
let factors = [];

let dialogTableVisible = ref(false)

let myChart = null;
let newData = Array.from({length: 0}, (_, i) => i);
let averageScore = 0.0;
let duration = 30;
let xData = Array.from({length: 30}, (_, i) => i);
let yData = [];
const updating = ref(true);
let option = {
  title: {
    text: '仿真互操作效用得分',
    left: 'center',
    fontSize: '16px'
  },
  xAxis: {
    type: 'category',
    data: xData,
    name: '时间(秒)',
  },
  yAxis: {
    name: '得分',
    type: 'value'
  },
  series: [
    {
      name: '当前仿真互操作效用得分',
      type: 'line',
      smooth: true,
      animation: false,
      data: yData,
      markLine: {
        symbol: 'none',
        silent: true, // true 不响应鼠标事件
        data: [{
          yAxis: averageScore
        }],
        label: {
          position: 'end', // 文字位置
          offset: [-100, 20],
          formatter: '当前30秒的平均得分: ' + averageScore.toFixed(1),  //文字
          color: '#DAA520FF'  // 文字颜色
        },
        lineStyle: {type: 'solid', color: '#F0E68CFF', width: 2}
      },
    }
  ],
  dataZoom: [
    {
      type: 'slider', // 使用滑动条型的 dataZoom 组件
      // start: 0, // 滑动条的初始位置（百分比）
      // end: 100, // 滑动条的结束位置（百分比）
      startValue: 0,
      endValue: 29,
      height: 20
    }
  ],
};

onMounted(() => {
  let charDom = document.getElementById('showscorebygraph')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  myChart.on('datazoom', function (param) {
    let endValue = myChart.getOption().dataZoom[0].endValue;
    let startValue = myChart.getOption().dataZoom[0].startValue;
    calculateAverageScore(startValue, endValue);
    let temp = {
      series: [
        {
          markLine: {
            symbol: 'none',
            silent: true, // true 不响应鼠标事件
            data: [{
              yAxis: averageScore
            }],
            label: {
              position: 'end', // 文字位置
              offset: [-100, 20],
              formatter: '当前' + (endValue - startValue + 1) + '秒的平均得分: ' + averageScore.toFixed(1),  //文字
              color: '#DAA520FF'  // 文字颜色
            },
            lineStyle: {type: 'solid', color: '#F0E68CFF', width: 2}
          },
        }
      ]
    };
    myChart.setOption(temp);
    updating.value = param.end === 100;
  });

  let i = 30

  option && myChart.setOption(option);

  setInterval(function () {
    if (newData.length > 0) {
      pushData(yData, newData[0]);
      newData.shift();
    } else {
      pushData(yData, averageScore);
    }
    if (yData.length >= duration) {
      pushData(xData, i);
      i++;
    }
    calculateAverageScore(Math.max(yData.length - duration, 0), yData.length - 1);
    option.series[0].markLine.data[0].yAxis = averageScore;
    option.series[0].markLine.label.formatter = '当前' + (myChart.getOption().dataZoom[0].endValue - myChart.getOption().dataZoom[0].startValue + 1) + '秒的平均得分: ' + averageScore.toFixed(1);
    option.dataZoom[0].startValue = xData[xData.length - 30];
    option.dataZoom[0].endValue = xData[xData.length - 1];

    if (updating.value === false) {
      return;
    }

    myChart.setOption(option)

  }, 1000)


  window.addEventListener('resize', () => {
    myChart.resize()
  })

  window.electronAPI.receiveSimulationScore(data => {
    newData.push(data['仿真互操作效用得分']);
    let idx = 1;
    for (let id of factors) {
      const findNode = graphInstance.getNode.value(id);
      if (findNode) {
        findNode.label = id + ' : x' + idx + ' = ' + data['仿真互操作效用得分来源'][idx - 1];
        ++idx;
      }
    }
    const findNode = graphInstance.getNode.value('output');
    if (findNode) {
      findNode.label = '得分: ' + data['仿真互操作效用得分'];
    }
    // console.log(newData);
  });

  let currentFactor = []

  window.electronAPI.receiveScoreFactor(data => {
    if (arraysEqual(currentFactor, data)) {
      return
    }
    currentFactor = data
    factors = []
    let length = 'root/仿真推演平台级信息/'.length;
    for (let item of data) {
      factors.push(item.substring(length));
    }
    drawFactor(factors);
  });
  // drawFactor(["/root/仿真推演平台级信息/平台2/应用列表/数学公式计算/公式计算结果表","/root/仿真推演平台级信息/平台7/应用列表/数学公式计算/公式计算结果表","/root/仿真推演平台级信息/平台8/应用列表/数学公式计算/公式计算结果表"])
})

function arraysEqual(a, b) {
  if (a.length !== b.length) {
    return false;
  }

  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) {
      return false;
    }
  }
  return true;
}

function pushData(list, data) {
  if (list.length >= 1000) {
    list.shift();
  }
  list.push(data);
}

function calculateAverageScore(start, end) {
  let sum = 0;
  for (let i = start; i <= end; i++) {
    sum += yData[i];
  }

  return averageScore = sum / (end - start + 1);
}

function viewLogHandler() {
  dialogTableVisible.value = true
}

function drawFactor(inputs) {
  let formula = 'average=(x1+x2+x3)/3'
  let output = '100'
  let node = []
  let edge = []

  node.push({id: 'formula', label: formula, position: {x: 180, y: 180}, style: {width: '250px'}})
  node.push({
    id: 'output',
    label: '得分：' + output,
    type: 'output',
    position: {x: 180, y: 250},
    style: {width: '250px'}
  })

  let achor = inputs.length / 2
  let start = 0
  let end = inputs.length - 1
  while (start < end) {
    node.push({
      id: inputs[start],
      type: 'input',
      label: inputs[start],
      position: {x: 285 - 120 * achor, y: 5},
      style: {width: '100px'}
    })
    edge.push({id: inputs[start], source: inputs[start], target: 'formula', animated: true, style: {stroke: '#000000'}})
    node.push({
      id: inputs[end],
      type: 'input',
      label: inputs[end],
      position: {x: 215 + 120 * achor, y: 5},
      style: {width: '100px'}
    })
    edge.push({id: inputs[end], source: inputs[end], target: 'formula', animated: true, style: {stroke: '#000000'}})
    start = start + 1
    end = end - 1
    achor = achor - 1
  }
  if (start === end) {
    node.push({id: inputs[end], type: 'input', label: inputs[end], position: {x: 250, y: 5}, style: {width: '100px'}})
    edge.push({id: inputs[end], source: inputs[end], target: 'formula', animated: true, style: {stroke: '#000000'}})
  }
  edge.push({id: 'formula', source: 'formula', target: 'output', animated: true, style: {stroke: '#000000'}})

  graphInstance.addNodes(node)
  graphInstance.addEdges(edge)
  graphInstance.fitView()
}
</script>

<template>
  <el-dialog v-model="dialogTableVisible" title="计算要素展示" width="44%" style="text-align: center;">
    <VueFlow fit-view-on-init style="height: 300px; width: 100%">
      <Background/>
    </VueFlow>
  </el-dialog>
  <div style="height: 5%; width: 100%">
    <a-badge>
      <a href="#" @click.prevent="viewLogHandler" style="margin-left: 10px;">查看计算要素</a>
    </a-badge>
  </div>
  <div id="showscorebygraph" style="height: 95%; width: 100%"></div>
</template>

<style>

</style>
