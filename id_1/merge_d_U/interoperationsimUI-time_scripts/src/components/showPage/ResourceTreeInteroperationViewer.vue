<script setup>
/**
 * 入口函数在onMounted
 */
import {VueFlow, useVueFlow, Position, MarkerType} from '@vue-flow/core'
import {Background} from '@vue-flow/background'
import {Controls} from '@vue-flow/controls'
import {MiniMap} from '@vue-flow/minimap'
import {onMounted, watch} from 'vue'
import CustomNode from "@/components/showPage/CustomNode.vue";
import {Mutex, Semaphore, withTimeout} from 'async-mutex';
import {useSimulationProjectInfoStore} from "../../stores/simulationProjectInfoStore.js";

const PaddingBetweenNodes = 5
const ChildNodeFontSize = 18
const emit = defineEmits(['exportCreateOperationPairEdge', 'onInteroperationEdgeClicked'])
const inputArguments = defineProps(['crmsAddr']) //crmsAddr会被fetch函数使用
const HandleNames = {
  ParentInput: 'parentInput',
  ChildrenOutput: 'childrenOutput',
  InteropFrom: 'interopSource',
  InteropTo: 'InteropTarget'
}

const HandleTypes = {
  Source: 'source',
  Target: 'target'
}
//用于存储互操作关系的边
let pairs = []

const store = useSimulationProjectInfoStore()

/**
 * 构造一个表示互操作关系的边
 * @param pair 分别包含互操作的from资源，to资源和互操作执行了什么操作的string
 */
function createOperationPairEdge(pair) {
  let from = pair.from
  let to = pair.to
  let op = pair.operation === 'SUBSCRIPTION' ? '通知' : '查询'
  graphInstance.addEdges({
    id: `interoperation:${from}=>${to}`,
    source: from,
    target: to,
    animated: true,
    label: op,
    data: pair,
    labelBgPadding: [8, 4],
    labelBgBorderRadius: 4,
    labelBgStyle: {fill: '#FFCC00', color: '#fff', fillOpacity: 0.7},
    labelStyle: {fontSize: `${ChildNodeFontSize * 2}px`},
    style: {stroke: '#660000', strokeWidth: 10},
    // type: 'custom',
    sourceHandle: HandleNames.InteropFrom,
    targetHandle: HandleNames.InteropTo,
    markerEnd: MarkerType.ArrowClosed,
    zIndex: 10
  })
  pairs.push(pair)
}

/**
 * 删除一个表示互操作关系的边
 * @param pair 分别包含互操作的from资源，to资源和互操作执行了什么操作的string
 */
function deleteOperationPairEdge(pair) {
  let from = pair.from
  let to = pair.to
  if (to !== null) {
    graphInstance.removeEdges(`interoperation:${from}=>${to}`);
    let deleteIndex = pairs.findIndex((item) => {
      return item.from === from && item.to === to
    })
    if (deleteIndex >= 0) {
      pairs.splice(deleteIndex, 1)
    }
  } else {
    const filteredPairs = pairs.filter(item => {return item.from === from});
    filteredPairs.forEach((item) => {
      graphInstance.removeEdges(`interoperation:${from}=>${item.to}`);
    });
    pairs = pairs.filter(item => item.from !== from);
  }
}

function flashEdge(data) {
  let from = data['邻居名称'] + data['数据来源']
  const matchingPairs = pairs.filter((obj) => obj.from === from)
  for (const pair of matchingPairs) {
    if (pair) {
      let edge = graphInstance.getEdge.value(`interoperation:${pair.from}=>${pair.to}`)
      if (edge !== undefined) {
        edge.style.stroke = '#FE7A36'
        setTimeout(() => {
          edge.style.stroke = '#660000'
        }, 500);
      }
    }
  }
}

/**
 * 该函数在onMounted回调中被调用，用于向父组件导出 createOperationPairEdge函数，这样父组件就可以让本组件构建一条新的互操作关系边
 */
function emitInterface() {
  emit("exportCreateOperationPairEdge", {
    newOperationPair: (pair) => createOperationPairEdge(pair)
  })
}

const ResourceIdExclusion = [
  "/root/资源树控制",
  "/root/资源调度指令信息"
]
const graphInstance = useVueFlow({
  fitViewOnInit: true,
  // set this to true so edges get elevated when selected, defaults to false
  elevateEdgesOnSelect: true,
  nodes: [],
})

function isExcludedResource(resourceId) {
  for (const idx in ResourceIdExclusion) {
    const exc = ResourceIdExclusion.at(idx)
    if (resourceId.startsWith(exc)) {
      return true
    }
  }
  return false
}

//注册vueflow的当edge被单击的回调，如果被点击的edge是互操作关系edge，那么会触发 onInteroperationEdgeClicked 回调，通知父组件

graphInstance.onEdgeClick((event) => {
  if (event.edge.id.startsWith("interoperation:")) {
    emit('onInteroperationEdgeClicked', {
      from: event.edge.source,
      to: event.edge.target,
      operation: event.edge.label,
      plat:event.edge.data.plat,
      time:event.edge.data.time,
      content:event.edge.data.content,
      delay:event.edge.data.delay
    })
  }
})

graphInstance.onEdgeMouseEnter((event) => {
  if (event.edge.id.startsWith("interoperation:")) {
    event.edge.style.stroke = '#FFCC00'
  }
})

graphInstance.onEdgeMouseLeave((event) => {
  if (event.edge.id.startsWith('interoperation:')) {
    event.edge.style.stroke = '#660000'
  }
})

//按照ResourceObject, Attribute, DataTable, Command的顺序对资源节点的孩子进行排序，其中孩子数量越少的ResourceObject越靠前
function sortTreeByResourceType(treeRoot, resourceDiscoveryResult) {
  let children = treeRoot.resourceChildren
  if (!children) {
    return
  }
  children.sort((child1, child2) => {
    let node1Type = resourceDiscoveryResult[child1.resourceId]
    let node2Type = resourceDiscoveryResult[child2.resourceId]
    if (node1Type === 3) {
      if (node2Type === 3) {
        return child1.resourceChildren.length < child2.resourceChildren.length
      } else {
        return -1
      }
    }
    return node1Type - node2Type
  })
  children.forEach((child, _, __) => {
    sortTreeByResourceType(child, resourceDiscoveryResult)
  })
}

//通过资源discovery结果还原资源树结构
function addResourceTreeNode(name, rootResourceId, resourceDiscoveryResult) {
  const nodesToAdd = []
  const edgesToAdd = []
  const LOTraversalResult = {}
  const containerNode = {
    id: `${name}_container`,
    label: `${name}`,
    position: {x: 100, y: 100},
    draggable: true,
    style: {backgroundColor: 'rgba(0, 185, 129, 0.5)', width: '200px', height: '200px', fontSize: '30px'},
    width: 100,
    height: 100,
    zIndex: 0,
    type: 'default'
  }
  const result = {
    id: `${name}${rootResourceId}`,
    resourceId: rootResourceId,
    label: name + "资源树",
    resourceType: 1,
    resourceChildren: [],
    width: 160,
    height: 40,
    position: {x: 0, y: 0},
    draggable: false,
    parentNode: `${name}_container`,
    type: 'custom',

    expandParent: true,
    style: {fontSize: '16px'},
    data: {
      label: name + "资源树",
      handles: [{id: HandleNames.ChildrenOutput, type: HandleTypes.Source, position: Position.Bottom}],
    },
    zIndex: 1
  }
  nodesToAdd.push(containerNode)
  nodesToAdd.push(result)
  //resourceTree对象的结构是 { 资源ID: 资源类型, 资源ID: 资源类型 }，而且资源ID按照'/'数量排序，
  // 因此可以遍历此对象的所有key，按照'/'层级还原资源父子关系
  for (const resourceId in resourceDiscoveryResult) {
    const path = resourceId.split('/').filter(Boolean)
    let current = result.resourceChildren
    let parent = result
    for (let i = 1; i < path.length; i++) {
      const segment = path[i]
      let find = current.find((obj) => {
        return obj.label === segment
      })
      if (!find && (i === path.length - 1)) {
        find = {
          id: name + resourceId,
          resourceId: resourceId,
          label: segment,
          data: {
            label: segment,
            handles: []
          },
          type: 'custom',
          resourceType: resourceDiscoveryResult[resourceId],
          width: 200,
          height: 40,
          position: {x: 0, y: 0},
          draggable: false,
          style: {backgroundColor: "", fontSize: `${ChildNodeFontSize}px`},
          zIndex: 1,
          parentNode: `${name}_container`,
          expandParent: true,
        }
        find.width = find.label.length * ChildNodeFontSize + 40
        switch (find.resourceType) {
          case 3: {
            find.icon = 'folder'
            find.style.backgroundColor = '#FFFFFB'
            find.resourceChildren = []
            find.data.handles = [{id: HandleNames.ParentInput, type: HandleTypes.Target, position: Position.Left}, {
              id: HandleNames.ChildrenOutput,
              type: HandleTypes.Source,
              position: Position.Bottom
            }]
            break
          }
          case 1101: {
            find.icon = 'label'
            find.style.backgroundColor = '#A8D8B9'
            find.data.handles = [{id: HandleNames.ParentInput, type: HandleTypes.Target, position: Position.Left}, {
              id: HandleNames.InteropTo,
              type: HandleTypes.Target,
              position: Position.Right
            },
              {
                id: HandleNames.InteropFrom,
                type: HandleTypes.Source,
                position: Position.Right
              }]
            break
          }
          case 1102: {
            find.icon = 'table_chart'
            find.style.backgroundColor = '#81C7D4'
            find.data.handles = [{id: HandleNames.ParentInput, type: HandleTypes.Target, position: Position.Left}, {
              id: HandleNames.InteropTo,
              type: HandleTypes.Target,
              position: Position.Right
            },
              {
                id: HandleNames.InteropFrom,
                type: HandleTypes.Source,
                position: Position.Right
              }]
            break
          }
          case 1103: {
            find.icon = 'table_chart'
            find.style.backgroundColor = '#33A6B8'
            find.data.handles = [{id: HandleNames.ParentInput, type: HandleTypes.Target, position: Position.Left}, {
              id: HandleNames.InteropTo,
              type: HandleTypes.Target,
              position: Position.Right
            },
              {
                id: HandleNames.InteropFrom,
                type: HandleTypes.Source,
                position: Position.Right
              }]
            break
          }
        }
        if (!LOTraversalResult[i]) {
          LOTraversalResult[i] = []
        }
        LOTraversalResult[i].push(find)
        current.push(find)
        nodesToAdd.push(find)
        edgesToAdd.push(
            {
              id: name + find.id,
              source: parent.id,
              target: find.id,
              sourceHandle: HandleNames.ChildrenOutput,
              targetHandle: HandleNames.ParentInput,
              type: 'smoothstep',
              style: {stroke: '#000000'}
            })
      }

      if (i + 1 === path.length) {
        break
      }
      if (!find.hasOwnProperty('resourceChildren')) {
        find.resourceChildren = []
      }
      parent = find
      current = find.resourceChildren
    }
  }
  //按照资源类型对children进行排序
  sortTreeByResourceType(result, resourceDiscoveryResult)
  return {
    tree: result, //还原出来的资源树结构
    containerNode: containerNode, //整棵资源树在vueflow中的容器node
    lor: LOTraversalResult, //资源树结构的层序遍历结果，暂时没用
    nodesToAdd: nodesToAdd, //要添加到vueflow的所有node
    edgesToAdd: edgesToAdd  //要添加到vueflow的所有edge
  }
}


function updateHandlePosition(node, handleId, handlePos) {
  const foundHandle = node.data.handles.find((handle) => handle.id === handleId)
  if (!foundHandle) {
    console.error(`cannot find handle ${handleId} in node ${node.id}`)
    return
  }
  foundHandle.position = handlePos
}

//调整一个子树根孩子nodes的位置，资源树是垂直排列的，为了不重叠，各node的位置y会受到同层级其他node的高度影响，还会受到同层级node的孩子以及孩子的孩子...（递归下去）的高度影响
//因此，这个函数的计算分为两步，先计算同层级孩子高度，然后再递归计算孩子的孩子的高度
function adjustSubTreePositionRecursively(subTreeRoot, leftOrRight) {
  let rootWidth = subTreeRoot.width
  let rootHeight = subTreeRoot.height
  let rootX = subTreeRoot.position.x
  let rootY = subTreeRoot.position.y
  let resourceChildren = subTreeRoot.resourceChildren
  if (!resourceChildren) {
    return {maxWidth: rootWidth + PaddingBetweenNodes, yOffset: 0}
  }
  //开始计算本节点所有孩子的位置
  let childHeightSum = 0
  let maxChildWidth = 0
  //首先遍历计算所有孩子node的高度，在计算过程中，根据之前已遍历的孩子高度之和调整当前孩子的y
  for (let childIdx = 0; childIdx < resourceChildren.length; ++childIdx) {
    let childResource = resourceChildren.at(childIdx)
    if (!childResource.resourceChildren) {
      updateHandlePosition(childResource, HandleNames.InteropTo, leftOrRight === -1 ? Position.Left : Position.Right)
      updateHandlePosition(childResource, HandleNames.InteropFrom, leftOrRight === -1 ? Position.Left : Position.Right)
    }
    updateHandlePosition(childResource, HandleNames.ParentInput, leftOrRight === 1 ? Position.Left : Position.Right)
    childResource.position.x = rootX + leftOrRight * (rootWidth + PaddingBetweenNodes) //x调整是以本子树根节点为基准进行偏移，与其他孩子无关
    childResource.position.y = rootY + rootHeight + PaddingBetweenNodes + childHeightSum //y根据之前已遍历的孩子高度之和为基准进行调整
    childHeightSum += childResource.height + PaddingBetweenNodes
    maxChildWidth = Math.max(childResource.width + PaddingBetweenNodes, maxChildWidth)
  }
  let nextYOffset = 0
  let nextLevelWidthMax = 0
  //然后计算孩子的孩子的孩子的...（递归）高度
  for (let childIdx = 0; childIdx < resourceChildren.length; ++childIdx) {
    let childResource = resourceChildren.at(childIdx)
    //nextYOffset就是之前已递归遍历的孩子的孩子高度之和
    childResource.position.y += nextYOffset
    let childInfo = adjustSubTreePositionRecursively(childResource, leftOrRight)
    nextYOffset += childInfo.yOffset
    nextLevelWidthMax = Math.max(childInfo.maxWidth, nextLevelWidthMax)
  }
  //这一项用于计算整棵树containerNode的宽度，也是一个递归累加的值
  maxChildWidth += nextLevelWidthMax
  return {
    maxWidth: maxChildWidth,
    yOffset: nextYOffset + childHeightSum/*最后计算出当前子树的总高度，这个信息会被上层递归用于计算nextYOffset*/
  }
}


//当vueflow的node有parentNode时，node的Position就不能为负值（会被强制改为0），但是根据递归算法的实现
//如果资源树是向左侧展开，那么node的Position就有负值，就需要整体向右移动，使所有的资源节点的x值变正
function moveNodesToRight(rootTreeNode, offset) {
  let resourceChildren = rootTreeNode.resourceChildren
  if (!resourceChildren) {
    return
  }
  for (let childIdx = 0; childIdx < resourceChildren.length; ++childIdx) {
    let childResource = resourceChildren.at(childIdx)
    childResource.position.x += offset - childResource.width
    moveNodesToRight(childResource, offset)
  }
}

//垂直排列资源树节点，资源树节点按照左右排列相对平衡地挂到树上
function adjustNodePositionVertically(rootTreeNode, containerNode, treeLOResult, offsets, leftOrRight /*1 means right, -1 means left*/) {
  let xOffset = offsets.x
  let yOffset = offsets.y
  rootTreeNode.position.y += PaddingBetweenNodes
  rootTreeNode.position.x += PaddingBetweenNodes
  let childInfo = adjustSubTreePositionRecursively(rootTreeNode, leftOrRight)
  // console.log(`${rootTreeNode.id}: ${childInfo.maxWidth}, ${childInfo.yOffset}`)
  containerNode.position.x = xOffset
  containerNode.position.y = yOffset
  containerNode.width = childInfo.maxWidth + 2 * PaddingBetweenNodes //左右各让出PaddingBetweenNodes的间距，不让资源树node正好贴在container的边上
  containerNode.height = childInfo.yOffset + rootTreeNode.height + 2 * PaddingBetweenNodes  //上下各让出PaddingBetweenNodes的间距，不让资源树node正好贴在container的边上
  if (leftOrRight === -1) {
    rootTreeNode.position.x += childInfo.maxWidth - rootTreeNode.width
    moveNodesToRight(rootTreeNode, childInfo.maxWidth)
  }
  return {x: Math.max(childInfo.maxWidth, 1000), y: childInfo.yOffset}
}

function findSubTreeAndRender(data) {
  //资源ID的前缀，这个是过滤子树的条件
  const resourcePrefix = "/root/仿真推演平台级信息/"
  const subTreeRoots = []
  //找到所有的子树根
  for (const resourceId in data) {
    if (!(resourceId.startsWith(resourcePrefix) && resourceId.split('/').length === 4)) {
      continue
    }
    subTreeRoots.push(resourceId)
  }
  let offsets = {x: 0, y: 0}
  let i = 0;
  for (let c = 0; c < Math.ceil(subTreeRoots.length / 3); ++c) {
    let yOffsetMax = 0
    for (let r = 0; r < 3; ++r) {
      if (i >= subTreeRoots.length) {
        return
      }
      const subTreeRootResourceId = subTreeRoots.at(i)
      const splits = subTreeRootResourceId.split('/')
      const subTreeName = splits.at(splits.length - 1)
      const subTreeData = {}
      //根据子树根找对应的子资源
      for (const resourceId in data) {
        if (!resourceId.startsWith(subTreeRootResourceId)) {
          continue
        }
        //将资源ID前缀去掉，还原为以该子树为根的ID
        const restoredResourceId = '/root' + resourceId.substring(subTreeRootResourceId.length)
        //用本代码文件开头定义的数组，过滤资源ID
        if (isExcludedResource(restoredResourceId)) {
          continue
        }
        subTreeData[restoredResourceId] = data[resourceId]
      }
      let tree = addResourceTreeNode(subTreeName, '/root', subTreeData)
      let result = tree.tree
      //层序遍历结果，暂时没有用
      let LOTraversalResult = tree.lor
      let nodesToAdd = tree.nodesToAdd
      let edgesToAdd = tree.edgesToAdd
      let containerNode = tree.containerNode
      let thisOffsets = adjustNodePositionVertically(result, containerNode, LOTraversalResult, offsets, (i % 2 === 0) ? 1 : -1)
      offsets.x += thisOffsets.x
      yOffsetMax = Math.max(thisOffsets.y, yOffsetMax)
      for (let node of nodesToAdd) {
        const findNode = graphInstance.getNode.value(node.id)
        if (findNode) {
          // Object.assign(findNode, node)
          findNode.position.x = node.position.x;
          findNode.position.y = node.position.y;
          findNode.width = node.width;
          findNode.height = node.height;
        } else {
          graphInstance.addNodes(node)
          graphInstance.fitView()
        }
      }
      for (let edge of edgesToAdd) {
        const findEdge = graphInstance.getEdge.value(edge.id)
        if (!findEdge) {
          graphInstance.addEdges(edge)
        }
      }
      ++i
    }
    offsets.x = 0
    offsets.y += yOffsetMax + 100
  }
}

// graphInstance.onNodesInitialized(() => {
//   graphInstance.fitView({includeHiddenNodes: true})
// })

//在组件初始化时获取资源树数据
onMounted(async () => {
  emitInterface()
  graphInstance.zoomOnScroll.value = true
  graphInstance.zoomOnPinch.value = true
  // fetch(`http://${inputArguments.crmsAddr}:8080/discovery`)
  //     .then(response => response.json()).then(data => {
  //       findSubTreeAndRender(data);
  //       console.log(data);
  //     }
  // ).catch(error => console.log(error))
  const dataQueue = []; // 数据队列
  let processingData = false;
  const mutex = new Mutex();

  // 创建一个 Promise 包装的异步函数
  const waitForResourceTreeReady = () => {
    return new Promise((resolve) => {
      window.electronAPI.receiveResourceTreeReady((data) => {
        mutex
            .runExclusive(() => {
              // ...
              dataQueue.push(data); // 将数据加入队列

              if (!processingData) {
                processingData = true;
                processNextData(); // 如果没有正在处理的数据，则开始处理队列中的数据
              }
            })
            .then((result) => {
              resolve(); // 解析 Promise
              // ...
            });
      })
    });
  };

  // 处理下一个数据
  const processNextData = () => {
    mutex
        .runExclusive(() => {
          // ...
          if (dataQueue.length > 0) {
            const data = dataQueue.shift(); // 从队列中取出下一个数据
            findSubTreeAndRender(data);
            processNextData(); // 继续处理下一个数据
          } else {
            processingData = false;
          }
        })
        .then((result) => {
          // ...
        });
  };

  // window.electronAPI.receiveResourceTreeReady((data) => {
  //   findSubTreeAndRender(data);
  // });

  window.electronAPI.receiveCommandPathData((data) => {
    let obj = JSON.parse(data['互操作路径'])
    obj['plat'] = data['执行平台']
    obj['time'] = data['下发时间']
    obj['content'] = data['指令内容']
    obj['delay'] = data['指令时延']
    // console.log(obj)
    if (obj.type === 'CREATE') {
      createOperationPairEdge(obj)
    } else if (obj.type === 'DELETE') {
      deleteOperationPairEdge(obj)
    }
  });

  window.electronAPI.receiveEdgeDataFrom((data) => {
    flashEdge(data)
    store.updateDataOfInteroperation(data)
  })

  // 等待资源树准备好
  await waitForResourceTreeReady();
})

</script>
<template>
  <VueFlow
      class="customnodeflow"
      :default-viewport="{ zoom: 0.4 }"
      :min-zoom="0.2"
      :max-zoom="2.0"
      fit-view-on-init>
    <template #node-custom="{ data }">
      <CustomNode :handles="data.handles" :label="data.label"/>
    </template>
    <MiniMap pannable zoomable/>

    <Controls/>

    <Background gap="20"/>
  </VueFlow>
</template>

<style scoped>

</style>
