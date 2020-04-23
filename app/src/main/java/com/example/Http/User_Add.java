package com.example.Http;

import android.util.Log;

import org.apache.http.NameValuePair;

import java.util.List;

public class User_Add {
    public static String send(List<NameValuePair> params) {
        // 返回值
        // 定位服务器的Servlet
        String servlet = "Add_UserServlet";
        // 通过 POST 方式获取 HTTP 服务器数据
        String responseMsg;
        responseMsg = MyHttpPost.executeHttpPost(servlet, params);
        Log.i("tag", "Add_UserServlet: responseMsg = " + responseMsg);
        System.out.println("成功发送");
        return responseMsg;
    }
}
