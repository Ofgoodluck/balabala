<script setup>

import * as echarts from 'echarts';
import {onMounted, ref} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

const store = useSimulationProjectInfoStore()
let nodesSet = store.getGraphNodesSet

let myChart = null;
let duration = 30;
let now = 0;
let xData = Array.from({length: 30}, (_, i) => i);
let posx = null;
let posy = null;
const updating = ref(true);
let start = false;
let startTime = 0;

let types = {
  'RETRIEVE': {
    color: '#7BD3EA',
    symbol: 'triangle'
  },
  'CREATE': {
    color: '#1ab99b',
    symbol: 'circle'
  },
  'UPDATE': {
    color: '#7360DF',
    symbol: 'diamond'
  },
  'SUBSCRIPTION': {
    color: '#FFCF81',
    symbol: 'custom'
  },
  'DELETE': {
    color: '#BF3131',
    symbol: 'rect'
  },
};

let categories = ['网络拓扑事件(真实)', '网络拓扑事件(预设)'];
let platsTrueIdx = {};
let idx = 3;
for (let node in nodesSet) {
  categories.push('——————————');
  categories.push(node + '互操作事件(真实)');
  categories.push(node + '互操作事件(预设)');
  platsTrueIdx[node] = idx;
  idx += 3;
}

let palettes = [
  '#6096B4',
  '#93BFCF',
  '#BDCDD6',
  '#EEE9DA'
];

let topoData1 = [
  {
    name: '网络拓扑',
    value: [1, 0, 10, '1-2, 1-3, 1-4, 1-5, 1-6, 1-7, 1-8, 1-9, 1-10'],
    itemStyle: {
      color: palettes[0]
    }
  }
];

let topoData2 = [
  {
    name: '网络拓扑',
    value: [0, 0, 10, '1-2, 1-3, 1-4, 1-5, 1-6, 1-7, 1-8, 1-9, 1-10'],
    itemStyle: {
      color: palettes[0]
    }
  }
];

let presetSubscriptionsUnfinished = [
  // {
  //   value: [3, 0, 29, '订阅'],
  //   itemStyle: {
  //     color: types['SUBSCRIPTION'].color
  //   }
  // },
];

let presetSubscriptionsFinished = [];

let trueSubscriptionsUnfinished = [];

let trueSubscriptionsFinished = [];

let interoperationData = [
  // {
  //   symbol: 'triangle',
  //   value: [3, 3, 'comment']
  // }
];

let option = {
  tooltip: {
    trigger: 'item',
    axisPointer: {
      type: 'cross',
      snap: true
    },
    formatter: function () {
      posx = myChart.getOption().xAxis[0].axisPointer.value;
      posy = myChart.getOption().yAxis[0].axisPointer.value;
      let res = '';
      for (let item of myChart.getOption().series) {
        if (item.hasOwnProperty('data')) {
          let datas = item.data;
          for (let data of datas) {
            if (data.name === '网络拓扑' && data.value[0] === posy && data.value[1] <= posx && data.value[2] > posx) {
              let text = '网络拓扑起始时间: ' + data.value[1] + ', 拓扑: ' + data.value[3];
              res += wrapText(text, 90);
            } else if (item.type === 'scatter' && Math.abs(data.value[0] - posx) < 0.3 && data.value[1] === posy) {
              let text = '指令发起时间: ' + data.value[0] + ', 指令内容: ' + data.value[2];
              res += wrapText(text, 90);
            } else if (item.type === 'custom' && data.value[0] === posy && data.value[1] <= posx && data.value[2] > posx) {
              let text = '指令发起时间: ' + data.value[1] + ', 指令内容: ' + data.value[3];
              res += wrapText(text, 90);
            }
          }
        }
      }
      return res;
    }
  },
  title: {
    text: '仿真推演时间轴',
    left: 'center'
  },
  dataZoom: [
    {
      type: 'slider',
      filterMode: 'weakFilter',
      showDataShadow: false,
      labelFormatter: '',
      height: 20,
      xAxisIndex: 0,
    },
    {
      type: 'slider',
      filterMode: 'none',
      showDataShadow: false,
      labelFormatter: '',
      width: 20,
      left: '92%',
      startValue: 0,
      endValue: 7,
      yAxisIndex: 0,
    }
  ],
  grid: {
    left: 150
  },
  xAxis: {
    type: 'value',
    data: xData,
    axisLine: {
      show: true,
      symbol: ['none', 'arrow']
    },
    axisTick: {
      show: true
    },
  },
  yAxis: {
    data: categories,
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
  },
  legend: {
    itemStyle: {},
    data: [
      {
        name: '创建',
        icon: types.CREATE.symbol,
        itemStyle: {
          color: types.CREATE.color
        }
      },
      {
        name: '查询',
        icon: types.RETRIEVE.symbol,
        itemStyle: {
          color: types.RETRIEVE.color
        }
      },
      {
        name: '更新',
        icon: types.UPDATE.symbol,
        itemStyle: {
          color: types.UPDATE.color
        }
      },
      {
        name: '删除',
        icon: types.DELETE.symbol,
        itemStyle: {
          color: types.DELETE.color
        }
      },
      {
        name: '订阅',
        icon: types.SUBSCRIPTION.symbol,
        itemStyle: {
          color: types.SUBSCRIPTION.color
        }
      },
    ],
    right: '2%',
    top: '8%',
    selectedMode: false
  },
  series: [
    {
      name: '创建',
      type: 'line',
      data: []
    },
    {
      name: '查询',
      type: 'line',
      data: []
    },
    {
      name: '更新',
      type: 'line',
      data: []
    },
    {
      name: '删除',
      type: 'line',
      data: []
    },
    {
      name: '订阅',
      type: 'line',
      data: []
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: topoData1
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: topoData2
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: presetSubscriptionsUnfinished
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: presetSubscriptionsFinished
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: trueSubscriptionsUnfinished
    },
    {
      type: 'custom',
      renderItem: renderItem,

      itemStyle: {
        opacity: 0.8
      },
      encode: {
        x: [1, 2],
        y: 0
      },
      data: trueSubscriptionsFinished
    },
    {
      type: 'scatter',
      data: interoperationData,
      symbolSize: 20,
    },
    {
      name: '时间线',
      type: 'line',
      smooth: true,
      animation: true,
      markLine: {
        symbol: 'none',
        silent: true, // true 不响应鼠标事件
        data: [{
          xAxis: now
        }],
        label: {
          position: 'end', // 文字位置
          offset: [0, 0],
          formatter: '当前时间点',  //文字
          color: '#DAA520FF'  // 文字颜色
        },
        lineStyle: {type: 'dotted', color: '#F0E68CFF', width: 2}
      },
    }
  ]
};

onMounted(() => {

  let charDom = document.getElementById('timeAxis')
  myChart = echarts.init(charDom);
  const chartObserver = new ResizeObserver(() => {
    myChart.resize();
  });
  chartObserver.observe(charDom);

  myChart.on('datazoom', function () {
    updating.value = myChart.getOption().dataZoom[0].endValue === xData[xData.length - 1];
  });

  let i = duration;

  option && myChart.setOption(option);

  // delete option.dataZoom;
  option.dataZoom.splice(1, 1);

  setInterval(function () {
    if (!start) {
      return;
    }
    now++;
    if (now >= duration) {
      xData.push(i);
      i++;
    }
    maintainFollowUpLastOne(topoData1);
    maintainFollowUpLastOne(topoData2);
    maintainFollowUpAll(presetSubscriptionsUnfinished);
    maintainFollowUpAll(trueSubscriptionsUnfinished);
    if (updating.value === false) {
      option.series[option.series.length - 1].markLine.data[0].xAxis = now;
      option.dataZoom[0].startValue = myChart.getOption().dataZoom[0].startValue;
      option.dataZoom[0].endValue = myChart.getOption().dataZoom[0].endValue;
      setTimeout(() => {
        myChart.setOption(option)
      }, 200);
    } else {
      setTimeout(() => {
        option.series[option.series.length - 1].markLine.data[0].xAxis = now;
        option.dataZoom[0].startValue = myChart.getOption().dataZoom[0].startValue;
        option.dataZoom[0].endValue = xData[xData.length - 1];
        myChart.setOption(option)
      }, 200);
    }


  }, 1000)

  window.addEventListener('resize', () => {
    myChart.resize()
  })

  window.electronAPI.receiveTemplatesCfg((data) => {
    handlePresetData(data);
    myChart.setOption(option);
  })

  window.electronAPI.receiveCommandData(handleTrueData)

  window.electronAPI.receiveEvaluationToolReady(() => {
    start = true;
    startTime = Date.now();
  });

})

function wrapText(text, maxCharactersPerLine) {
  let res = '';
  for (let i = 0; i < text.length; i += maxCharactersPerLine) {
    res += text.substring(i, i + maxCharactersPerLine) + '<br>';
  }
  return res;
}

function maintainFollowUpLastOne(data) {
  if (now > data[data.length - 1].value[2]) {
    data[data.length - 1].value[2] = now;
  }
}

function maintainFollowUpAll(datas) {
  for (let data of datas) {
    if (now > data.value[2]) {
      data.value[2] = now;
    }
  }
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

function getResourceIdFrom(str) {
  const match = str.match(/FROM +([^ ]+) +TO/);
  if (match && match[1]) {
    return match[1];
  } else {
    return '';
  }
}

function handlePresetData(datas) {
  for (let data of datas) {
    let command = data['指令内容'];
    let plat = data['执行平台'];
    let time = data['仿真时刻'];
    let platIdx = platsTrueIdx[plat] + 1;
    let yData = {};
    let commandTypeFirst = getCommandDataTypeFirst(command);
    switch (commandTypeFirst) {
      case 'CREATE': {
        let commandTypeSecond = getCommandDataTypeSecond(command);
        if (commandTypeSecond === 'SUBSCRIPTION') {
          yData['itemStyle'] = {
            color: types['SUBSCRIPTION'].color
          };
          yData['value'] = [platIdx, time, time + duration, command];
          presetSubscriptionsUnfinished.push(yData);
        } else {
          yData['symbol'] = types['CREATE'].symbol;
          yData['itemStyle'] = {
            color: types['CREATE'].color
          };
          yData['value'] = [time, platIdx, command];
          interoperationData.push(yData);
        }
        break;
      }
      case 'DELETE': {
        let commandTypeSecond = getCommandDataTypeSecond(command);
        if (commandTypeSecond === 'SUBSCRIPTION') {
          let fromId = getResourceIdFrom(command);
          let find = null;
          for (let i = 0; i < presetSubscriptionsUnfinished.length; ++i) {
            if (getResourceIdFrom(presetSubscriptionsUnfinished[i].value[3]) === fromId) {
              find = presetSubscriptionsUnfinished.splice(i, 1)[0];
              break;
            }
          }
          if (find) {
            find.value[2] = time;
            presetSubscriptionsFinished.push(find);
          }
        }
        yData['symbol'] = types['DELETE'].symbol;
        yData['itemStyle'] = {
          color: types['DELETE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
      case 'UPDATE': {
        yData['symbol'] = types['UPDATE'].symbol;
        yData['itemStyle'] = {
          color: types['UPDATE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
      case 'RETRIEVE': {
        yData['symbol'] = types['RETRIEVE'].symbol;
        yData['itemStyle'] = {
          color: types['RETRIEVE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
    }
  }
}

function handleTrueData(datas) {
  for (let data of datas) {
    let timeNow = data['下发时间'];
    let command = data['指令内容'];
    let plat = data['执行平台'];
    let time = (timeNow - startTime) / 1000;
    let platIdx = platsTrueIdx[plat];
    let yData = {};
    let commandTypeFirst = getCommandDataTypeFirst(command);
    switch (commandTypeFirst) {
      case 'CREATE': {
        let commandTypeSecond = getCommandDataTypeSecond(command);
        if (commandTypeSecond === 'SUBSCRIPTION') {
          yData['itemStyle'] = {
            color: types['SUBSCRIPTION'].color
          };
          yData['value'] = [platIdx, time, now, command];
          trueSubscriptionsUnfinished.push(yData);
        } else {
          yData['symbol'] = types['CREATE'].symbol;
          yData['itemStyle'] = {
            color: types['CREATE'].color
          };
          yData['value'] = [time, platIdx, command];
          interoperationData.push(yData);
        }
        break;
      }
      case 'DELETE': {
        let commandTypeSecond = getCommandDataTypeSecond(command);
        if (commandTypeSecond === 'SUBSCRIPTION') {
          let fromId = getResourceIdFrom(command);
          let find = null;
          for (let i = 0; i < trueSubscriptionsUnfinished.length; ++i) {
            if (getResourceIdFrom(trueSubscriptionsUnfinished[i].value[3]) === fromId) {
              find = trueSubscriptionsUnfinished.splice(i, 1)[0];
              break;
            }
          }
          if (find) {
            find.value[2] = time;
            trueSubscriptionsFinished.push(find);
          }
        }
        yData['symbol'] = types['DELETE'].symbol;
        yData['itemStyle'] = {
          color: types['DELETE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
      case 'UPDATE': {
        yData['symbol'] = types['UPDATE'].symbol;
        yData['itemStyle'] = {
          color: types['UPDATE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
      case 'RETRIEVE': {
        yData['symbol'] = types['RETRIEVE'].symbol;
        yData['itemStyle'] = {
          color: types['RETRIEVE'].color
        };
        yData['value'] = [time, platIdx, command];
        interoperationData.push(yData);
        break;
      }
    }
  }
}

function renderItem(params, api) {
  let categoryIndex = api.value(0);
  let start = api.coord([api.value(1), categoryIndex]);
  let end = api.coord([api.value(2), categoryIndex]);
  let height = api.size([0, 1])[1] * 0.9;

  let rectShape = echarts.graphic.clipRectByRect({
    x: start[0],
    y: start[1] - height / 2,
    width: end[0] - start[0],
    height: height
  }, {
    x: params.coordSys.x,
    y: params.coordSys.y,
    width: params.coordSys.width,
    height: params.coordSys.height
  });

  return rectShape && {
    type: 'rect',
    transition: ['shape'],
    shape: rectShape,
    style: api.style()
  };
}


</script>

<template>
  <div id="timeAxis" style="height: 100%; width: 100%">
  </div>
</template>

<style scoped>

</style>
