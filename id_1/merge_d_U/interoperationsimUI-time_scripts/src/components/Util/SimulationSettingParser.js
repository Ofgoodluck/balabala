import {reactive} from "vue";
import {useSimulationEditorInfoStore} from "@/stores/simulationEditorInfoStore";

export function simulationTaskNameParser(taskName){
    useSimulationEditorInfoStore().simulationTaskName=taskName
}
export function simulationEquipmentParser(equipments){


    let content = JSON.parse(equipments)

    content.forEach((item)=>{
        useSimulationEditorInfoStore().equipmentInfo.push({
            name:item['装备名称'],
            softwareIndex:item['装备软件目录'],
            softwarePath:item['装备数据发生器软件路径'],
            descriptionPath:item['装备资源子树描述文件路径']
        })
    })



}

export function simulationAppParser(applist){

    let content = JSON.parse(applist)
    content.forEach((item)=>{
        useSimulationEditorInfoStore().applistInfo.push({
            name:item['应用名称'],
            softwareIndex:item['应用软件目录'],
            softwarePath:item['软件路径'],
            descriptionPath:item['应用资源子树描述文件路径']
        })
    })

}