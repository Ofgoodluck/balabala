import {defineStore} from "pinia";
import {ref} from "vue";

export const useSimulationEditorInfoStore = defineStore('simulationEditorInfoStore', () => {

    let simulationTaskName=ref('')

    let equipmentInfo = ref([

    ])

    let applistInfo = ref([])



    return { equipmentInfo,applistInfo,simulationTaskName}
})