package com.example.Login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.Http.LoginPostService;
import com.example.SQLite.MyHelper;
import com.example.final_chat.R;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.example.SQLite.sql_exist.tabbleIsExist;


public class MainActivity extends AppCompatActivity {

    public EditText name,pwd;
    public Button registerbtn,loginbtn;
    public static String User_Now;

    Handler handler;
    public Queue<String> que = new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_log);


        name = findViewById(R.id.login_edit_account);
        pwd = findViewById(R.id.login_edit_pwd);

        registerbtn = findViewById(R.id.login_btn_register);
        loginbtn = findViewById(R.id.login_btn_login);






        try {
            FileInputStream fis = openFileInput("data.txt");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            String r = new String(buffer);
            if(r.contains("&!@"))
            {
                String[] str = r.split("&!@");
                name.setText(str[0]);
                pwd.setText(str[1]);
                new LoginPostThread(str[0],str[1]).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectingToInternet() == false)
                {
                    Toast.makeText(MainActivity.this,"网络未连接",Toast.LENGTH_SHORT).show();
                    return;
                }
                String user = name.getText().toString();
                String password = pwd.getText().toString();
                if(user.equals(""))
                {
                    Toast.makeText(MainActivity.this,"请输入账号",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.equals(""))
                {
                    Toast.makeText(MainActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
                    return;
                }
                new LoginPostThread(user,password).start();
            }
        });


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(com.example.Login.MainActivity.this,com.example.Login.Register.class);
                startActivity(intent);
                finish();
            }
        });

        //Handle,Msg返回成功信息，跳转到其他Activity
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 111) {  // 处理发送线程传回的消息
                    if(msg.obj.toString().equals("账号密码不匹配"))
                    {
                        Toast.makeText(MainActivity.this,"账号密码不匹配",Toast.LENGTH_SHORT).show();
                    } else if(msg.obj.toString().equals("FAILED"))
                    {
                        Toast.makeText(MainActivity.this,"服务器未开启，请联系管理员",Toast.LENGTH_SHORT).show();
                    } else if(msg.obj.toString().equals("登录成功"))
                    {


                        //将账号密码存入到文件
                        try {
                            FileOutputStream fos = openFileOutput("data.txt", MODE_PRIVATE);
                            String string = name.getText().toString() + "&!@" + pwd.getText().toString();
                            fos.write(string.getBytes());
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        User_Now = name.getText().toString();
                        //Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();

                        MyHelper myHelper = new MyHelper(MainActivity.this,name.getText().toString()+".db");
                        SQLiteDatabase db = myHelper.getWritableDatabase();
                        //创建数据库，判断是否存在基本数据表
                        try{
                            System.out.println("tabbleIsExist(myHelper,\"User_\" + name.getText().toString())" + tabbleIsExist(myHelper,"User_" + name.getText().toString()));
                            if(tabbleIsExist(myHelper,"User_" + name.getText().toString()) == false)
                            {
                                db.execSQL("create table User_" + name.getText().toString() + " (_id integer primary " + " key autoincrement,user text,word text)");
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        try{
                        if(tabbleIsExist(myHelper, name.getText().toString() + "_friend"))
                            db.execSQL("DROP TABLE " + name.getText().toString() + "_friend");
                        db.execSQL("create table "+ name.getText().toString() + "_friend (_id integer primary " + " key autoincrement,user text)");
                        while(que.size() != 0){
                            db.execSQL("insert into " + name.getText().toString() + "_friend (user) " + " values(?)",new Object[]{que.poll()});
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }









                        Intent intent = new Intent(MainActivity.this,com.example.Fragment.MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if(msg.obj.toString().equals("未注册"))
                    {
                        Toast.makeText(MainActivity.this,"账号未注册",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }






    //登录Thread调用LoginPostService，返回Msg
    public class LoginPostThread extends Thread {
        public String id, password;
        public LoginPostThread(String id, String password) {
            this.id = id;
            this.password = password;
        }
        @Override
        public void run() {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("User_Name", id));
            params.add(new BasicNameValuePair("User_Password", password));
            // 发送数据，获取对象
            String responseMsg = LoginPostService.send(params);

            JSONObject Msg =null;
            int responseInt = 0;
            if(responseMsg != "FAILED")
            {
                Msg = JSONObject.fromObject(responseMsg);
                responseMsg = Msg.getString("TARGET");
                if(responseMsg.equals("登录成功")){
                    String username;
                    responseInt = Msg.getInt("COUNT");
                    for(int i=0;i<responseInt;i++)
                    {
                        username = Msg.getString("TARGET" + i);
                        que.offer(username);
                    }
                }


//                try{
//                    MyHelper myHelper = new MyHelper(MainActivity.this,name.getText().toString()+".db");
//                    SQLiteDatabase db = myHelper.getWritableDatabase();
//                    if(tabbleIsExist(myHelper, name.getText().toString() + "_friend"))
//                        db.execSQL("DROP TABLE " + name.getText().toString() + "_friend");
//                    db.execSQL("create table "+ name.getText().toString() + "_friend (_id integer primary " + " key autoincrement,user text)");
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

            }
            // 准备发送消息
            Message msg = handler.obtainMessage();
            // 设置消息默认值
            msg.what = 111;
            msg.obj = responseMsg;
            handler.sendMessage(msg);
        }
    }

    // 检测网络状态
    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }


}
