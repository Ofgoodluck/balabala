//下载nodejssdk包：https://git.marslab.org.cn/eve/nodejssdk/-/blob/master/crmsSdk-1.0.4.tgz?ref_type=heads
//npm install ~/Downloads/crmsSdk-1.0.4.tgz
import {CrmsContext} from "crmsSdk";
import {PrimitiveContent, Req, Subscription, SubscriptionOperation} from "crmsSdk/crms/InternalCommon.js";
import {
    NotificationContentType,
    Operation,
    PrimitiveContentType,
    SubscriptionEventType
} from "crmsSdk/crms/Enumeration.js";
import {DataTable} from "crmsSdk/crms/ResourceModel.js"
import {JsonMarshaller} from "crmsSdk/crms/JsonMarshaller.js";
import {DataType} from "crmsSdk/crms/Enumeration.js";

const JsonMarshallerHandler = new JsonMarshaller()

function isIterable(obj) {
    return obj != null && typeof obj[Symbol.iterator] === 'function';
}

const readFromBuffer = (type, value) => {
    switch (type) {
        case DataType.uint8:
            return value.readUint8(0)
        case DataType.uint16:
            return value.readUint16LE(0)
        case DataType.uint32:
            return value.readUint32(0)
        case DataType.uint64:
            return Number(value.readBigUInt64LE(0))
        case DataType.int8:
            return value.readInt8(0)
        case DataType.int16:
            return value.readInt16LE(0)
        case DataType.int32:
            return value.readInt32LE(0)
        case DataType.int64:
            return Number(value.readBigInt64LE(0))
        case DataType.float16:
            return value.readFloatLE(0)
        case DataType.float32:
            return value.readFloatLE(0)
        case DataType.float64:
            return value.readDoubleLE()
        case DataType.char1:
            return value.readInt8(0)
        case DataType.uchar1:
            return value.readUInt8(0)
        case DataType.string:
            return value.toString()
    }
}

export const resourceIdsNeedSub = {
    DockerCpuAndMem: '硬件资源占用表',
    DockerLog: '容器CRMS日志',
    DockerCommand: '资源调度指令执行历史',
    SimulationScore: '互操作仿真评估历史',
    Delay: '数据交互时延历史',
    BandWidth: '流量带宽占用历史',
    DockerSoftwareState: '应用状态历史',
    DockerDeviceState: '装备状态历史',
    DockerState: '容器运行状态',
    DockerCpuAndMemCfg: '平台容器配置',
    ModelEvaluationToolState: '评估工具状态响应',
    InteroperationTemplate: '互操作模板',
    ScoreFactor: '仿真互操作效用得分计算要素'
}

export class Message {
    constructor(dataType, content) {
        this.dataType = dataType;
        this.content = content;
    }
}

export class Handler {

    constructor() {

    }

    static getResourceNameAndDockerId(resourceId) {
        let pos = resourceId.lastIndexOf('/');
        let resourceName = resourceId.slice(pos + 1);
        let parentId = resourceId.slice(0, pos);
        pos = parentId.lastIndexOf('/');
        let separate = parentId.lastIndexOf('_');
        let dockerId = parentId.slice(pos + 1, separate);
        return {
            resourceName: resourceName,
            dockerId: dockerId
        };
    }

    static async onNotification(notification) {
        // console.log('get notification');
        const pcValue = notification.primitiveContent.value;
        if (notification.notificationContentType === NotificationContentType.resourceStructureOnly) {
            if (pcValue.resourceId.startsWith('/root/仿真推演平台级信息')) {
                Handler.treeData[pcValue.resourceId] = pcValue.resourceType;
                // console.log(Handler.treeData);
                mainWindow.webContents.send('receiveResourceTreeReady', Handler.treeData);
            }
            return;
        }
        switch (notification.primitiveContent.type) {
            case PrimitiveContentType.subscriptionOperation: {
                console.log('create subscription success, resourceId: ' + notification.resourceId.toString());
                break;
            }
            case PrimitiveContentType.attribute: {
                const resourceId = pcValue.resourceId;
                console.log('get notification from: ' + resourceId);
                const attribute = pcValue;
                const resourceNameAndDockerId = Handler.getResourceNameAndDockerId(resourceId);
                const resourceName = resourceNameAndDockerId.resourceName;
                switch (resourceName) {
                    case resourceIdsNeedSub.DockerState: {
                        // let dockerState = attribute.data.toString();
                        break;
                    }
                    case resourceIdsNeedSub.ScoreFactor: {
                        console.log(attribute.data.toString());
                        let factor = JSON.parse(attribute.data.toString());
                        mainWindow.webContents.send('receiveScoreFactor', factor);
                        break;
                    }
                }
                break;
            }
            case PrimitiveContentType.dataTable: {
                const resourceId = pcValue.resourceId;
                // console.log('get notification from: ' + resourceId);
                const dataTable = pcValue;
                const resourceNameAndDockerId = Handler.getResourceNameAndDockerId(resourceId);
                const resourceName = resourceNameAndDockerId.resourceName;
                const dockerId = resourceNameAndDockerId.dockerId;
                switch (resourceName) {
                    case resourceIdsNeedSub.DockerCpuAndMem: {
                        for (const row of dataTable.content) {
                            const cpuAndMem = {
                                '平台名称': dockerId,
                                '计算资源占用率': row['计算资源占用率'].toString(),
                                '内存资源占用率': row['内存资源占用率'].toString()
                            }
                            mainWindow.webContents.send('receiveDockerCpuAndMemData', cpuAndMem);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.DockerLog: {
                        for (const row of dataTable.content) {
                            const log = {
                                '平台名称': dockerId,
                                '时间': row['时间'].toString(),
                                '日志行': row['日志行'].toString(),
                                '日志等级': row['日志等级'].toString()
                            }
                            mainWindow.webContents.send('receiveDockerLogData', log);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.DockerCommand: {
                        if (dataTable.hasOwnProperty('content')) {
                            let commands = [];
                            for (const row of dataTable.content) {
                                const command = {
                                    '执行平台': row['执行平台'].toString(),
                                    '指令发起服务名称': row['指令发起服务名称'].toString(),
                                    '下发时间': Number(row['下发时间'].readBigInt64LE(0)),
                                    '指令内容': row['指令内容'].toString(),
                                    '指令时延': Number(row['指令时延'].readBigUInt64LE(0)),
                                    '互操作路径': row['互操作路径'].toString(),
                                    '指令执行结果': row['指令执行结果'].toString()
                                };
                                if (command['互操作路径'] !== '{}' && command['指令执行结果'] === 'Success') {
                                    mainWindow.webContents.send('receiveCommandPathData', command);
                                }
                                if (command['指令发起服务名称'] === '指令执行智能体') {
                                    commands.push(command);
                                }
                            }
                            mainWindow.webContents.send('receiveCommandData', commands);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.SimulationScore: {
                        for (const row of dataTable.content) {
                            const score = {
                                '仿真互操作效用得分': row['仿真互操作效用得分'].readDoubleLE(0),
                                '仿真互操作效用得分来源': JSON.parse(row['仿真互操作效用得分来源'].toString())
                            }
                            mainWindow.webContents.send('receiveSimulationScore', score);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.Delay: {
                        let delays = {};
                        if (isIterable(dataTable.content)) {
                            for (const row of dataTable.content) {
                                const delay = {
                                    '平台名称': row['平台名称'].toString(),
                                    '邻居名称': row['邻居名称'].toString(),
                                    '交互类型': row['交互类型'].toString(),
                                    '数据内容': row['数据内容'].toString(),
                                    '数据来源': row['数据来源'].toString(),
                                    '发送时间': Number(row['发送时间'].readBigUInt64LE(0)),
                                    '交互时延': Number(row['交互时延'].readBigInt64LE(0))
                                }
                                if (delay['数据来源'] !== '' && delay['交互类型'] === 'Notification') {
                                    let content = JSON.parse(delay['数据内容'])
                                    JsonMarshallerHandler.deserializeRsp(content)
                                    if (content.hasOwnProperty('primitiveContent') && content.primitiveContent.hasOwnProperty('value') && content.primitiveContent.value.hasOwnProperty('content')) {
                                        // console.log(content)
                                        const items = content.primitiveContent.value.content
                                        if (isIterable(items)) {
                                            const columns = content.primitiveContent.value.columns;
                                            const row = {}
                                            for (const item of items) {
                                                for (const [k, v] of Object.entries(item)) {
                                                    if (!k.startsWith('__')) {
                                                        row[k] = readFromBuffer(columns[k], v)
                                                    }
                                                }
                                            }
                                            delay['数据内容'] = JSON.stringify(row)
                                        }
                                        mainWindow.webContents.send(
                                            'receiveEdgeDataFrom',
                                            delay
                                        )
                                    }
                                }
                                if (delays.hasOwnProperty(delay['邻居名称'] + '-' + delay['平台名称'])) {
                                    delays[delay['邻居名称'] + '-' + delay['平台名称']] = delay['交互时延'];
                                } else {
                                    delays[delay['平台名称'] + '-' + delay['邻居名称']] = delay['交互时延'];
                                }
                            }
                        }
                        // console.log(delays);
                        mainWindow.webContents.send('receiveEdgeDelayData', delays);
                        break;
                    }
                    case resourceIdsNeedSub.BandWidth: {
                        let nodeBandWidths = {};
                        let edgeBandWidths = {};
                        let nodeIOBandWidths = {};
                        for (const row of dataTable.content) {
                            let bandWidth = {
                                '平台名称': row['平台名称'].toString(),
                                '邻居名称': row['邻居名称'].toString(),
                                '带宽占用': Number(row['带宽占用'].readBigUInt64LE(0))
                            };
                            if (nodeBandWidths.hasOwnProperty(bandWidth['平台名称'])) {
                                nodeBandWidths[bandWidth['平台名称']]['平台流量'] += bandWidth['带宽占用'];
                            } else {
                                nodeBandWidths[bandWidth['平台名称']] = {
                                    '平台流量': 0,
                                };
                                nodeBandWidths[bandWidth['平台名称']]['平台流量'] += bandWidth['带宽占用'];
                            }
                            if (nodeBandWidths.hasOwnProperty(bandWidth['邻居名称'])) {
                                nodeBandWidths[bandWidth['邻居名称']]['平台流量'] += bandWidth['带宽占用'];
                            } else {
                                nodeBandWidths[bandWidth['邻居名称']] = {
                                    '平台流量': 0,
                                };
                                nodeBandWidths[bandWidth['邻居名称']]['平台流量'] += bandWidth['带宽占用'];
                            }
                            if (edgeBandWidths.hasOwnProperty(bandWidth['邻居名称'] + '-' + bandWidth['平台名称'])) {
                                edgeBandWidths[bandWidth['邻居名称'] + '-' + bandWidth['平台名称']] += bandWidth['带宽占用'];
                            } else if (edgeBandWidths.hasOwnProperty(bandWidth['平台名称'] + '-' + bandWidth['邻居名称'])) {
                                edgeBandWidths[bandWidth['平台名称'] + '-' + bandWidth['邻居名称']] += bandWidth['带宽占用'];
                            } else {
                                edgeBandWidths[bandWidth['平台名称'] + '-' + bandWidth['邻居名称']] = bandWidth['带宽占用'];
                            }
                            if (nodeIOBandWidths.hasOwnProperty(bandWidth['平台名称']) && nodeIOBandWidths[bandWidth['平台名称']].hasOwnProperty('出口流量')) {
                                nodeIOBandWidths[bandWidth['平台名称']]['出口流量'] += bandWidth['带宽占用'];
                            } else if (nodeIOBandWidths.hasOwnProperty(bandWidth['平台名称'])) {
                                nodeIOBandWidths[bandWidth['平台名称']]['出口流量'] = bandWidth['带宽占用'];
                            } else {
                                nodeIOBandWidths[bandWidth['平台名称']] = {};
                                nodeIOBandWidths[bandWidth['平台名称']]['出口流量'] = bandWidth['带宽占用'];
                            }
                            if (nodeIOBandWidths.hasOwnProperty(bandWidth['邻居名称']) && nodeIOBandWidths[bandWidth['邻居名称']].hasOwnProperty('入口流量')) {
                                nodeIOBandWidths[bandWidth['邻居名称']]['入口流量'] += bandWidth['带宽占用'];
                            } else if (nodeIOBandWidths.hasOwnProperty(bandWidth['邻居名称'])) {
                                nodeIOBandWidths[bandWidth['邻居名称']]['入口流量'] = bandWidth['带宽占用'];
                            } else {
                                nodeIOBandWidths[bandWidth['邻居名称']] = {};
                                nodeIOBandWidths[bandWidth['邻居名称']]['入口流量'] = bandWidth['带宽占用'];
                            }
                        }
                        for (let key in nodeBandWidths) {
                            nodeBandWidths[key]['平台名称'] = key;
                            // console.log(nodeBandWidths[key]);
                            mainWindow.webContents.send('receivePlatBandWidthData', nodeBandWidths[key]);
                        }
                        mainWindow.webContents.send('receivePlatIOBandWidthData', nodeIOBandWidths);
                        // console.log(edgeBandWidths);
                        mainWindow.webContents.send('receiveEdgeBandWidthData', edgeBandWidths);
                        break;
                    }
                    case resourceIdsNeedSub.DockerSoftwareState: {
                        for (const row of dataTable.content) {
                            const softwareState = {
                                '平台名称': dockerId,
                                '应用名称': row['应用名称'].toString(),
                                '存活状态': row['存活状态'].toString(),
                            }
                            // console.log(softwareState);
                            mainWindow.webContents.send('receiveDockerSoftwareStateData', softwareState);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.DockerDeviceState: {
                        for (const row of dataTable.content) {
                            const deviceState = {
                                '平台名称': dockerId,
                                '装备名称': row['装备名称'].toString(),
                                '存活状态': row['存活状态'].toString(),
                            }
                            // console.log(deviceState);
                            mainWindow.webContents.send('receiveDockerDeviceStateData', deviceState);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.ModelEvaluationToolState: {
                        for (const row of dataTable.content) {
                            const toolState = {
                                '状态': row['状态'].toString(),
                                '消息': row['消息'].toString(),
                            }
                            // console.log(toolState);
                            if (toolState['状态'] === 'ready') {
                                mainWindow.webContents.send('receiveEvaluationToolReady');
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case PrimitiveContentType.resourceObject: {
                Handler.createUpdateSubscription(pcValue.resourceId + '/容器运行状态').then();
                Handler.createUpdateSubscription(pcValue.resourceId + '/硬件资源占用表').then();
                Handler.createUpdateSubscription(pcValue.resourceId + '/容器CRMS日志').then();
                Handler.createUpdateSubscription(pcValue.resourceId + '/应用状态历史').then();
                Handler.createUpdateSubscription(pcValue.resourceId + '/装备状态历史').then();
                let dockerId = await Handler.retrieveAttribute(pcValue.resourceId + '/容器id');
                let dockerState = await Handler.retrieveAttribute(pcValue.resourceId + '/容器运行状态');
                let dockerIdAndState = {};
                let pos1 = pcValue.resourceId.lastIndexOf('/');
                let pos2 = pcValue.resourceId.lastIndexOf('_');
                dockerIdAndState['平台名称'] = pcValue.resourceId.slice(pos1 + 1, pos2);
                dockerIdAndState['容器id'] = dockerId;
                dockerIdAndState['容器运行状态'] = dockerState;
                // console.log(dockerIdAndState);
                mainWindow.webContents.send('receiveDockerIdAndState', dockerIdAndState);
                break;
            }
            case PrimitiveContentType.command: {
                const resourceId = pcValue.resourceId;
                // console.log('get notification from: ' + resourceId);
                const dataTable = pcValue;
                const resourceNameAndDockerId = Handler.getResourceNameAndDockerId(resourceId);
                const resourceName = resourceNameAndDockerId.resourceName;
                switch (resourceName) {
                    case resourceIdsNeedSub.DockerCpuAndMemCfg: {
                        for (const row of dataTable.content) {
                            const cpuAndMemCfg = {
                                '平台名称': row['平台名称'].toString(),
                                '计算资源配额': row['计算资源配额'].readDoubleLE(0),
                                '内存资源配额': row['内存资源配额'].toString()
                            }
                            // console.log(cpuAndMemCfg);
                            mainWindow.webContents.send('receiveDockerCpuAndMemCfgData', cpuAndMemCfg);
                        }
                        break;
                    }
                    case resourceIdsNeedSub.InteroperationTemplate: {
                        let templates = [];
                        for (const row of dataTable.content) {
                            const template = {
                                '仿真时刻': Number(row['仿真时刻'].readBigUInt64LE(0)),
                                '执行平台': row['执行平台'].toString(),
                                '指令内容': row['指令内容'].toString(),
                            }
                            templates.push(template);
                        }
                        mainWindow.webContents.send('receiveTemplatesCfg', templates);
                        break;
                    }
                }
                break;
            }
        }
    }

    static async createUpdateSubscription(resourceId) {
        const sub = new Subscription({
            eventType: SubscriptionEventType.updated,
            notificationContentType: NotificationContentType.modifiedAttribute,
            notificationURI: ['tcp://127.0.0.1:1123']
        })
        const so = new SubscriptionOperation(sub, Operation.Create);
        const pc = new PrimitiveContent(PrimitiveContentType.subscriptionOperation, so);
        const req = new Req(Operation.Update, resourceId, 'node.js sdk create Subscription', pc);
        return await this.ctx.sendRequest(req);
    }

    static async createChildCreateSubscription(resourceId) {
        const sub = new Subscription({
            eventType: SubscriptionEventType.childCreated,
            notificationContentType: NotificationContentType.referenceOnly,
            notificationURI: ['tcp://127.0.0.1:1123']
        })
        const so = new SubscriptionOperation(sub, Operation.Create);
        const pc = new PrimitiveContent(PrimitiveContentType.subscriptionOperation, so);
        const req = new Req(Operation.Update, resourceId, 'node.js sdk create Subscription', pc);
        return await this.ctx.sendRequest(req);
    }

    static async createRootSubscription() {
        const sub = new Subscription({
            eventType: SubscriptionEventType.childCreated | SubscriptionEventType.childDeleted,
            notificationContentType: NotificationContentType.resourceStructureOnly,
            notificationURI: ['tcp://127.0.0.1:1123']
        })
        const so = new SubscriptionOperation(sub, Operation.Create);
        const pc = new PrimitiveContent(PrimitiveContentType.subscriptionOperation, so);
        const req = new Req(Operation.Update, '/root', 'node.js sdk create Subscription', pc);
        return await this.ctx.sendRequest(req);
    }

    static sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    static async retrieveAttribute(resourceId) {
        const req = new Req(Operation.Retrieve, resourceId, 'node sdk retrieve attribute');
        while (true) {
            const rsp = await this.ctx.sendRequest(req);
            if (rsp.primitiveContent.value.hasOwnProperty('data')) {
                return rsp.primitiveContent.value.data.toString();
            }
            await Handler.sleep(1000);
        }
    }

    static async updateCommand(command, arg) {
        const row = [{
            '命令': Buffer.from(command),
            '参数': Buffer.from(arg)
        }]
        let dataTable = new DataTable(undefined, undefined, undefined, row);
        let pc = new PrimitiveContent(PrimitiveContentType.command, dataTable);
        let req = new Req(Operation.Update, '/root/评估工具信息/评估工具控制命令', 'node.js sdk update dataTable', pc);
        let result = await this.ctx.sendRequest(req);
        console.log(result.detailMessage);
    }
}

Handler.ctx = new CrmsContext('tcp://127.0.0.1:1121');
Handler.ctx.setNotificationReceiver(1123, Handler.onNotification)
Handler.treeData = {};
