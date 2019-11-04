package com.example.common.util.asyncmanager;

import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;

public class HttpRequest {
    private final String TAG = "HttpRequest";

    private int count = 0;

    private AsyncHttpParameter.In parameter;
    private AsyncHttpJob mHttpJob;

    public HttpRequest(AsyncHttpParameter.In parameter, AsyncHttpJob httpJob) {
        this.parameter = parameter;
        this.mHttpJob = httpJob;
    }

    public HttpResult doGet() {
        HttpResult httpResult = new HttpResult();
        count = 0;
        String urlPath = parameter.requestUrl;
        if (!TextUtils.isEmpty(parameter.params)) {
            if (urlPath.endsWith("?")) {
                urlPath = parameter.requestUrl + parameter.params;
            } else {
                urlPath = parameter.requestUrl + "?" + parameter.params;
            }
        }
        urlPath = urlPath.replaceAll(" ", "%20");
        Log.i(TAG, urlPath + "  path ");

        while (count < parameter.tryCount) {
            try {
                HttpURLConnection conn;
                URL url = new URL(urlPath);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(parameter.connectTimeout);
                conn.setReadTimeout(parameter.readTimeout);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("GET");
                boolean isGzip = setHeaders(conn, parameter.requestHeaders);
                conn.connect();

                if (mHttpJob != null) {
                    mHttpJob.cancelTimeOut();
                }

                int responseCode = conn.getResponseCode();
                httpResult.responseCode = responseCode;
                Log.i(TAG, "doGet responseCode:" + responseCode + "  " + count);
                if (responseCode == 200) {
                    String result = readHttpResult(conn, isGzip);
                    httpResult.resultType = AsyncManager.RESULT_SUCCESS;
                    httpResult.result = result;
                    return httpResult;
                }

                // 最后一次网络请求失败，不再等待重试
                if (count < (parameter.tryCount - 1) && parameter.trySpace > 0) {
                    try {
                        Thread.sleep(parameter.trySpace);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception when doGet retry sleep " + e);
                    }
                }

            } catch (Exception e) {
                Log.w(TAG, e);
            }
            count++;
        }
        httpResult.resultType = AsyncManager.RESULT_FAILED;
        httpResult.result = null;
        return httpResult;
    }

    public HttpResult doPost() {
        HttpResult httpResult = new HttpResult();
        count = 0;
        while (count < parameter.tryCount) {
            try {
                String urlPath = parameter.requestUrl;
                URL url = new URL(urlPath);
                Log.i(TAG, urlPath + "  path post");
                HttpURLConnection connection = null;
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(parameter.connectTimeout);
                connection.setReadTimeout(parameter.readTimeout);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type", "application/json");
                boolean isGzip = setHeaders(connection, parameter.requestHeaders);
                connection.connect();

                if (mHttpJob != null) {
                    mHttpJob.cancelTimeOut();
                }

                DataOutputStream out = null;
                if (!TextUtils.isEmpty(parameter.params)) {
                    out = new DataOutputStream(connection.getOutputStream());
                    out.write(parameter.params.getBytes("UTF-8"));
                    out.flush();
                }

                int responseCode = connection.getResponseCode();
                httpResult.responseCode = responseCode;
                Log.i(TAG, "doPost responseCode:" + responseCode);
                if (responseCode == 200) {
                    String result = readHttpResult(connection, isGzip);
                    httpResult.resultType = AsyncManager.RESULT_SUCCESS;
                    httpResult.result = result;
                    return httpResult;
                }

                if (count < (parameter.tryCount - 1) && parameter.trySpace > 0) {
                    try {
                        Thread.sleep(parameter.trySpace);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception when doPost retry sleep " + e);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            count++;
        }
        httpResult.resultType = AsyncManager.RESULT_FAILED;
        httpResult.result = null;
        return httpResult;
    }

    public boolean setHeaders(URLConnection connection, Map<String, String> headerMap) {
        boolean isGzip = false;
        if (headerMap != null && headerMap.size() > 0) {
            Iterator iterator = headerMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                String value = headerMap.get(key);
                if ("gzip".equals(value)) {
                    isGzip = true;
                }
                connection.setRequestProperty(key, value);
            }
        }
        return isGzip;
    }

    public String readHttpResult(URLConnection connection, boolean isGzip) throws IOException {
        StringBuffer resultSB = null;
        InputStream ins = null;
        if (isGzip) {
            ins = new GZIPInputStream(connection.getInputStream());
        } else {
            ins = connection.getInputStream();
        }
        if (ins != null) {
            InputStreamReader insr = new InputStreamReader(ins, "UTF-8");
            resultSB = new StringBuffer();
            int respInt = insr.read();
            while (respInt != -1) {
                resultSB.append((char) respInt);
                respInt = insr.read();
            }
            ins.close();
            insr.close();
        }
        String result = null;
        if (resultSB != null) {
            result = resultSB.toString();
        }
        if (TextUtils.isEmpty(result)) {
            //确保不返回null
            return "";
        }
        return result;
    }

}
