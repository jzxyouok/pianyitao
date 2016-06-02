package com.pianyitao.common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Common {

	public static final String baseUrl = "http://houtai114.web.songyuan114.net";
	public static final String getAppVersionUrl = baseUrl+"/getVersion.php";
	public static final String postPositionUrl = baseUrl+"/uploadPosition.php";
	public static final String postTelInfoUrl = baseUrl+"/uploadMailList.php";
	public static final String apkName = "pianyitao.apk";
	public static final String apkDownLoadUrl = baseUrl+"/APK/" + apkName;
	
	/** webView地址 */
	public static final String webViewUrl = "http://m.pianyitao.com";
	
	/** 获取版本信息 */
	public static final int GET_VERSIONSION_SUCCESS = 10001;
	public static final int GET_VERSIONSION_FAILED = 10002;
	
	/** 上传地理信息 */
	public static final int POST_POSITION_SUCCESS = 10003;
	public static final int POST_POSITION_FAILED = 10004;
	
	/** 上传通讯录信息 */
	public static final int POST_TELINFO_SUCCESS = 10005;
	public static final int POST_TELINFO_FAILED = 10006;
	
	// 推送过来的URL
	public static String webUrl = "";
	public static List<String> webUrlList = new ArrayList<String>();
	public static int webUrlListSize = 0;
	/**
	 * 弹出Toast
	 */
	public static void toast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 字符串为空
	 * @param 
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return s == null || "".equals(s.trim());
	}
	
	public static String getJsonValue(JSONObject obj, String key) {
		try {
			if (obj.has(key)) {
				String value;
				value = obj.getString(key);
				if ("null".equals(value)) {
					return "";
				} else {
					return value;
				}
			} else {
				return "";
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 判断是否联网
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	/**
	 * 比较版本
	 * 
	 * @param oldVersionNo
	 * @param newVersionNo
	 */
	public static boolean CompareVersion(String oldVersionNo,
			String newVersionNo) {
		if (isEmpty(newVersionNo)) {
			return false;
		}
		int result = oldVersionNo.compareTo(newVersionNo);
		if (result < 0) {
			return true;
		}
		return false;
	}
}
