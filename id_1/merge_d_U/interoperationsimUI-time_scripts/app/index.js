// let Handler =null;
// import('../src/components/Util/GetSubscribedData.mjs').then((lib)=>{
//     Handler = lib.Handler;
//     console.log(lib.Handler);
//     console.log('lalala');})
const {is} = require('@electron-toolkit/utils')

const path = require('path');
const {app, BrowserWindow, ipcMain} = require('electron');

// Handle creating/removing shortcuts on Windows when installing/uninstalling.
if (require('electron-squirrel-startup')) {
    app.quit();
}

const isDev = is.dev;

function createWindow() {
    // Create the browser window.
    global.mainWindow = new BrowserWindow({
        width: 1920,
        height: 1080,
        webPreferences: {
            preload: path.join(__dirname, '../preload/preload.mjs'),
            nodeIntegration: true,
        },
    });

    // Open the DevTools.
    if (isDev && process.env['ELECTRON_RENDERER_URL']) {
        mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL']);
        mainWindow.removeMenu();
        // mainWindow.webContents.openDevTools();
    } else {
        // mainWindow.removeMenu();
        mainWindow.removeMenu();
        mainWindow.loadFile(path.join(__dirname, '../renderer/index.html'));
    }
}

async function subscriptionBigTree(Handler) {
    // console.log(Handler);
    Handler.createRootSubscription().then();
    Handler.createChildCreateSubscription('/root/仿真编排信息/编排对象状态信息').then();
    Handler.createUpdateSubscription('/root/仿真配置/平台容器配置').then();
    Handler.createUpdateSubscription('/root/仿真配置/互操作模板').then();
    Handler.createUpdateSubscription('/root/仿真编排信息/互操作仿真结果/资源调度指令执行历史').then();
    Handler.createUpdateSubscription('/root/仿真编排信息/互操作仿真结果/互操作仿真评估历史').then();
    Handler.createUpdateSubscription('/root/仿真编排信息/互操作仿真结果/仿真互操作效用得分计算要素').then();
    Handler.createUpdateSubscription('/root/仿真编排信息/网络仿真/数据交互时延历史').then();
    Handler.createUpdateSubscription('/root/仿真编排信息/网络仿真/流量带宽占用历史').then();
    Handler.createUpdateSubscription('/root/评估工具信息/评估工具状态响应').then();
}

// async function sendCommand(event,data) {
//     // const GetSubscribedData = await import('../src/components/Util/GetSubscribedData.mjs');
//     const Handler = GetSubscribedData.Handler;
//     Handler.updateCommand('start', data).then();
// }

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
    const modulePath = path.join(__dirname, './GetSubscribedData.js');
    let Handler;
    import(modulePath).then((lib) => {
        Handler = lib.Handler;
        subscriptionBigTree(Handler).then();
        ipcMain.handle('sendStartSimulationMessage', (event, data) => {
            Handler.updateCommand('start', data).then();
        });
    });
    createWindow();
    app.on('activate', function () {
        // On macOS it's common to re-create a window in the app when the
        // dock icon is clicked and there are no other windows open.
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});
