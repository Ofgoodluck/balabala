import {defineStore} from 'pinia'

export const useEditStepsCountStore = defineStore('count', {
    state: () => {
        return {count: 0}
    },
    actions: {
        increment() {
            this.count++
        },
        decrement() {
            this.count--
        },
    }
})

export const useEditInfoStore = defineStore('editInfo', {
    state: () => {
        return {
            testTaskName: '',
            equipments: [],
            applications: [],
            interoperationTemplate: {},
            platformsConfig: [],
            networkConfig: [],
            roles: [],
            evaluateSoftwareConfig: {},

            equipmentsName: [],
            applicationsName: []
        }
    },
    actions: {
        loadEquipment(equipment) {
            if (this.equipmentsName.includes(equipment.name)) {
                return
            }
            this.equipments.push({
                "装备名称": equipment.name,
                "装备软件目录": equipment.dir,
                "装备数据发生器软件路径": equipment.path,
                "装备资源子树描述文件路径": equipment.tree.path
            })
            this.equipmentsName.push(equipment.name)
        },
        loadApplication(application) {
            if (this.applicationsName.includes(application.name)) {
                return
            }
            this.applications.push({
                "应用名称": application.name,
                "应用软件目录": application.dir,
                "软件路径": application.path,
                "应用资源子树描述文件路径": application.tree.path
            })
            this.applicationsName.push(application.name)
        },
        loadNode(formState) {
            this.platformsConfig.push({
                "平台名称": formState.node_name,
                "平台id": formState.node_id,
                "平台ip": formState.node_ip,
                "计算资源配额": formState.node_computationSetting,
                "内存资源配额": formState.node_memorySetting + formState.memorySettingUnit,
                "平台装备配置": formState.equipments.map(({name, args}) => ({
                    "装备名称": name,
                    "参数": args
                })),
                "平台应用配置": formState.applications.map(({name, args}) => ({
                    "应用名称": name,
                    "参数": args
                }))
            })
            this.roles.push({
                "平台名称": formState.node_name,
                "角色名称": formState.role
            })
        },
        removeNode(nodeName) {
            this.platformsConfig = this.platformsConfig.filter(item => {
                return item['平台名称'] !== nodeName
            })
            this.roles = this.roles.filter(item => {
                return item['平台名称'] !== nodeName
            })
        },
        loadLink(formState, from, to) {
            this.networkConfig.push({
                "平台名称": from,
                "邻居名称": to,
                "带宽kb/s": parseInt(formState.bandWidth),
                "时延ms": parseInt(formState.delay),
                "丢包率%": parseInt(formState.lossRate)
            })
        },
        loadInteroperationTemplate(interoperationTemplate) {
            this.interoperationTemplate = {
                "模板名称": interoperationTemplate.name,
                "模板路径": interoperationTemplate.path
            }
        },
        loadEvaluateSoftware(evaluateSoftware) {
            this.evaluateSoftwareConfig = {
                '软件路径': evaluateSoftware.path,
                '参数': evaluateSoftware.config
            }
        },
        format() {
            return {
                '测试任务': this.testTaskName,
                '参试装备列表': this.equipments,
                '参试应用列表': this.applications,
                '互操作模板配置': this.interoperationTemplate,
                '参试平台容器配置': this.platformsConfig,
                '网络仿真参数配置': this.networkConfig,
                '互操作选角': this.roles,
                '仿真测试评价软件配置': this.evaluateSoftwareConfig
            }
        }
    }
})