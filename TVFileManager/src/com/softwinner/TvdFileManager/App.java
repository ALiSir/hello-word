package com.softwinner.TvdFileManager;

import java.util.Iterator;
import java.util.List;

//import lib.standardutils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

//import rx.android.plugins.RxBus;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushMessageReceiver;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class App extends Application {
	
	public static final String id = "abcdefghijklmnopqrstuvwxyz_";
	
	public static final String SEND_RECV_AUTH_ACTION = "send_recv_auth_action";
	
	private static App mInstance;
	
	private static String mChannelId = "";
	
	public static class MyPushMessageReceiver extends PushMessageReceiver {

		@Override
		public void onBind(Context context, int errorCode, String appid, String userId, String channelId, String requestId) {
			String responseString = "[onBind] errorCode=" + errorCode + " appid="
	                + appid + " userId=" + userId + " channelId=" + channelId
	                + " requestId=" + requestId;
	        Log.e("TVFM", responseString);
	        //8:4296823368311901421
	        //9:3598170542917421065
	        //10:3946110647180361698
	        if (errorCode == 0) {
	            // 绑定成功
	        	mChannelId = channelId;
	        }
		}

		@Override
		public void onDelTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
			
		}

		@Override
		public void onListTags(Context arg0, int arg1, List<String> arg2, String arg3) {
			
		}

		@Override
		public void onMessage(Context context, String message, String customContentString) {
			String messageString = "message=\"" + message + "\" customContentString=" + customContentString;
	        Log.e("TVFM", messageString);
	        if (!TextUtils.isEmpty(message)) {
	            JSONObject customJson = null;
	            try {
	                customJson = new JSONObject(message);
	                String customContent = null;
	                if (!customJson.isNull("custom_content")) {
	                	customContent = customJson.getString("custom_content");
	                }
	                if(!TextUtils.isEmpty(customContent)) {
	                	customJson = new JSONObject(customContent);
	                }
	                String content1 = null;
	                if (!customJson.isNull("content1")) {
	                    content1 = customJson.getString("content1");
	                }
	                String content2 = null;
	                if(!customJson.isNull("content2")){
	                	content2 = customJson.getString("content2");
	                }
	                Log.i("mydebug", customJson.toString());
	                String username = "";
	                String dir = "";
	                if(content1 != null) {
	                	int pos = content1.lastIndexOf(":");
	                	if(pos != -1) {
	                		username = content1.substring(0, pos);
	                		dir = content1.substring(pos + 1);
	                		Intent intent = new Intent(SEND_RECV_AUTH_ACTION);
	                		intent.putExtra("username", username);
	                		intent.putExtra("dir", dir);
	                		intent.putExtra("content2",content2 );
	                		App.getInstance().sendBroadcast(intent);
	                	}
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	        }
		}

		@Override
		public void onNotificationArrived(Context context, String title, String description, String customContentString) {
		}

		@Override
		public void onNotificationClicked(Context arg0, String arg1, String arg2, String arg3) {
			
		}

		@Override
		public void onSetTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
			
		}

		@Override
		public void onUnbind(Context context, int errorCode, String requestId) {
			String responseString = "onUnbind errorCode = " + errorCode + " requestId = " + requestId;
	        Log.e("TVFM", responseString);
	        if (errorCode == 0) {
	            // 解绑定成功
	        	mChannelId = "";
	        }
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		int pid = android.os.Process.myPid();
		String processAppName = getAppName(pid);
		
		if (processAppName == null ||!processAppName.equalsIgnoreCase("com.softwinner.TvdFileManager")) {
		} else {
			String api_key = this.getMetaValue(getApplicationContext(), "api_key");
			PushManager.startWork(getApplicationContext(),
	                PushConstants.LOGIN_TYPE_API_KEY,
	                api_key);
		}
	}
	
	//测试
	public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            Log.e("StringUtils", "error " + e.getMessage());
        }
        return apiKey;
    }
	
	public static App getInstance() {
		return mInstance;
	}
	
	public String getChannelId() {
		return mChannelId;
	}
	
	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			try {
				if (info.pid == pID) {
					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					// Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
					// info.processName +"  Label: "+c.toString());
					// processName = c.toString();
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
				// Log.d("Process", "Error>> :"+ e.toString());
			}
		}
		return processName;
	}
	
	public boolean isAppOnForeground() {
	    //Returns a list of application processes that are running on the device
	    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	    String packageName = getApplicationContext().getPackageName();
	    Log.e("AppOnForeground", "package Name = " + packageName);
	    
	    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    if (appProcesses == null)
	    	return false;
	    
	    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
	    	// The name of the process that this object is associated with.
	    	Log.e("AppOnForeground", appProcess.processName);
	    	if (appProcess.processName.equals(packageName)
	    			&& appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
	    		return true;
	    	}
	    }
	    return false;
	}
}
