package com.zxx.download.okhttp;

/**
 * Created by zhaoxx on 2018/11/26.
 */

public class FilePoint {
    private String fileName;//文件名
    private String url;//下载地址
    private String filePath;//下载目录

    public FilePoint(String url) {
        this.url = url;
    }

    public FilePoint(String url, String filePath, String fileName) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
