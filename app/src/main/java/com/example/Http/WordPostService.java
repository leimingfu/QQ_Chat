package com.example.Http;

import org.apache.http.NameValuePair;

import java.util.List;

public class WordPostService {


    public static String send(List<NameValuePair> params) {
        // 定位服务器的Servlet
        String servlet = "Final_TestServlet";
        // 通过 POST 方式获取 HTTP 服务器数据
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        return responseMsg;
    }
}
