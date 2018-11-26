package com.zxx.download.okhttp.net;

/**
 * Created by zhaoxx on 2018/11/26.
 */

public interface NetListener {
    /**
     * 网络状态监听
     * @param state
     */
    public void onChange(int state);
}
