<template>
  <a-radio-group id="interactModeSelector" v-model:value="interactMode" style="height: 5%; width: 100%">
    <a-radio-button value="default" ghost>信息显示</a-radio-button>
    <a-radio-button value="addNode" ghost>编辑节点</a-radio-button>
    <a-radio-button value="addEdge" ghost>编辑链路</a-radio-button>
  </a-radio-group>
  <div style="height: 100%; width: 100%">
    <div id="container" style="background-color: #a7d0dc; height: 95%; width: 100%">

    </div>
  </div>

  <a-modal v-model:open="addNodeOpen" :footer="null">
    <AddNodeComponents @addNodeFinish=addNodeFinishHandler></AddNodeComponents>
  </a-modal>

  <a-modal v-model:open="updateEdgeInfoOpen" :footer="null"
           :title="updateEdgeChoose.from+'与'+updateEdgeChoose.to+'链接参数'">
    <UpdateEdgeInfo :from='updateEdgeChoose.from' :to='updateEdgeChoose.to' :edgeId='updateEdgeChoose.id'
                    @updateEdgeInfoFinish=updateEdgeInfoFinishHandler></UpdateEdgeInfo>
  </a-modal>

</template>

<script setup>
import G6 from "@antv/g6";
import {onMounted, reactive, ref} from "vue";
import nodePic from "@/assets/元数据管理-浅蓝.png";
import AddNodeComponents from "@/components/editor/model/AddNode.vue";
import UpdateEdgeInfo from "@/components/editor/model/UpdateEdgeInfo.vue";
import {useEditInfoStore} from "@/stores/EditInfoStore";

const interactMode = ref('default')
let graph = null
const clickPos = reactive({})
const addingEdge = reactive({state: false, edge: null})
let addNodeOpen = ref(false)
let updateEdgeInfoOpen = ref(false)
let updateEdgeChoose = reactive({
  id: '',
  from: '',
  to: ''
})
const editInfoStore = useEditInfoStore()

const addNodeFinishHandler = (nodeInfo) => {
  console.log("nodeInfo" + JSON.stringify(nodeInfo))
  nodeInfo.x = clickPos.x
  nodeInfo.y = clickPos.y
  graph.addItem("node", nodeInfo)
  addNodeOpen.value = false
}
const updateEdgeInfoFinishHandler = (edgeInfo) => {
  console.log('updateEdgeInfoFinishHandler ',"edgeinfo" + JSON.stringify(edgeInfo))

  let label = "带宽" + edgeInfo.bandWidth + "kb/s 时延" + edgeInfo.delay + "ms 丢包率" + edgeInfo.lossRate + "%"
  const edge = graph.findById(updateEdgeChoose.id)
  graph.updateItem(edge, {
    label: label
  })
  updateEdgeInfoOpen.value = false
}


onMounted(() => {
  const data = {
    nodes: [
      // { id: 'node1', x: 350, y: 200 ,label:"node1"},
      // { id: 'node2', x: 350, y: 250 ,label:"node2"},
      // { id: 'node3', x: 100, y: 200 ,label:"node3"},
    ],
  };
  graph = new G6.Graph({
    container: 'container',
    width: 500,
    height: 500,
    linkCenter: true,
    // plugins: [tooltip],
    modes: {
      // 默认交互模式
      default: ['drag-node', 'click-select', {
        type: 'tooltip',
        offset: 10,
        formatText(model) {
          console.log(model)
          if (model.type === 'image') {
            return `
                    <h4>节点配置信息</h4>
                    <ul>
                      <li>平台ID: ${model.id}</li>
                      <li>平台IP: ${model.node_ip}</li>
                      <li>平台名称: ${model.node_name}</li>
                      <li>计算资源配额: ${model.node_computationSetting}</li>
                      <li>内存资源配额: ${model.node_memorySetting}</li>
                      <li>角色定义: ${model.node_role}</li>
                      <li>平台装备配置: ${JSON.stringify(model.node_equipmentsSetting)}</li>
                      <li>平台应用配置: ${JSON.stringify(model.node_applicationsSetting)}</li>
                    </ul>`
          }
        }
      }],
      // 增加节点交互模式
      addNode: ['click-add-node', 'click-select'],
      // 增加边交互模式
      addEdge: ['click-add-edge', 'click-select'],
    },
    nodeStateStyles: {
      hover: {
        'text-shape': {
          opacity: 0.4,
        }
      },
      selected: {
        stroke: '#666',
        lineWidth: 2,
        fill: 'steelblue'
      }
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
      type: 'line',
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
    fitView: true
  });

  graph.data(data);
  graph.render();

  document.getElementById('interactModeSelector').addEventListener('change', ev => {
    const value = ev.target.value;
    // 切换交互模式
    graph.setMode(value);
  })

  window.addEventListener('resize', () => {
    if (!graph || graph.get('destroyed')) return
    let container = document.getElementById('container')
    let height = container.offsetHeight
    let width = container.offsetWidth
    if (!width || !height) return
    graph.changeSize(width, height)
  })

  // 封装点击添加节点的交互
  G6.registerBehavior('click-add-node', {
    // 设定该自定义行为需要监听的事件及其响应函数
    getEvents() {
      return {
        'canvas:click': 'onLClick',
        'node:click': 'onNodeLClick',
        'node:contextmenu': 'onNodeRClick'
      };
    },
    onLClick(ev) {
      console.log('click-add-node')
      addNodeOpen.value = true
      // 在图上新增一个节点
      clickPos.x = ev.canvasX
      clickPos.y = ev.canvasY
    },
    onNodeLClick(ev) {
      console.log('onNodeLClick: ', ev)
      const node = ev.item
      addNodeOpen.value = true
    },
    onNodeRClick(ev) {
      console.log('onNodeRClick: ', ev)
      const node = ev.item
      editInfoStore.removeNode(node.getModel().node_name)
      graph.removeItem(node)
    }
  });

  // 封装点击添加边的交互
  G6.registerBehavior('click-add-edge', {
    // 设定该自定义行为需要监听的事件及其响应函数
    getEvents() {
      return {
        'node:click': 'onNodeClick',
        mousemove: 'onMousemove',
        'edge:click': 'onEdgeLClick',
        'edge:contextmenu': 'onEdgeRClick',
      };
    },
    // getEvents 中定义的 'node:click' 的响应函数
    onNodeClick(ev) {
      console.log('onNodeClick', ev)
      const node = ev.item
      // 鼠标当前点击的节点的位置
      const point = {x: ev.x, y: ev.y}
      const model = node.getModel()
      if (addingEdge.state && addingEdge.edge) {
        if (graph.find('edge', (edge) => {
          return edge.getModel().id === addingEdge.sourceId + '-' + model.id
        })) {
          //判断是否是重复边
          console.log('is repeat')
          console.log(graph)
          graph.removeItem(addingEdge.edge)
          // console.log(graph)
          // console.log(graph.save())
          // {
          //   const data = graph.save()
          //   data.edges = data.edges.filter(item => {
          //     console.log(item.id, addingEdge.edge.getModel().id)
          //     return item.id !== addingEdge.edge.getModel().id
          //   })
          //   graph.changeData(data)
          // }
          addingEdge.edge = null
          addingEdge.state = false
          return
        }
        graph.updateItem(addingEdge.edge, {
          id: addingEdge.sourceId + '-' + model.id,
          target: model.id,
        })
        updateEdgeChoose.id = addingEdge.edge.get('id')
        updateEdgeChoose.from = addingEdge.sourceId
        updateEdgeChoose.to = model.label
        addingEdge.edge = null
        addingEdge.state = false
        updateEdgeInfoOpen.value = true
      } else {
        // 在图上新增一条边，结束点是鼠标当前点击的节点的位置
        addingEdge.edge = graph.addItem('edge', {
          source: model.id,
          target: point,
        });
        addingEdge.sourceId = model.label
        addingEdge.state = true;
      }
    },
    // getEvents 中定义的 mousemove 的响应函数
    onMousemove(ev) {
      // 鼠标的当前位置
      const point = {x: ev.x, y: ev.y};
      if (addingEdge.state && addingEdge.edge) {
        // 更新边的结束点位置为当前鼠标位置
        graph.updateItem(addingEdge.edge, {
          target: point,
        });
      }
    },
    //getEvents 中定义的 'edge:click' 的响应函数
    onEdgeLClick(ev) {
      console.log('onEdgeLClick', ev)
      const currentEdge = ev.item
      // 拖拽过程中，点击会点击到新增的边上
      if (addingEdge.state && addingEdge.edge === currentEdge) {
        graph.removeItem(addingEdge.edge)
        addingEdge.edge = null
        addingEdge.state = false
      }
      console.log('onEdgeLClick', graph)
    },
    onEdgeRClick(ev) {
      console.log('onEdgeRClick', ev)
      const currentEdge = ev.item
      graph.removeItem(currentEdge)
      console.log(graph)
      addingEdge.edge = null
      addingEdge.state = false
    },
  });
})


</script>

<style scoped>

#container {
  position: relative;
}

</style>
