package com.example.common.util.asyncmanager;

import java.util.concurrent.Callable;


import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by tcc on 2017/12/15.
 * 用来执行异步任务
 */

public class AsyncCallableJob extends AsyncTask {
    private final String TAG = "AsyncCallableJob";

    private Callable callable;
    private AsyncCallableListener callableListener;

    public AsyncCallableJob(Callable callable, AsyncCallableListener callableListener) {
        this.callable = callable;
        this.callableListener = callableListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            return callable.call();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (callableListener != null) {

            if (o == null) {
                callableListener.onCallResult(AsyncManager.RESULT_FAILED, null);
            } else {
                callableListener.onCallResult(AsyncManager.RESULT_SUCCESS, o);
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (callableListener != null) {
            callableListener.onCallResult(AsyncManager.RESULT_CANCEL, null);
        }
    }
}
