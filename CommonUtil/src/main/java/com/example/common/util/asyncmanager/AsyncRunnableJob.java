package com.example.common.util.asyncmanager;

import android.os.AsyncTask;
import android.util.Log;


/**
 * Created by hpplay on 2018/5/4.
 */

public class AsyncRunnableJob extends AsyncTask {
    private final String TAG = "AsyncRunnableJob";

    private Runnable runnable;
    private AsyncRunnableListener runnableListener;

    public AsyncRunnableJob(Runnable callable, AsyncRunnableListener runnableListener) {
        this.runnable = callable;
        this.runnableListener = runnableListener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            runnable.run();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (runnableListener != null) {
            runnableListener.onRunResult(AsyncManager.RESULT_SUCCESS);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (runnableListener != null) {
            runnableListener.onRunResult(AsyncManager.RESULT_CANCEL);
        }
    }
}