/*
* ProcessEx.java 
* Created on  202017/7/25 17:04 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core;

import com.alibaba.fastjson.JSON;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.UserAgentInfo;
import com.ifeng.hippo.proxy.Proxy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.io.InputStream;
import java.net.URI;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ProcessEx{
    private UserAgentInfo userAgentInfo;
    private Proxy proxy ;
    private Process process;
    private String pvOrUv;
    private HttpClient httpClient;
    private HttpGet request;
    private HttpClientContext context = new HttpClientContext();
    private String proxyPoolKey = "";
    private int taskCount;

    public ProcessEx(){
        pvOrUv = " PV ";
    }
    public void wrap(Process process){
        this.process = process;
    }

    public void wrap(HttpClient httpClient ){
        this.httpClient = httpClient;
    }
    public InputStream getInputStream(){
        if (process != null) {
            return process.getInputStream();
        } else {
            return null;
        }
    }

    public HttpResponse getHttpResponse() throws Exception{
        if (httpClient != null){
            return httpClient.execute(request,context);
        }else {
            return null;
        }
    }

    private void caculateTaskCount(TaskFragment tf) {
        if (tf == null){
            return;
        }
        taskCount ++;
        if (tf.getSubFragments() != null) {
            for (TaskFragment t : tf.getSubFragments()) {
                caculateTaskCount(t);
            }
        }
    }

    private int successCount = 0;
    private String returnStr = "open error";
    private KeyValuePair<String,String> resKv = new KeyValuePair<>();

    public KeyValuePair<String,String> execute(TaskFragment tf) throws Exception{
        if (taskCount == 0) {
            caculateTaskCount(tf);
        }

        if (httpClient != null){

            HttpResponse res = httpClient.execute(request,context);
            String cookie = "cookie:"+ JSON.toJSONString(context.getCookieStore().getCookies());

            if (null == resKv.getK() || "".equals(resKv.getK()))
                resKv.setK(cookie);

            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                successCount++;
//                Thread.currentThread().sleep(tf.getWaitTimeout() * 1000);
                if (tf.getSubFragments() != null && tf.getSubFragments().size() > 0) {
                    for (TaskFragment t : tf.getSubFragments()) {
                        request.setURI(new URI(t.getUrl()));
                        execute(t);
                    }
                }
            }
        }else {
            returnStr = "open error";
        }

        if (taskCount == successCount){
            returnStr = "open success";
        }
        resKv.setV(returnStr);
        return resKv;
    }

    public void releaseConnection(){
        try {
            if (request != null) {
                request.releaseConnection();
            }
        }catch (Exception er){
            er.printStackTrace();
        }
    }
    public HttpClientContext getContext() {
        return context;
    }

    public void setRequest(HttpGet request) {
        this.request = request;
    }

    public String getPvOrUv() {
        return pvOrUv;
    }

    public void setPvOrUv(String pvOrUv) {
        this.pvOrUv = pvOrUv;
    }

    public UserAgentInfo getUserAgentInfo() {
        return userAgentInfo;
    }

    public void setUserAgentInfo(UserAgentInfo userAgentInfo) {
        this.userAgentInfo = userAgentInfo;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void destroy(){
        if (process != null){
            process.destroy();
        }
    }

    public String getProxyPoolKey() {
        return proxyPoolKey;
    }

    public void setProxyPoolKey(String proxyPoolKey) {
        this.proxyPoolKey = proxyPoolKey;
    }
}
