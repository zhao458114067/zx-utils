package com.zx.repository.util;

import com.alibaba.fastjson.JSONObject;
import com.zx.repository.constant.Constants;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author : zhaoxu
 */
@Component
public class HttpClientUtil {
    /**
     * get请求,params可为null,headers可为null
     *
     * @param headers 请求头
     * @param url 地址
     * @param params 参数
     * @return 字符串
     * @throws IOException io出错
     */
    public static String get(Map<String, String> headers, String url, JSONObject params) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建get请求
        HttpGet httpGet = null;
        List<BasicNameValuePair> paramList = new ArrayList<>();
        if (params != null) {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext()) {
                String paramName = iterator.next();
                paramList.add(new BasicNameValuePair(paramName, params.get(paramName).toString()));
            }
        }
        if (url.contains(Constants.QUESTION)) {
            httpGet = new HttpGet(url + "&" + EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8)));
        } else {
            httpGet = new HttpGet(url + "?" + EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8)));
        }

        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpGet.addHeader(headerName, headers.get(headerName));
            }
        }
        httpGet.addHeader("Content-Type", "application/json");
        HttpEntity entity = httpClient.execute(httpGet).getEntity();
        String response = EntityUtils.toString(entity);
        httpClient.close();
        return response;
    }

    /**
     * post请求,params可为null,headers可为null
     *
     * @param headers 请求头
     * @param url 地址
     * @param params 参数
     * @return 字符串
     * @throws IOException io出错
     */
    public static String post(Map<String, String> headers, String url, JSONObject params) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpPost.addHeader(headerName, headers.get(headerName));
            }
        }
        httpPost.addHeader("Content-Type", "application/json");
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString());
            httpPost.setEntity(stringEntity);
        }
        HttpEntity entity = httpClient.execute(httpPost).getEntity();
        String response = EntityUtils.toString(entity);
        httpClient.close();
        return response;
    }

    /**
     * delete,params可为null,headers可为null
     *
     * @param headers 请求头
     * @param url 地址
     * @param params 参数
     * @return 字符串
     * @throws IOException io出错
     */
    public static String delete(Map<String, String> headers, String url, JSONObject params) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建delete请求，HttpDeleteWithBody 为内部类，类在下面
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpDelete.addHeader(headerName, headers.get(headerName));
            }
        }
        httpDelete.addHeader("Content-Type", "application/json");
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString());
            httpDelete.setEntity(stringEntity);
        }
        HttpEntity entity = httpClient.execute(httpDelete).getEntity();
        String response = EntityUtils.toString(entity);
        httpClient.close();
        return response;
    }

    /**
     * put,params可为null,headers可为null
     *
     * @param headers 请求头
     * @param url 地址
     * @param params 参数
     * @return 字符串
     * @throws IOException io出错
     */
    public static String put(Map<String, String> headers, String url, JSONObject params) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建put请求
        HttpPut httpPut = new HttpPut(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpPut.addHeader(headerName, headers.get(headerName));
            }
        }
        httpPut.addHeader("Content-Type", "application/json");
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString());
            httpPut.setEntity(stringEntity);
        }
        // 从响应模型中获得具体的实体
        HttpEntity entity = httpClient.execute(httpPut).getEntity();
        String response = EntityUtils.toString(entity);
        httpClient.close();
        return response;
    }

    public static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody() {
            super();
        }
    }
}