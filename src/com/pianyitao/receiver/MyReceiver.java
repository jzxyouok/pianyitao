package com.pianyitao.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import com.pianyitao.activity.MainActivity;
import com.pianyitao.common.Common;

import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		JSONObject dataJson = null;
		String webUrlString = null;
		Bundle bundle = intent.getExtras();
        
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        
        if(null != extras && !extras.isEmpty()){
        	try {
				dataJson = new JSONObject(extras);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
        	
        	if(null != dataJson && dataJson.length() > 0){
        		try {
        			Object webUrlObject = dataJson.get("webUrl");
        			
        	        if(null != webUrlObject && !"".equals(webUrlObject)){
        	        	webUrlString = webUrlObject.toString();
        	        }
        			
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        	
        }
        
        if(null == webUrlString || "".equals(webUrlString)){
        	webUrlString = Common.webViewUrl;
        }
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			System.out.println("收到了自定义消息。消息内容是："
					+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			// 自定义消息不会展示在通知栏，完全要开发者写代码去处理
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			System.out.println("收到了通知");
			// 在这里可以做些统计，或者做些其他工作
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {
			System.out.println("用户点击打开了通知");
			Log.i("yyy", "url:"+webUrlString);
			// 在这里可以自己写代码去定义用户点击后的行为
			Intent i = new Intent(context, MainActivity.class); // 自定义打开的界面
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setAction(Intent.ACTION_MAIN); // 设置跳转到当前页
			i.addCategory(Intent.CATEGORY_LAUNCHER); // 设置跳转到当前页
			i.putExtra("webUrlString", webUrlString);
			Common.webUrl = webUrlString;
			context.startActivity(i);
		}
	}

}
