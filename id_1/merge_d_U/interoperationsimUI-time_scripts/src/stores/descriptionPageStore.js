import {defineStore} from "pinia";
import {useSimulationProjectInfoStore} from "@/stores/simulationProjectInfoStore";


export const useDescriptionPageStore = defineStore('descriptionPageStore', {
        state: () => ({
                platformInfo:useSimulationProjectInfoStore().getPlatformDescription,
                errorLog:'',
                computingResUsed:0,
                storeResUsed:30,
        }),
        getters: {
                getPlatformIP(state){
                        return state.platformInfo.ip
                },
                getPlatformName(state){
                        return state.platformInfo.name
                },
                getPlatformId(state){
                        return state.platformInfo.nodeId
                },
                getPlatformEquipments(state){
                        return state.platformInfo.equipments
                },
                getPlatformApplist(state){
                        return state.platformInfo.applist
                }

                //TODOS 设置内存使用，CPU使用，日志
        },
        actions: {


        }
    }
)