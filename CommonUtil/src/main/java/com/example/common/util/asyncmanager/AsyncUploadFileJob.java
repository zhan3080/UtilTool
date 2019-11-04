package com.example.common.util.asyncmanager;

import android.os.AsyncTask;

/**
 * Created by jrh on 2018/11/5.
 * 用来上传文件
 */

public class AsyncUploadFileJob extends AsyncTask {

    private String TAG = "AsyncUploadFileJob";
    public int id;

    private AsyncUploadFileParameter inParameter;
    private AsyncUploadFileListener requestListener;

    private UploadFileRequest uploadFileRequest;

    public AsyncUploadFileJob(AsyncUploadFileParameter fileParameter, AsyncUploadFileListener requestListener) {
        this.inParameter = fileParameter;
        this.requestListener = requestListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        uploadFileRequest = new UploadFileRequest(inParameter.in.url, inParameter.in.localPath, inParameter.in.headParameter, inParameter.in.httpMethod);
        return uploadFileRequest.uploadFile();
    }

    @Override
    protected void onPostExecute(Object object) {
        super.onPostExecute(object);
        if (requestListener != null) {
            if (object == null) {
                inParameter.out.resultType = AsyncManager.RESULT_FAILED;
                requestListener.onRequestResult(inParameter);
            } else {
                inParameter.out.resultType = AsyncManager.RESULT_SUCCESS;
                inParameter.out.setResult(object);
                requestListener.onRequestResult(inParameter);
            }
            requestListener = null;
        }
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

}
