<script setup lang="ts">
</script>

<template>
  <div class="dashboard-container">
    <header class="main-header">
      <h1>无人机群九宫格目标搜索与打击任务规划系统</h1>
    </header>

    <main class="main-content">
      <div class="left-column">
        <div class="panel">
          <h2>打击无人机状态</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>无人机ID</th>
                  <th>暂停器</th>
                  <th>位置</th>
                  <th>当前执行任务</th>
                  <th>任务列表</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>attackDrone1</td>
                  <td>0</td>
                  <td>39.9073° N, 11</td>
                  <td>暂停器</td>
                  <td><button>查看</button></td>
                </tr>
                <tr>
                  <td>attackDrone2</td>
                  <td>0</td>
                  <td>6.3972° E</td>
                  <td>打击</td>
                  <td><button>查看</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="panel">
          <h2>自杀式无人机状态</h2>
          <div class="table-container scrollable">
            <table>
              <thead>
                <tr>
                  <th>无人机ID</th>
                  <th>归属区域</th>
                  <th>位置</th>
                  <th>是否存活</th>
                  <th>执行任务</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>SuicideDrone1</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone2</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone3</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone4</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                 <tr>
                  <td>SuicideDrone5</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone6</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone7</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone8</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone9</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone10</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone11</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone12</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone13</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
                <tr>
                  <td>SuicideDrone14</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>存活</td>
                  <td>侦察</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="center-column">
        <div class="grid-container">
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
          <div class="grid-item"></div>
        </div>
        <div class="charts-container">
          <div class="chart-item">
            <div class="pie-chart" style="--p:80; --c:#7fb2d7;"></div>
            <span>自杀机人员数量统计</span>
          </div>
          <div class="chart-item">
            <div class="pie-chart" style="--p:50; --c:#8fbbbf;"></div>
            <span>载弹量统计</span>
          </div>
          <div class="chart-item">
            <div class="pie-chart" style="--p:65; --c:#a9d5e8;"></div>
            <span>敌人数量统计</span>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div class="panel">
          <h2>敌方目标状态</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>目标ID</th>
                  <th>系统区域</th>
                  <th>位置</th>
                  <th>目标类型</th>
                  <th>是否存活</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>target1</td>
                  <td>A1</td>
                  <td>39.9073° N, 11</td>
                  <td>机场</td>
                  <td>是</td>
                </tr>
                <tr>
                  <td>target2</td>
                  <td>A1</td>
                  <td>6.3972° E</td>
                  <td>雷达站</td>
                  <td>是</td>
                </tr>
                <tr>
                  <td>target3</td>
                  <td>A2</td>
                  <td>39.9073° N, 11</td>
                  <td>指挥所</td>
                  <td>是</td>
                </tr>
                <tr>
                  <td>target4</td>
                  <td>A3</td>
                  <td>6.3972° E</td>
                  <td>雷达站</td>
                  <td>是</td>
                </tr>
                <tr>
                  <td>target5</td>
                  <td>A3</td>
                  <td>39.9073° N, 11</td>
                  <td>油库</td>
                  <td>是</td>
                </tr>
                <tr>
                  <td>target6</td>
                  <td>A4</td>
                  <td>6.3972° E</td>
                  <td>地面导弹</td>
                  <td>否</td>
                </tr>
                <tr>
                  <td>target7</td>
                  <td>A2</td>
                  <td>39.9073° N, 11</td>
                  <td>地面导弹</td>
                  <td>否</td>
                </tr>
                <tr>
                  <td>target8</td>
                  <td>A3</td>
                  <td>6.3972° E</td>
                  <td>地面导弹</td>
                  <td>是</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="panel">
          <h2>事件记录</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>事件ID</th>
                  <th>事件类型</th>
                  <th>事件来源</th>
                  <th>事件描述</th>
                  <th>触发时间</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>1</td>
                  <td>目标发现</td>
                  <td>SuicideDrone1</td>
                  <td>A1区域发现机场</td>
                  <td>18:22:21</td>
                </tr>
                <tr>
                  <td>2</td>
                  <td>目标打击</td>
                  <td>SuicideDrone3</td>
                  <td>A2区域打击地面导...</td>
                  <td>18:23:30</td>
                </tr>
                <tr>
                  <td>3</td>
                  <td>区域侦察</td>
                  <td>SuicideDrone5</td>
                  <td>A3区域完成全区域...</td>
                  <td>18:24:00</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style>
:root {
  --bg-color: #e9eef5;              /* 雾蓝灰 */
  --panel-bg-color: #d6e4e9;        /* 鼠尾草绿 */
  --grid-item-bg-color: #7fb2d7;    /* 降低饱和度 */
  --border-color: #c3d4e0;          /* 更浅的冷灰 */
  --text-color: #2d3748;            /* 深石墨 */
  --header-color: #1a365d;          /* 深蓝灰 */
  --table-header-text-color: #4a5568;
  --hover-color: #a9d5e8;           /* 柔和青蓝 */
  --button-bg-color: #5a9fd4;       /* 低饱和蓝 */
}
</style>

<style scoped>
.dashboard-container {
  background-color: var(--bg-color);
  color: var(--text-color);
  font-family: 'Microsoft YaHei', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  width: 100%;
  min-width: 1800px;
}

.main-header {
  text-align: center;
  margin-bottom: 20px;
}

.main-header h1 {
  font-size: 2.2em;
  color: var(--header-color);
  text-shadow: none;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 100px);
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 2;
}

.center-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 3;
}

.panel {
  background-color: var(--panel-bg-color);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.panel h2 {
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  color: var(--header-color);
  font-size: 1.3em;
}

.table-container {
  width: 100%;
  overflow-x: auto;
  flex-grow: 1;
}

.table-container.scrollable {
  max-height: 40vh;
  overflow-y: auto;
}

.scrollable::-webkit-scrollbar {
  width: 8px;
}
.scrollable::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.scrollable::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 4px;
}
.scrollable::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 1em;
}

th, td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
}

thead th {
  color: var(--table-header-text-color);
  text-align: center;
}

tbody td {
  text-align: center;
}

tbody tr:hover {
  background-color: var(--hover-color);
}

button {
  background-color: var(--button-bg-color);
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background-color 0.3s;
}

button:hover {
  background-color: var(--header-color);
}

/* 正方形九宫格 */
.grid-container {
  flex-grow: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  aspect-ratio: 1 / 1;          /* 关键：保持正方形 */
  gap: 6px;
  background-color: var(--border-color);
  border: 2px solid var(--border-color);
  padding: 6px;
  border-radius: 8px;
}

.grid-item {
  background-color: var(--grid-item-bg-color);
  border-radius: 4px;
  transition: transform .2s;
}
.grid-item:hover {
  transform: scale(1.05);
}

.charts-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: var(--panel-bg-color);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  min-height: 150px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  font-size: 1.1em;
}

.pie-chart {
  --w: 100px;
  width: var(--w);
  height: var(--w);
  position: relative;
  display: inline-grid;
  place-content: center;
  margin: 5px;
}

.pie-chart:before {
  content: "";
  position: absolute;
  border-radius: 50%;
  inset: 0;
  background: conic-gradient(var(--c) calc(var(--p)*1%), #10667c 0);
}
</style>