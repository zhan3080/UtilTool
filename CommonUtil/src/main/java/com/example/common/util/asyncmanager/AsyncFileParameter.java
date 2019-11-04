package com.example.common.util.asyncmanager;

import android.text.TextUtils;

/**
 * Created by tcc on 2017/11/30.
 */

public class AsyncFileParameter {

    public int id;
    public In in;
    public Out out;

    /**
     * 下载SDK变量地址文件，请使用此构造方法
     *
     * @param fileUrl
     * @param savePath
     */
    public AsyncFileParameter(String fileUrl, String savePath) {
        this.in = new In();
        this.out = new Out();

        this.in.fileUrl = fileUrl;
        this.in.savePath = savePath;

        if (TextUtils.isEmpty(savePath)) {
            throw new NullPointerException("savePath can not be null");
        }
    }

    public class In {
        public String id;
        // 文件下载地址
        public String fileUrl;
        // 保存地址
        public String savePath;
    }

    public class Out {
        /**
         * { @link AsyncManager.RESULT_SUCCESS,AsyncManager.RESULT_FAILED ...}
         */
        public int resultType;
        // 请求结果
        public String result;

        // 当前下载进度
        public long currentSize;
        // 文件大小
        public long totalSize;
    }
}
