package com.softwinner.TvdFileManager;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddSambaDirDialog {
	
	public static final int TYPE_PUBLIC = 1;
	
	public static final int TYPE_PRIVATE = 2;
	
	private Context mContext;
	
	private EditText mSambaDir;
	
	private TextView mSambaAddTitle;
	
	private Dialog mDialog;
	
	private LinearLayout mOkBtn;
	
	private LinearLayout mCancelBtn;
	
	private ImageView mCloseBtn;
	
	private Callback mCallback;
	
	public AddSambaDirDialog(Context context, int type) {
		mContext = context;
		mDialog = new Dialog(context, R.style.custom_dialog);
		mDialog.setContentView(R.layout.dialog_addsambadir);
		
		mSambaAddTitle = (TextView) mDialog.findViewById(R.id.samba_add_title);
		if(type == TYPE_PUBLIC) {
			mSambaAddTitle.setText("创建Samba公开共享");
		} else if(type == TYPE_PRIVATE) {
			mSambaAddTitle.setText("创建Samba私有共享");
		}
		
		mSambaDir = (EditText) mDialog.findViewById(R.id.samba_dir_name);
		mOkBtn = (LinearLayout) mDialog.findViewById(R.id.btn_ok);
		mCancelBtn = (LinearLayout) mDialog.findViewById(R.id.btn_cancel);
		mCloseBtn = (ImageView) mDialog.findViewById(R.id.btn_close);
		
		mOkBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String sambadir = mSambaDir.getText().toString();
				
				if(TextUtils.isEmpty(sambadir)) {
					Toast.makeText(mContext, "名称不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				
				mDialog.dismiss();
				
				if(mCallback != null) {
					mCallback.onOk(sambadir);
				}
			}
		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				if(mCallback != null) {
					mCallback.onCancel();
				}
			}
		});
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				if(mCallback != null) {
					mCallback.onCancel();
				}
			}
		});
	}
	
	public void show() {
		mDialog.show();
	}
	
	public void dismiss() {
		mDialog.dismiss();
	}
	
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	
	public interface Callback {
		public void onOk(String dirName);
		public void onCancel();
	}
}
