package com.pianyitao.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

/**
 * APK更新管理类
 * 
 * @author Royal
 * 
 */
public class VersionUpdateManager {

	// 上下文对象
	private Context mContext;
	// 是否终止下载
	private boolean isInterceptDownload = false;
	// 进度条显示数值
	private int progress = 0;
	String url;
	ProgressDialog pd;

	/**
	 * 参数为Context(上下文activity)的构造函数
	 * 
	 * @param context
	 */
	public VersionUpdateManager(Context context,
			String url) {
		this.mContext = context;
		this.url = url;
	}

	/**
	 * 弹出下载框
	 */
	public void showDownloadDialog() {
		pd = new ProgressDialog(mContext);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载，请耐心等待....");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		// 开启另一线程下载
		Thread downLoadThread = new Thread(downApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 从服务器下载新版apk的线程
	 */
	private Runnable downApkRunnable = new Runnable() {
		@Override
		public void run() {
			if (!android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				// 如果没有SD卡
				Builder builder = new Builder(mContext);
				builder.setTitle("提示");
				builder.setMessage("当前设备无SD卡，数据无法下载");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				return;
			} else {
				try {
					File file = getFileFromServer(url, pd);

					installApk(file);
					pd.dismiss(); // 结束掉进度条对话框
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	};

	/**
	 * 从服务器下载apk:
	 * 
	 * @param path
	 * @param pd
	 * @return
	 * @throws Exception
	 */
	public File getFileFromServer(String path, ProgressDialog pd)
			throws Exception {
		// 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			// 获取到文件的大小
			int length = conn.getContentLength();
			InputStream is = conn.getInputStream();
			File file = new File(Environment.getExternalStorageDirectory(),
					Common.apkName);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len;
			int total = 0;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				// 获取当前下载量
				progress = (int) (((float) total / length) * 100);
				handler.sendEmptyMessage(1);
				if (len <= 0) {
					// 下载完成通知安装
					Message msg = Message.obtain();
					msg.what = 0;
					msg.obj = file;
					handler.sendMessage(msg);
					break;
				}
			}
			fos.close();
			bis.close();
			is.close();
			return file;
		}
		return null;
	}

	/**
	 * 声明一个handler来跟进进度条
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				pd.setProgress(progress);
				break;
			case 0:
				pd.dismiss();
				// 安装apk文件
				installApk((File) msg.obj);
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 安装apk
	 */
	private void installApk(File file) {

		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");// 编者按：此处Android应为android，否则造成安装不了
		mContext.startActivity(intent);
	}
}
