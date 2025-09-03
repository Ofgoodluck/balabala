<script setup >

import * as echarts from 'echarts';
import {onMounted} from "vue";


let option1 = null;
let myChart1 = null;

let option2 = null;
let myChart2 = null;

let option3 = null;
let myChart3 = null;

let option4 = null;
let myChart4 = null;

onMounted(() => {
  let newData = {
    '仿真互操作效用得分': 70.06,
    '仿真计算资源占用率': 60.345,
    '仿真网络带宽利用率': 45.456,
    '仿真内存资源占用率': 76.02
  }
  myChart1 = echarts.init(document.getElementById('score1'));
  myChart2 = echarts.init(document.getElementById('score2'));
  myChart3 = echarts.init(document.getElementById('score3'));
  myChart4 = echarts.init(document.getElementById('score4'));

  setInterval(function () {

    let temp1 = {
      title: {
        text: '当前仿真互操作效用得分',
        textStyle:{
          overflow: 'breakAll',
          fontSize:'16px'
        },
        left: 'center',
        top: '80%',
      },
      axisLabel: {
        color: '#1E90FF'
      },
      tooltip: {
        formatter: '{a} <br/>{b} : {c}'
      },
      color: ['#1E90FF'],
      series: [
        {
          name: '当前仿真互操作效用得分',
          type: 'gauge',
          progress: {
            show: true,
            width: 5
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}',
            fontSize:'20px'
          },
          axisLine:{
            lineStyle:{
              width: 5
            }
          },
          axisTick: {
            splitNumber: 2
          },
          data: [
            {
              value: newData['仿真互操作效用得分'].toFixed(1),
              name: '得分'
            }
          ]
        }
      ]
    };
    myChart1.setOption(temp1)

    let temp2 = {
      title: {
        text: '当前仿真计算资源占用率',
        textStyle:{
          overflow: 'breakAll',
          fontSize:'16px'
        },
        left: 'center',
        top: '80%',
      },
      tooltip: {
        formatter: '{a} <br/>{b} : {c}'
      },
      series: [
        {
          name: '当前仿真计算资源占用率',
          type: 'gauge',
          progress: {
            show: true,
            width: 5
          },
          axisTick: {
            splitNumber: 2
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}',
            fontSize:'20px'
          },
          axisLine:{
            lineStyle:{
              width: 5
            }
          },
          data: [
            {
              value: newData['仿真计算资源占用率'].toFixed(1),
              name: '占用率'
            }
          ]
        }
      ]
    };
    myChart2.setOption(temp2)

    let temp3 = {
      title: {
        text: '当前仿真网络带宽利用率',
        textStyle:{
          overflow: 'breakAll',
          fontSize:'16px'
        },
        left: 'center',
        top: '80%',
      },
      tooltip: {
        formatter: '{a} <br/>{b} : {c}'
      },
      series: [
        {
          name: '当前仿真网络带宽利用率',
          type: 'gauge',
          progress: {
            show: true,
            width: 5
          },
          axisTick: {
            splitNumber: 2
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}',
            fontSize:'20px'
          },
          axisLine:{
            lineStyle:{
              width: 5
            }
          },
          data: [
            {
              value: newData['仿真网络带宽利用率'].toFixed(1),
              name: '利用率'
            }
          ]
        }
      ]
    };
    myChart3.setOption(temp3)

    let temp4 = {
      title: {
        text: '当前仿真存储资源占用率',
        textStyle:{
          overflow: 'breakAll',
          fontSize:'16px'
        },
        left: 'center',
        top: '80%',
      },
      tooltip: {
        formatter: '{a} <br/>{b} : {c}'
      },
      series: [
        {
          name: '当前仿真存储资源占用率',
          type: 'gauge',
          progress: {
            show: true,
            width: 5
          },
          axisTick: {
            splitNumber: 2
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}',
            fontSize:'20px'
          },
          axisLine:{
            lineStyle:{
              width: 5
            }
          },
          data: [
            {
              value: newData['仿真内存资源占用率'].toFixed(1),
              name: '占用率'
            }
          ]
        }
      ]
    };
    myChart4.setOption(temp4)
  },1000)

  option1 && myChart1.setOption(option1);
  option2 && myChart2.setOption(option2);
  option3 && myChart3.setOption(option3);
  option4 && myChart4.setOption(option4);

  window.electronAPI.receiveSimulationScore(data => {
    newData = data;
    // console.log(newData);
  });

  window.addEventListener('resize', () => {
    myChart1.resize()
    myChart2.resize()
    myChart3.resize()
    myChart4.resize()
  })
})
</script>

<template>
    <div class="container">
      <div id="score1" style="height: 50%; width: 50%"></div>
      <div id="score2" style="height: 50%; width: 50%"></div>
      <div id="score3" style="height: 50%; width: 50%"></div>
      <div id="score4" style="height: 50%; width: 50%"></div>
    </div>

</template>

<style scoped>
.container{
  display: flex;
  flex-wrap: wrap;
  height: 100%;
  width: 100%;
}
</style>