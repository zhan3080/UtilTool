package com.example.common.util.asyncmanager;


import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by tcc on 2017/11/30.
 * 用来文件下载
 */

public class AsyncFileJob extends AsyncTask {

    private String TAG = "AsyncFileJob";
    public int id;

    private AsyncFileParameter fileParameter;
    private AsyncFileRequestListener requestListener;

    private FileRequest.DownloadListener downloadListener = new FileRequest.DownloadListener() {
        @Override
        public void onDownLoad(int taskCode, long currentSize, long totalSize, int status) {
            if (status == FileRequest.TASKDONWLOADING) {
                publishProgress(currentSize, totalSize);
            }
        }
    };
    private FileRequest fileRequest;

    public AsyncFileJob(AsyncFileParameter fileParameter, AsyncFileRequestListener requestListener) {
        this.fileParameter = fileParameter;
        this.requestListener = requestListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        fileRequest = new FileRequest(fileParameter.in.fileUrl, fileParameter.in.savePath);
        fileRequest.setDownloadListener(downloadListener);
        return fileRequest.download();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        boolean result = false;
        try {
            result = (Boolean) o;
        } catch (Exception e) {
            Log.w(TAG, e);
        }

        if (requestListener != null && fileParameter != null) {
            fileParameter.out.resultType = result ? AsyncManager.RESULT_FILE_DOWNLOAD_SUCCESS : AsyncManager.RESULT_FILE_DOWNLOAD_ERROR;
            requestListener.onDownloadFinish(fileParameter);
        }
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);

        long currentSize = 0L;
        long totalSize = 0L;

        try {
            currentSize = Long.valueOf(values[0].toString());
            totalSize = Long.valueOf(values[1].toString());
        } catch (Exception e) {
            Log.w(TAG, e);
        }

        if (requestListener != null && fileParameter != null) {
            fileParameter.out.resultType = AsyncManager.RESULT_FILE_DOWNLOADING;
            fileParameter.out.currentSize = currentSize;
            fileParameter.out.totalSize = totalSize;
            requestListener.onDownloadUpdate(currentSize, totalSize);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (fileRequest != null) {
            try {
                fileRequest.shutDown();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        if (requestListener != null && fileParameter != null) {
            fileParameter.out.resultType = AsyncManager.RESULT_FILE_DOWNLOAD_CANCEL;
            requestListener.onDownloadFinish(fileParameter);
        }
    }

}
