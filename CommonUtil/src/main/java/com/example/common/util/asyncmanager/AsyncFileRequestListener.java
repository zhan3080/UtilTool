package com.example.common.util.asyncmanager;

/**
 * Created by tcc on 2017/11/30.
 */

public abstract class AsyncFileRequestListener {

    /**
     * 下载进度回调
     *
     * @param currentSize 当前下载进度
     * @param totalSize 文件总大小
     */
    public void onDownloadUpdate(long currentSize, long totalSize) {

    }

    /**
     * 下载结果回调
     *
     * @param result {@link AsyncManager#RESULT_FILE_DOWNLOAD_SUCCESS,...}
     */
    public abstract void onDownloadFinish(AsyncFileParameter result);

}
