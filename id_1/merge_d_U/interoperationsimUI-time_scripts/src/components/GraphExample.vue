<template>
      <div id="container_info" style="background-color: white;height: 100%; width: 100%;">

      </div>

</template>
<script setup>
import {onMounted, reactive, ref} from 'vue';

import G6 from "@antv/g6";

let data1={

  nodes:[
    {
      id: "无人车",
    },
    {
      id: "无人车_计算装备",
    },
    {
      id: "无人车_存储装备",
    },
    {
      id: "无人车_感知装备",
    },
    {
      id: "无人车_雷达",
    },
    {
      id: "无人车_红外",
    },
    {
      id: "无人车_光电",
    },

    {
      id: "无人机",
    },
    {
      id: "无人机_计算装备",
    },
    {
      id: "无人机_存储装备",
    },
    {
      id: "无人机_感知装备",
    },
    {
      id: "无人机_雷达",
    },
    {
      id: "无人机_红外",
    },
    {
      id: "无人机_光电",
    }

  ],
  edges:[
    {
      source:'无人车',
      target:'无人车_计算装备'
    },
    {
      source:'无人车',
      target:'无人车_存储装备'
    },
    {
      source:'无人车',
      target:'无人车_感知装备'
    },
    {
      source:'无人车_感知装备',
      target:'无人车_雷达'
    },
    {
      source:'无人车_感知装备',
      target:'无人车_红外'
    },
    {
      source:'无人车_感知装备',
      target:'无人车_光电'
    },
    {
      source:'无人机',
      target:'无人机_计算装备'
    },
    {
      source:'无人机',
      target:'无人机_存储装备'
    },
    {
      source:'无人机',
      target:'无人机_感知装备'
    },

    {
      source:'无人机_感知装备',
      target:'无人机_雷达'
    },
    {
      source:'无人机_感知装备',
      target:'无人机_红外'
    },
    {
      source:'无人机_感知装备',
      target:'无人机_光电'
    }

  ]
}


let graph =null

const lineDash = [4, 2, 1, 2];
G6.registerEdge(
    'line-dash',
    {
      afterDraw(cfg, group) {
        // get the first shape in the group, it is the edge's path here=
        const shape = group.get('children')[0];
        let index = 0;
        // Define the animation
        shape.animate(
            () => {
              index++;
              if (index > 9) {
                index = 0;
              }
              const res = {
                lineDash,
                lineDashOffset: -index,
              };
              // returns the modified configurations here, lineDash and lineDashOffset here
              return res;
            },
            {
              repeat: true, // whether executes the animation repeatly
              duration: 3000, // the duration for executing once
            },
        );
      },
    },
    'cubic', // extend the built-in edge 'cubic'
);

onMounted(()=>{


  graph = new G6.Graph({
    container: 'container_info',
    layout: {
      type: 'force2',
      linkDistance: 50,

    },
    defaultNode: {
      type: 'circle',
      size:40
    },
    defaultEdge: {
      type: 'line',
    },
    modes: {
      default: [
        'drag-canvas',
        'zoom-canvas',
          'drag-node'
      ],
    },
    fitView: true,
  });


  graph.node(function (node) {
    if (node.id.includes('无人车'))
    {
      console.log(node.id)
      return {
        label: node.id,
        // comboId:'无人车'
      };
    }
    else{
      return {
        label: node.id,
        // comboId:'无人机'
      }
    }


  });


  graph.data(data1)

  graph.render();

  // graph.updateChild(data1,'融合任务')
  // graph.updateChild(data2,'融合任务')

  graph.addItem('edge',{
    source:'无人机_光电',
    target:'无人车_光电',
    type:'line-dash',
    style: {
      stroke: '#c50a2d',
      lineWidth: 5,
      endArrow:true
      // ... 其他样式属性
    },
  })

  graph.addItem('edge',{
    source:'无人车_红外',
    target:'无人机_雷达',
    type:'line-dash',
    style: {
      stroke: '#0f39b1',
      lineWidth: 5,
      endArrow:true
      // ... 其他样式属性
    },
  })

  // graph.fitView()
  //
  // graph.on('node:drag', function (e) {
  //   refreshDragedNodePosition(e);
  //   graph.layout()
  // });
  function refreshDragedNodePosition(e) {
    const model = e.item.get('model');
    model.fx = e.x;
    model.fy = e.y;
    model.x = e.x;
    model.y = e.y;
  }

  const descriptionDiv = document.createElement('div');
  descriptionDiv.innerHTML = 'Wait for the layout to complete...';
  const container = document.getElementById('container_info');
  container.appendChild(descriptionDiv);

  let UGVNodes = graph.getNodes().filter((node) => !node.getModel().id.includes('无人车'));
  let UAVNodes = graph.getNodes().filter((node) => !node.getModel().id.includes('无人机'));
  graph.on('afterlayout', () => {
    descriptionDiv.innerHTML = '';
    const hull1 = graph.createHull({
      id: 'UGVNodes-hull',
      type: 'bubble',
      members: UGVNodes,
      padding: 50,
    });
    const hull2 = graph.createHull({
      id: 'UAVNodes-hull',
      type: 'bubble',
      members: UAVNodes,
      padding: 50,
      style: {
        fill: 'lightgreen',
        stroke: 'green',
      },
    });



    graph.on('afterupdateitem', (e) => {
      hull1.updateData(hull1.members);
      hull2.updateData(hull2.members);

    });
  });

})


</script>