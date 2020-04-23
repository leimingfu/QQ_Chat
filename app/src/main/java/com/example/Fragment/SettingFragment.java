package com.example.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.Login.MainActivity;
import com.example.Service.PostService;
import com.example.final_chat.R;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends Fragment {


	View settingLayout;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		settingLayout = inflater.inflate(R.layout.setting_layout,
				container, false);
		return settingLayout;
	}


	public TextView tv;
	public Button btn;
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private void init() {

		//启动服务
		conn = new MyServiceConn();
		getActivity().bindService(new Intent(getActivity(), PostService.class), conn,
				BIND_AUTO_CREATE);
		doRegisterReceiver();


		tv = settingLayout.findViewById(R.id.setting_user);
		btn = settingLayout.findViewById(R.id.setting_exit);
		tv.setText(MainActivity.User_Now);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					FileOutputStream fos = getActivity().openFileOutput("data.txt", MODE_PRIVATE);
					String string = "";
					fos.write(string.getBytes());
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Intent intent = new Intent(getActivity(),com.example.Login.MainActivity.class);
				startActivity(intent);
				service.Asya();


			}
		});


	}

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
