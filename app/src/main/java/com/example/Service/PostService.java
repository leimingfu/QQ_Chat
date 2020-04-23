package com.example.Service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.Fragment.Result;
import com.example.Http.WordPostService;
import com.example.Login.MainActivity;
import com.example.SQLite.MyHelper;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.SQLite.sql_exist.tabbleIsExist;

public class PostService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        new ReceivePostThread().start();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 222) {  // 处理发送线程传回的消息
                    sendContentBroadcast(msg.obj.toString());
                }
            }
        };
    }


    /**
     * 解除绑定时调用该方法
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public final class LocalBinder extends Binder {
        public PostService getService() {
            return PostService.this;
        }
    }

    /**
     * 发送广播
     */
    protected void sendContentBroadcast(String name) {
        // TODO Auto-generated method stub
        Intent intent=new Intent();
        intent.setAction("com.example.Service.content");
        intent.putExtra("name", name);
        sendBroadcast(intent);
    }


    public void Asya(){
        sendContentBroadcast("canel");
    }




    Handler handler;




    /**
     *间隔一秒向服务器发送请求，如果有新消息则更新本地数据库，并发送广播告知Activity
     *
     *
     *
     */
    public class ReceivePostThread extends Thread {
        public String userword;
        public String username;

        public ReceivePostThread() {
            username = com.example.Login.MainActivity.User_Now;
            userword = "@qwe#";
        }

        @Override
        public void run() {
            // Sevice传回int
            String responseMsg;
            // 要发送的数据
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_from", username));
            params.add(new BasicNameValuePair("user_to", "NOBODY"));
            params.add(new BasicNameValuePair("word", userword));
            System.out.println("user_from = " + username + ", user_to = NOBODY, word = " + userword);
            while(com.example.Fragment.MainActivity.XUANCHENG_FLAG)
            {
                System.out.println("Post -->" + username);
                // 发送数据，获取对象
                responseMsg = WordPostService.send(params);
                JSONObject Msg =null;
                int responseInt = 0;
                String ss = "content";
                if(responseMsg != "FAILED")
                {
                    Msg = JSONObject.fromObject(responseMsg);
                    responseInt = Msg.getInt("ENTER_CODE");
                }
                if(responseInt == 1 && responseMsg.equals("FAILED") == false)
                {
                    MyHelper myHelper = new MyHelper(PostService.this,com.example.Login.MainActivity.User_Now+".db");
                    SQLiteDatabase db=myHelper.getWritableDatabase();
                    int count = Msg.getInt("TARGET");
                    String[] str;
                    String tmp = null;
                    for(int i = 1; i <= count; i++)
                    {
                        tmp = Msg.getString("TARGET" + i);
                        str = tmp.split("_!&_");
                        if(str[1].equals("@#Request_ADD_YOUU#@")){
                            ss = "friend";
                            if(tabbleIsExist(myHelper,"User_AdD") == false)
                            {
                                db.execSQL("create table User_AdD (_id integer primary " + " key autoincrement,user text)");
                            }
                            String name = str[0];
                            String tmp_eq = "";
                            Cursor cursor=db.rawQuery("select * from User_AdD",null);
                            while(cursor.moveToNext())
                            {
                                tmp_eq = cursor.getString(cursor.getColumnIndex("user"));
                                if(tmp_eq.equals(name))
                                    break;
                            }
                            cursor.close();
                            if(tmp_eq.equals(name) == false)
                            {
                                db.execSQL("insert into User_AdD (user) " + " values(?)",new Object[]{str[0]});
                            }

                        } else if(str[1].equals("@#YES_ADD_YOUU#@")){
                            ss = "friend";
                            if(tabbleIsExist(myHelper,str[0]) == false)
                            {
                                db.execSQL("create table " + str[0] + " (_id integer primary " + " key autoincrement,user text,word text)");
                            }
                            db.execSQL("insert into " + str[0] + "(user,word) " + " values(?,?)",new Object[]{MainActivity.User_Now,"你好，我们开始聊天吧！"});
                            db.execSQL("insert into User_" + com.example.Login.MainActivity.User_Now + "(user,word) " + " values(?,?)",new Object[]{str[0],"你好，我们开始聊天吧！"});






                            String name = str[0];
                            String tmp_eq = "";
                            Cursor cursor=db.rawQuery("select * from " + MainActivity.User_Now + "_friend",null);
                            while(cursor.moveToNext())
                            {
                                tmp_eq = cursor.getString(cursor.getColumnIndex("user"));
                                if(tmp_eq.equals(name))
                                    break;
                            }
                            cursor.close();
                            if(tmp_eq.equals(name) == false)
                            {
                                db.execSQL("insert into " + MainActivity.User_Now + "_friend (user) " + " values(?)",new Object[]{str[0]});
                            }

                        } else if(str[1].equals("@#YES_CANEL_ADD_YOUU#@")){
                            ss = "friend";
                            db.execSQL("delete from User_AdD where user = ?",new String[]{str[0]});
                            if(tabbleIsExist(myHelper,str[0]) == false)
                            {
                                db.execSQL("create table " + str[0] + " (_id integer primary " + " key autoincrement,user text,word text)");
                            }
                            try{
                                db.execSQL("insert into " + str[0] + "(user,word) " + " values(?,?)",new Object[]{str[0],"你好，我们开始聊天吧！"});
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                db.execSQL("insert into User_" + com.example.Login.MainActivity.User_Now + "(user,word) " + " values(?,?)",new Object[]{str[0],"你好，我们开始聊天吧！"});
                            }catch (Exception e){
                                e.printStackTrace();
                            }





                            String name = str[0];
                            String tmp_eq = "";
                            Cursor cursor=db.rawQuery("select * from " + MainActivity.User_Now + "_friend",null);
                            while(cursor.moveToNext())
                            {
                                tmp_eq = cursor.getString(cursor.getColumnIndex("user"));
                                if(tmp_eq.equals(name))
                                    break;
                            }
                            cursor.close();
                            if(tmp_eq.equals(name) == false)
                            {
                                db.execSQL("insert into " + MainActivity.User_Now + "_friend (user) " + " values(?)",new Object[]{str[0]});
                            }
                        }
                        else if(str[1].equals("@#NO_ADD_YOUU#@")){
                            ss = "friend";
                            db.execSQL("delete from User_AdD where user = ?",new String[]{str[0]});
                        }
                        else{
                            db.execSQL("insert into " + str[0] + "(user,word) " + " values(?,?)",new Object[]{str[0],str[1]});
                            db.execSQL("delete from User_" + com.example.Login.MainActivity.User_Now + " where user = ?",new String[]{str[0]});
                            db.execSQL("insert into User_" + com.example.Login.MainActivity.User_Now + "(user,word) " + " values(?,?)",new Object[]{str[0],str[1]});
                        }
                    }
                    db.close();
                    Message msg = handler.obtainMessage();
                    msg.what = 222;
                    msg.obj = ss;
                    handler.sendMessage(msg);
                }
                try {
                    Thread.sleep(1000);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }






}
