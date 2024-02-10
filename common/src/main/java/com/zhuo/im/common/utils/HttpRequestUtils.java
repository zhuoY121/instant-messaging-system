package com.zhuo.im.common.utils;

import com.alibaba.fastjson.JSON;
import com.zhuo.im.common.config.GlobalHttpClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @description:
 **/
@Component
public class HttpRequestUtils {

    @Autowired
    private CloseableHttpClient httpClient;

    @Autowired
    private RequestConfig requestConfig;

    @Autowired
    GlobalHttpClientConfig httpClientConfig;

    public String doGet(String url, Map<String, Object> params, String charset) throws Exception {
        return doGet(url, params, null, charset);
    }

    /**
     * Obtain server data through the given url address
     *
     * @param url server address
     * @param params encapsulates user parameters
     * @param charset set character encoding
     * @return
     */
    public String doGet(String url, Map<String, Object> params, Map<String, Object> header, String charset) throws Exception {

        if (StringUtils.isEmpty(charset)) {
            charset = "utf-8";
        }
        URIBuilder uriBuilder = new URIBuilder(url);

        if (params != null) {
            // Traverse the map and splice request parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }

        // Declare http get request
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setConfig(requestConfig);

        if (header != null) {
            // Traverse the map and splice header parameters
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        String result = "";
        try {
            // Send request
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // Determine whether the status code is 200
            if (response.getStatusLine().getStatusCode() == 200) {
                // return the response body
                result = EntityUtils.toString(response.getEntity(), charset);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * GET request, with URL parameters
     *
     * @param url
     * @param params
     * @return If the status code is 200, return body, if not 200, return null
     * @throws Exception
     */
    public String doGet(String url, Map<String, Object> params) throws Exception {
        return doGet(url, params, null);
    }

    /**
     * GET request, without URL parameters
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String doGet(String url) throws Exception {
        return doGet(url, null, null);
    }

    public String doPost(String url, Map<String, Object> params, String jsonBody, String charset) throws Exception {
        return doPost(url,params,null,jsonBody,charset);
    }

    /**
     * Post request with parameters
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String doPost(String url, Map<String, Object> params, Map<String, Object> header, String jsonBody, String charset) throws Exception {

        if (StringUtils.isEmpty(charset)) {
            charset = "utf-8";
        }
        URIBuilder uriBuilder = new URIBuilder(url);

        if (params != null) {
            // Traverse the map and splice request parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }

        // Declare httpPost request
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        // Add configurations
        httpPost.setConfig(requestConfig);

        if (StringUtils.isNotEmpty(jsonBody)) {
            StringEntity s = new StringEntity(jsonBody, charset);
            s.setContentEncoding(charset);
            s.setContentType("application/json");

            // Put the json body into the post request
            httpPost.setEntity(s);
        }

        if (header != null) {
            // Traverse the map and splice header parameters
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                httpPost.addHeader(entry.getKey(),entry.getValue().toString());
            }
        }

        String result = "";
        // CloseableHttpClient httpClient = HttpClients.createDefault(); // single
        CloseableHttpResponse response = null;
        try {
            // Send request
            response = httpClient.execute(httpPost);
            // Determine whether the status code is 200
            if (response.getStatusLine().getStatusCode() == 200) {
                // return the response body
                result = EntityUtils.toString(response.getEntity(), charset);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Post request without parameters
     * @param url
     * @return
     * @throws Exception
     */
    public String doPost(String url) throws Exception {
        return doPost(url, null,null,null);
    }

    /**
     * GET: General way of calling methods
     * @param url
     * @param tClass
     * @param map
     * @param charSet
     * @return
     * @throws Exception
     */
    public <T> T doGet(String url, Class<T> tClass, Map<String, Object> map, String charSet) throws Exception {

        String result = doGet(url, map, charSet);
        if (StringUtils.isNotEmpty(result))
            return JSON.parseObject(result, tClass);
        return null;
    }

    /**
     * GET: General way of calling methods
     * @param url
     * @param tClass
     * @param map
     * @param charSet
     * @return
     * @throws Exception
     */
    public <T> T doGet(String url, Class<T> tClass, Map<String, Object> map, Map<String, Object> header, String charSet) throws Exception {

        String result = doGet(url, map, header, charSet);
        if (StringUtils.isNotEmpty(result))
            return JSON.parseObject(result, tClass);
        return null;
    }

    /**
     * POST: General way of calling methods
     * @param url
     * @param tClass
     * @param map
     * @param jsonBody
     * @param charSet
     * @return
     * @throws Exception
     */
    public <T> T doPost(String url, Class<T> tClass, Map<String, Object> map, String jsonBody, String charSet) throws Exception {

        String result = doPost(url, map, jsonBody, charSet);
        if (StringUtils.isNotEmpty(result))
            return JSON.parseObject(result, tClass);
        return null;
    }

    public <T> T doPost(String url, Class<T> tClass, Map<String, Object> map, Map<String, Object> header, String jsonBody, String charSet) throws Exception {

        String result = doPost(url, map, header, jsonBody, charSet);
        if (StringUtils.isNotEmpty(result))
            return JSON.parseObject(result, tClass);
        return null;
    }

    /**
     * POST: General way of calling methods
     * @param url
     * @param map
     * @param jsonBody
     * @param charSet
     * @return
     * @throws Exception
     */
    public String  doPostString(String url, Map<String, Object> map, String jsonBody, String charSet) throws Exception {
        return doPost(url, map, jsonBody, charSet);
    }

    /**
     * POST: General way of calling methods
     * @param url
     * @param map
     * @param jsonBody
     * @param charSet
     * @return
     * @throws Exception
     */
    public String  doPostString(String url, Map<String, Object> map, Map<String, Object> header, String jsonBody, String charSet) throws Exception {
        return doPost(url, map, header, jsonBody, charSet);
    }

}
