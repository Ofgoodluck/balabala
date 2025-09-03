import {defineStore} from "pinia";
import {ref} from "vue";

export const useEquipmentInfoStore = defineStore('equipmentInfoStore', () => {


    let equipmentInfo = ref([
        {
            name:"雷达A",
            dataSoftPath:"/home/soft",
            descriptionFilePath:"/home/description"
        },
        {
            name:"光电A",
            dataSoftPath:"/home/soft",
            descriptionFilePath:"/home/description"
        }
    ])

    function addEquipmentInfo ( name,dataSoftPath,descriptionFilePath){
        let tempData = {
            name:name,
            dataSoftPath:dataSoftPath,
            descriptionFilePath:descriptionFilePath
        }
        equipmentInfo.value.push(tempData)
    }

    return { equipmentInfo}
})