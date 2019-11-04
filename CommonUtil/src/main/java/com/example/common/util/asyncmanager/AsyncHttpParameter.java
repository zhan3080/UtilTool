package com.example.common.util.asyncmanager;

import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

/**
 * Created by tcc on 2017/11/29.
 */

public class AsyncHttpParameter {

    private final String TAG = "AsyncHttpParameter";

    public In in;
    public Out out;
    public int id;

    public static final int DEFAULT_TRY_COUNT = 3;
    public static final int DEFAULT_SPACE = 0;

    public AsyncHttpParameter(String url, String params) {
        this(url, params, DEFAULT_TRY_COUNT);
    }

    /**
     * 请求SDK变量地址，请使用此构造方法
     *
     * @param url
     * @param params
     */
    public AsyncHttpParameter(String url, String params, int tryCount) {

        if (tryCount <= 0) {
            Log.w(TAG, "tryCount must bigger than 0,use default value");
            tryCount = DEFAULT_TRY_COUNT;
        }

        this.in = new In();
        this.out = new Out();

        this.in.requestUrl = url;
        this.in.tryCount = tryCount;
        if (TextUtils.isEmpty(params)) {
            this.in.params = "";
        } else {
            this.in.params = params;
        }
    }


    public class In {
        public String id;
        // 请求地址
        public String requestUrl;
        // 参数
        public String params = "";

        // 请求方法，默认GET
        public int requestMethod = AsyncManager.METHOD_GET;
        // HTTP 请求的连接超时，默认15秒
        public int connectTimeout = 15000;
        // HTTP 请求的读取超时，默认15秒
        public int readTimeout = 15000;
        // 加载失败最大尝试次数，默认请求三次
        public int tryCount = DEFAULT_TRY_COUNT;
        // 重试间隔
        public int trySpace = DEFAULT_SPACE;
        // HTTP 请求头
        public Map<String, String> requestHeaders;
    }

    public class Out {
        /**
         * {@link AsyncManager#RESULT_SUCCESS,AsyncManager#RESULT_FAILED ...}
         */
        public int resultType;
        // 请求结果
        public String result;
        // http 响应状态码
        public int responseCode = -1;
    }

}