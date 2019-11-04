package com.example.common.util.asyncmanager;

/**
 * Created by hpplay on 2018/5/4.
 */

public interface AsyncRunnableListener {

    /**
     * Runnable执行结果
     *
     * @param resultType {@link AsyncManager#RESULT_SUCCESS,AsyncManager#RESULT_CANCEL}
     */
    void onRunResult(int resultType);

}
