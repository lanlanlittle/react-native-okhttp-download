package com.zxx.download.okhttp.apk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.zxx.download.okhttp.net.NetListener;
import com.zxx.download.okhttp.util.NetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoxx on 2018/11/27.
 */

public class ApkBroadCastReceiver extends BroadcastReceiver {
    // 广播回调
    private static List<ApkListener> mList = new ArrayList<>();

    public static void addListener(ApkListener listener){
        mList.add(listener);
    }

    public static void removeListener(ApkListener listener){
        for (int i = 0; i< mList.size(); i++){
            if(listener == mList.get(i)){
                mList.remove(listener);
                break;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PackageManager manager = context.getPackageManager();
        // 安装成功
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            dealInstall(packageName);
        }

        // 删除成功
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            dealDelete(packageName);
        }

        // 替换成功
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            dealReplace(packageName);
        }
    }

    private static void dealInstall(String pkName){
        for (int i = 0; i< mList.size(); i ++){
            mList.get(i).onInstall(pkName);
        }
    }

    private static void dealDelete(String pkName){
        for (int i = 0; i< mList.size(); i ++){
            mList.get(i).onDelete(pkName);
        }
    }

    private static void dealReplace(String pkName){
        for (int i = 0; i< mList.size(); i ++){
            mList.get(i).onReplace(pkName);
        }
    }
}
