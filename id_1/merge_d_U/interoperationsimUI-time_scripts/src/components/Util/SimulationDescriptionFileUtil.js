import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";

function getFileContent(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsText(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

function makeSimulationFile(taskName, applist, equipmentSet, edgesSet, nodesSet, resultSoftwarePath, templateName, templatePath) {
    let result = {}
    let equipmentSheets = []
    let appSheets = []
    for (const equipment of equipmentSet) {
        equipmentSheets.push({
            "装备名称": equipment.name,
            "装备软件目录": equipment.softwareIndex,
            "装备数据发生器软件路径": equipment.softwarePath,
            "装备资源子树描述文件路径": equipment.descriptionPath,

        })
    }
    result['参试装备列表'] = equipmentSheets

    for (const app of applist) {
        appSheets.push({
            "应用名称": app.name,
            "应用软件目录": app.softwareIndex,
            "软件路径": app.softwarePath,
            "应用资源子树描述文件路径": app.descriptionPath
        })
    }
    result['参试应用列表'] = appSheets


    let roleSet = []
    let dockerSet = []
    let nodeIdSet = Object.keys(nodesSet)


    for (const nodeId of nodeIdSet) {
        roleSet.push({
            "平台名称": nodesSet[nodeId].name,
            "角色名称": nodesSet[nodeId].role
        })
        dockerSet.push({
            "平台id": nodeId,
            "平台名称": nodesSet[nodeId].name,
            "平台ip": nodesSet[nodeId].ip,
            "平台装备配置": nodesSet[nodeId].equipments,
            "平台应用配置": nodesSet[nodeId].applist,
            "计算资源配额":nodesSet[nodeId].computationSetting,
            "内存资源配额":nodesSet[nodeId].memorySetting
        })
    }
    result["参试平台个数"] = nodeIdSet.length
    result["互操作选角"] = roleSet
    result["参试平台容器配置"] = dockerSet

    let networkSet = []
    let edgeIdSet = Object.keys(edgesSet)
    for (const edgeId of edgeIdSet) {
        networkSet.push({
            "平台名称": edgesSet[edgeId].from,
            "邻居名称": edgesSet[edgeId].to,
            "带宽kb/s": edgesSet[edgeId].bandWidth,
            "时延ms": edgesSet[edgeId].delay,
            "丢包率%": edgesSet[edgeId].lossRate
        })
        // networkSet.push({
        //     "平台id":edgesSet[edgeId].to,
        //     "邻居id":edgesSet[edgeId].from,
        //     "带宽":edgesSet[edgeId].bandWidth,
        //     "时延":edgesSet[edgeId].delay,
        //     "丢包率":edgesSet[edgeId].lossRate
        // })
    }
    result["网络仿真参数配置"] = networkSet
    result["测试任务"] = taskName
    // result["互操作模板"]=interoperateTemplate
    result["互操作模板配置"] = {
        '模板名称': templateName,
        '模板路径': templatePath
    }

    result["仿真编排任务评价软件路径"] = resultSoftwarePath

    return result
}


function readSimulationDescriptionFile(fileTextJsonObj, projectName) {
    let fileJsonObj = JSON.parse(fileTextJsonObj)


    let nodesSet = {}
    let edgesSet = {}
    let graphContent = {
        nodes: [],
        edges: []
    }
    let equipmentsSet = []
    let equipmentsSetting = fileJsonObj["参试装备列表"]

    for (const item of equipmentsSetting) {
        equipmentsSet.push({
            name: item['装备名称'],
            softwareIndex: item['装备软件目录'],
            softwarePath: item['装备数据发生器软件路径'],
            descriptionPath: item['装备资源子树描述文件路径']
        })
    }

    let usedAppSet = []
    let appSetting = fileJsonObj['参试应用列表']
    for (const appItem of appSetting) {
        usedAppSet.push({
            name: appItem['应用名称'],
            softwareIndex: appItem['应用软件目录'],
            softwarePath: appItem['软件路径'],
            descriptionPath: appItem['应用资源子树描述文件路径']
        })
    }

    let networkSetting = fileJsonObj['网络仿真参数配置']
    for (const item of networkSetting) {
        let id = item["平台名称"] + '-' + item["邻居名称"]

        graphContent.edges.push({
            id: id,
            source: item["平台名称"],
            target: item["邻居名称"],
            label: "带宽:" + item["带宽kb/s"] + 'kb/s 时延:' + item["时延ms"] + 'ms 丢包率' + item["丢包率%"] + "%"
        })
        edgesSet[id] = {
            id: id,
            from: item["平台名称"],
            to: item["邻居名称"],
            bandWidth: item["带宽kb/s"],
            delay: item["时延ms"],
            lossRate: item["丢包率%"]
        }
    }
    let roleSetting = fileJsonObj['互操作选角']
    for (const item of roleSetting) {

        let randomNumber = Math.floor(Math.random() * 300) - 100;
        let name = item['平台名称']
        fileJsonObj['参试平台容器配置'].forEach(info => {
            if (info['平台名称'] === name) {
                graphContent.nodes.push({
                    id: name,
                    label: name,
                    x: 350 + randomNumber, y: 200 - randomNumber
                })
            }
        })


        nodesSet[name] = {
            role: item['角色名称'],
            computationUsage: 0,
            storeUsage: 0,
            softwareState: {},
            equipmentState: {},
            errorLog: []
        }
    }
    let equipmentSetting = fileJsonObj['参试平台容器配置']

    for (const item of equipmentSetting) {
        //修改了主键，以平台名称进行了保存

        let id = item['平台id']
        let name = item['平台名称']
        let toItem = nodesSet[name]
        toItem['nodeId'] = item['平台id']
        toItem['ip'] = item['平台ip']
        toItem['name'] = item['平台名称']

        toItem['computationSetting'] = item['计算资源配额']
        toItem['memorySetting'] = item['内存资源配额']

        toItem['computationUsage'] = 0
        toItem['memoryUsage'] = 0
        toItem['runningState'] = 'Running'

        // console.log(JSON.stringify(toItem))

        item['平台装备配置'].forEach(equipment => {
            toItem['equipmentState'][equipment['装备名称']] = {
                equipmentName: equipment['装备名称'],
                aliveState: '运行',
                args:equipment['参数']
            }
        })
        item['平台应用配置'].forEach(software => {
            toItem['softwareState'][software['应用名称']] = {
                softwareName: software['应用名称'],
                aliveState: '运行',
                args:software['参数']
            }
        })

        toItem['equipments'] = []
        for (let eq of item['平台装备配置']) {
            toItem['equipments'].push(eq['装备名称'])
        }
        toItem['applist'] = []
        for (let sf of item['平台应用配置']) {
            toItem['applist'].push(sf['应用名称'])
        }
    }
    let interOperateTemplate = fileJsonObj['互操作模板配置']['模板路径']

    let resultSoftwareCfg = fileJsonObj['仿真测试评价软件配置']

    useSimulationProjectInfoStore().addProjectInfo(projectName, nodesSet, edgesSet, graphContent, equipmentsSet, usedAppSet, resultSoftwareCfg, interOperateTemplate)

    // return {
    //     name: simulationName,
    //     content: {
    //         nodesSet: nodesSet,
    //         edgesSet: edgesSet,
    //         equipmentSet: equipmentsSet,
    //         graphContent: graphContent
    //     }
    // }

}


export {makeSimulationFile, readSimulationDescriptionFile, getFileContent}
