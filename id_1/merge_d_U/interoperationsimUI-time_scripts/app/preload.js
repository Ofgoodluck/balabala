const {contextBridge, ipcRenderer} = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
    receiveData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDataFromNotification', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receivePlatBandWidthData: (handlerDataCallBack) => {
        ipcRenderer.on('receivePlatBandWidthData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receivePlatIOBandWidthData: (handlerDataCallBack) => {
        ipcRenderer.on('receivePlatIOBandWidthData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveEdgeBandWidthData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveEdgeBandWidthData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveEdgeDelayData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveEdgeDelayData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveEdgeDataFrom: (handlerDataCallBack) => {
        ipcRenderer.on('receiveEdgeDataFrom', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveSimulationScore: (handlerDataCallBack) => {
        ipcRenderer.on('receiveSimulationScore', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveResourceTreeReady: (handlerDataCallBack) => {
        ipcRenderer.on('receiveResourceTreeReady', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveEvaluationToolReady: (handlerDataCallBack) => {
        ipcRenderer.once('receiveEvaluationToolReady', (event) => {
            handlerDataCallBack()
        })
    },

    receiveScoreFactor: (handlerDataCallBack) => {
        ipcRenderer.on('receiveScoreFactor', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveCommandData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveCommandData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveCommandPathData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveCommandPathData', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveTemplatesCfg: (handlerDataCallBack) => {
        ipcRenderer.once('receiveTemplatesCfg', (event, data) => {
            handlerDataCallBack(data)
        })
    },

    receiveDockerCpuAndMemData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerCpuAndMemData', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台4',
            //     '计算资源占用率': '2.56 %',
            //     '内存资源占用率': '4.71 %'
            // }
            handlerDataCallBack(data)
        })
    },

    receiveDockerCpuAndMemCfgData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerCpuAndMemCfgData', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台1',
            //     '计算资源配额': 0.2,
            //     '内存资源配额': 256m
            // }
            handlerDataCallBack(data)
        })
    },

    receiveDockerLogData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerLogData', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台1',
            //     '时间': '2024-01-02 16:06:07,629 ',
            //     '日志行': ' REQ RQI: push /root/global/平台2/资源调度指令信息/指令执行智能体_资源调度信息表, op: Update, to: /root/global/平台2/资源调度指令信息/指令执行智能体_资源调度信息表 failed, code: Not_allowed, detail:ResourcePush can only be applied to local resources',
            //     '日志等级': 'CRMS@平台1:ERROR'
            // }
            handlerDataCallBack(data)
        })
    },

    receiveDockerSoftwareStateData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerSoftwareStateData', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台1',
            //     '应用名称': '雷达融合计算',
            //     '存活状态': '运行中'
            // }
            handlerDataCallBack(data)
        })
    },

    receiveDockerDeviceStateData: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerDeviceStateData', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台1',
            //     '装备名称': '雷达',
            //     '存活状态': '运行中'
            // }
            handlerDataCallBack(data)
        })
    },

    receiveDockerIdAndState: (handlerDataCallBack) => {
        ipcRenderer.on('receiveDockerIdAndState', (event, data) => {
            // let tmp = {
            //     '平台名称': '平台1',
            //     '容器id': '123465789',
            //     '容器运行状态': 'running'
            // }
            handlerDataCallBack(data)
        })
    },

    sendStartSimulationMessage: (data) => {
        ipcRenderer.invoke('sendStartSimulationMessage', data);
    },
})
