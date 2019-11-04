package com.example.common.util.asyncmanager;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


//// 下载类
public class FileRequest {

    private final String TAG = "FileRequest";

    public static final int TASKDONWLOADING = 0;// 任务正在下载
    public static final int TASKDONWLOADED = 1;// 任务下载结束
    public static final int TASKDOWNLOADERROR = 2;// 下载失败
    public static final int TASKDOWNLOADCANCEL = 3;// 取消下载

    private long mDownloadedSize = 0;
    private long mTotalSize;
    private int mDownloadPercent;
    private String mLocalPath;
    private String mURL;
    private int mTaskCode = 0;
    private DownloadListener mDownloadListener;
    private boolean isShutDown = false;

    private final String cacheName = ".cache";

    public FileRequest(String url, String localPath) {

        this.mLocalPath = localPath;
        this.mURL = url;

    }

    //// 下载方法
    protected boolean download() {
        File file = new File(mLocalPath + cacheName);
        if (file.exists()) {
            file.delete();
            mDownloadedSize = 0;// file.length();
        } else {
            mDownloadedSize = 0;
        }
        File resultFile = new File(mLocalPath);
        if (resultFile.exists()) {
            resultFile.delete();
        }
        Log.i(TAG, "mURL, " + mURL + " downloadedSize, " + mDownloadedSize);

        HttpURLConnection httpConnection = null;
        URL url = null;
        try {
            url = new URL(mURL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept-Encoding", "identity");
            mTotalSize = httpConnection.getContentLength();
            Log.i(TAG, "totalSize, " + mTotalSize);
            if (mTotalSize == 0) {
                makeDownLoadError();
                return false;
            } else if (mDownloadedSize == mTotalSize) {
                //////// 已下载到本地
                return true;
            } else if (mDownloadedSize > mTotalSize) {
                if (!file.delete()) {
                    makeDownLoadError();
                    return false;
                }
            }
            httpConnection.disconnect();
        } catch (Exception e) {
            Log.w(TAG, e);
            makeDownLoadError();
            return false;
        } finally {
            try {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        InputStream inStream = null;
        RandomAccessFile randomAccessFile = null;
        boolean result = false;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept", "image/gif, " + "image/jpeg, "
                    + "image/pjpeg, " + "image/pjpeg, " + "application/x-shockwave-flash, "
                    + "application/xaml+xml, " + "application/vnd.ms-xpsdocument, "
                    + "application/x-ms-xbap, " + "application/x-ms-application, "
                    + "application/vnd.ms-excel, " + "application/vnd.ms-powerpoint, "
                    + "application/msword, " + "*/*");
            httpConnection.setRequestProperty("Accept-Language", "zh-CN");
            httpConnection.setRequestProperty("Referer", mURL);
            httpConnection.setRequestProperty("Charset", "UTF-8");
            httpConnection.setRequestProperty("Range", "bytes=" + mDownloadedSize + "-");
            httpConnection.setRequestProperty("Connection", "Keep-Alive");
            httpConnection.setConnectTimeout(30 * 1000);

            inStream = httpConnection.getInputStream();

            File saveFile = new File(mLocalPath + cacheName);
            randomAccessFile = new RandomAccessFile(saveFile, "rwd");
            randomAccessFile.seek(mDownloadedSize);

            int offset = 0;
            int count = 0;
            int perUnit = (int) mTotalSize / 1024 / 100;
            byte[] buffer = new byte[1024];
            while ((offset = inStream.read(buffer, 0, 1024)) != -1 && !isShutDown) {
                randomAccessFile.write(buffer, 0, offset);
                count++;
                if (count == perUnit && mDownloadedSize < mTotalSize) {
                    mDownloadPercent = (int) (mDownloadedSize * 100 / mTotalSize);
                    //////// 下载百分百mDownloadPercent
                    count = 0;

                    if (mDownloadListener != null) {
                        mDownloadListener.onDownLoad(mTaskCode, mDownloadedSize, mTotalSize, TASKDONWLOADING);
                    }

                }
                mDownloadedSize += offset;
            }

            result = true;
            if (isShutDown) {
                if (mDownloadListener != null) {
                    // 取消下载
                    mDownloadListener.onDownLoad(mTaskCode, mTotalSize, mTotalSize, TASKDOWNLOADCANCEL);
                }
                result = false;
            } else {
                if (mDownloadedSize >= mTotalSize && mDownloadedSize > 0 && mTotalSize > 0) {
                    ///////// 下载完成
                    renameToNewFile(mLocalPath + cacheName, mLocalPath);
                }
                if (mDownloadListener != null) {
                    // 正在下载
                    mDownloadListener.onDownLoad(mTaskCode, mTotalSize, mTotalSize, TASKDONWLOADED);
                }
            }

        } catch (Exception e) {
            Log.w(TAG, e);
            makeDownLoadError();
            result = false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        return result;
    }

    private boolean renameToNewFile(String src, String dest) {
        File srcDir = new File(src); // 就文件夹路径
        boolean isOk = srcDir.renameTo(new File(dest)); // dest新文件夹路径，通过renameto修改
        return isOk;
    }

    public void makeDownLoadError() {
        if (mDownloadListener != null) {
            mDownloadListener.onDownLoad(mTaskCode, mDownloadedSize, mTotalSize, TASKDOWNLOADERROR);
        }
    }

    public void setDownloadListener(DownloadListener downloadListener) {

        this.mDownloadListener = downloadListener;
    }

    public void setTaskCode(int taskCode) {
        this.mTaskCode = taskCode;
    }

    public interface DownloadListener {
        void onDownLoad(int taskCode, long currentSize, long totalSize, int status);// TASKDONWLOADING 正在下载，TASKDONWLOADED下载结束
    }

    public void shutDown() {
        this.isShutDown = true;
    }
}