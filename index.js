'use strict';

/**
 * 仅仅android平台
 */
import { NativeModules, DeviceEventEmitter } from 'react-native';
import { EventEmitter } from 'events';

const { RNDownload } = NativeModules;

let Downloadok3 = {
    emitter : new EventEmitter(),
}

/**
 * `addListener` inherits from `events` module
 * @method addListener
 * @param {String} eventName - the event name
 * @param {Function} trigger - the function when event is fired
 */
Downloadok3.addListener = Downloadok3.emitter.addListener.bind(Downloadok3.emitter);

/**
 * 下载监听
 * onStart 下载开始
 * onProgress 下载进度
 * onCancel 取消下载
 * onCancelWifi 非Wi-Fi取消下载
 * onError 异常
 */
DeviceEventEmitter.addListener('onStart', data => {
    Downloadok3.emitter.emit('DownloadListner', {type: 'onStart', data: data});
});

DeviceEventEmitter.addListener('onProgress', data => {
    Downloadok3.emitter.emit('DownloadListner', {type: 'onProgress', data: data});
});

DeviceEventEmitter.addListener('onCancel', data => {
    Downloadok3.emitter.emit('DownloadListner', {type: 'onCancel', data: data});
});

DeviceEventEmitter.addListener('onCancelWifi', data => {
    Downloadok3.emitter.emit('DownloadListner', {type: 'onCancelWifi', data: data});
});

DeviceEventEmitter.addListener('onError', data => {
    Downloadok3.emitter.emit('DownloadListner', {type: 'onError', data: data});
});

/**
 * 安装监听
 * onInstall 安装成功
 * onDelete 删除成功
 * onReplace 替换成功
 * onNotPromission 没有安装权限
 */
DeviceEventEmitter.addListener('onInstall', data => {
    Downloadok3.emitter.emit('ApkListener', {type: 'onInstall', data: data});
});

DeviceEventEmitter.addListener('onDelete', data => {
    Downloadok3.emitter.emit('ApkListener', {type: 'onDelete', data: data});
});

DeviceEventEmitter.addListener('onReplace', data => {
    Downloadok3.emitter.emit('ApkListener', {type: 'onReplace', data: data});
});

DeviceEventEmitter.addListener('onNotPromission', data => {
    Downloadok3.emitter.emit('ApkListener', {type: 'onNotPromission', data: data});
});

/**
 * `removeAllListeners` inherits from `events` module
 * @method removeAllListeners
 * @param {String} eventName - the event name
 */
Downloadok3.removeAllListeners = Downloadok3.emitter.removeAllListeners.bind(Downloadok3.emitter);

/**
 * 原生方法
 */
Downloadok3.download = (url, path, name, wifi) => {
    RNDownload.download(url, path, name, wifi);
}

Downloadok3.restartWifi = (url, path, name, wifi) => {
    RNDownload.restartWifi(url, path, name, wifi);
}

Downloadok3.cancel = (url) => {
    RNDownload.cancel(url);
}

Downloadok3.install = (apkPath) => {
    RNDownload.install(apkPath);
}

Downloadok3.deleteFile = (dirPath) => {
    RNDownload.deleteFile(dirPath);
}

Downloadok3.openInstallUnknowSetting = () => {
    RNDownload.openInstallUnknowSetting();
}

Downloadok3.getFileSize = (filePath) => {
    return RNDownload.getFileSize(filePath);
}

export default Downloadok3;
