package com.zxx.download.okhttp.apk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.zxx.download.okhttp.util.ApkUtil;
import com.zxx.download.okhttp.util.PermissionUtil;

/**
 * Created by zhaoxx on 2018/11/27.
 */

public class ApkManager {
    private static ApkManager mApkManager = null;
    private ApkListener listener = null;

    public ApkManager(ApkListener listener){
        this.listener = listener;

        ApkBroadCastReceiver.addListener(listener);
    }

    public static ApkManager getInstance(ApkListener listener){
        if(mApkManager == null){
            mApkManager = new ApkManager(listener);
        }

        return mApkManager;
    }

    public void installApk(final Context context, String pkPath){
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //兼容8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    listener.onNotPromission();
                    Toast.makeText(context, "安装应用需要打开未知来源权限，请去设置中开启权限", Toast.LENGTH_SHORT);
                    return;
                }
            }
        }
        ApkUtil.installApk(context, pkPath);
    }
}
