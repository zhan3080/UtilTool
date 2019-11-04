package com.example.common.util.asyncmanager;

/**
 * Created by tcc on 2017/11/29.
 */

public interface AsyncHttpRequestListener {

    /**
     * 将网络请求结果返回给调用者
     */
    void onRequestResult(AsyncHttpParameter result);

}
