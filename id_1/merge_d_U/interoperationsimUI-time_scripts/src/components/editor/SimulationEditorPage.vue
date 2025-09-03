<script setup>

import {onMounted, reactive, ref} from "vue";
import G6 from '@antv/g6';
import AddNodeComponents from "@/components/editor/model/AddNode.vue";
import UpdateEdgeInfo from "@/components/editor/model/UpdateEdgeInfo.vue";
import UpdateNodeInfo from "@/components/editor/modal/UpdateNodeInfo.vue";
import UploadSimFile from "@/components/editor/modal/UploadSimFile.vue";
import {message, notification} from "ant-design-vue";
import {EditOutlined} from '@ant-design/icons-vue'
import {useRouter} from "vue-router";
import {makeSimulationFile} from "@/components/Util/SimulationDescriptionFileUtil";
import {useSimulationEditorInfoStore} from "@/stores/simulationEditorInfoStore";
import nodePic from '@/assets/元数据管理-浅蓝.png'

let initProps = defineProps([

])

let equipmentSet = reactive([
  // {
  //   name:'radarA',
  //   softwarePath:'/usr/bin/radarA.so',
  //   descriptionPath:'/etc/radarA.json'
  // },
  // {
  //   name:'radarB',
  //   softwarePath:'/usr/bin/radarB.so',
  //   descriptionPath:'/etc/radarB.json'
  // },
  // {
  //   name:'radarC',
  //   softwarePath:'/usr/bin/radarC.so',
  //   descriptionPath:'/etc/radarC.json'
  // }
])

let applist=reactive([])

let nodesSet = reactive({

})

let edgesSet = reactive({

})

let resultSoftwarePath = ref('')
let interoperateTemplate = ref('')
let templateName = ref('')
let templatePath = ref('')

let commitOpen = ref(false)
let addNodeOpen = ref(false)
let updateEdgeInfoOpen = ref(false)
let updateEdgeChoose = reactive({
  id:'',
  from:'',
  to:''
})
let updateNodeChoose = ref('')
let updateNodeInfoOpen = ref(false)
let graph = null
let router = null
let taskName = useSimulationEditorInfoStore().simulationTaskName
onMounted(()=>{

  equipmentSet=useSimulationEditorInfoStore().equipmentInfo
  applist = useSimulationEditorInfoStore().applistInfo

  console.log('applist'+JSON.stringify(applist))
  router= useRouter()
  const data = {
    nodes: [
      // { id: 'node1', x: 350, y: 200 ,label:"node1"},
      // { id: 'node2', x: 350, y: 250 ,label:"node2"},
      // { id: 'node3', x: 100, y: 200 ,label:"node3"},
    ],
  };
  // for (let i = 1; i <=10; i++) {
  //   let randomNumber = Math.floor(Math.random() * 300) - 100;
  //   data.nodes.push({
  //     id: i.toString(), x: 100+randomNumber, y: 200-randomNumber ,label:i.toString()
  //   })
  // }

  const tooltip = new G6.Tooltip({
    offsetX: 10,
    offsetY: 20,
    getContent(e) {
      const outDiv = document.createElement('div');
      outDiv.style.width = '180px';
      let id = e.item.getModel().id

      if (e.item.getType()==='node'){
        let nodesIdSet = Object.keys(nodesSet)
        if (nodesIdSet.includes(id)){
          console.log("nodeset"+JSON.stringify(nodesSet[id]))
          outDiv.innerHTML = `
      <h4>节点配置信息</h4>
      <ul>
        <li>平台id: ${id}</li>
        <li>平台IP: ${nodesSet[id].ip}</li>
        <li>平台名称: ${nodesSet[id].name}</li>
        <li>计算资源配额: ${nodesSet[id].computationSetting}</li>
        <li>内存资源配额: ${nodesSet[id].memorySetting}</li>
        <li>角色定义: ${nodesSet[id].role}</li>
        <li>平台装备配置: ${nodesSet[id].equipments}</li>
                <li>平台应用配置: ${nodesSet[id].applist}</li>
      </ul>`
        }else {
          outDiv.innerHTML = `
      <h4>连接配置</h4>
      <ul>
        <li>该节点尚未配置，请配置</li>
      </ul>`
        }
      }

     else if (e.item.getType()==='edge') {
        let edgesIdSet = Object.keys(edgesSet)
        if (edgesIdSet.includes(id)) {
          outDiv.innerHTML = `
      <h4>连接配置信息</h4>
      <ul>
        <li>带宽: ${edgesSet[id].bandWidth}</li>
        <li>时延: ${edgesSet[id].delay}</li>
        <li>丢包率: ${edgesSet[id].lossRate}</li>
      </ul>`
        }
      }
      return outDiv
    },
    itemTypes: ['node','edge'],
  },
  );


  graph = new G6.Graph({
    container: 'container',
    linkCenter: true,
    plugins:[tooltip],
    modes: {
      default: [ 'drag-canvas',
        'zoom-canvas',
        'drag-node',        {
          type: 'create-edge',

          key:'shift',
          shouldBegin: e => {

            return true;
          },
          shouldEnd: e => {
            return true;
          },
          // update the sourceAnchor
          getEdgeConfig: () => {
            return {
              // sourceAnchor: sourceAnchorIdx
            }
          }
        }],
    },
    defaultNode: {
      type: 'image',
      img: nodePic,
      size: 50,
      labelCfg: {
        position: 'bottom',
        offset: 10,
        style: {
          fill: '#c2a337',
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
        size: 20,
        refY: 10,
        refX: 10,
        position: 'center', // 其实默认就是 center，这里写出来便于理解
        autoRotate: true,   // 使文本随边旋转
        style: {
          lineWidth: 0.8,     // 文本白边粗细
          fill: '#ffffff',  // 文本颜色
        }
      }
    },
  });

  graph.data(data);
  graph.render();

  graph.on('edge:dblclick',e=>{
    graph.removeItem(e.item)
  })
  graph.on('node:dblclick',e=>{
    updateNodeChoose.value = e.item.getModel().id
    updateNodeInfoOpen.value = true
  })



  graph.on('aftercreateedge', (e) => {
    const edges = graph.save().edges;
    G6.Util.processParallelEdges(edges);
    graph.getEdges().forEach((edge, i) => {
      graph.updateItem(edge, {
        curveOffset: edges[i].curveOffset,
        curvePosition: edges[i].curvePosition,
      });
    });

    updateEdgeChoose.id = e.edge.getModel().id
    updateEdgeChoose.from = e.edge.getModel().source
    updateEdgeChoose.to = e.edge.getModel().target

    console.log("up"+JSON.stringify(updateEdgeChoose))
    updateEdgeInfoOpen.value= true
  });


})


function addNodeFinishHandler(nodeInfo){
  console.log("nodeInfo"+JSON.stringify(nodeInfo))
  graph.addItem("node", nodeInfo)
  nodesSet[nodeInfo.id] = {
    ip: nodeInfo.node_ip,
    name: nodeInfo.node_name,
    nodeId: nodeInfo.id,
    computationSetting: nodeInfo.node_computationSetting,
    memorySetting: nodeInfo.node_memorySetting
  }
  addNodeOpen.value = false
}



function updateEdgeInfoFinishHandler(edgeInfo){
  console.log("edgeinfo"+JSON.stringify(edgeInfo))

  let label = "带宽"+edgeInfo.bandWidth+" 时延"+edgeInfo.delay+" 丢包率"+edgeInfo.lossRate
  let targetEdge = graph.findById(edgeInfo.edgeId)
  if (targetEdge!==undefined){
    edgesSet[edgeInfo.edgeId]={
      id:edgeInfo.edge,
      from:updateEdgeChoose.from,
      to:updateEdgeChoose.to,
      bandWidth:edgeInfo.bandWidth,
      delay:edgeInfo.delay,
      lossRate:edgeInfo.lossRate
    }

    graph.updateItem(targetEdge, {
      label: label
    });
  }
  updateEdgeInfoOpen.value= false
}
function saveStringToFile (name,content)  {
  const json = content;
  const filename = name+'.json';

  const jsonString = JSON.stringify(json);
  const blob = new Blob([jsonString], { type: 'application/json' });

  const url = URL.createObjectURL(blob);

  const element = document.createElement('a');
  element.setAttribute('href', url);
  element.setAttribute('download', filename);
  element.style.display = 'none';
  document.body.appendChild(element);
  element.click();
  document.body.removeChild(element);

  URL.revokeObjectURL(url);
}
function uploadReadyHandler(uploadInfoArray){
  resultSoftwarePath.value = uploadInfoArray[0]
  interoperateTemplate.value = uploadInfoArray[1]
   templateName.value = uploadInfoArray[1]
   templatePath.value = uploadInfoArray[2]
  commitOpen.value=false

  let finalSimulationFile = makeSimulationFile(taskName,applist,equipmentSet,edgesSet,nodesSet,resultSoftwarePath.value,templateName.value,templatePath.value)

  console.log("final result"+JSON.stringify(finalSimulationFile))
  saveStringToFile(taskName,finalSimulationFile)
}

function updateNodeInfoFinishHandler (nodeInfo){
  console.log('nodeInfo' + JSON.stringify(nodeInfo))
  updateNodeInfoOpen.value = false
  let id = updateNodeChoose.value
  let item = nodesSet[id]
  item['nodeId']=nodeInfo.node
  item['role']=nodeInfo.role
  item['equipments']=nodeInfo.equipments
  item['applist']=nodeInfo.applist

  // nodesSet[id]={
  //   nodeId:nodeInfo.node,
  //   role:nodeInfo.role,
  //   equipments:nodeInfo.equipments,
  //   applist:nodeInfo.applist
  // }
  equipmentSet = reactive(nodeInfo.allEquipments)
  console.log("nodeset"+JSON.stringify(nodesSet))
}

function commitBtnClick() {
  let hasSetNodes = Object.keys(nodesSet);
  let graphNodes = graph.getNodes()
  if (hasSetNodes.length===graphNodes.length){
    commitOpen.value = true
  }
  else {
    notification.open({
      message: '仍有平台未配置角色',
    });
  }
}
function backBtnClick(){
  router.push('./')
}


</script>

<template>

  <div style="width: 100%; background-color: #100c2a">
    <div class="head-between" >

      <div style="display: flex;" >
        <EditOutlined  style="color: white; font-size: 35px"/>
        <a-typography-title :level="3" style="color: white">仿真推演描述文件编辑系统</a-typography-title>


      </div>







      <div class="right">
        <a-button  @click="addNodeOpen=true" type="primary" style="margin-right: 5px">添加节点</a-button>
        <br>
        <a-button  @click="commitBtnClick" type="primary" style="margin-right: 5px">生成方案</a-button>
        <br>
        <a-button  @click="backBtnClick" type="primary" style="margin-right: 5px">返回主页</a-button>
      </div>

    </div>

    <div id="container" ref="graphRef" style="background-color: #100c2a;height: 100%; width: 100%">

    </div>
  </div>



  <div>
    <a-modal v-model:open="addNodeOpen" :footer="null" >
      <AddNodeComponents @addNodeFinish=addNodeFinishHandler></AddNodeComponents>
    </a-modal>
  </div>

  <div>
    <a-modal v-model:open="updateEdgeInfoOpen" :footer="null" :title ="updateEdgeChoose.from+'与'+updateEdgeChoose.to+'链接参数'">
      <UpdateEdgeInfo :from='updateEdgeChoose.from' :to='updateEdgeChoose.to' :edgeId = 'updateEdgeChoose.id' @updateEdgeInfoFinish =updateEdgeInfoFinishHandler ></UpdateEdgeInfo>
    </a-modal>
  </div>

  <div>
    <a-modal v-model:open="updateNodeInfoOpen" :footer="null" :title = "updateNodeChoose+'参数设置'" >
      <UpdateNodeInfo :existed-equipments = 'equipmentSet' :node-id="updateNodeChoose" :existed-applist="applist" @updateNodeInfoFinish = updateNodeInfoFinishHandler></UpdateNodeInfo>
    </a-modal>
  </div>


  <div>
    <a-modal v-model:open = "commitOpen" :footer="null">
      <UploadSimFile @UploadReady='uploadReadyHandler'
      ></UploadSimFile>
    </a-modal>

  </div>



</template>

<style scoped>
.right {
  display: flex;
  justify-content: flex-end;
}

.head-between {
  padding-left: 10px;
  padding-right: 10px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  margin-top: 10px;
}
</style>