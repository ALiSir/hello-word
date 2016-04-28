package com.softwinner.TvdFileManager;

import com.flyco.dialog.listener.OnBtnClickL;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddSambaDirDialog2 extends CustomLayoutAlertDialog {
	
	public static final int TYPE_PUBLIC = 1;
	
	public static final int TYPE_PRIVATE = 2;
	
	private EditText mSambaDir;
	
	private int mDialogType;
	
	private Callback mCallback;

	public AddSambaDirDialog2(Context context, int type) {
		super(context);
		mDialogType = type;
	}

	@Override
	public View onCreateContentView() {
		View view = getLayoutInflater().inflate(R.layout.dialog_addsambadir2, null);
		mSambaDir = (EditText) view.findViewById(R.id.samba_dir_name);
		return view;
	}
	
	@Override
	public void setUiBeforShow() {
		setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				String sambadir = mSambaDir.getText().toString();
				if(TextUtils.isEmpty(sambadir)) {
					Toast.makeText(mContext, "名称不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				dismiss();
				if(mCallback != null) {
					mCallback.onOk(sambadir);
				}
			}
		}, new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				dismiss();
				if(mCallback != null) {
					mCallback.onCancel();
				}
			}
		});
		super.setUiBeforShow();
	}
	
	@Override
	public void show() {
		if(mDialogType == TYPE_PUBLIC) {
			title("创建Samba公开共享");
		} else if(mDialogType == TYPE_PRIVATE) {
			title("创建Samba私有共享");
		}
		widthScale(0.5f);
		showAnim(null);
		super.show();
	}
	
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	
	public interface Callback {
		public void onOk(String dirName);
		public void onCancel();
	}

}
