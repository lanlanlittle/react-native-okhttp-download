package com.zxx.download.okhttp.apk;

/**
 * Created by zhaoxx on 2018/11/27.
 */

public interface ApkListener {
    /**
     * 安装
     */
    public void onInstall(String pkName);

    /**
     * 删除
     */
    public void onDelete(String pkName);

    /**
     * 替换
     */
    public void onReplace(String pkName);

    /**
     * 没有权限
     */
    public void onNotPromission();
}
