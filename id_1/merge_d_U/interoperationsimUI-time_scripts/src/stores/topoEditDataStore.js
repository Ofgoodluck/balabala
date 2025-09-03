import {defineStore} from "pinia";
import {ref} from "vue";

export const useGraphEditStore = defineStore('graphEditData', () => {

    const graphEditData = ref('')

    return { graphEditData}
})