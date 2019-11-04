package com.example.common.util.asyncmanager;

import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by jrh on 2018/11/5.
 */

public class AsyncUploadFileParameter {
    private final String TAG = "AsyncUploadFileParameter";

    public int id;
    public In in;
    public Out out;

    public AsyncUploadFileParameter(String url, String[] localPath, Map<String, String> headParameter) {
        this.in = new In();
        this.out = new Out();

        this.in.url = url;
        this.in.localPath = localPath;
        this.in.headParameter = headParameter;


        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("savePath can not be null");
        }
    }

    public class In {
        public String id;
        // 请求地址
        public String url;
        // 上传的文件路径
        public String[] localPath;
        // 请求头参数
        public Map<String, String> headParameter;
        // 返回参数类型，默认返回字符串
        public Class resultClass = String.class;

        public HttpMethod httpMethod;
    }

    public class Out {
        /**
         * {@link AsyncManager#RESULT_SUCCESS, AsyncManager#RESULT_FAILED ...}
         */
        public int resultType;
        // 请求结果
        private Object result;

        public void setResult(Object result) {
            this.result = result;
        }

        public <T> T getResult() {
            Class<T> classOfT = in.resultClass;
            try {
                return classOfT.cast(result);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            return null;
        }
    }
}
