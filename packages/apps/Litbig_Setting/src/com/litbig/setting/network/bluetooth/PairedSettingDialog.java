package com.litbig.setting.network.bluetooth;

import com.litbig.app.setting.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class PairedSettingDialog extends Dialog {
	private final String TAG = "PairedSettingDialog";

	private Button mBtnChange;
	private Button mBtnDisconnect;
	private Button mBtnCancel;

	private BluetoothListItem mBluetoothItem;

	private EditText mEtName;

	private Context mContext;
	private Handler mHandler;

	private InputMethodManager mInputMethodManager;
	
	protected PairedSettingDialog(Context context, BluetoothListItem item, Handler handler, InputMethodManager inputmethodmanager) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mHandler = handler;
		mBluetoothItem = item;
		mContext = context;
		mInputMethodManager = inputmethodmanager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);

		setContentView(R.layout.bluetooth_paired_setting_dialog);

		mBtnChange = findViewById(R.id.btn_name_change);
		mBtnDisconnect = findViewById(R.id.btn_disconnect);
		mBtnCancel = findViewById(R.id.btn_cancel);

		mBtnChange.setOnClickListener(mBtnClickListener);
		mBtnDisconnect.setOnClickListener(mBtnClickListener);
		mBtnCancel.setOnClickListener(mBtnClickListener);

		mEtName = findViewById(R.id.name);
		mEtName.setText(mBluetoothItem.getName());
		mEtName.setSelection(mEtName.getText().length());

		mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if (view == mBtnChange) {
				String str = mEtName.getText().toString();
				if(!str.equals("") && !str.equals(mBluetoothItem.getName())){
					mHandler.obtainMessage(BluetoothLayout.MESSAGE_DEVICE_NAME_CHANGE, str).sendToTarget();
					cancel();
				}
			} else if (view == mBtnDisconnect) {
				mHandler.obtainMessage(BluetoothLayout.MESSAGE_PAIRING_DISCONNECT).sendToTarget();
				cancel();
			} else if (view == mBtnCancel) {
				cancel();
			}
		}
	};
}
