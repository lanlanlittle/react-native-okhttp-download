package com.zxx.download.okhttp.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by zhaoxx on 2018/11/26.
 */

/**
 * 获取路径，文件、目录删除操作
 */

public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 获取存储路径
     * @param context
     * @return
     */
    public static String getStoreDir(Context context){
        String path = "";
        path = getExternalDir(context);
        if(path == null || path.length() <= 0){
            path = getInternalDir(context);
        }
        return path;
    }

    /**
     * 获取外部sd卡路径
     * @param context
     * @return
     */
    public static String getExternalDir(Context context){
        String path = "";

        File dir = context.getExternalFilesDir(null);
        if(dir != null){
            path = dir.getPath();
        }

        return path;
    }

    /**
     * 给内部存储路径
     * @param context
     * @return
     */
    public static String getInternalDir(Context context){
        String path = "";

        File dir = context.getFilesDir();
        if(dir != null){
            path = dir.getPath();
        }

        return path;
    }

    /**
     * 获取下载路径
     * @param context
     * @return
     */
    public static String getDownloadDir(Context context){
        return getStoreDir(context) + "/apks/";
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(java.io.File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 2      * 删除指定目录下文件及目录
     * 3      * @param deleteThisPath
     * 4      * @param filepath
     * 5      * @return
     * 6
     */

    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        deleteFileSafely(file);
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            deleteFileSafely(file);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 安全删除文件.
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    /**
     * 2      * 格式化单位
     * 3      * @param size
     * 4      * @return
     * 5
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if(size <= 0){
            return 0 + "B";
        }

        if (kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte/1024;
        if(megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte/1024;
        if(gigaByte < 1) {
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte/1024;
        if(teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }

        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 获取下载文件的名称
     * @param url
     * @return
     */
    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
