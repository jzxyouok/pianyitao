package com.pianyitao.thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.pianyitao.common.Common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * App 最新版本获取
 * 
 * @author zhaoJunhua
 *
 */
public class GetAppVersionThread extends Thread {

	private Handler handler;

	public GetAppVersionThread(Handler handler) {
		super("GetAppVersionThread");
		this.handler = handler;
	}

	@Override
	public void run() {
		BufferedReader buffer = null;
		// 新建connection对象 默认值为:null
		HttpURLConnection httpUrlconnection = null;

		String httpDownStr = null;
		try {
			// 存储http请求返回的信息 并以String 类型返回
			StringBuffer sb = new StringBuffer();
			// 临时存储信息
			String line = null;
			// 缓存字符流信息,bufferedreader 可以一次读取大量的数据，减少了io次数，提升效率
			String sUrl = Common.getAppVersionUrl;
			// 同过url字符串转化成url对象
			URL url = new URL(sUrl);
			// 根据URL的请求协议生成的URLconnection类
			httpUrlconnection = (HttpURLConnection) url.openConnection();
			// httpUrlconnection.addRequestProperty("user","haiersoft_eshealthcr");
			// 设置链接超时时间为5秒
			httpUrlconnection.setConnectTimeout(5000);
			// 设置读取超时时间为5秒
			httpUrlconnection.setReadTimeout(5000);
			// 允许connection读入 默认值为:true 没特殊要求可以不写
			// httpUrlconnection.setDoInput(true);
			// 判断链接状态 HttpURLConnection.HTTP_OK为链接正常 值为200
			int state = httpUrlconnection.getResponseCode();
			if (state != HttpURLConnection.HTTP_OK) {
				// 链接出错 返回空
//				sendFailMessage(handler, Common.GET_VERSIONSION_FAILED,
//						"链接服务器异常Code=" + state);
				return;
			}
			// 通过getInputStream() 发送请求 把获得的字节流转换成字符流 并存储在appBufferedReader
			buffer = new BufferedReader(new InputStreamReader(
					httpUrlconnection.getInputStream(), "UTF-8"));
			// 从buffer中逐行取出数据 并添加到sb中 直到 buffer.readLine() 为空
			while ((line = buffer.readLine()) != null) {
				// 向sb中追加信息
				sb.append(line);
			}

			if (Common.isEmpty(sb.toString())) {
//				sendFailMessage(handler, Common.GET_VERSIONSION_FAILED,
//						"链接服务器异常");
			} else {
				resolveResult(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
//			sendFailMessage(handler, Common.GET_VERSIONSION_FAILED, "链接服务器异常");
		} finally {
			try {
				if (null != buffer) {
					buffer.close();
				}
				// 关闭流
				if (null != httpUrlconnection) {
					httpUrlconnection.disconnect();
				}
			} catch (Exception e) {
				// 关闭流失败 清空sb中的内容
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解析接口返回的值
	 * 
	 * @param jsonData
	 */
	private void resolveResult(String jsonData) {
		String result = "";
		try {
			JSONObject json = new JSONObject(jsonData);
			// 获取RetureCode的值
			result = Common.getJsonValue(json, "version");
			Message msg = Message.obtain();
			msg.obj = result;

			// 发送查询成功的消息
			msg.what = Common.GET_VERSIONSION_SUCCESS;
			handler.sendMessage(msg);
			return;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendFailMessage(handler, Common.GET_VERSIONSION_FAILED, result);
	}

	/**
	 * 发送失败的消息
	 * 
	 * @param handler
	 * @param what
	 * @param result
	 */
	private void sendFailMessage(Handler handler, int what, String result) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = (Common.isEmpty(result) ? "服务器返回信息不合法" : result);
//		handler.sendMessage(msg);
	}
}
