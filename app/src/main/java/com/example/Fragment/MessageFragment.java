package com.example.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.Message.Message_Page;
import com.example.SQLite.MyHelper;
import com.example.final_chat.R;
import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

	View messageLayout;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		messageLayout = inflater.inflate(R.layout.message_layout,
				container, false);
		return messageLayout;
	}


	MyHelper myHelper;
	List<Result> resultinfo = new ArrayList<>();
	private ListView listView;
	MyBaseAdapter myBaseAdapter;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}


	/**
	 * 用于绑定服务
	 * @mReceiver 服务
	 */
	private ContentReceiver mReceiver;


	public void init()
	{

		//启动服务
		doRegisterReceiver();

		listView = (ListView) messageLayout.findViewById(R.id.fragment_message_lv);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView.setStackFromBottom(true);
		myHelper = new MyHelper(getActivity(),com.example.Login.MainActivity.User_Now+".db");
		getList();
		myBaseAdapter = new MyBaseAdapter();
		listView.setAdapter(myBaseAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle=new Bundle();
				Result r=resultinfo.get(position);
				String r1=r.getUser();
				bundle.putString("FriendName",r1);
				Intent intent=new Intent(getActivity(),Message_Page.class);
				intent.putExtras(bundle);
				startActivity(intent);
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
			TextView tv2;
			Result result=resultinfo.get(position);
			view = View.inflate(getActivity(),R.layout.left_item,null);
			tv1=(TextView)view.findViewById(R.id.tvname);
			tv2=(TextView)view.findViewById(R.id.tv_leftname);
			tv1.setText(result.getWord());
			tv2.setText(result.getUser());
			return view;
		}






	}


	private void getList() {
		SQLiteDatabase db=myHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select * from User_" + com.example.Login.MainActivity.User_Now,null);
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





	@Override
	public void onResume()
	{
		super.onResume();
		System.out.println("MessageFragment coming onResume()");
		Reset();
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
			if(name.equals("canel") == false)
				Reset();
			else
				getActivity().unregisterReceiver(mReceiver);
		}
	}










}
