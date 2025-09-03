<template>
  <div class="topoInfo">
    <div class="titleTopo">Mininet网络拓扑</div>
    <div class="topoChart" id="realTopoContainer"></div>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";
import G6 from "@antv/g6";
import platformLogo from '@/assets/元数据管理-浅蓝.png'
import switchLogo from '@/assets/交换机1.png'


let activeKey = ref('')
let tabList = ref([])

let nodesSet = reactive({})

let edgesSet = reactive({})


let infoStore = useSimulationProjectInfoStore()
let graph = null
let graphContent = null

function handleResize() {
  if (!graph || graph.get('destroyed')) return
  let container = document.getElementById('realTopoContainer')
  let height = container.offsetHeight
  let width = container.offsetWidth
  if (!width || !height) return
  graph.changeSize(width, height)
}

onMounted(() => {
  tabList.value = infoStore.projectList
  activeKey.value = tabList.value[0]
  infoStore.chooseProject = activeKey.value
  graphContent = infoStore.getGraphContentByChoose
  nodesSet = infoStore.getGraphNodesSet
  edgesSet = infoStore.getGraphEdgesSet
  let tempNodes = []
  let tempEdges = []

  for (let key in nodesSet) {
    tempNodes.push({
      'id': key+'交换机',
      'label': key+'交换机',
      'x': Math.random() * 100,
      'y': Math.random() * 100,
      'img': switchLogo
    })
    tempNodes.push({
      'id': key,
      'label': key,
      'x': 100 + Math.random() * 100,
      'y': 100 + Math.random() * 100,
      'img': platformLogo
    })
    tempEdges.push({'id': key + '-' + key+'交换机', 'source': key, 'target': key+'交换机'})
  }

  for (let key in edgesSet) {
    let from = edgesSet[key].from
    let to = edgesSet[key].to
    let from_id = from+'交换机'
    let to_id = to+'交换机'
    tempEdges.push({
      'id': from_id + '-' +  to_id,
      'source': from_id,
      'target': to_id
    })
  }

  graphContent.nodes = tempNodes
  graphContent.edges = tempEdges

  const tooltip = new G6.Tooltip({
        offsetX: 10,
        offsetY: 20,
        getContent(e) {
          const outDiv = document.createElement('div');
          outDiv.style.width = '180px';
          let id = e.item.getModel().label

          if (e.item.getType() === 'node') {
            let nodesIdSet = Object.keys(nodesSet)

            if (nodesIdSet.includes(id)) {
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
            } else {
              outDiv.innerHTML = `
                <h4>信息</h4>
                <ul>
                  <li>未获取到节点信息</li>
                </ul>`
            }
          }
          return outDiv
        },
        itemTypes: ['node', 'edge'],
      },
  );


  let width = 520
  let height = 310

  graph = new G6.Graph({
    container: 'realTopoContainer',
    width: width,
    height: height,
    linkCenter: true,
    fitCenter: true,
    layout: {
      type: 'force2',
      linkDistance: 50
    },
    plugins: [tooltip],
    modes: {
      default: ['drag-canvas',
        'zoom-canvas',
        'drag-node'],
    },
    defaultNode: {
      type: 'image',
      size: 30,
      labelCfg: {
        position: 'bottom',
        offset: 10,
        style: {
          fill: '#666',
          fontSize: 16
        },
      },
      anchorPoints: [
        [0, 0.5], // 左侧中间
        [1, 0.5], // 右侧中间
      ]
    },
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

  graph.data(graphContent);
  graph.render();

  window.addEventListener('resize', handleResize)
})
</script>

<style>
.topoInfo {
  height: 100%;
  width: 100%;
}

.titleTopo {
  height: 7%;
  width: 100%;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
}

.topoChart {
  height: 90%;
  margin-left: 10px;
  margin-right: 10px;
}
</style>
