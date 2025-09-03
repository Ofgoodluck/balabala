<script setup>

import InterActTable from "@/components/showPage/InterActTable.vue";
import DockerDescriptionPart from "@/components/showPage/DockerDescriptionPart.vue";
import FlowOfNode from "@/components/showPage/FlowOfNode.vue";
import FlowOfEachEdge from "@/components/showPage/FlowOfEachEdge.vue";
import ChartOfEachEdge from "@/components/showPage/ChartOfEachEdge.vue";
import ResourceTreeInteroperationViewer from "@/components/showPage/ResourceTreeInteroperationViewer.vue";
import showScore from "@/components/showPage/showScore.vue"
import TopoGraph from "@/components/showPage/TopoGraph.vue";
import {ref} from "vue";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";
import ShowScoreByGraph from "@/components/showPage/showScoreByGraph.vue";
import GlobalNetworkTraffic from "@/components/showPage/administratorsPage/GlobalNetworkTraffic.vue";
import NodeDescriptionForAdmin from "@/components/showPage/administratorsPage/NodeDescriptionForAdmin.vue";
import InteractionInfo from "@/components/showPage/administratorsPage/InteractionInfo.vue";
import RealTopo from "@/components/showPage/administratorsPage/RealTopo.vue";
import TimeAxis from "@/components/showPage/administratorsPage/timeAxis.vue";
import BigResourceTree from "./administratorsPage/BigResourceTree.vue";

let topoGraphShow = ref(false)
let resGraphShow = ref(true)
let checkModel = ref(false)
const store = useSimulationProjectInfoStore()

const showNetworkTopology = ref(true)

</script>

<script>
import { useSimulationProjectInfoStore } from '@/stores/simulationProjectInfoStore'
import { ref } from 'vue'

let dialogTableVisible = ref(false)
let data = ref([])
let pair = ref({})
const columns = [
  {
    title: '行号',
    dataIndex: 'index',
    width: 50
  },
  {
    title: '平台名称',
    dataIndex: 'from',
    width: 100
  },
  {
    title: '邻居名称',
    dataIndex: 'to',
    width: 100
  },
  {
    title: '交互类型',
    dataIndex: 'interoperationType',
    width: 100
  },
  {
    title: '交互时延/ms',
    dataIndex: 'delay',
    width: 50
  },
  {
    title: '数据内容',
    dataIndex: 'data'
  }
]

export default {
  data() {
    return {
      selectedKeys: ref(['1']),// 初始选中的标签页
    };
  },

  childInterface: {},

  methods: {
    onInteroperationEdgeClicked(clickedPair) {
      // alert(`from:${clickedPair.from}-to:${clickedPair.to}-op:${clickedPair.operation}`)
      let pos1 = clickedPair.from.indexOf('/');
      let pos2 = clickedPair.to.indexOf('/');
      let edge = clickedPair.from.slice(0, pos1) + '-' + clickedPair.to.slice(0, pos2);
      // console.log(edge);
      useSimulationProjectInfoStore().setChooseEdges(edge)
      pair.value = clickedPair
      dialogTableVisible.value = true
    },

    getChildInterface(newOperationPair) {
      this.$options.childInterface = newOperationPair;
    },

    handleMenuSelect(selected) {
      // console.log(selected);
      this.selectedKeys.value = selected.selectedKeys;
    }
  },

}

</script>


<template>
  <a-layout class="layout">
    <a-layout-header>
      <div class="logo">互操作测试结果展示界面</div>
      <a-menu
          theme="dark"
          mode="horizontal"
          v-model:selectedKeys="selectedKeys"
          :style="{ lineHeight: '64px' }"
          @select="handleMenuSelect"
      >
        <a-menu-item key="1">全局视角</a-menu-item>
        <a-menu-item key="2">测试视角</a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-layout-content>
      <div v-show="selectedKeys.includes('1')" style="width: 100%; height: 100%;">
        <a-row style="height: 100%; width: 100%">

          <a-col :span="6" class="page-col">


            <a-row class="page-first-row">
              <a-col :span="24" style="width: 100%; height: 100%; border-bottom: solid">
                <DockerDescriptionPart></DockerDescriptionPart>
              </a-col>
            </a-row>

            <a-row class="page-second-row">
              <div class="title">互操作仿真评估结果展示</div>
              <div :style="{height:'95%', width:'100%'}">
                <showScoreByGraph></showScoreByGraph>
              </div>
            </a-row>


          </a-col>

          <a-col :span="10" :style="{width:'100%',height:'100%',borderStyle:'solid'}">

            <a-radio-group v-model:value="checkModel" button-style="solid" style="height: 5%; padding: 5px">
              <a-radio-button :value='true'>互操作关系视图</a-radio-button>
              <a-radio-button :value='false'>网络拓扑视图</a-radio-button>
            </a-radio-group>

            <div style="height: 95%">
              <TopoGraph v-show="!checkModel"></TopoGraph>
              <ResourceTreeInteroperationViewer v-show="checkModel"
                                                @export-create-operation-pair-edge="getChildInterface"
                                                @on-interoperation-edge-clicked="onInteroperationEdgeClicked"
                                                crms-addr="127.0.0.1"/>
            </div>

          </a-col>

          <a-col :span="8" :style="{width:'100%',height:'100%'}">

            <div style="height:31%; border-style:solid; padding: 5px">
              <FlowOfNode></FlowOfNode>
            </div>

            <div style="height:35%; border-style:solid; padding: 5px ">
              <FlowOfEachEdge></FlowOfEachEdge>
            </div>

            <div style="height:34%; border-style:solid; padding: 5px">
              <ChartOfEachEdge></ChartOfEachEdge>
            </div>
          </a-col>
        </a-row>
      </div>
      <div v-show="selectedKeys.includes('2')" style="width: 100%; height: 100%;">
        <a-row style="height: 100%; width: 100%">

          <a-col :span="6" class="page-col">


            <a-row class="admin-page-first-row">
              <a-col :span="24" style="width: 100%; height: 100%; border-bottom: solid">
                <NodeDescriptionForAdmin></NodeDescriptionForAdmin>
              </a-col>
            </a-row>

            <a-row class="admin-page-second-row">
              <div :style="{height:'100%', width:'100%'}">
                <InteractionInfo></InteractionInfo>
              </div>
            </a-row>


          </a-col>

          <a-col :span="18"
                 :style="{width:'100%',height:'100%',borderBottom:'solid',borderTop:'solid',borderRight:'solid'}">

            <a-row class="right-first-row">
              <a-col :span="13" :style="{width:'100%',height:'100%',borderBottom:'solid'}">
                <BigResourceTree crms-addr="127.0.0.1"></BigResourceTree>
              </a-col>
              <a-col :span="11" :style="{width:'100%',height:'100%',borderBottom:'solid',borderLeft:'solid'}">
                <a-row class="page-first-row">
                  <a-col :span="24" :style="{width:'100%',height:'100%',borderBottom:'solid'}">
                    <RealTopo></RealTopo>
                  </a-col>
                </a-row>
                <a-row class="page-second-row">
                  <GlobalNetworkTraffic></GlobalNetworkTraffic>
                </a-row>
              </a-col>
            </a-row>

            <a-row class="right-second-row">
              <div :style="{height:'100%', width:'100%'}">
                <TimeAxis></TimeAxis>
              </div>
            </a-row>

          </a-col>

        </a-row>
      </div>
    </a-layout-content>
  </a-layout>

  <el-dialog v-model="dialogTableVisible" title="互操作关系查看" width="60%">
    <p>执行平台:           {{pair.plat}}</p>
    <p>执行时间:           {{pair.time}}</p>
    <p>对应互操作指令:      {{pair.content}}</p>
    <p>指令时延:           {{pair.delay}}</p>
    <p>该互操作关系边对应的数据:</p>
    <a-table
      ref="interActTable"
      :size="'small'"
      :style="{ height: `500px` }"
      :columns="columns"
      :data-source="store.getDataOfInteroperation(pair)"
      :pagination="false"
      :scroll="{ y: 500 * 0.84 }"
      :bordered="true"
    >
    </a-table>
  </el-dialog>
</template>

<style scoped>
.site-layout-content {
  min-height: 280px;
  padding: 24px;
  background: #fff;
}

.logo {
  float: left;
  width: 300px;
  height: 31px;
  margin: 16px 24px 16px 0;
  line-height: 30px;
  text-align: left;
  color: #f5ebe0;
  font-size: 150%;
}

.ant-row-rtl #components-layout-demo-top .logo {
  float: right;
  margin: 16px 0 16px 24px;
}

[data-theme='dark'] .site-layout-content {
  background: #141414;
}

.page-col {
  width: 100%;
  height: 100%;
  border-style: solid;
}

.page-first-row {
  width: 100%;
  height: 50%;
}

.page-second-row {
  width: 100%;
  height: 50%;
}

.admin-page-first-row {
  width: 100%;
  height: 45%;
}

.admin-page-second-row {
  width: 100%;
  height: 55%;
}

.right-first-row {
  width: 100%;
  height: 70%;
}

.right-second-row {
  width: 100%;
  height: 30%;
}

.title {
  height: 3%;
  width: 100%;
  text-align: center;
  margin-top: 10px;
  font-size: calc(100vw * 16 / 1920);
  font-weight: bold;
}
</style>
