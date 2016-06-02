package com.pianyitao.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.pianyitao.common.Common;
import com.pianyitao.thread.PostPositionThread;

public class AppService extends Service {

	private LocationClient mLocationClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	private String deviceid;
	private String te1;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 获取手机号码
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		deviceid = tm.getDeviceId();// 获取智能设备唯一编号
		te1 = tm.getLine1Number();// 获取本机号码
		onfresh();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Common.POST_POSITION_SUCCESS:

				break;
			case Common.POST_POSITION_FAILED:
				break;
			}
		}

	};

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		stopListener();
		super.onDestroy();
	}

	public void onfresh() {
		// Common.getLoacation(getActivity());
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开GPS
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(300000);// 设置发起定位请求的间隔时间为3000ms
		option.disableCache(false);// 禁止启用缓存定位
		option.setPriority(LocationClientOption.NetWorkFirst);// 网络定位优先
		mLocationClient.setLocOption(option);// 使用设置
		mLocationClient.start();// 开启定位SDK
		mLocationClient.requestLocation();// 开始请求位置
	}

	/**
	 * 停止，减少资源消耗
	 */
	public void stopListener() {
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.stop();// 关闭定位SDK
			mLocationClient = null;
		}
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location != null) {
				// // 获得经纬度
				new PostPositionThread(handler, te1, deviceid,
						location.getLongitude() + "", location.getLatitude()
								+ "").start();
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {

		}

	}
}
