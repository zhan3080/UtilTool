package com.example.common.util.asyncmanager;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tcc on 2017/11/29.
 */

public class AsyncManager {

    private static final String TAG = "AsyncManager";

    private static AsyncManager instance = new AsyncManager();

    public static final int METHOD_GET = 0;// 请求类型GET
    public static final int METHOD_POST = 1;// 请求类型POST

    public static final int RESULT_SUCCESS = 0;// 请求成功，成功获取到服务器数据
    public static final int RESULT_FAILED = 1;// 请求失败，未获取到服务器数据
    public static final int RESULT_CANCEL = 2;// 请求取消
    public static final int RESULT_NULL_URL = 3;// 空的请求地址
    public static final int RESULT_INVALID_TYPE = 4;// 无效的请求类型

    public static final int RESULT_FILE_DOWNLOADING = 5;// 正在下载
    public static final int RESULT_FILE_DOWNLOAD_CANCEL = 6;// 取消下载
    public static final int RESULT_FILE_DOWNLOAD_ERROR = 7;// 下载错误
    public static final int RESULT_FILE_DOWNLOAD_SUCCESS = 8;// 下载完成

    public static final int RESULT_UPLOAD_STATUS_SUCCESS = 1; //上传成功
    public static final int RESULT_UPLOAD_STATUS_BUSY = 2; //正在上传中
    public static final int RESULT_UPLOAD_STATUS_FAILED = 3; //上传失败

    private static final int MAX_SEMAPHORE = 30;
    // 短时任务并发量
    private volatile int mSemaphore = MAX_SEMAPHORE;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    // SDK有几个常驻线程，不能完全按照CPU_COUNT数量来分配最大线程数
    // 常驻线程：1、LogWriter，2、IM消息 3、IM心跳
    private static final int MAXIMUM_POOL_SIZE = Math.max(MAX_SEMAPHORE, CPU_COUNT * 2 + 1);
    private static final int KEEP_ALIVE_SECONDS = 60;

    private volatile List<AsyncTask> mSemaphoreTaskList = new ArrayList<AsyncTask>();
    private volatile List<AsyncTask> mTaskList = new ArrayList<AsyncTask>();
    private boolean isDebug = false;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE);

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                MAXIMUM_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;

        Log.i(TAG, "  MAXIMUM_POOL_SIZE: " + MAXIMUM_POOL_SIZE
                + "  KEEP_ALIVE_SECONDS: " + KEEP_ALIVE_SECONDS);
    }

    private AsyncManager() {
    }

    public static synchronized AsyncManager getInstance() {
        return instance;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    private void printTaskDetail() {
        if (isDebug) {
            Log.i(TAG, "printTaskDetail running list zie :" + mTaskList.size()
                    + "  waiting task size:" + mSemaphoreTaskList.size()
                    + " Semaphore: " + mSemaphore);
        }
    }

    private void exeTask(AsyncTask asyncTask, boolean ignoreParallel) {
//        Log.i(TAG, "exeTask  mSemaphore: " + mSemaphore + "/" + THREAD_POOL_EXECUTOR.getActiveCount());
        if (ignoreParallel) {
            try {
                // 立即执行任务和常驻任务不计入并发
                asyncTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                mTaskList.add(asyncTask);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        } else if (mSemaphore > 0) {
            try {
                asyncTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                mTaskList.add(asyncTask);
                --mSemaphore;
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        } else {
            Log.w(TAG, "exeTask parallel too many,wait amount. mSemaphore: " + mSemaphore);
            mSemaphoreTaskList.add(asyncTask);
        }
        printTaskDetail();
    }

    private void releaseTask(AsyncTask asyncTask) {
        try {
            //https://bugly.qq.com/v2/crash-reporting/crashes/820cbca48f/195644?pid=1
            if (mTaskList.contains(asyncTask)) {
                mTaskList.remove(asyncTask);
                ++mSemaphore;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
            ++mSemaphore;
        }
        printTaskDetail();
        if (mSemaphore > 0 && mSemaphoreTaskList.size() > 0) {
            AsyncTask task = mSemaphoreTaskList.remove(0);
            exeTask(task, false);
        }
    }

    public AsyncRunnableJob exeRunnable(Runnable runnable, AsyncRunnableListener runnableListener) {
        return exeRunnable(runnable, runnableListener, false);
    }

    /**
     * 执行的任务不计入并发控制
     * 例如：立即执行的任务 和 常驻的任务
     *
     * @param runnable
     * @param runnableListener
     * @return
     */
    public AsyncRunnableJob exeRunnableWithoutParallel(Runnable runnable, AsyncRunnableListener runnableListener) {
        return exeRunnable(runnable, runnableListener, true);
    }

    private AsyncRunnableJob exeRunnable(Runnable runnable, AsyncRunnableListener runnableListener, boolean ignoreParallel) {
        AsyncRunnableJob asyncTask = new AsyncRunnableJob(runnable, runnableListener) {

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }

        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    /**
     * 执行异步任务
     *
     * @param callable
     */
    public AsyncCallableJob exeCallable(Callable callable, AsyncCallableListener callableListener) {
        return exeCallable(callable, callableListener, false);
    }

    public AsyncCallableJob exeCallableWithoutParallel(Callable callable, AsyncCallableListener callableListener) {
        return exeCallable(callable, callableListener, true);
    }

    private AsyncCallableJob exeCallable(Callable callable, AsyncCallableListener callableListener, boolean ignoreParallel) {

        AsyncCallableJob asyncTask = new AsyncCallableJob(callable, callableListener) {

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }
        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    /**
     * 网络请求
     *
     * @param inParameter
     * @param requestListener
     * @return
     */
    public AsyncHttpJob exeHttpTask(AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener) {
        return exeHttpTask(inParameter, requestListener, false);
    }

    public AsyncHttpJob exeHttpTaskWithoutParallel(AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener) {
        return exeHttpTask(inParameter, requestListener, true);
    }

    private AsyncHttpJob exeHttpTask(AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener, boolean ignoreParallel) {

        if (inParameter == null) {
            return null;
        }

//        Log.i(TAG, "exeHttpTask  url=" + inParameter.in.requestUrl);
        if (TextUtils.isEmpty(inParameter.in.requestUrl)) {
            if (requestListener != null) {
                inParameter.out.resultType = RESULT_NULL_URL;
                requestListener.onRequestResult(inParameter);
            }
            return null;
        }

        if (inParameter.in.requestMethod == METHOD_POST) {
            return doPostRequest(inParameter, requestListener, ignoreParallel);
        } else {
            return doGetRequest(inParameter, requestListener, ignoreParallel);
        }
    }


    /**
     * 文件下载
     *
     * @param inParameter
     * @param requestListener
     * @return
     */
    public AsyncFileJob exeFileTask(AsyncFileParameter inParameter, AsyncFileRequestListener requestListener) {
        return exeFileTask(inParameter, requestListener, false);
    }

    public AsyncFileJob exeFileTaskWithoutParallel(AsyncFileParameter inParameter, AsyncFileRequestListener requestListener) {
        return exeFileTask(inParameter, requestListener, true);
    }

    private AsyncFileJob exeFileTask(AsyncFileParameter inParameter, AsyncFileRequestListener requestListener, boolean ignoreParallel) {

        if (inParameter == null) {
            return null;
        }

        if (TextUtils.isEmpty(inParameter.in.fileUrl)) {
            if (requestListener != null) {
                inParameter.out.resultType = RESULT_NULL_URL;
                requestListener.onDownloadFinish(inParameter);
            }
            return null;
        }

        return downLoad(inParameter, requestListener, ignoreParallel);
    }

    /**
     * 文件上传
     *
     * @param inParameter
     * @param requestListener
     * @return
     */

    public AsyncUploadFileJob exeUploadFileTask(AsyncUploadFileParameter inParameter, AsyncUploadFileListener requestListener) {
        return exeUploadFileTask(inParameter, requestListener, false);
    }

    public AsyncUploadFileJob exeUploadFileTaskWithoutParallel(AsyncUploadFileParameter inParameter, AsyncUploadFileListener requestListener) {
        return exeUploadFileTask(inParameter, requestListener, true);
    }

    private AsyncUploadFileJob exeUploadFileTask(AsyncUploadFileParameter inParameter, AsyncUploadFileListener requestListener, boolean ignoreParallel) {

        if (inParameter == null) {
            return null;
        }

//        Log.i(TAG, "exeUploadFileTask  url=" + inParameter.in.url);
        if (TextUtils.isEmpty(inParameter.in.url)) {
            if (requestListener != null) {
                inParameter.out.resultType = RESULT_NULL_URL;
                requestListener.onRequestResult(inParameter);
            }
            return null;
        }

        return upload(inParameter, requestListener, ignoreParallel);
    }

    /**
     * HTTP/HTTPS GET请求
     *
     * @param inParameter
     * @param requestListener
     * @return
     */
    private AsyncHttpJob doGetRequest(final AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener, boolean ignoreParallel) {

        AsyncHttpJob asyncTask = new AsyncHttpJob(METHOD_GET, inParameter, requestListener) {
            @Override
            protected void onPostExecute(Object object) {
                super.onPostExecute(object);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }
        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    /**
     * HTTP/HTTPS POSY请求
     *
     * @param inParameter
     * @param requestListener
     * @return
     */
    private AsyncHttpJob doPostRequest(final AsyncHttpParameter inParameter, AsyncHttpRequestListener requestListener, boolean ignoreParallel) {

//        Log.d(TAG, "doPostRequest in " + inParameter.in.params);
        AsyncHttpJob asyncTask = new AsyncHttpJob(METHOD_POST, inParameter, requestListener) {
            @Override
            protected void onPostExecute(Object object) {
                super.onPostExecute(object);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }
        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    /**
     * 文件下载
     *
     * @param inParameter
     * @param requestListener
     * @return
     */
    private AsyncFileJob downLoad(AsyncFileParameter inParameter, AsyncFileRequestListener requestListener, boolean ignoreParallel) {

        AsyncFileJob asyncTask = new AsyncFileJob(inParameter, requestListener) {

            @Override
            protected void onPostExecute(Object object) {
                super.onPostExecute(object);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }
        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    /**
     * 文件上传
     *
     * @param inParameter
     * @param requestListener
     * @param ignoreParallel
     * @return
     */
    private AsyncUploadFileJob upload(AsyncUploadFileParameter inParameter, AsyncUploadFileListener requestListener, boolean ignoreParallel) {

        AsyncUploadFileJob asyncTask = new AsyncUploadFileJob(inParameter, requestListener) {

            @Override
            protected void onPostExecute(Object object) {
                super.onPostExecute(object);
                releaseTask(this);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                releaseTask(this);
            }
        };
        exeTask(asyncTask, ignoreParallel);
        return asyncTask;
    }

    public int getTaskSize() {
        return mTaskList.size();
    }

    public int getCachedTaskSize() {
        return mSemaphoreTaskList.size();
    }

    public void cancelAllTask() {
        Log.i(TAG, "cancelAllTask");
        for (AsyncTask asyncTask : mTaskList) {
            try {
                asyncTask.cancel(true);
            } catch (Exception e) {
                Log.w(TAG, "cancelAllTask waring in cancel asyncTask");
            }
        }
    }

}
