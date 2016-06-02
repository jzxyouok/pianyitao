package com.pianyitao.common;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;

public class MyAppliacation extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		JPushInterface.setDebugMode(true);
		// 初始化 极光推送
		JPushInterface.init(getApplicationContext());
	}

}
