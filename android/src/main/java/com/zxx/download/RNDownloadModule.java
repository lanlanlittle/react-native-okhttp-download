package com.zxx.download;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zxx.download.okhttp.DownloadListner;
import com.zxx.download.okhttp.DownloadManager;
import com.zxx.download.okhttp.FilePoint;
import com.zxx.download.okhttp.apk.ApkListener;
import com.zxx.download.okhttp.apk.ApkManager;
import com.zxx.download.okhttp.util.ApkUtil;
import com.zxx.download.okhttp.util.FileUtil;
import com.zxx.download.okhttp.util.NetUtil;
import com.zxx.download.okhttp.util.PermissionUtil;

import java.io.File;
import java.security.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNDownloadModule extends ReactContextBaseJavaModule {

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  private final ReactApplicationContext reactContext;

  public RNDownloadModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNDownload";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  /**
   * 下载
   * @param url
   * @param path
   * @param name
   * @param wifi
   */
  @ReactMethod
  public void download(String url, String path, String name, boolean wifi) {
      // 没有权限
      if(!PermissionUtil.hasPermission(reactContext, Manifest.permission.INTERNET) ||
              !PermissionUtil.hasPermission(reactContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
              !PermissionUtil.hasPermission(reactContext, Manifest.permission.READ_EXTERNAL_STORAGE) ||
              !PermissionUtil.hasPermission(reactContext, Manifest.permission.ACCESS_NETWORK_STATE)){
        Toast.makeText(reactContext, "请先打开读写、访问网络和网络状态权限", Toast.LENGTH_SHORT);
        return;
      }

      // 网络不可用
      if(NetUtil.getNetWorkState(reactContext) == NetUtil.NETWORK_NONE){
        return;
      }

      // 指定wifi下载但不是wifi
      if(wifi && NetUtil.getNetWorkState(reactContext) != NetUtil.NETWORK_WIFI){
        return;
      }

      DownloadManager.getInstance(this.reactContext).start(url, path, name, createDownloadListener(), wifi);
  }

  /**
   * 恢复wifi下取消的任务
   */
  @ReactMethod
  public void restartWifi(String url, String path, String name, boolean wifi) {
    // 没有权限
    if(!PermissionUtil.hasPermission(reactContext, Manifest.permission.INTERNET) ||
            !PermissionUtil.hasPermission(reactContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
            !PermissionUtil.hasPermission(reactContext, Manifest.permission.READ_EXTERNAL_STORAGE) ||
            !PermissionUtil.hasPermission(reactContext, Manifest.permission.ACCESS_NETWORK_STATE)){
      Toast.makeText(reactContext, "请先打开读写、访问网络和网络状态权限", Toast.LENGTH_SHORT);
      return;
    }

    // 网络不可用
    if(NetUtil.getNetWorkState(reactContext) == NetUtil.NETWORK_NONE){
      return;
    }

    // 指定wifi下载但不是wifi
    if(wifi && NetUtil.getNetWorkState(reactContext) != NetUtil.NETWORK_WIFI){
      return;
    }

    DownloadManager.getInstance(this.reactContext).restart(url, createDownloadListener(), wifi);
  }

  /**
   * 取消
   * @param url
   */
  @ReactMethod
  public void cancel(String url) {
    DownloadManager.getInstance(this.reactContext).cancel(url);
  }

  /**
   * 安装apk
   * @param apkPath
   */
  @ReactMethod
  public void install(String apkPath){
    ApkManager.getInstance(createApkListener()).installApk(reactContext, apkPath);
  }

  /**
   * 删除指定文件或文件夹下的所有东西
   */
  @ReactMethod
  public void deleteFile(String dirPath){
    FileUtil.deleteFolderFile(dirPath, true);
  }

  /**
   * 打开安装未知应用的设置
   */
  @ReactMethod
  public void openInstallUnknowSetting(){
    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
    reactContext.startActivity(intent);
  }

  /**
   * 获取文件或文件夹下所有文件的大小
   * @param promise
   */
  @ReactMethod
  public void getFileSize(String filePath, Promise promise){
    File file = new File(filePath);
    long size = FileUtil.getFolderSize(file);
    WritableMap map = Arguments.createMap();
    map.putString("size", String.format("%d", size));
    promise.resolve(map);
  }

  /**
   * 获取根据路径生成的默认下载路径
   * @param url
   */
  @ReactMethod
  public void getDefaultPathByUrl(String url, Promise promise){
    String path = FileUtil.getDownloadDir(reactContext) + FileUtil.getFileName(url);
    WritableMap map = Arguments.createMap();
    map.putString("path", path);
    promise.resolve(map);
  }

  /**
   * 判断应用是否安装
   * @param pkName
   * @param promise
   */
  @ReactMethod
  public void isAppInstall(String pkName, Promise promise){
    WritableMap map = Arguments.createMap();
    map.putBoolean("install", ApkUtil.isAppInstall(reactContext, pkName));
    promise.resolve(map);
  }

  @ReactMethod
  public void isFileExist(String filePath, Promise promise){
    WritableMap map = Arguments.createMap();
    map.putBoolean("exist", FileUtil.isFileExist(filePath));
    promise.resolve(map);
  }

  @ReactMethod
  public void openApp(String pkName, String txt){
    if(ApkUtil.isAppInstall(reactContext, pkName)){
      if(!TextUtils.isEmpty(txt))
        Toast.makeText(reactContext, txt, Toast.LENGTH_SHORT);

      ApkUtil.openApp(reactContext, pkName);
    }else{
      Toast.makeText(reactContext, "应用未安装", Toast.LENGTH_SHORT);
    }
  }

  private DownloadListner createDownloadListener(){
    return new DownloadListner() {
      @Override
      public void onFinished(FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onFinished", map);
      }

      @Override
      public void onProgress(float progress, float cur, float total, FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putDouble("progress", progress);
        map.putDouble("cur", cur);
        map.putDouble("total", total);
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onProgress", map);

      }

      @Override
      public void onCancel(FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCancel", map);
      }

      @Override
      public void onCancelWifi(FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCancelWifi", map);
      }

      @Override
      public void onStart(FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onStart", map);
      }

      @Override
      public void onError(FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putString("url", mFilePoint.getUrl());
        map.putString("path", mFilePoint.getFilePath());
        map.putString("name", mFilePoint.getFileName());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onError", map);
      }
    };
  }

  private ApkListener createApkListener(){
    return new ApkListener() {
      @Override
      public void onInstall(String pkName) {
        WritableMap map = Arguments.createMap();
        map.putString("pkName", pkName);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onInstall", map);
      }

      @Override
      public void onDelete(String pkName) {
        WritableMap map = Arguments.createMap();
        map.putString("pkName", pkName);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onDelete", map);
      }

      @Override
      public void onReplace(String pkName) {
        WritableMap map = Arguments.createMap();
        map.putString("pkName", pkName);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onReplace", map);
      }

      @Override
      public void onNotPromission() {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onNotPromission", null);
      }
    };
  }
}