package com.example.common.util.asyncmanager;

public class HttpResult {
    public int resultType;
    public String result;
    public int responseCode;

    @Override
    public String toString() {
        return "HttpResult{" +
                "resultType=" + resultType +
                ", responseCode=" + responseCode +
                '}';
    }
}
