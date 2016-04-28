package com.baidu.push.example;

import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.baidu.android.pushservice.PushMessageReceiver;

public class PushTestReceiver extends PushMessageReceiver {

	@Override
	public void onBind(Context arg0, int arg1, String arg2, String arg3,
			String arg4, String arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDelTags(Context arg0, int arg1, List<String> arg2,
			List<String> arg3, String arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onListTags(Context arg0, int arg1, List<String> arg2,
			String arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(Context arg0, String arg1, String arg2) {
		Toast.makeText(arg0, "参数1："+arg1 +";参数2:"+arg2, Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void onNotificationArrived(Context arg0, String arg1, String arg2,
			String arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotificationClicked(Context arg0, String arg1, String arg2,
			String arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetTags(Context arg0, int arg1, List<String> arg2,
			List<String> arg3, String arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnbind(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

}
