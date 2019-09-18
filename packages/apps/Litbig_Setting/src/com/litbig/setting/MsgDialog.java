package com.litbig.setting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.litbig.app.setting.R;

public class MsgDialog extends Dialog{

	public static final int MSG_EVENT_OK = 1;
	public static final int MSG_EVENT_CANCEL = 2;
	
	private Handler mHandler;
	
	private Context mContext;
	
	private TextView mTvTitle, mTvContent;
	private Button mBtnOk, mBtnCancel;
	
	private String mTitle, mMsg;
	
	public MsgDialog(Context context, String title, String msg, Handler handler) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		
		mContext = context;
		mTitle = title;
		mMsg = msg;
		mHandler = handler;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.msg_dialog);
		
		mTvTitle = findViewById(R.id.title);
		mTvTitle.setText(mTitle);
		mTvContent = findViewById(R.id.content);
		mTvContent.setText(mMsg);
		
		mBtnOk = findViewById(R.id.btn_ok);
		mBtnCancel = findViewById(R.id.btn_cancel);
		
		BtnClickListener btnClickListener = new BtnClickListener();
		mBtnOk.setOnClickListener(btnClickListener);
		mBtnCancel.setOnClickListener(btnClickListener);
	}
	
	private class BtnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			
			if (view == mBtnOk) {
				mHandler.sendEmptyMessage(MSG_EVENT_OK);
			}
			
			else if (view == mBtnCancel) {
				mHandler.sendEmptyMessage(MSG_EVENT_CANCEL);
				cancel();
			}
		}
		
	}
}
