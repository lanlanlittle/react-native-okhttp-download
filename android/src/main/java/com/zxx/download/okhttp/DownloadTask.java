package com.zxx.download.okhttp;

/**
 * Created by zhaoxx on 2018/11/26.
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zxx.download.okhttp.util.HttpUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.MemoryHandler;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 下载任务
 */
public class DownloadTask{

    private final int THREAD_COUNT = 4;//线程数
    private FilePoint mPoint;
    private long mFileLength;


    private boolean isDownloading = false;
    private int childCanleCount;//子线程取消数量
    private int childFinshCount;
    private HttpUtil mHttpUtil;
    private long[] mProgress;
    private File[] mCacheFiles;
    private File mTmpFile;//临时占位文件
    private boolean cancel;//是否取消下载
    private boolean cancelwifi; //非wifi取消下载

    private final int MSG_PROGRESS = 1;//进度
    private final int MSG_FINISH = 2;//完成下载
    private final int MSG_CANCEL = 3;//取消
    private final int MSG_START = 4;//开始
    private final int MSG_ERROR = 5;//失败
    private final int MSG_CANCEL_WIFI = 6;//取消非Wi-Fi
    private DownloadListner mListner;//下载回调监听
    private Handler mHandler = null;
    private long mLastTime = 0;
    private DownloadManager manager = null;

    /**
     * 任务管理器初始化数据
     * @param point
     * @param l
     */
    DownloadTask(FilePoint point, DownloadListner l, DownloadManager manager) {
        this.mPoint = point;
        this.mListner = l;
        this.mProgress = new long[THREAD_COUNT];
        this.mCacheFiles = new File[THREAD_COUNT];
        this.mHttpUtil = HttpUtil.getInstance();
        this.manager = manager;
        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_PROGRESS://进度
                        long progress = 0;
                        for (int i = 0, length = mProgress.length; i < length; i++) {
                            progress += mProgress[i];
                        }
                        mListner.onProgress(progress * 1.0f / mFileLength, progress, mFileLength, mPoint);
                        break;
                    case MSG_FINISH://完成
                        childFinshCount++;
                        if (childFinshCount % THREAD_COUNT != 0) return;
                        // 完成收尾
                        finish();
                        mListner.onFinished(mPoint);
                        break;
                    case MSG_CANCEL://取消
                        childCanleCount++;
                        if (childCanleCount % THREAD_COUNT != 0) return;
                        resetStutus();
                        mListner.onCancel(mPoint);
                        break;
                    case MSG_START://开始
                        mListner.onStart(mPoint);
                        break;
                    case MSG_ERROR://失败
                        mListner.onError(mPoint);
                        break;
                    case MSG_CANCEL_WIFI://非wifi
                        mListner.onCancelWifi(mPoint);
                }
            };
        };


    }

    /**
     * 任务回调消息
     * @param msg
     */

    private static final String TAG = "DownloadTask";

    public synchronized void start() {
        try {
            Log.e(TAG, "start: " + isDownloading + "\t" + mPoint.getUrl());

            if (isDownloading) return;
            isDownloading = true;
            mHandler.sendEmptyMessage(MSG_START);
            mHttpUtil.getContentLength(mPoint.getUrl(), new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() != 200) {
                        close(response.body());
                        mHandler.sendEmptyMessage(MSG_ERROR);
                        // 取消
                        finish();
                        return;
                    }
                    // 获取资源大小
                    mFileLength = response.body().contentLength();
                    close(response.body());
                    // 在本地创建一个与资源同样大小的文件来占位
                    mTmpFile = new File(mPoint.getFilePath(), mPoint.getFileName() + ".tmp");
                    if (!mTmpFile.getParentFile().exists()) mTmpFile.getParentFile().mkdirs();
                    RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");
                    tmpAccessFile.setLength(mFileLength);
                    /*将下载任务分配给每个线程*/
                    long blockSize = mFileLength / THREAD_COUNT;// 计算每个线程理论上下载的数量.

                    /*为每个线程配置并分配任务*/
                    for (int threadId = 0; threadId < THREAD_COUNT; threadId++) {
                        long startIndex = threadId * blockSize; // 线程开始下载的位置
                        long endIndex = (threadId + 1) * blockSize - 1; // 线程结束下载的位置
                        if (threadId == (THREAD_COUNT - 1)) { // 如果是最后一个线程,将剩下的文件全部交给这个线程完成
                            endIndex = mFileLength - 1;
                        }
                        download(startIndex, endIndex, threadId);// 开启线程下载
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            resetStutus();
        }
    }

    public void download(final long startIndex, final long endIndex, final int threadId) throws IOException {
        long newStartIndex = startIndex;
        // 分段请求网络连接,分段将文件保存到本地.
        // 加载下载位置缓存文件
        final File cacheFile = new File(mPoint.getFilePath(), "thread" + threadId + "_" + mPoint.getFileName() + ".cache");
        mCacheFiles[threadId] = cacheFile;
        final RandomAccessFile cacheAccessFile = new RandomAccessFile(cacheFile, "rwd");
        if (cacheFile.exists()) {// 如果文件存在
            String startIndexStr = cacheAccessFile.readLine();
            try {
                if(startIndexStr != null)
                    newStartIndex = Integer.parseInt(startIndexStr);//重新设置下载起点
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        final long finalStartIndex = newStartIndex;
        mHttpUtil.downloadFileByRange(mPoint.getUrl(), finalStartIndex, endIndex, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 206) {// 206：请求部分资源成功码
                    mHandler.sendEmptyMessage(MSG_ERROR);
                    // 取消
                    finish();
                    return;
                }
                InputStream is = response.body().byteStream();// 获取流
                RandomAccessFile tmpAccessFile = new RandomAccessFile(mTmpFile, "rw");// 获取前面已创建的文件.
                tmpAccessFile.seek(finalStartIndex);// 文件写入的开始位置.
                  /*  将网络流中的文件写入本地*/
                byte[] buffer = new byte[1024 << 2];
                int length = -1;
                int total = 0;// 记录本次下载文件的大小
                long progress = 0;
                while ((length = is.read(buffer)) > 0) {
                    if (cancel) {
                        //关闭资源
                        close(cacheAccessFile, is, response.body());
                        return;
                    }
                    tmpAccessFile.write(buffer, 0, length);
                    total += length;
                    progress = finalStartIndex + total;

                    //将当前现在到的位置保存到文件中
                    cacheAccessFile.seek(0);
                    cacheAccessFile.write((progress + "").getBytes("UTF-8"));
                    //发送进度消息
                    mProgress[threadId] = progress - startIndex;
                    // 500毫秒更新一次
                    if (System.currentTimeMillis() - mLastTime > 1000/30) {
                        mLastTime = System.currentTimeMillis();
                        mHandler.sendEmptyMessage(MSG_PROGRESS);
                    }
                }
                //关闭资源
                close(cacheAccessFile, is, response.body());
                mTmpFile.renameTo(new File(mPoint.getFilePath(), mPoint.getFileName()));//下载完毕后，重命名目标文件名
                mHandler.sendEmptyMessage(MSG_PROGRESS);
                //发送完成消息
                mHandler.sendEmptyMessage(MSG_FINISH);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                isDownloading = false;
                mHandler.sendEmptyMessage(MSG_ERROR);
                finish();
            }
        });
    }

    /**
     * 关闭资源
     *
     * @param closeables
     */
    private void close(Closeable... closeables) {
        int length = closeables.length;
        try {
            for (int i = 0; i < length; i++) {
                Closeable closeable = closeables[i];
                if (null != closeable)
                    closeables[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < length; i++) {
                closeables[i] = null;
            }
        }
    }

    /**
     * 删除临时文件
     */
    private void cleanFile(File... files) {
        for (int i = 0, length = files.length; i < length; i++) {
            if (null != files[i])
                files[i].delete();
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        cancel = true;
        cleanFile(mTmpFile);
        if (isDownloading) {
            if (null != mListner) {
                resetStutus();
                mHandler.sendEmptyMessage(MSG_CANCEL);
            }
        }
    }

    /**
     * 取消wifi
     */
    public void cancelWifi() {
        cancelwifi = true;
        cleanFile(mTmpFile);
        if (isDownloading) {
            if (null != mListner) {
                resetStutus();
                mHandler.sendEmptyMessage(MSG_CANCEL_WIFI);
            }
        }
    }

    /**
     * 重置下载状态
     */
    private void resetStutus() {
        cancel = false;
        isDownloading = false;
        cancelwifi = false;
        childCanleCount = 0;
        childFinshCount = 0;
    }

    public void finish(){
        // 重置状态
        resetStutus();
        // 取消队列
        manager.cancelError(mPoint.getUrl());
        // 删除所有临时文件
        cleanFile(mCacheFiles);
        // 删除临时文件
        cleanFile(mTmpFile);
    }
}
