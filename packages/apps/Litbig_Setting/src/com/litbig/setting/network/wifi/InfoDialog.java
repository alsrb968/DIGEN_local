package com.litbig.setting.network.wifi;

import android.app.Dialog;
import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.litbig.app.setting.R;

public class InfoDialog extends Dialog{
	private final String TAG = "InfoDialog";
	
	private Button mBtnConnect, mBtnForget, mBtnCancel;
	
	private WifiManager mWifiManager;
	private WifiListItem mWifiItem;
	
	private TextView mTvSsid, mTvSignal, mTvStatus, mTvLinkSpeed, mTvIpAddress, mTvSecure;
	private Context mContext;
	
	private WifiManager.ActionListener mConnectListener;
	private WifiManager.ActionListener mForgetListener;
	
	private RelativeLayout mConnectedWifiInfoLayout;
	
	protected InfoDialog(Context context, WifiListItem item, WifiManager manager, WifiManager.ActionListener connectListener, WifiManager.ActionListener forgetListener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		
		mWifiItem = item;
		mWifiManager = manager;
		mContext = context;
		mConnectListener = connectListener;
		mForgetListener = forgetListener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.wifi_info_dialog);
		
		BtnClickListener btnClickListener = new BtnClickListener();
		
		mBtnConnect = findViewById(R.id.btn_connect);
		mBtnForget = findViewById(R.id.btn_forget);
		mBtnCancel = findViewById(R.id.btn_cancel);

		mBtnConnect.setOnClickListener(btnClickListener);
		mBtnForget.setOnClickListener(btnClickListener);
		mBtnCancel.setOnClickListener(btnClickListener);
		
		mTvSsid = findViewById(R.id.ssid);
		mTvSsid.setText(mWifiItem.ssid);

		mTvStatus = findViewById(R.id.wifi_status);
		mTvSignal = findViewById(R.id.wifi_signal);
		mTvSecure = findViewById(R.id.wifi_secure);
		mTvLinkSpeed = findViewById(R.id.wifi_link_speed);
		mTvIpAddress = findViewById(R.id.wifi_ip_address);
		
		mConnectedWifiInfoLayout = findViewById(R.id.connected_wifi_info);
		
		mConnectedWifiInfoLayout.setVisibility(View.GONE);

		mBtnConnect.setVisibility(View.GONE);
		
		if(mWifiItem.getConfig() != null) {
			// Saved Wifi Item
			DetailedState state = mWifiItem.getState();
			
			if(state == DetailedState.CONNECTED) {
				mConnectedWifiInfoLayout.setVisibility(View.VISIBLE);
				
				int index = state.ordinal();
				String[] formats = mContext.getResources().getStringArray(R.array.wifi_status);
				
				if (index < formats.length && formats[index].length() != 0)
		        	mTvStatus.setText(formats[index]);

				WifiInfo info = mWifiItem.getInfo();
				if(info != null && info.getLinkSpeed() != -1) {
					mTvLinkSpeed.setText(info.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);

					int ipAddress = info.getIpAddress();
					String ipString = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
					mTvIpAddress.setText(ipString);
				}
			} else {
				mConnectedWifiInfoLayout.setVisibility(View.GONE);
			}

			int level = mWifiItem.getLevel();
			if(level != -1) {
				String[] signal = mContext.getResources().getStringArray(R.array.wifi_signal);
				mTvSignal.setText(signal[level]);
			}
			mTvSecure.setText(mWifiItem.getSecurityString(true));
			
			if(mWifiItem.getState() == DetailedState.AUTHENTICATING || mWifiItem.getState() == DetailedState.CONNECTED || mWifiItem.getState() == DetailedState.CONNECTING) {
				// connected wifi
			} else {
				mBtnConnect.setVisibility(View.VISIBLE);
			}
		}
		else {
			Log.d(TAG, "this is not saved wifi item");
		}
	}
	
	private class BtnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			
			if (view == mBtnCancel) {
				cancel();
			}
			else if (view == mBtnConnect) {
				mWifiManager.connect(mWifiItem.getConfig(), mConnectListener);
			}
			else if (view == mBtnForget) {
				mWifiManager.forget(mWifiItem.networkId, mForgetListener);
			}
		}
	}
	
	public int convertFrequencyToChannel(int freq) {
	    if (freq >= 2412 && freq <= 2484) {
	        return (freq - 2412) / 5 + 1;
	    } else if (freq >= 5170 && freq <= 5825) {
	        return (freq - 5170) / 5 + 34;
	    } else {
	        return -1;
	    }
	}
}
