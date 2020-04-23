package com.example.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Fragment.ChatDBOpenHelper;
import com.example.Fragment.MainActivity;
import com.example.Fragment.MessageFragment;
import com.example.Fragment.Result;
import com.example.Http.WordPostService;
import com.example.Login.Register;
import com.example.SQLite.MyHelper;
import com.example.final_chat.R;

import net.sf.json.JSONObject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * 通知栏问题没解决
 *
 *
 *
 */


public class Message_Page extends AppCompatActivity {


    /**
     * @FirendName
     * 传过来的好友名字
     * */
    private String FriendName;



    /**
     * 用于绑定服务
     * @mReceiver 服务
     */
    private ContentReceiver mReceiver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message__page);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        FriendName=bundle.getString("FriendName");
        System.out.println("FriendName --> " + FriendName);
        //启动服务
        doRegisterReceiver();
        init();
    }


    MyHelper myHelper;
    List<Result> resultinfo = new ArrayList<>();



    private ListView listView;
    MyBaseAdapter myBaseAdapter;






    public Button btn_chat_message_send;
    public EditText et_chat_message;
    public TextView tv_chat_message;



    public void init()
    {
        //	View view = getActivity().getLayoutInflater().inflate(R.layout.message_layout, null);
        btn_chat_message_send = findViewById(R.id.btn_chat_message_send);
        et_chat_message = findViewById(R.id.et_chat_message);
        tv_chat_message = findViewById(R.id.username);
        tv_chat_message.setText(FriendName);
        listView = findViewById(R.id.lv_chat_dialog);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        myHelper = new MyHelper(Message_Page.this,com.example.Login.MainActivity.User_Now+".db");

        getList();
        myBaseAdapter = new MyBaseAdapter();
        listView.setAdapter(myBaseAdapter);

        btn_chat_message_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_chat_message.getText().toString().equals("") == true)
                {
                    Toast.makeText(Message_Page.this,"请输入内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                SQLiteDatabase db=myHelper.getWritableDatabase();
                db.execSQL("insert into " + FriendName + " (user,word) " + " values(?,?)",new Object[]{com.example.Login.MainActivity.User_Now,et_chat_message.getText().toString()});
                db.execSQL("delete from User_" + com.example.Login.MainActivity.User_Now + " where user = ?",new String[]{FriendName});
                db.execSQL("insert into User_" + com.example.Login.MainActivity.User_Now + "(user,word) " + " values(?,?)",new Object[]{FriendName,et_chat_message.getText().toString()});
                System.out.println("数据已经存入");
                db.close();
                Reset();
                new SendPostThread(com.example.Login.MainActivity.User_Now,et_chat_message.getText().toString()).start();
                et_chat_message.setText("");
            }
        });





    }



    private class MyBaseAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return resultinfo.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView tv1;
            //TextView tv2;
            Result result=resultinfo.get(position);
            if(result.getUser().equals(com.example.Login.MainActivity.User_Now) == true)
            {
                view = View.inflate(Message_Page.this,R.layout.right_item,null);
                tv1=(TextView)view.findViewById(R.id.tv_chat_me_message);
                //tv2=(TextView)view.findViewById(R.id.tv_name);
            }
            else
            {
                view = View.inflate(Message_Page.this,R.layout.left_item,null);
                tv1=(TextView)view.findViewById(R.id.tvname);
                //tv2=(TextView)view.findViewById(R.id.tv_leftname);
            }
            tv1.setText(result.getWord());
            //tv2.setText(result.getUser());

            return view;
        }






    }


    private void getList() {
        SQLiteDatabase db=myHelper.getReadableDatabase();
        String sql = "select * from " + FriendName;
        Cursor cursor=db.rawQuery(sql,null);
        while(cursor.moveToNext())
        {
            Result result=new Result();
            result.setid(cursor.getInt(cursor.getColumnIndex("_id")));
            result.setUser(cursor.getString(cursor.getColumnIndex("user")));
            result.setWord(cursor.getString(cursor.getColumnIndex("word")));
            resultinfo.add(result);
            System.out.println(result.toString());
        }
        cursor.close();
        db.close();
    }



    public void Reset()
    {
        resultinfo.clear();//清空List数组，重新扫描数据库内容存入
        getList();
        myBaseAdapter.notifyDataSetChanged();//Listview局部刷新，避免页面元素每次刷新都从第一条开始显示
    }




/**
 * SendPostThread  主要用于发送数据，也可以接收数据
 *
 *
 * */
    public class SendPostThread extends Thread {
        public String userword;
        public String username;

        public SendPostThread(String username, String userword) {
            this.username = username;
            this.userword = userword;
        }

        public void run() {
            // Sevice传回int
            String responseMsg;
            // 要发送的数据
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user_from", username));
            params.add(new BasicNameValuePair("user_to", FriendName));
            params.add(new BasicNameValuePair("word", userword));
            System.out.println("name = " + username + ", word = " + userword);
            // 发送数据，获取对象
            responseMsg = WordPostService.send(params);
            if(responseMsg == "发送失败")
                Toast.makeText(Message_Page.this,"发送失败，请重新发送",Toast.LENGTH_SHORT).show();
            if(responseMsg == "FAILED")
                Toast.makeText(Message_Page.this,"服务器未开启，请联系管理员",Toast.LENGTH_SHORT).show();

        }
    }



    @Override
    public void onStop()
    {
        super.onStop();
        finish();
    }


    /**
     * 注册广播接收者
     */
    private void doRegisterReceiver() {
        mReceiver=new ContentReceiver();
        IntentFilter filter = new IntentFilter(
                "com.example.Service.content");
        registerReceiver(mReceiver, filter);
    }


    /**
     *接收广播
     *
     *
     *
     */
    public class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("name");
            if(name.equals("canel") == false)
                Reset();
        }
    }








}
