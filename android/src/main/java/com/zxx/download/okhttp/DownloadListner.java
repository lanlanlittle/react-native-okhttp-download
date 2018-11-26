package com.zxx.download.okhttp;

/**
 * Created by zhaoxx on 2018/11/26.
 */

/**
 * 下载回调
 */

public interface DownloadListner {
    /**
     * 下载完成
     * @param mFilePoint
     */
    void onFinished(FilePoint mFilePoint);

    /**
     * 下载进度
     * @param progress
     * @param cur
     * @param total
     * @param mFilePoint
     */
    void onProgress(float progress, float cur, float total, FilePoint mFilePoint);

    /**
     * 取消下载
     */
    void onCancel();

    /**
     * 取消下载wifi
     */
    void onCancelWifi();

    /**
     * 开始下载
     */
    void onStart();

    /**
     * 下载出错
     */
    void onError();
}
