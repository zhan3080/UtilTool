package com.example.common.util.asyncmanager;

/**
 * Created by tcc on 2017/12/15.
 * 异步任务结果回调
 */

public interface AsyncCallableListener {

    /**
     * Callable执行结果
     *
     * @param resultType {@link AsyncManager#RESULT_SUCCESS,AsyncManager#RESULT_CANCEL}
     */
    void onCallResult(int resultType, Object result);

}
