package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

//@SpringBootTest
public class HttpClientTest {

    /**
     * 测试HttpGet请求
     */
    @Test
    public void HttpGetTest() throws IOException {

        //创建httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();


        //创建get请求对象
        HttpGet httpGet=new HttpGet("http://localhost:8080/user/shop/status");

        //发送请求,得到响应
        CloseableHttpResponse response = httpClient.execute(httpGet);


        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("响应状态码为:"+statusCode);

        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        System.out.println("响应体为:"+body);

        response.close();
        httpClient.close();
    }

    /**
     * 测试HttpPost请求
     */
    @Test
    public void HttpPostTest() throws Exception {

        //创建httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();


        //创建get请求对象
        HttpPost httpPost=new HttpPost("http://localhost:8080/admin/employee/login");

        //构造发送请求的json数据,发送格式，编码
        JSONObject json=new JSONObject();
        json.put("username","admin");
        json.put("password","123456");

        StringEntity entity=new StringEntity(json.toString());

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("Content-Encoding","utf-8");



        //发送请求
        CloseableHttpResponse response = httpClient.execute(httpPost);


        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("响应状态码为:"+statusCode);

        HttpEntity responseEntity = response.getEntity();
        String body = EntityUtils.toString(responseEntity);
        System.out.println("响应体为:"+body);

        response.close();
        httpClient.close();
    }
}
