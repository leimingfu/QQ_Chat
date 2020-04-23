package com.example.Login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.Http.RegisterPostService;
import com.example.SQLite.MyHelper;
import com.example.final_chat.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.SQLite.sql_exist.tabbleIsExist;


public class Register extends AppCompatActivity {



    public EditText name,pwd1,pwd2;
    public Button btn_certain,btn_cancel;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = (EditText) findViewById(R.id.resetpwd_edit_name);
        pwd1 = (EditText) findViewById(R.id.resetpwd_edit_pwd_old);
        pwd2 = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);

        btn_certain = (Button) findViewById(R.id.register_btn_sure);
        btn_cancel = (Button) findViewById(R.id.register_btn_cancel);


        btn_certain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectingToInternet() == false) {
                    Toast.makeText(Register.this, "网络未连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                String user = name.getText().toString();
                String password1 = pwd1.getText().toString();
                String password2 = pwd2.getText().toString();
                if (user.equals("")) {
                    Toast.makeText(Register.this, "请输入账号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password1.equals("")) {
                    Toast.makeText(Register.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password2.equals("")) {
                    Toast.makeText(Register.this, "请输入确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password1.equals(password2) == false)
                {
                    Toast.makeText(Register.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                new RegisterPostThread(user, password1).start();
            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Handle,Msg返回成功信息，跳转到其他Activity
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 222) {  // 处理发送线程传回的消息
                    if (msg.obj.toString().equals("已注册")) {
                        Toast.makeText(Register.this, "账号已被注册，请重新输入", Toast.LENGTH_SHORT).show();
                    } else if (msg.obj.toString().equals("FAILED")) {
                        Toast.makeText(Register.this, "服务器未开启，请联系管理员", Toast.LENGTH_SHORT).show();
                    } else if (msg.obj.toString().equals("注册成功")) {
                        //Toast.makeText(Register.this,"注册成功",Toast.LENGTH_SHORT).show();
                        try {
                            FileOutputStream fos = openFileOutput("data.txt", MODE_PRIVATE);
                            String string = name.getText().toString() + "&!@" + pwd1.getText().toString();
                            fos.write(string.getBytes());
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        com.example.Login.MainActivity.User_Now = name.getText().toString();
                        try{
                            MyHelper myHelper = new MyHelper(Register.this,name.getText().toString()+".db");
                            SQLiteDatabase db = myHelper.getWritableDatabase();
                            if(tabbleIsExist(myHelper,name.getText().toString()))
                                db.execSQL("DROP TABLE User_" + name.getText().toString());
                            db.execSQL("create table User_" + name.getText().toString() + " (_id integer primary " + " key autoincrement,user text,word text)");
//                            if(tabbleIsExist(myHelper,name.getText().toString()) == false)
//                            {
//                                db.execSQL("create table User_" + name.getText().toString() + " (_id integer primary " + " key autoincrement,user text,word text)");
//                            }
                            if(tabbleIsExist(myHelper, name.getText().toString() + "_friend"))
                                db.execSQL("DROP TABLE " + name.getText().toString() + "_friend");
                            db.execSQL("create table "+ name.getText().toString() + "_friend (_id integer primary " + " key autoincrement,user text)");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(Register.this,com.example.Fragment.MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (msg.obj.toString().equals("注册失败")) {
                        Toast.makeText(Register.this,"数据库错误",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };


    }


    //登录Thread调用LoginPostService，返回Msg
    public class RegisterPostThread extends Thread {
        public String id, password;
        public RegisterPostThread(String id, String password) {
            this.id = id;
            this.password = password;
        }
        @Override
        public void run() {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("User_Name", id));
            params.add(new BasicNameValuePair("User_Password", password));
            // 发送数据，获取对象
            String responseMsg = RegisterPostService.send(params);
            // 准备发送消息
            Message msg = handler.obtainMessage();
            // 设置消息默认值
            msg.what = 222;
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
