package com.litbig.setting.network.wifi;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.litbig.app.setting.R;
import com.litbig.setting.network.NetworkViewInterface;

public class WiFiLayout extends RelativeLayout implements NetworkViewInterface{
	
	private final String TAG = "NetWorkLayout";

	private TextView mTvWifiItemCnt, mTvWifiSwitch;
	private ImageButton mBtnRefresh;

	private ScrollView mScrollView;
	
	private Context mContext;
	private WifiManager mWifiManager;
	
	private boolean mWifiState = false;
	
	private ListView mLvWifi;
	private ArrayList<WifiListItem> mWifiList;
	private ListViewAdapter mListViewAdapter;
	
	private ProgressBar mPbLoading;
	
	private LinearLayout mLlWifiListArea;
	
	private WifiInfo mLastInfo;
	private DetailedState mLastState;
	
	private ConnectDialog mConDialog;
	private InfoDialog mInfoDialog;
	
	private final Scanner mScanner;
	
	private final AtomicBoolean mConnected = new AtomicBoolean(false);

	final int MESSAGE_WIFI_REFRESH = 1;

	private InputMethodManager mInputMethodManager;
	
	// Combo scans can tack 5-6s to complete - set to 10s
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
	
	public WiFiLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;

		IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
		filter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		mContext.registerReceiver(mReceiver, filter);
		
		mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		
		mWifiList = new ArrayList<>();
		mListViewAdapter = new ListViewAdapter(mWifiList);
		
		mScanner = new Scanner();
		
		mInputMethodManager = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		mTvWifiItemCnt 	= findViewById(R.id.tv_wifi_item_count);
		mTvWifiSwitch	= findViewById(R.id.btn_tb_wifi_switch);
		
		mBtnRefresh		= findViewById(R.id.btn_wifi_refresh);

		mLlWifiListArea = findViewById(R.id.wifi_list_area);
		
		BtnClickListener btnClickListener = new BtnClickListener();
		mBtnRefresh.setOnClickListener(btnClickListener);
		mTvWifiSwitch.setOnClickListener(btnClickListener);
		
		mPbLoading = findViewById(R.id.progress_loading);
		Drawable d = mContext.getResources().getDrawable(R.drawable.ani_mini_progress);
		mPbLoading.setIndeterminateDrawable(d);
		mPbLoading.setVisibility(View.GONE);
		
		mLvWifi	= (ListView)findViewById(R.id.lv_wifi);
		mLvWifi.setOnTouchListener(mListTouchListener);
		mLvWifi.setAdapter(mListViewAdapter);
		
		ItemClickListener itemClickListener = new ItemClickListener();
		mLvWifi.setOnItemClickListener(itemClickListener);
		
		if (mWifiManager == null)
			mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		
		mWifiState = mWifiManager.isWifiEnabled();

		
		setSwitchWifiButton(mWifiState);
		setWifiListVisibility(mWifiState);
	}
	
	@Override
	public void onResume() {
		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
	}
	
	@Override
	public void onPause() {		
		if (mWifiManager.isWifiEnabled()) {
			mScanner.pause();
		}
		
		if(mInputMethodManager != null && mConDialog != null) {
			mInputMethodManager.hideSoftInputFromWindow(mConDialog.getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		
		if (mContext != null) {
			mContext.unregisterReceiver(mReceiver);
		}
		
		super.onDetachedFromWindow();
	}
	
	private void setWifiListVisibility(boolean value) {
		if(value) {
            mLlWifiListArea.setVisibility(View.VISIBLE);
		} else {
            mLlWifiListArea.setVisibility(View.GONE);
		}
	}

	public final Comparator mComparator = new Comparator<WifiListItem>() {

		private final Collator collator = Collator.getInstance();
		
		@Override
		public int compare(WifiListItem obj1, WifiListItem obj2) {

			if (obj1.getState() == DetailedState.CONNECTED ||
					obj1.getState() == DetailedState.CONNECTING ||
					obj1.getState() == DetailedState.OBTAINING_IPADDR) {
				return -1;
			}
			
			else {
				if (obj1.getLevel() > obj2.getLevel())
					return -1;
				
				else if (obj1.getLevel() < obj2.getLevel())
					return 1;
				
				else 
					return 0;
			}
		}
	};
	
	private void setLoadingProgressVisible(boolean value) {
		if (mPbLoading == null)
			return ;
		
		if (value) 
			mPbLoading.setVisibility(View.VISIBLE);
		else
			mPbLoading.setVisibility(View.GONE);
	}
	
	private void setSwitchWifiButton(boolean value) {
		if (mTvWifiSwitch == null)
			return ;
		
		mTvWifiSwitch.setSelected(value);		
		mTvWifiSwitch.setClickable(true);
		
		setWifiListVisibility(value);
		
		if(value) {
			mTvWifiSwitch.setText(mContext.getString(R.string.on));
		} else {
			mTvWifiSwitch.setText(mContext.getString(R.string.off));
		}
	}
	
	private void refreshWifiList() {
		
		mListViewAdapter.clear();
		mListViewAdapter.notifyDataSetChanged();
		mTvWifiItemCnt.setText(mContext.getString(R.string.wifi_con_scan_result) + " [ 0 ]");
		setLoadingProgressVisible(true);
		
		if (mWifiState) {
			mScanner.resume();
		}
	}

	private void updateConnectionState(DetailedState state) {
		if(!mWifiManager.isWifiEnabled()) {
			mScanner.pause();
			return ;
		}
		
		mLastInfo = mWifiManager.getConnectionInfo();
		
		if (state != null) {
			mLastState = state;
		}
		
		for(int i = 0; i < mWifiList.size(); i++) {
			mWifiList.get(i).update(mLastInfo, mLastState);
		}

		Collections.sort(mWifiList, mComparator);
		mListViewAdapter.notifyDataSetChanged();
	}
	
	private void handleWifiStateChanged(int state) {
		switch (state) {
		case WifiManager.WIFI_STATE_ENABLING:
			// show loading progress
			setLoadingProgressVisible(true);
			mTvWifiSwitch.setClickable(false);
			
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			// hide loading progress
			setSwitchWifiButton(true);
			
			if (mTvWifiItemCnt != null)
				mTvWifiItemCnt.setText(mContext.getString(R.string.wifi_con_scanning));
			
			mScanner.resume();
			mWifiState = true;
			
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			// show loading progress
			setLoadingProgressVisible(true);
			mTvWifiSwitch.setClickable(false);
			
			mListViewAdapter.clear();
			mListViewAdapter.notifyDataSetChanged();
			
			break;
		case WifiManager.WIFI_STATE_DISABLED:
			// hide loading progress
			setLoadingProgressVisible(false);
			setSwitchWifiButton(false);
			
			mScanner.pause();
			mWifiState = false;
			break;
		default:
			setSwitchWifiButton(false);
			
			break;
		}
	}
	
	public class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			WifiListItem item = (WifiListItem)parent.getItemAtPosition(position);
			
			if (item.getConfig() != null) {	// saved wifi item
				// show wifi info Dialog
				if (item.networkId != WifiConfiguration.INVALID_NETWORK_ID){
					mInfoDialog = new InfoDialog(mContext, item, mWifiManager, mConnectListener, mForgetListener);
					mInfoDialog.show();
				}
			} else {
				if (item.security == WifiListItem.SECURITY_NONE) {
					item.generateOpenNetworkConfig();
					mWifiManager.connect(item.getConfig(), mConnectListener);
				} else {
					// show Wifi Connect Dialog
					mConDialog = new ConnectDialog(mContext, item, mWifiManager, mConnectListener, mInputMethodManager);
					mConDialog.show();
				}
			}
		}
	}

    private View.OnTouchListener mListTouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mScrollView.requestDisallowInterceptTouchEvent(true);
			return false;
		}
	};

	public class BtnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			
			if (view == mTvWifiSwitch) {
				setLoadingProgressVisible(true);
				int wifiApState = mWifiManager.getWifiApState();
				if(!mWifiState && (wifiApState == WifiManager.WIFI_AP_STATE_ENABLING || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
//					mWifiManager.setWifiApEnabled(null, false);
				}
				
				mTvWifiSwitch.setClickable(false);
				
				if(!mWifiManager.setWifiEnabled(!mWifiState)) {
					// error
					setLoadingProgressVisible(false);
					mTvWifiSwitch.setClickable(true);
				}
			}
			
			else if (view == mBtnRefresh) {
				refreshWifiList();
			}
		}
	}

	private ActionListener mConnectListener = new ActionListener() {
		
		@Override
		public void onSuccess() {
			if (mConDialog != null)
				mConDialog.dismiss();
			
			if (mInfoDialog != null)
				mInfoDialog.dismiss();
		}
		
		@Override
		public void onFailure(int reason) {
			Log.d(TAG, "Wifi Connection Failed : " + reason);
		}
	};

	private ActionListener mForgetListener = new ActionListener() {
		
		@Override
		public void onSuccess() {
			if (mConDialog != null)
				mConDialog.dismiss();

			if (mInfoDialog != null)
				mInfoDialog.dismiss();
			
			if (mWifiManager.isWifiEnabled())
				mScanner.resume();
		}
		
		@Override
		public void onFailure(int reason) {
		}
	};
		
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) ||
					action.equals(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION) ||
					action.equals(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION)) {
				// hide loading progressbar
				setLoadingProgressVisible(false);
				
				mListViewAdapter.clear();
				
				MultiMap<String, WifiListItem> apMap = new MultiMap<String, WifiListItem>();
				
				final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
				
				if (configs != null) {
					for (WifiConfiguration config : configs) {
						WifiListItem item = new WifiListItem(mContext, config);
						
						item.update(mLastInfo, mLastState);
						apMap.put(item.ssid, item);
					}
				}
				
				final List<ScanResult> results = mWifiManager.getScanResults();
				if (results != null) {
					for (ScanResult result : results) {
						
						// ignore hidden and ad-hoc network
						if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]") || result.SSID.isEmpty())
							continue;
						
						boolean found = false;
						
						for (WifiListItem item : apMap.getAll(result.SSID)) {
							if (item.update(result)) {
								mWifiList.add(item);
								found = true;
							}
						}

						if (!found) {
							WifiListItem item = new WifiListItem(mContext, result);
							
							mWifiList.add(item);
							apMap.put(item.ssid, item);
						}
					}
				}

				int size = mWifiList.size();
				
				if (mTvWifiItemCnt != null)
					mTvWifiItemCnt.setText(context.getString(R.string.wifi_con_scan_result) + " [ " + size + " ]");
				
				Collections.sort(mWifiList, mComparator);
				mListViewAdapter.notifyDataSetChanged();
			}
			
			else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
			}
			
			else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				SupplicantState state = (SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
				
				if(!mConnected.get() && SupplicantState.isHandshakeState(state)) {
					updateConnectionState(WifiInfo.getDetailedStateOf(state));
				} else {
					updateConnectionState(null);
				}
				
				if(state == SupplicantState.DISCONNECTED || state == SupplicantState.COMPLETED) {
					mScanner.resume();
				}
			}
			
			else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				mConnected.set(info.isConnected());
				updateConnectionState(info.getDetailedState());
			}
			
			else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
				updateConnectionState(null);
			}
		}
	};
	
	private class Scanner extends Handler {
		
		private int mRetry = 0;
		
		void resume() {
			if (!hasMessages(0)) {
				sendEmptyMessage(0);
			}
		}
		
		void forceScan() {
			removeMessages(0);
			sendEmptyMessage(0);
		}
		
		void pause() {
			mRetry = 0;
			removeMessages(0);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mWifiManager.startScan()) {
				mRetry = 0;
			}
			else if (++mRetry >= 3) {
				mRetry = 0;
				Log.e(TAG, "wifi scan failed..");
				
				return ;
			}
			sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
		}
	}
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_WIFI_REFRESH:
				refreshWifiList();
				break;
			}	
		}
	};

	@Override
	public void setScrollView(ScrollView v) {
		mScrollView = v;
	}
}
