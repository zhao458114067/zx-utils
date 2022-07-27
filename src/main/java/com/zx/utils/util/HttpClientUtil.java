package com.zx.utils.util;

import com.alibaba.fastjson.JSONObject;
import com.zx.utils.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: zhaoxu
 * @description:
 */
@Slf4j
public class HttpClientUtil {
    private static PoolingHttpClientConnectionManager cm = null;
    private static RequestConfig requestConfig = null;

    static {

        LayeredConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            log.error("创建SSL连接失败");
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        //多线程调用注意配置，根据线程数设定
        cm.setMaxTotal(200);
        //多线程调用注意配置，根据线程数设定
        cm.setDefaultMaxPerRoute(300);
        requestConfig = RequestConfig.custom()
                //数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(20000)
                //连接建立时间，三次握手完成时间
                .setConnectTimeout(20000)
                //重点参数
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();
    }

    public static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        return httpClient;
    }

    public static void closeResponse(CloseableHttpResponse closeableHttpResponse) throws IOException {
        EntityUtils.consume(closeableHttpResponse.getEntity());
        closeableHttpResponse.close();
    }

    /**
     * get请求,params可为null,headers可为null
     *
     * @param headers
     * @param url
     * @return
     * @throws IOException
     */
    public static String get(JSONObject headers, String url, JSONObject params) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse closeableHttpResponse = null;
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
        try {
            if (url.contains(Constants.QUESTION)) {
                httpGet = new HttpGet(url + "&" + EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8)));
            } else {
                httpGet = new HttpGet(url + "?" + EntityUtils.toString(new UrlEncodedFormEntity(paramList, Consts.UTF_8)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpGet.addHeader(headerName, headers.get(headerName).toString());
            }
        }
        httpGet.setConfig(requestConfig);
        httpGet.addHeader("Content-Type", "application/json");
        httpGet.addHeader("lastOperaTime", String.valueOf(System.currentTimeMillis()));
        try {
            closeableHttpResponse = httpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            String response = EntityUtils.toString(entity);
            closeResponse(closeableHttpResponse);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post请求,params可为null,headers可为null
     *
     * @param headers
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String post(JSONObject headers, String url, JSONObject params) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse closeableHttpResponse = null;
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpPost.addHeader(headerName, headers.get(headerName).toString());
            }
        }
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("lastOperaTime", String.valueOf(System.currentTimeMillis()));
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString(), "UTF-8");
            httpPost.setEntity(stringEntity);
        }
        try {
            closeableHttpResponse = httpClient.execute(httpPost);
            HttpEntity entity = closeableHttpResponse.getEntity();
            String response = EntityUtils.toString(entity);
            closeResponse(closeableHttpResponse);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * delete,params可为null,headers可为null
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String delete(JSONObject headers, String url, JSONObject params) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse closeableHttpResponse = null;
        // 创建delete请求，HttpDeleteWithBody 为内部类，类在下面
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpDelete.addHeader(headerName, headers.get(headerName).toString());
            }
        }
        httpDelete.setConfig(requestConfig);
        httpDelete.addHeader("Content-Type", "application/json");
        httpDelete.addHeader("lastOperaTime", String.valueOf(System.currentTimeMillis()));
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString(), "UTF-8");
            httpDelete.setEntity(stringEntity);
        }
        try {
            closeableHttpResponse = httpClient.execute(httpDelete);
            HttpEntity entity = closeableHttpResponse.getEntity();
            String response = EntityUtils.toString(entity);
            closeResponse(closeableHttpResponse);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * put,params可为null,headers可为null
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String put(JSONObject headers, String url, JSONObject params) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse closeableHttpResponse = null;
        // 创建put请求
        HttpPut httpPut = new HttpPut(url);
        if (headers != null) {
            Iterator iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String headerName = iterator.next().toString();
                httpPut.addHeader(headerName, headers.get(headerName).toString());
            }
        }
        httpPut.setConfig(requestConfig);
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.addHeader("lastOperaTime", String.valueOf(System.currentTimeMillis()));
        if (params != null) {
            StringEntity stringEntity = new StringEntity(params.toJSONString(), "UTF-8");
            httpPut.setEntity(stringEntity);
        }
        try {
            // 从响应模型中获得具体的实体
            closeableHttpResponse = httpClient.execute(httpPut);
            HttpEntity entity = closeableHttpResponse.getEntity();
            String response = EntityUtils.toString(entity);
            closeResponse(closeableHttpResponse);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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