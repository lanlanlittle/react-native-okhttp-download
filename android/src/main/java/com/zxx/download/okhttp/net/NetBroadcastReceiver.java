package com.zxx.download.okhttp.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.zxx.download.okhttp.util.NetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoxx on 2018/11/26.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {
    // 广播回调
    private static List<NetListener> mList = new ArrayList<>();

    public static void addListener(NetListener listener){
        mList.add(listener);
    }

    public static void removeListener(NetListener listener){
        for (int i = 0; i< mList.size(); i++){
            if(listener == mList.get(i)){
                mList.remove(listener);
                break;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub  
        // 判断24 7.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // 这个监听wifi的打开与关闭，与wifi的连接无关
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                dealConnect(context);
                return;
            }
            // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
            // .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
            // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
            // 当然刚打开wifi肯定还没有连接到有效的无线
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                dealConnect(context);
                return;
            }
        }else{
            // 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
            // 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
            // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                dealConnect(context);
            }
        }
    }

    private static void dealConnect(Context context){
        int state = NetUtil.getNetWorkState(context);
        for (int i = 0; i< mList.size(); i ++){
            mList.get(i).onChange(state);
        }
    }
}
