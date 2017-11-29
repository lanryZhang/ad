package com.ifeng.hippo.utils;

/**
 * Created by zhanglr on 2016/9/19.
 */
public class HttpResult {
    private int statusCode;
    private Object body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
