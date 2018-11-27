package com.zxx.download.okhttp.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by zhaoxx on 2018/11/27.
 */

public class ApkUtil {
    /**
     * 获取包名信息
     */
    public static String getAppProcessName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    /**
     * 进行安装
     */
    public static void installApk(Context context, String apkPath){
        File file = new File(apkPath);
        if(file != null){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri data;
            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //兼容8.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        Toast.makeText(context, "需要开启安装未知来源应用权限", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // "net.csdn.blog.ruancoder.fileprovider"即是在清单文件中配置的authorities
                data = FileProvider.getUriForFile(context, getAppProcessName(context)+".com.zxx.download.provider",file);
                // 给目标应用一个临时授权
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(file);
            }

            intent.setDataAndType(data, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}
