package com.example.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Http.User_Add;
import com.example.Http.WordPostService;
import com.example.Login.MainActivity;
import com.example.Message.Message_Page;
import com.example.SQLite.MyHelper;
import com.example.Service.PostService;
import com.example.final_chat.R;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.example.SQLite.sql_exist.tabbleIsExist;

public class ContactsFragment extends Fragment {

	View contactsLayout;
	private Context mContext;
	Handler handler;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		contactsLayout = inflater.inflate(R.layout.contacts_layout,
				container, false);
		this.mContext = getActivity();
		return contactsLayout;
	}


	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private Button btn;
	private EditText ed;
	MyHelper myHelper;
	//好友申请数据处理
	List<Result> resultinfo1 = new ArrayList<>();
	private ListView listView1;
	MyBaseAdapter1 myBaseAdapter1;
	//好友列表数据处理
	List<Result> resultinfo2 = new ArrayList<>();
	private ListView listView2;
	MyBaseAdapter2 myBaseAdapter2;

	/**
	 * 用于绑定服务
	 * @mReceiver 服务
	 */
	private ContentReceiver mReceiver;
	/**
	 * 用于绑定服务
	 * @mReceiver 服务
	 * @conn 绑定回调函数
	 */

	private MyServiceConn conn;
	private PostService service;

	public void init(){


		handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 333) {  // 处理发送线程传回的消息
					if(!msg.obj.toString().equals("responseMsg"))
						Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_LONG).show();
					//sendContentBroadcast();
				}
			}
		};


		myHelper = new MyHelper(getActivity(),com.example.Login.MainActivity.User_Now+".db");


		if(tabbleIsExist(myHelper,"User_AdD") == false)
		{
			SQLiteDatabase db=myHelper.getWritableDatabase();
			db.execSQL("create table User_AdD (_id integer primary " + " key autoincrement,user text)");
			db.close();
		}




		//启动服务

		//启动服务
		conn = new MyServiceConn();
		getActivity().bindService(new Intent(getActivity(), PostService.class), conn,
				BIND_AUTO_CREATE);
		doRegisterReceiver();


		btn = contactsLayout.findViewById(R.id.btn_search);
		ed = contactsLayout.findViewById(R.id.ed_search);
		listView1 = (ListView) contactsLayout.findViewById(R.id.add_user_list);
		listView1.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView1.setStackFromBottom(true);

		listView2 = (ListView) contactsLayout.findViewById(R.id.friendlist);
		listView2.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView2.setStackFromBottom(true);


		getList1();
		myBaseAdapter1 = new MyBaseAdapter1();
		listView1.setAdapter(myBaseAdapter1);

		getList2();
		myBaseAdapter2 = new MyBaseAdapter2();
		listView2.setAdapter(myBaseAdapter2);
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle=new Bundle();
				Result r=resultinfo2.get(position);
				String r1=r.getUser();
				SQLiteDatabase db=myHelper.getReadableDatabase();
				if(tabbleIsExist(myHelper,r1) == false)
				{
					db.execSQL("create table " + r1 + " (_id integer primary " + " key autoincrement,user text,word text)");
				}
				bundle.putString("FriendName",r1);
				Intent intent=new Intent(getActivity(),Message_Page.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});



		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ed.getText().toString().equals("")){
					Toast.makeText(mContext,"请输入好友名",Toast.LENGTH_SHORT).show();
				}
				new AddPostThread(MainActivity.User_Now,ed.getText().toString(),"REQUEST").start();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
				ed.setText("");
				if(imm != null){
					imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
				}

//				myHelper = new MyHelper(getActivity(),com.example.Login.MainActivity.User_Now+".db");
//				SQLiteDatabase db=myHelper.getReadableDatabase();
//				db.execSQL("create table " + ed.getText().toString() + " (_id integer primary " + " key autoincrement,user text,word text)");
//				db.execSQL("insert into " + ed.getText().toString() + "(user,word) " + " values(?,?)",new Object[]{com.example.Login.MainActivity.User_Now,"你好，我们开始聊天吧！"});
//				db.execSQL("insert into User_" + com.example.Login.MainActivity.User_Now + "(user,word) " + " values(?,?)",new Object[]{ed.getText().toString(),"你好，我们开始聊天吧！"});
//				db.close();
			}
		});
	}

	/**
	 * AddPostThread  发送请求添加好友
	 *
	 *
	 * */
	public class AddPostThread extends Thread {
		public String user_to;
		public String user_from;
		public String word;

		public AddPostThread(String user_from,String user_to,String word) {
			this.user_from = user_from;
			this.user_to = user_to;
			this.word = word;
		}

		public void run() {
			// Sevice传回int
			String responseMsg;
			// 要发送的数据
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_from", user_from));
			params.add(new BasicNameValuePair("user_to", user_to));
			params.add(new BasicNameValuePair("word", word));
			// 发送数据，获取对象
			responseMsg = User_Add.send(params);
			System.out.println(responseMsg);
			Message msg = handler.obtainMessage();
			msg.what = 333;
			if(responseMsg.equals("未注册"))
				msg.obj = "用户未注册，请重新输入";
			if(responseMsg.equals("已注册"))
				msg.obj = "等待好友确认";
			if(responseMsg.equals("是好友"))
				msg.obj = "你们已经是好友，无需重复添加";
			if(responseMsg.equals("responseMsg"))
				msg.obj = "responseMsg";
			handler.sendMessage(msg);

		}
	}







	private class MyBaseAdapter1 extends BaseAdapter {

		@Override
		public int getCount() {
			return resultinfo1.size();
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
			TextView tv;
			Button btn1,btn2;
			final Result result=resultinfo1.get(position);
			view = View.inflate(getActivity(),R.layout.add_user,null);
			tv=view.findViewById(R.id.add_tv);
			btn1 = view.findViewById(R.id.add_no);
			btn2 = view.findViewById(R.id.add_yes);
			tv.setText(result.getUser());
			btn1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						new AddPostThread(MainActivity.User_Now,result.getUser(),"NO").start();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
			btn2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						new AddPostThread(MainActivity.User_Now,result.getUser(),"YES").start();
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
			return view;
		}






	}




	private class MyBaseAdapter2 extends BaseAdapter {

		@Override
		public int getCount() {
			return resultinfo2.size();
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
			TextView tv;
			final Result result=resultinfo2.get(position);
			view = View.inflate(getActivity(),R.layout.friend_list,null);
			tv=view.findViewById(R.id.friend_name);
			tv.setText(result.getUser());
			return view;
		}






	}


	private void getList1() {
		SQLiteDatabase db=myHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select * from User_AdD",null);
		while(cursor.moveToNext())
		{
			Result result=new Result();
			result.setid(cursor.getInt(cursor.getColumnIndex("_id")));
			result.setUser(cursor.getString(cursor.getColumnIndex("user")));
			result.setWord("");
			resultinfo1.add(result);
			System.out.println(result.toString());
		}
		cursor.close();
		db.close();
	}


	private void getList2() {
		SQLiteDatabase db=myHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select * from " + MainActivity.User_Now + "_friend",null);
		while(cursor.moveToNext())
		{
			Result result=new Result();
			result.setid(cursor.getInt(cursor.getColumnIndex("_id")));
			result.setUser(cursor.getString(cursor.getColumnIndex("user")));
			resultinfo2.add(result);
		}
		cursor.close();
		db.close();
	}



	public void Reset()
	{
		resultinfo1.clear();//清空List数组，重新扫描数据库内容存入
		getList1();
		myBaseAdapter1.notifyDataSetChanged();//Listview局部刷新，避免页面元素每次刷新都从第一条开始显示

		resultinfo2.clear();//清空List数组，重新扫描数据库内容存入
		getList2();
		myBaseAdapter2.notifyDataSetChanged();//Listview局部刷新，避免页面元素每次刷新都从第一条开始显示
	}




	/**
	 * 注册广播接收者
	 */
	private void doRegisterReceiver() {
		mReceiver=new ContentReceiver();
		IntentFilter filter = new IntentFilter(
				"com.example.Service.content");
		getActivity().registerReceiver(mReceiver, filter);
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
			System.out.println("ContactsFragment __name -->" + name);
			if(name.equals("friend"))
				Reset();
		}
	}






	public class MyServiceConn implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((PostService.LocalBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			 service = null;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unbindService(conn);
		if (mReceiver!=null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}







}
