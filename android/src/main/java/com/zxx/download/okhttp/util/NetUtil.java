package com.zxx.download.okhttp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by zhaoxx on 2018/11/26.
 */

public class NetUtil {
    /** 
      * 没有连接网络 
      */
    public static final int NETWORK_NONE = -1;

    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;

    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                return NETWORK_WIFI;
            }else if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                return NETWORK_MOBILE;
            }
        }

        return NETWORK_NONE;
    }
}
