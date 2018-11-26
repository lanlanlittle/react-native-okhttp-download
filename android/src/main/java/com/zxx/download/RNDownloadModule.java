package com.zxx.download;

import android.Manifest;
import android.webkit.DownloadListener;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.zxx.download.okhttp.DownloadListner;
import com.zxx.download.okhttp.DownloadManager;
import com.zxx.download.okhttp.FilePoint;
import com.zxx.download.okhttp.util.NetUtil;
import com.zxx.download.okhttp.util.PermissionUtil;

import java.util.HashMap;
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

  private DownloadListner createDownloadListener(){
    return new DownloadListner() {
      @Override
      public void onFinished(FilePoint mFilePoint) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onFinished", null);
      }

      @Override
      public void onProgress(float progress, float cur, float total, FilePoint mFilePoint) {
        WritableMap map = Arguments.createMap();
        map.putDouble("progress", progress);
        map.putDouble("cur", cur);
        map.putDouble("total", total);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onProgress", map);
      }

      @Override
      public void onCancel() {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCancel", null);
      }

      @Override
      public void onCancelWifi() {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onCancelWifi", null);
      }

      @Override
      public void onStart() {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onStart", null);
      }

      @Override
      public void onError() {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onError", null);
      }
    };
  }
}