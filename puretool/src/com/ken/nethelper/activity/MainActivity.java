package com.ken.nethelper.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ken.nethelper.R;
import com.ken.nethelper.bean.SMS;
import com.ken.nethelper.constant.C;
import com.ken.nethelper.util.RouterManager;
import com.ken.nethelper.util.SMSManager;
import com.ken.nethelper.util.SanXunTools;
import com.ken.nethelper.util.WifiAdmin;

public class MainActivity extends Activity implements OnClickListener {

	public static final String TAG = "MainActivity";
	
	//线程消息标识
	private final int MSG_OPENWIFI = 0011;
	private final int MSG_SCANWIFI = 0022;
	private final int MSG_CLOSEWIFI = 0033;

	private Context context = this;
	private MyReceiver myReceiver;

	private Button btnSave;
	private EditText edtShi, edtGe , edtTUser , edtTPwd , edtAdd , edtWUser , edtWPwd;
	private TextView txtLog;
	private WifiAdmin wifiAdmin;
	
	//闪讯密码
	private String sxPwd = null;
	String lastTime ,hour,min,tadd,tuser,tpwd,wuser,wpwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		initData();
	}
	
	/**
	 * 初始化界面
	 */
	private void initViews() {
		btnSave = (Button) findViewById(R.id.btn_save);
		edtShi = (EditText) findViewById(R.id.edt_shi);
		edtGe = (EditText) findViewById(R.id.edt_ge);
		txtLog = (TextView) findViewById(R.id.txt_log);
		edtAdd = (EditText)findViewById(R.id.edtAdd);
		edtTUser = (EditText) findViewById(R.id.edtUser);
		edtTPwd = (EditText) findViewById(R.id.edtPwd);
		edtWUser = (EditText) findViewById(R.id.edtWifiUser);
		edtWPwd = (EditText) findViewById(R.id.edtWifiPwd);

		btnSave.setOnClickListener(this);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		
		//注册广播
		registetRecriver();
		refreshValue();
		
		wifiAdmin = new WifiAdmin(context);
		
		txtLog.setText(C.UI_INFO.LOG_TITLE+ lastTime);
		edtShi.setText(hour);
		edtGe.setText(min);
		edtAdd.setText(tadd);
		edtTUser.setText(tuser);
		edtTPwd.setText(tpwd);
		edtWUser.setText(wuser);
		edtWPwd.setText(wpwd);

	}
	
	
	public void refreshValue()
	{
		SharedPreferences preferences = getSharedPreferences(C.UI_INFO.LOG_DISK,
				Context.MODE_PRIVATE);
		 lastTime = preferences.getString(C.UI_INFO.LOG_LASTTIME, C.UI_INFO.LOG_EMPTY);
		 hour = preferences.getString(C.UI_INFO.LOG_HOUR, C.UI_INFO.LOG_TIMEINTIT);
		 min = preferences.getString(C.UI_INFO.LOG_MIN, C.UI_INFO.LOG_TIMEINTIT);
		 tadd = preferences.getString(C.UI_INFO.LOG_ADD, C.UI_INFO.LOG_EMPTY);
		 tuser = preferences.getString(C.UI_INFO.LOG_ROUTERUSER, C.UI_INFO.LOG_EMPTY);
		 tpwd = preferences.getString(C.UI_INFO.LOG_ROUTERPWD, C.UI_INFO.LOG_EMPTY);
		 wuser = preferences.getString(C.UI_INFO.LOG_WIFIUSER, C.UI_INFO.LOG_EMPTY);
		 wpwd = preferences.getString(C.UI_INFO.LOG_WIFIPWD, C.UI_INFO.LOG_EMPTY);
		
		C.OPENWRT.OPENWRT_ADD = tadd;
		C.OPENWRT.USER_NAME = tuser;
		C.OPENWRT.USER_PWD = tpwd;
		C.OPENWRT.WIFI_SSID = wuser;
		C.OPENWRT.WIFI_PWD = wpwd;
	}
	@Override
	protected void onResume() {
		// TODO 自动生成的方法存根
		super.onResume();
		refreshValue();
	}

	/**
	 * 注册广播
	 */
	private void registetRecriver() {
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();

		// 短信到来的广播
		filter.addAction(C.SMS_INFO.SMS_RECEIVER);

		// 闹钟
		filter.addAction(SMSManager.ALARM_ACTION);

		// 注册广播
		registerReceiver(myReceiver, filter);
	}

	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自动生成的方法存根

			// 获取时间
			SimpleDateFormat formatter = new SimpleDateFormat(
					C.UI_INFO.LOG_TIME_FORMAT, Locale.CHINA);
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String time = formatter.format(curDate);
			
			// 接收到短信
			if (intent.getAction().equals(C.SMS_INFO.SMS_RECEIVER)) 
			{
				if (intent.getExtras() != null) 
				{
					List<SMS> list = new ArrayList<SMS>();
					SMSManager manager = new SMSManager();
					list = manager.digestMsg(intent.getExtras());
					for (SMS sms : list) 
					{
						Log.i(TAG,sms.getTarget() + ":" + sms.getContent() + ":" + sms.getTime());
						// 是闪讯
						if (sms.getTarget().equals(C.SMS_INFO.SMS_SX_REC)) 
						{
							 sxPwd = SanXunTools.getPWD(sms.getContent());
							// 先打开wifi
							new Thread(thread_open).start();
						}

					}

				}
			}
			
			//闹钟
			else if(intent.getAction().equals(SMSManager.ALARM_ACTION))
			{
				SMS sms = new SMS(C.SMS_INFO.SMS_SX_ADD,
						C.SMS_INFO.SMS_SX_CONTENT, null);
				SMSManager manager = new SMSManager();
				manager.sendMsg(sms);
				
				Log.i("sms" ,  "sms send compeleted!");
				
				return;
			}
			

			// 显示在log
			SharedPreferences preferences = getSharedPreferences(C.UI_INFO.LOG_DISK,
					Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			String lastTime = preferences.getString(C.UI_INFO.LOG_LASTTIME, C.UI_INFO.LOG_EMPTY);
			lastTime = time;
			editor.putString(C.UI_INFO.LOG_LASTTIME, lastTime);
			editor.commit();

			txtLog.setText(C.UI_INFO.LOG_TITLE + time);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		super.onDestroy();
		unregisterReceiver(myReceiver);
	}

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		if (v.getId() == R.id.btn_save) {
			int hour = Integer.parseInt(edtShi.getText().toString());
			int min = Integer.parseInt(edtGe.getText().toString());
			SharedPreferences preferences = getSharedPreferences(C.UI_INFO.LOG_DISK,
					Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(C.UI_INFO.LOG_HOUR, edtShi.getText().toString());
			editor.putString(C.UI_INFO.LOG_MIN, edtGe.getText().toString());
			editor.putString(C.UI_INFO.LOG_ADD, edtAdd.getText().toString());
			editor.putString(C.UI_INFO.LOG_ROUTERUSER, edtTUser.getText().toString());
			editor.putString(C.UI_INFO.LOG_ROUTERPWD, edtTPwd.getText().toString());
			editor.putString(C.UI_INFO.LOG_WIFIUSER, edtWUser.getText().toString());
			editor.putString(C.UI_INFO.LOG_WIFIPWD, edtWPwd.getText().toString());

			editor.commit();
			refreshValue();

			Toast.makeText(context, C.UI_INFO.LOG_SAVECOMLETE, Toast.LENGTH_SHORT).show();
			SMSManager manager = new SMSManager();
			manager.setTask(hour, min, true, context);
		}
	}

	// 打开wifi的操作
	private Thread thread_open = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO 自动生成的方法存根
			Message m = handler.obtainMessage();
			wifiAdmin.openWifi();

			// 停留3秒
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			m.what = MSG_OPENWIFI;
			handler.sendMessage(m);
		}
	});

	// 扫描wifi的操作
	private Thread thread_scan = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO 自动生成的方法存根
			Message m = handler.obtainMessage();
			m.what = MSG_SCANWIFI;
			wifiAdmin.startScan();
			handler.sendMessage(m);
		}
	});

	// 连接wifi并发送密码
	private Thread thread_send = new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO 自动生成的方法存根
			
			int count = 0 ;
			while (!RouterManager.setPWD(sxPwd)) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				count ++ ;
				
				//尝试50次仍然失败则退出
				if(count > 50 ) break;
			}
			Message m = handler.obtainMessage();
			m.what = MSG_CLOSEWIFI;
			handler.sendMessage(m);

		}
	});
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			switch (msg.what) {
			
			//打开wifi
			case MSG_OPENWIFI:
				
				new Thread(thread_scan).start();
				break;
				
			//扫描wifi
			case MSG_SCANWIFI:

				if (wifiAdmin.isWifiOk() && (wifiAdmin.getWifiList() != null)) {
					wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(
							C.OPENWRT.WIFI_SSID, C.OPENWRT.WIFI_PWD, 3));
					new Thread(thread_send).start();

				} else {
					new Thread(thread_open).start();// 一次获取不一定成功，循环获取
				}
			
				break;
				
			//关闭wifi
			case MSG_CLOSEWIFI:
					wifiAdmin.closeWifi();
				break;
			default:
				break;
			}
			
		}

	};
}
