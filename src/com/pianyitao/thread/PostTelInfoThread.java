package com.pianyitao.thread;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.pianyitao.common.Common;

public class PostTelInfoThread extends Thread {

	private Handler handler;
	private Map<String,String> map;

	public PostTelInfoThread(Handler handler,Map<String,String> map){
		this.handler = handler;
		this.map = map;
	}
	
	
	@Override
	public void run() {
		try {
			HttpUtils httpUtils = new HttpUtils(5000);
			RequestParams params = new RequestParams();
			
			params.addBodyParameter("mailListData",(new JSONObject(map)).toString());

			String url = Common.postTelInfoUrl;
			url = url.replace(" ", "%20");
			httpUtils.send(HttpMethod.POST, url, params,new RequestCallBack<String>() {
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					//sendMsg(Common.POST_POSITION_FAILED,"服务器连接异常");
				}

				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
//					sendMsg(Common.POST_POSITION_SUCCESS,"上传成功");
				}

			});
			
			
//			HttpClient httpclient = new DefaultHttpClient();
//			String uri = Common.postTelInfoUrl;
//			HttpPost httppost = new HttpPost(uri);
//			// 添加http头信息 Content-Type: application/json; charset=utf-8
//
//			// http post的json数据格式： {"name": "your name","parentId":
//			// "id_of_parent"}
//			JSONObject obj = new JSONObject();
//
//			obj.put("mailListJsonData", new JSONObject(map));
//			httppost.setEntity(new StringEntity(obj.toString()));
//			HttpResponse response;
//			response = httpclient.execute(httppost);
//			// 检验状态码，如果成功接收数据
//			int code = response.getStatusLine().getStatusCode();
//			if (code == 200) {
//				//sendMsg(Common.POST_TELINFO_SUCCESS, "上传成功");
//			}
		} catch (Exception e) {
			e.printStackTrace();
			//sendMsg(Common.POST_TELINFO_FAILED, "连接服务器异常");
		}

	}
	

	private void sendMsg(int what ,Object obj){
		Message msg  = new Message();
		msg.obj = obj;
		msg.what = what;
		handler.sendMessage(msg);
		
	}

}
