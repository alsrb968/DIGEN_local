package com.litbig.setting;

import com.litbig.app.setting.R;
import com.litbig.setting.network.bluetooth.BluetoothLayout;
import com.litbig.setting.network.wifi.WiFiLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class MainActivity extends Activity {
	private final int SET_NETWORK_MODE 	= 1;
	private final int SET_SYSTEM_MODE 	= 2;
	
	private ImageButton mBtnSetNetwork, mBtnSetSystem;
	
	private RelativeLayout mRlSetNetwork, mRlSetSystem;
	
	private WiFiLayout mWifiLayout;
	private BluetoothLayout mBlutoothLayout;
	private ScrollView mScrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		mBtnSetNetwork	= findViewById(R.id.btn_set_network);
		mBtnSetSystem	= findViewById(R.id.btn_set_system);

		mRlSetNetwork	= findViewById(R.id.content_setting_network);
		mRlSetSystem	= findViewById(R.id.content_setting_system);

		mWifiLayout = mRlSetNetwork.findViewById(R.id.wifi_area);
		mBlutoothLayout = mRlSetNetwork.findViewById(R.id.bluetooth_area);

		mScrollView 	= findViewById(R.id.scroll_view);
		
		BtnClickListener btnClickListener = new BtnClickListener();
		mBtnSetNetwork.setOnClickListener(btnClickListener);
		mBtnSetSystem.setOnClickListener(btnClickListener);

		String mode = getIntent().getStringExtra("mode");

		if (mode == null) {
			changeMode(SET_NETWORK_MODE);
		} else {
			mode = mode.toLowerCase();
			
			if(mode.equals("wifi")) {
				changeMode(SET_NETWORK_MODE);
			} else if (mode.equals("bluetooth")) {
				changeMode(SET_NETWORK_MODE);
			}	
		}
	}
	
	public class BtnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			
			if (view == mBtnSetNetwork) {
				changeMode(SET_NETWORK_MODE);
			}
			
			else if (view == mBtnSetSystem) {
				changeMode(SET_SYSTEM_MODE);
			}
		}
	}
	
	
	private void changeMode(int mode) {
		
		if (mRlSetNetwork == null || mRlSetSystem == null)
			return ;

		mRlSetNetwork.setVisibility(View.GONE);
		mRlSetSystem.setVisibility(View.GONE);

		mBtnSetNetwork.setSelected(false);
		mBtnSetSystem.setSelected(false);
		
		switch(mode) {
		case SET_NETWORK_MODE:
			mBlutoothLayout.setScrollView(mScrollView);
			mWifiLayout.setScrollView(mScrollView);
			mRlSetNetwork.setVisibility(View.VISIBLE);
			mBtnSetNetwork.setSelected(true);
			break;
		case SET_SYSTEM_MODE:
			mRlSetSystem.setVisibility(View.VISIBLE);
			mBtnSetSystem.setSelected(true);
			break;
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent == null)
			return ;

		String mode = intent.getStringExtra("mode");

		if (mode == null) {
			changeMode(SET_NETWORK_MODE);
		} else {
			mode = mode.toLowerCase();

			if(mode.equals("wifi")) {
				changeMode(SET_NETWORK_MODE);
			} else if (mode.equals("bluetooth")) {
				changeMode(SET_NETWORK_MODE);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mWifiLayout.onResume();
		mBlutoothLayout.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mWifiLayout.onPause();
		mBlutoothLayout.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
