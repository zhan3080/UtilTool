package com.example.common.util.asyncmanager;


import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;


/**
 * Created by tcc on 2017/11/29.
 * 用来执行HTTP请求
 */

public abstract class AsyncHttpJob extends AsyncTask {

    private final String TAG = "AsyncHttpJob";

    public int id;

    private AsyncHttpParameter inParameter;
    private AsyncHttpRequestListener requestListener;
    private int method;

    private HttpRequest httpRequest;

    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG, "http request timeout");
            onPostExecute(null);
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public AsyncHttpJob(int method, AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener) {
        this.method = method;
        this.inParameter = inParameter;
        this.requestListener = requestListener;

    }

    @Override
    protected Object doInBackground(Object[] params) {
        httpRequest = new HttpRequest(inParameter.in, this);
        mHandler.postDelayed(mTimeOutRunnable, inParameter.in.readTimeout + inParameter.in.readTimeout);
        HttpResult result = null;
        if (method == AsyncManager.METHOD_POST) {
            result = httpRequest.doPost();
        } else {
            result = httpRequest.doGet();
        }
//        Log.i(TAG, "doInBackground result: " + result);

        return result;
    }

    @Override
    protected void onPostExecute(Object object) {
        super.onPostExecute(object);
        Log.i(TAG, "onPostExecute object: " + object);
        if (requestListener != null) {
            if (object == null || !(object instanceof HttpResult)) {
                inParameter.out.resultType = AsyncManager.RESULT_FAILED;
                requestListener.onRequestResult(inParameter);
            } else {
                HttpResult httpResult = (HttpResult) object;
                inParameter.out.resultType = httpResult.resultType;
                inParameter.out.result = httpResult.result;
                inParameter.out.responseCode = httpResult.responseCode;
                requestListener.onRequestResult(inParameter);
            }
            requestListener = null;
        }
        cancelTimeOut();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i(TAG, "onCancelled");
        if (requestListener != null) {
            inParameter.out.resultType = AsyncManager.RESULT_CANCEL;
            requestListener.onRequestResult(inParameter);
            requestListener = null;
        }
        cancelTimeOut();
    }

    void cancelTimeOut() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOutRunnable);
            mHandler = null;
        }
    }

}
