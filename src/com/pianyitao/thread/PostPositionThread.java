package com.pianyitao.thread;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.pianyitao.common.Common;

import android.os.Handler;
import android.os.Message;
public class PostPositionThread extends Thread {

	private Handler handler;
	private String phoneNumber;
	private String mac;
	private String longitude;
	private String latitude;

	public PostPositionThread(Handler handler,String phoneNumber ,String mac ,String longitude, String latitude){
		this.handler = handler;
		this.phoneNumber = phoneNumber;
		this.mac = mac;
		this.longitude = longitude;
		this.latitude = latitude;
		PostPositionRequest(phoneNumber,mac,longitude,latitude);
	}
	private void PostPositionRequest(String phoneNumber ,String mac ,String longitude, String latitude){
		HttpUtils httpUtils = new HttpUtils(5000);
		RequestParams params = new RequestParams();
		
		params.addBodyParameter("phoneNumber",phoneNumber);
		params.addBodyParameter("mac",mac);
		params.addBodyParameter("longitude",longitude);
		params.addBodyParameter("latitude",latitude);

		String url = Common.postPositionUrl;
		url = url.replace(" ", "%20");
		httpUtils.send(HttpMethod.POST, url, params,new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				//sendMsg(Common.POST_POSITION_FAILED,"服务器连接异常");
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
//				sendMsg(Common.POST_POSITION_SUCCESS,"上传成功");
			}

		});
	}
	

	private void sendMsg(int what ,String obj){
		Message msg  = new Message();
		msg.obj = obj;
		msg.what = what;
		handler.sendMessage(msg);
		
	}

}
