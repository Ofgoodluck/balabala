import {defineStore} from "pinia";
import {useDescriptionPageStore} from "@/stores/descriptionPageStore";
import {Queue} from "../components/Util/Queue.js";

export const useSimulationProjectInfoStore = defineStore('simulationProjectInfoStore', {
        state: () => ({
            simulationProjectInfo: {},
            chooseProject: '',
            choosePlatform: '',
            chooseEdge: [],
            chooseEquipment: {
                name: '',
                type: '',
                softwareIndex: "",
                softwarePath: "",
                descriptionPath: "",
                aliveState: ""
            },
            dataOfInteroperation: {}
        }),
        getters: {
            projectList(state) {
                return Object.keys(state.simulationProjectInfo)
            },
            getChoosePlatform(state) {
                return state.choosePlatform
            },
            getChooseEdge(state) {
                return state.chooseEdge
            },
            getGraphContentByChoose(state) {
                if (state.chooseProject !== '' || Object.keys(state.simulationProjectInfo).includes(state.chooseProject)) {
                    return state.simulationProjectInfo[state.chooseProject].graphContent
                } else return ''
            },
            getGraphNodesSet(state) {
                if (state.chooseProject !== '' || Object.keys(state.simulationProjectInfo).includes(state.chooseProject)) {
                    return state.simulationProjectInfo[state.chooseProject].nodesSet
                } else return ''
            },
            getGraphEdgesSet(state) {
                if (state.chooseProject !== '' || Object.keys(state.simulationProjectInfo).includes(state.chooseProject)) {
                    return state.simulationProjectInfo[state.chooseProject].edgesSet
                } else return ''
            },
            getEquipmentsSet(state) {
                return state.simulationProjectInfo[state.chooseProject].equipmentsSet
            },
            getAppSet(state) {
                return state.simulationProjectInfo[state.chooseProject].appSet
            },
            getPlatformIdList(state) {
                if (state.chooseProject !== '' || Object.keys(state.simulationProjectInfo).includes(state.chooseProject)) {
                    return Object.keys(state.simulationProjectInfo[state.chooseProject].nodesSet)
                } else return []
            },
            getPlatformDescription(state) {

                let nodesSet = this.getGraphNodesSet
                if (Object.keys(nodesSet).includes(state.choosePlatform)) {
                    return nodesSet[state.choosePlatform]
                } else return ''
            },


            getPlatformIP(state) {
                return this.getPlatformDescription.ip
            },
            getPlatformName(state) {
                return this.getPlatformDescription.name
            },
            getPlatformId(state) {
                return this.getPlatformDescription.nodeId
            },
            getPlatformEquipments(state) {
                return this.getPlatformDescription.equipments
            },
            getPlatformApplist(state) {
                return this.getPlatformDescription.applist
            },
            getPlatformRole(state) {
                return this.getPlatformDescription.role
            },

            //TODOS 设置内存使用，CPU使用，日志
            getPlatformComputationSetting(state) {
                return this.getPlatformDescription.computationSetting
            },
            getPlatformMemorySetting(state) {
                return this.getPlatformDescription.memorySetting
            },
            getPlatformComputationUsage(state) {
                return this.getPlatformDescription.computationUsage
            },
            getPlatformMemoryUsage(state) {
                return this.getPlatformDescription.memoryUsage
            },

            getRunningState(state) {
                return this.getPlatformDescription.runningState
            },
            // getSoftwareState(state) {
            //     return this.getPlatformDescription.softwareState
            // },
            // getEquipmentState(state) {
            //     return this.getPlatformDescription.equipmentState
            // },


            // getStateByName: (state) => (softwareName,type) => {
            //     let nodesSet = state.simulationProjectInfo[state.chooseProject].nodesSet
            //     let info = null
            //     if (type==='装备'){
            //         info = nodesSet[state.choosePlatform].equipmentState
            //     }else {
            //         info = nodesSet[state.choosePlatform].softwareState
            //     }
            //     let software = info[softwareName]
            //     return software ? software.aliveState : 'null'
            // },

            getEquipmentState(state) {
                let node = this.getPlatformDescription
                let info = null
                if (state.chooseEquipment.type === '装备') {
                    info = node.equipmentState
                } else {
                    info = node.softwareState
                }
                let software = info[state.chooseEquipment.name]
                return software ? software.aliveState : ''
            },

            getPlatformErrorLog(state) {
                return this.getPlatformDescription.errorLog
            },
            getChooseEquipment(state) {
                return state.chooseEquipment
            },
            getDataOfInteroperation() {
                return (pair) => {
                    let pos = pair.to.indexOf('/')
                    let key = pair.from + '-' + pair.to.slice(0, pos)
                    if (this.dataOfInteroperation.hasOwnProperty(key)) {
                        return this.dataOfInteroperation[key].list()
                    }
                    return []
                }
            }

        },
        actions: {
            addProjectInfo(name, nodesSet, edgesSet, graphContent, equipmentsSet, applist, resultSoftwareCfg, interOperateTemplate) {
                if (Object.keys(this.simulationProjectInfo).length === 0) {
                    this.choosePlatform = Object.keys(nodesSet)[0]
                    this.chooseProject = name
                }
                this.simulationProjectInfo[name] = {
                    nodesSet: nodesSet,
                    edgesSet: edgesSet,
                    graphContent: graphContent,
                    equipmentsSet: equipmentsSet,
                    appSet: applist,
                    resultSoftwareCfg: resultSoftwareCfg,
                    interOperateTemplate: interOperateTemplate
                }
                // console.log('projectInfo' + this.choosePlatform + JSON.stringify(this.simulationProjectInfo))
            },
            updateUsageData(data) {
                // let tmp = {
                //     '平台名称': '平台4',
                //     '计算资源占用率': '2.56 %',
                //     '内存资源占用率': '4.71 %'
                // }

                let computationUsage = data['计算资源占用率']
                let memoryUsage = data['内存资源占用率']
                let platformName = data['平台名称']
                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]
                node.computationUsage = parseFloat(computationUsage.replace("%", ""))
                node.memoryUsage = parseFloat(memoryUsage.replace("%", ""))
            },

            updateEquipmentState(data) {
                // let tmp = {
                //     '平台名称': '平台1',
                //     '装备名称': '雷达',
                //     '存活状态': '运行中'
                // }

                let platformName = data['平台名称']
                let equipmentName = data['装备名称']
                let aliveState = data['存活状态']

                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]

                let software = node['equipmentState'][equipmentName]
                software.aliveState = aliveState

                if (this.chooseEquipment.name === data['平台名称']) {
                    this.chooseEquipment.state = aliveState
                }
            },
            updateSoftwareState(data) {
                // let tmp = {
                //     '平台名称': '平台1',
                //     '应用名称': '雷达融合计算',
                //     '存活状态': '运行中'
                // }
                let platformName = data['平台名称']
                let softwareName = data['应用名称']
                let aliveState = data['存活状态']

                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]

                let software = node['softwareState'][softwareName]
                software.aliveState = aliveState

                if (this.chooseEquipment.name === data['平台名称']) {
                    this.chooseEquipment.state = aliveState
                }
            },

            updateErrorLog(data) {
                let platformName = data['平台名称']
                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]
                if (node.errorLog.length > 500) {
                    node.errorLog.shift()
                }
                node.errorLog.push({
                    time: data['时间'],
                    logContent: data['日志行'],
                    logLevel: data['日志等级']
                })
            },

            updateResourceSetting(data) {
                // let tmp = {
                //     '平台名称': '平台1',
                //     '计算资源配额': 0.2,
                //     '内存资源配额': 256m
                // }
                let platformName = data['平台名称']
                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]
                node.computationSetting = data['计算资源配额']
                node.memorySetting = data['内存资源配额']
            },

            updatePlatformState(data) {
                // let tmp = {
                //     '平台名称': '平台1',
                //     '容器id': '123465789',
                //     '容器运行状态': 'running'
                // }
                let platformName = data['平台名称']
                let nodesSet = this.simulationProjectInfo[this.chooseProject].nodesSet
                let node = nodesSet[platformName]
                node.runningState = data['容器运行状态']

            },

            setChoosePlatform(platformID) {
                if (this.choosePlatform !== platformID) {
                    let keys = Object.keys(this.simulationProjectInfo[this.chooseProject].nodesSet[this.choosePlatform].equipmentState)
                    this.chooseEquipment = {
                        name: keys.length === 0 ? '' : keys[0],
                        type: '装备'
                    }
                }
                this.choosePlatform = platformID

                // console.log("length"+this.getPlatformEquipments+this.getPlatformApplist)
                // if (this.getPlatformEquipments.length!==0){
                //     this.setChooseEquipment('装备',this.getPlatformEquipments[0])
                // }else if (this.getPlatformApplist.length!==0){
                //     this.setChooseEquipment('应用',this.getPlatformApplist[0])
                // }
                // console.log("length"+this.getPlatformEquipments+this.getPlatformApplist+JSON.stringify(this.getChooseEquipment))

            },

            setChooseEdges(edgeID) {
                if (!this.simulationProjectInfo[this.chooseProject].edgesSet.hasOwnProperty(edgeID)) {
                    return;
                }
                if (this.chooseEdge.length === Object.keys(this.getGraphEdgesSet).length) {
                    this.chooseEdge = [];
                    this.chooseEdge.push(edgeID);
                } else if (this.chooseEdge.includes(edgeID)) {
                    this.chooseEdge.splice(this.chooseEdge.indexOf(edgeID), 1);
                    if (this.chooseEdge.length === 0) {
                        for (let key in this.simulationProjectInfo[this.chooseProject].edgesSet) {
                            this.chooseEdge.push(key);
                        }
                    }
                } else if (this.chooseEdge.includes(this.convertEdge(edgeID))) {
                    this.chooseEdge.splice(this.chooseEdge.indexOf(this.convertEdge(edgeID)), 1);
                    if (this.chooseEdge.length === 0) {
                        for (let key in this.simulationProjectInfo[this.chooseProject].edgesSet) {
                            this.chooseEdge.push(key);
                        }
                    }
                } else {
                    this.chooseEdge.push(edgeID);
                }
            },
            setChooseProject(project) {

                this.chooseProject = project
                if (this.chooseProject !== '' || Object.keys(this.simulationProjectInfo).includes(this.chooseProject)) {
                    this.choosePlatform = Object.keys(this.simulationProjectInfo[this.chooseProject].nodesSet)[0]
                }
            },
            setChooseEquipment(type, name) {
                this.chooseEquipment.type = type
                this.chooseEquipment.name = name
                // let node = this.getPlatformDescription
                // let stateInfo = null
                // let info = null
                // if (type === '装备') {
                //     info = node.equipments
                //     stateInfo = node.equipmentState
                // } else {
                //     info = node.appSet
                //     stateInfo = node.softwareState
                // }
                // let software = info[name]
                // this.chooseEquipment.softwareIndex = software.softwareIndex
                // this.chooseEquipment.softwarePath = software.softwarePath
                // this.chooseEquipment.descriptionPath = software.descriptionPath
                // this.chooseEquipment.aliveState = stateInfo[name].aliveState
            },

            convertEdge(edge) {
                const pos = edge.lastIndexOf('-');
                return edge.slice(pos + 1) + '-' + edge.slice(0, pos);
            },

            updateDataOfInteroperation(data) {
                let key = data['邻居名称'] + data['数据来源'] + '-' + data['平台名称']
                if (!this.dataOfInteroperation.hasOwnProperty(key)) {
                    this.dataOfInteroperation[key] = new Queue()
                }
                let index = this.dataOfInteroperation[key].tailIndex
                if (this.dataOfInteroperation[key].length > 100) {
                    this.dataOfInteroperation[key].dequeue()
                }
                this.dataOfInteroperation[key].enqueue({
                    index: index,
                    from: data['平台名称'],
                    to: data['邻居名称'],
                    interoperationType: data['交互类型'],
                    delay: data['交互时延'],
                    data: data['数据内容']
                });
            }

        }
    }
)
