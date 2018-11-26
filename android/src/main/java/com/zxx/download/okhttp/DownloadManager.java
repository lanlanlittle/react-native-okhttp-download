package com.zxx.download.okhttp;

import android.content.Context;
import android.text.TextUtils;

import com.zxx.download.okhttp.net.NetBroadcastReceiver;
import com.zxx.download.okhttp.net.NetListener;
import com.zxx.download.okhttp.util.FileUtil;
import com.zxx.download.okhttp.util.NetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoxx on 2018/11/26.
 */

/**
 * 断点续传
 */

public class DownloadManager {

    private static Context mContext = null;
    private static String DEFAULT_FILE_DIR;//默认下载目录
    private Map<String, DownloadTask> mDownloadTasks;//文件下载任务索引，String为url,用来唯一区别并操作下载的文件
    private Map<String, DownloadTask> mDownloadTasksWifi;//wifi下暂停后的
    private static DownloadManager mInstance;
    private static final String TAG = "DownloadManager";
    private static boolean needWifi = false; //是否需要wifi下载

    /**
     * 取消下载
     */
    public void cancel(String... urls) {
        //单任务取消或多任务取消下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).cancel();
                mDownloadTasks.remove(url);
            }
        }
    }

    /**
     * 取消下载所有
     */
    public void cancelAll() {
        for (String key : mDownloadTasks.keySet()){
            mDownloadTasks.get(key).cancel();
            mDownloadTasks.remove(key);
        }
    }

    /**
     * 非wifi下取消下载
     */
    public void cancelAllWifi(){
        mDownloadTasksWifi.clear();
        for (String key : mDownloadTasks.keySet()){
            mDownloadTasks.get(key).cancelWifi();
            mDownloadTasks.remove(key);
        }
    }

    /**
     * 任务出错时取消当前任务
     */
    public void cancelError(String... urls){
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).cancel();
                mDownloadTasks.remove(url);
            }
        }
    }

    /**
     * 开始下载任务
     */
    public void start(String url,  DownloadListner l, boolean needWifi) {
        start(url, null, null, l, needWifi);
    }

    /**
     * 开始下载任务
     */
    public void start(String url, String filePath, String fileName, DownloadListner l, boolean needWifi) {
        this.needWifi = needWifi;
        if(isDownload(url)) return;

        if (TextUtils.isEmpty(filePath)) {//没有指定下载目录,使用默认目录
            filePath = FileUtil.getDownloadDir(mContext);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = FileUtil.getFileName(url);
        }
        mDownloadTasks.put(url, new DownloadTask(new FilePoint(url, filePath, fileName), l, this));
        download(url);
    }

    /**
     * 恢复因wifi暂停的任务
     */
    public void restart(String url, DownloadListner l, boolean needWifi){
        for (String key : mDownloadTasksWifi.keySet()){
            start(key, l, needWifi);
            mDownloadTasksWifi.remove(key);
        }
    }


    /**
     * 下载文件
     */
    private void download(String... urls) {
        //单任务开启下载或多任务开启下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).start();
            }
        }
    }

    public boolean isDownload(String url){
        return mDownloadTasks.containsKey(url);
    }


    public static DownloadManager getInstance(Context context) {//管理器初始化
        if (mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mContext = context;
                    mInstance = new DownloadManager(context);
                }
            }
        }
        return mInstance;
    }

    public DownloadManager(Context context) {
        mContext = context;
        mDownloadTasks = new HashMap<>();
        mDownloadTasksWifi = new HashMap<>();
        NetBroadcastReceiver.addListener(new NetListener() {
            @Override
            public void onChange(int state) {
                if(needWifi && state != NetUtil.NETWORK_WIFI){
                    // 取消所有的下载任务
                    cancelAllWifi();
                }
            }
        });
    }
}
