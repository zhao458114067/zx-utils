package com.zx.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

/**
 * @author: zhaoxu
 * @description:
 */
public class HttpClientUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final RequestConfig requestConfig;
    private static final CloseableHttpClient httpClient;

    static {
        LayeredConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            log.error("创建SSL连接失败");
        }
        assert sslsf != null;
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(Runtime.getRuntime().availableProcessors() * 2);
        cm.setDefaultMaxPerRoute(Runtime.getRuntime().availableProcessors() * 2);

        requestConfig = RequestConfig.custom()
                .setSocketTimeout(20000)
                .setConnectTimeout(20000)
                .setExpectContinueEnabled(true)
                .setConnectionRequestTimeout(10000)
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    public static void closeResponse(CloseableHttpResponse closeableHttpResponse) throws IOException {
        EntityUtils.consume(closeableHttpResponse.getEntity());
        closeableHttpResponse.close();
    }

    public static <T> T get(String url, Class<T> tClass) {
        return get(null, url, tClass);
    }

    /**
     * get请求
     * @param headers 请求头，可为空
     * @param url     请求地址
     * @param tClass  返回类型
     * @return
     * @param <T>     返回类型泛型
     */
    public static <T> T get(JSONObject headers, String url, Class<T> tClass) {
        // 创建get请求
        HttpGet httpGet = new HttpGet(url);
        initHttpRequest(headers, httpGet);
        return executeRequest(httpGet, tClass);
    }

    public static <T> T post(String url, Object requestBody, Class<T> tClass) {
        return post(null, url, requestBody, tClass);
    }

    /**
     * post请求
     *
     * @param headers       请求头，可为空
     * @param url           请求地址
     * @param requestBody   请求体，可为空
     * @param tClass        返回类型
     * @return
     * @param <T>           返回类型泛型
     */
    public static <T> T post(JSONObject headers, String url, Object requestBody, Class<T> tClass) {
        // 创建post请求
        HttpPost httpRequest = new HttpPost(url);
        return executeRequestWithBody(headers, requestBody, tClass, httpRequest);
    }

    public static <T> T put(String url, Object requestBody, Class<T> tClass) {
        return put(null, url, requestBody, tClass);
    }

    /**
     * put请求
     *
     * @param headers       请求头，可为空
     * @param url           请求地址
     * @param requestBody   请求体，可为空
     * @param tClass        返回类型
     * @return
     * @param <T>           返回类型泛型
     */
    public static <T> T put(JSONObject headers, String url, Object requestBody, Class<T> tClass) {
        // 创建put请求
        HttpPut httpRequest = new HttpPut(url);
        return executeRequestWithBody(headers, requestBody, tClass, httpRequest);
    }

    public static <T> T delete(String url, Class<T> tClass) {
        return delete(null, url, null, tClass);
    }

    public static <T> T delete(String url, Object requestBody, Class<T> tClass) {
        return delete(null, url, requestBody, tClass);
    }

    /**
     * delete请求
     *
     * @param headers       请求头，可为空
     * @param url           请求地址
     * @param requestBody   请求体，可为空
     * @param tClass        返回类型
     * @return
     * @param <T>           返回类型泛型
     */
    public static <T> T delete(JSONObject headers, String url, Object requestBody, Class<T> tClass) {
        // 创建delete请求，HttpDeleteWithBody 为内部类，类在下面
        HttpDeleteWithBody httpRequest = new HttpDeleteWithBody(url);
        return executeRequestWithBody(headers, requestBody, tClass, httpRequest);
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

    private static <T> T executeRequestWithBody(JSONObject headers, Object requestBody, Class<T> tClass, HttpEntityEnclosingRequestBase requestBase) {
        initHttpRequest(headers, requestBase);
        setRequestBody(requestBody, requestBase);
        return executeRequest(requestBase, tClass);
    }

    private static void initHttpRequest(JSONObject headers, HttpRequestBase httpRequestBase) {
        if (headers != null) {
            headers.forEach((k, v) -> httpRequestBase.addHeader(k, v.toString()));
        }
        httpRequestBase.addHeader("Content-Type", "application/json");
        httpRequestBase.setConfig(requestConfig);
    }

    private static void setRequestBody(Object requestBody, HttpEntityEnclosingRequest entityEnclosingRequest) {
        if (requestBody != null) {
            String requestJsonString = JSON.toJSONString(requestBody);
            StringEntity stringEntity = new StringEntity(requestJsonString, "UTF-8");
            entityEnclosingRequest.setEntity(stringEntity);
        }
    }

    private static <T> T executeRequest(HttpRequestBase httpRequestBase, Class<T> tClass) {
        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(httpRequestBase)) {
            HttpEntity entity = closeableHttpResponse.getEntity();
            String resposneString = EntityUtils.toString(entity);
            return JSON.parseObject(resposneString, tClass);
        } catch (IOException e) {
            log.error("接口请求异常，httpRequestBase：{}", httpRequestBase);
            throw new RuntimeException(e);
        }
    }
}