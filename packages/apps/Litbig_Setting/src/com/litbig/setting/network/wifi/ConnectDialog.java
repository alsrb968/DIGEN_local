package com.litbig.setting.network.wifi;

import com.litbig.app.setting.R;
import com.litbig.setting.network.wifi.WifiListItem.PskType;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectDialog extends Dialog{
	private final String TAG = "Wi-Fi ConnectDialog";
	
	private Button mBtnConnect, mBtnCancel;
	
	private WifiManager mWifiManager;
	private WifiListItem mWifiItem;
	
	private TextView mTvSsid;
	private EditText mEtPassword;
	
	private CheckBox mCbNotiPassword, mCbAutoConnect;
	
	private WifiManager.ActionListener mConnectListener;
	
	private InputMethodManager mInputMethodManager;

	private Context mContext;
	
	private Toast mToast;
	
	protected ConnectDialog(Context context, WifiListItem item, WifiManager manager, WifiManager.ActionListener listener
			,InputMethodManager inputmethodmanager) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		
		mWifiItem = item;
		mWifiManager = manager;
		mContext = context;
		mConnectListener = listener;
		mInputMethodManager = inputmethodmanager;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.wifi_connect_dialog);
		
		mBtnConnect = findViewById(R.id.btn_connect);
		mBtnCancel = findViewById(R.id.btn_cancel);

		mBtnConnect.setOnClickListener(mBtnClickListener);
		mBtnCancel.setOnClickListener(mBtnClickListener);
		
		mTvSsid = findViewById(R.id.ssid);
		mTvSsid.setText(mWifiItem.ssid);
		
		mEtPassword = findViewById(R.id.password);
		mEtPassword.setOnFocusChangeListener(mFocusChangeListener);
		mEtPassword.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				String editText = mEtPassword.getText().toString();
				String sourceText = source.toString();
				int editLength = editText.length();
				int sourceLength = sourceText.length(); 
				if ((editLength + sourceLength) > 63 && !editText.equals(sourceText)) {
					if(mToast == null) 
						mToast = Toast.makeText(mContext, R.string.wifi_toast_lack_password_length, Toast.LENGTH_SHORT);
					if(!mToast.getView().isShown()) mToast.show();
					
					if (sourceLength > 1) {
						return sourceText.substring(0, 63 - editLength);
					} else {
						return "";
					}
				}
				return null;
			}
		}});
		
		mCbNotiPassword = findViewById(R.id.cbox_noti_password);
		mCbAutoConnect = findViewById(R.id.cbox_auto_connect);

		mCbAutoConnect.setChecked(true);
		mCbAutoConnect.setEnabled(false);
		mCbAutoConnect.setAlpha(0.4f);
		
		mCbNotiPassword.setOnCheckedChangeListener(mCheckedChacgedListener);
		mCbAutoConnect.setOnCheckedChangeListener(mCheckedChacgedListener);

	}

	private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View view, boolean isFocused) {
			if (isFocused) {
				mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			} else {
				mInputMethodManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
			}
		}
	};

	private View.OnClickListener mBtnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view == mBtnConnect) {
				// set connect
				String password = mEtPassword.getText().toString();
				if(password.length() < 8) {
					Toast.makeText(mContext, R.string.wifi_toast_lack_password_length, Toast.LENGTH_SHORT).show();
				} else {
					setConnect(mWifiItem, password);
				}
			}
			else if (view == mBtnCancel) {
				cancel();
			}
		}
	};
	
	private CompoundButton.OnCheckedChangeListener mCheckedChacgedListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton button, boolean value) {
			if (button == mCbNotiPassword) {
				if(value)
					mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				else
					mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}
			else if (button == mCbAutoConnect) {
				if(value) {
					
				}
				
				else {
					
				}
			}
		}
	};
	
	public int convertFrequencyToChannel(int freq) {
	    if (freq >= 2412 && freq <= 2484) {
	        return (freq - 2412) / 5 + 1;
	    } else if (freq >= 5170 && freq <= 5825) {
	        return (freq - 5170) / 5 + 34;
	    } else {
	        return -1;
	    }
	}
	
	private void setConnect(WifiListItem item, String password) {
		WifiConfiguration config = new WifiConfiguration();
		
		config.SSID = "\"".concat(item.ssid).concat("\"");
		config.status = WifiConfiguration.Status.DISABLED;
		config.priority = 40;
		
		if(item.security == WifiListItem.SECURITY_WEP){
			
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.wepKeys[0] = "\"".concat(password).concat("\"");
			config.wepTxKeyIndex = 0;					
		}
		
		else if(item.security == WifiListItem.SECURITY_PSK) {
			
			if(item.pskType == PskType.WPA || item.pskType == PskType.WPA_WPA2){
				
				config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				config.preSharedKey = "\"".concat(password).concat("\"");
			}
			
			else if(item.pskType == PskType.WPA2) {
			
				config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				config.preSharedKey = "\"".concat(password).concat("\"");
			}
		}
		
		else if(item.security == WifiListItem.SECURITY_NONE) {
		
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedAuthAlgorithms.clear();
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			 
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		}
		
		mWifiManager.connect(config, mConnectListener);
	}
}
