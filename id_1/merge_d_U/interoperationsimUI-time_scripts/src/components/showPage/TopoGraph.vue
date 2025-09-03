<template>



<!--      <a-tabs v-model:activeKey="activeKey" @change="tabChangeHandler"-->

<!--              style=" color: white;background-color: #100c2a;width: 100%; height: 10%; padding-left: 20px;">-->
<!--        <a-tab-pane :key="tabItem" :tab="tabItem" v-for="tabItem of tabList"></a-tab-pane>-->
<!--      </a-tabs>-->
<!--  <div style="height: 1%; background-color: #238cb9"></div>-->
      <div id="container_info" style="height: 100%; width: 100%;">

      </div>








</template>
<script setup>
import {onMounted, reactive, ref} from 'vue';
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";
import G6 from "@antv/g6";
import platformLogo from '@/assets/元数据管理-浅蓝.png'

let activeKey = ref('')
let buttonText = ref('')
let tabList = ref([])

let nodesSet = reactive({

})

let edgesSet = reactive({

})


let infoStore = useSimulationProjectInfoStore()
let graph = null
let graphContent =null

function tabChangeHandler(){

  infoStore.setChooseProject(activeKey.value)
  console.log("list"+useSimulationProjectInfoStore().getPlatformIdList)
  nodesSet =infoStore.getGraphNodesSet
  edgesSet = infoStore.getGraphEdgesSet
  graph.data(infoStore.getGraphContentByChoose);
  graph.render();
}

function handleResize () {
  if (!graph || graph.get('destroyed')) return
  let container = document.getElementById('container_info')
  let height = container.offsetHeight
  let width = container.offsetWidth
  if (!width || !height) return
  graph.changeSize(width, height)
}

onMounted(()=>{


  handleResize()
  window.addEventListener('resize', handleResize)

  tabList.value = infoStore.projectList
  activeKey.value = tabList.value[0]
  infoStore.chooseProject = activeKey.value
  graphContent =infoStore.getGraphContentByChoose

  nodesSet =infoStore.getGraphNodesSet
  edgesSet = infoStore.getGraphEdgesSet

  console.log('nodes'+JSON.stringify(nodesSet))

  const tooltip = new G6.Tooltip({
        offsetX: 10,
        offsetY: 20,
        getContent(e) {
          const outDiv = document.createElement('div');
          outDiv.style.width = '180px';
          let id = e.item.getModel().label


          if (e.item.getType()==='node'){
            let nodesIdSet = Object.keys(nodesSet)

            if (nodesIdSet.includes(id)){
              outDiv.innerHTML = `
      <h4>节点配置信息</h4>
      <ul>
        <li>平台id: ${nodesSet[id].nodeId}</li>
        <li>平台IP: ${nodesSet[id].ip}</li>
        <li>平台名称: ${nodesSet[id].name}</li>
        <li>角色定义: ${nodesSet[id].role}</li>
        <li>平台装备配置: ${nodesSet[id].equipments}</li>
        <li>平台应用配置: ${nodesSet[id].applist}</li>
      </ul>`
            }else {
              outDiv.innerHTML = `
      <h4>信息</h4>
      <ul>
        <li>未获取到节点信息</li>
      </ul>`
            }
          }

          else if (e.item.getType()==='edge') {
            id = e.item.getModel().id
            let edgesIdSet = Object.keys(edgesSet)
            if (edgesIdSet.includes(id)) {
              outDiv.innerHTML = `
      <h4>连接配置信息</h4>
      <ul>
        <li>带宽: ${edgesSet[id].bandWidth} kb/s</li>
        <li>时延: ${edgesSet[id].delay} ms</li>
        <li>丢包率: ${edgesSet[id].lossRate} %</li>
      </ul>`
            }
          }
          return outDiv
        },
        itemTypes: ['node','edge'],
      },
  );


  graph = new G6.Graph({
    container: 'container_info',
    linkCenter: true,
    fitCenter:true,
    layout: {
      type: 'force2',
      linkDistance: 400,

    },
    plugins:[tooltip],
    modes: {
      default: [ 'drag-canvas',
        'zoom-canvas',
        'drag-node'],
    },
    defaultNode: {
      type: 'image',
      size:50,
      img: platformLogo,
      labelCfg: {
        position: 'bottom',
        offset: 10,
        style: {
          fill: '#666',
        },
      },
      anchorPoints: [
        [0, 0.5], // 左侧中间
        [1, 0.5], // 右侧中间
      ]
    },
    // defaultNode: {
    //   type: 'circle',
    //   size: 50,
    //   style: {
    //     fill: '#4992ff',
    //     stroke: '#4992ff',
    //
    //   }
    // },
    defaultEdge: {
      type: 'quadratic',
      style: {
        stroke: '#F6BD16',
        lineWidth: 2,
      },
      labelCfg: {
        refY: 10,
        refX: 10,
        position: 'center', // 其实默认就是 center，这里写出来便于理解
        autoRotate: true,   // 使文本随边旋转
        style: {
          lineWidth: 1,     // 文本白边粗细
          fill: '#3580a1',  // 文本颜色
        }
      }
    },
  });

  graph.on('node:click',(e)=>{
    console.log("click"+e.item.getModel().label)
    useSimulationProjectInfoStore().setChoosePlatform(e.item.getModel().label)
  })

  graph.on('edge:click', (e)=>{
    useSimulationProjectInfoStore().setChooseEdges(e.item.getModel().id)
  })


  graph.data(graphContent);
  graph.render();


})


</script>
