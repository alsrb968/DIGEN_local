package com.litbig.setting.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.litbig.keypad.KeypadView;
import com.litbig.setting.wifi.popup.AddNetworkPopup;
import com.litbig.setting.wifi.popup.ConnectPopup;
import com.litbig.setting.wifi.popup.SavedConnectPopup;
import com.litbig.setting.wifi.popup.SavedSecurityPopup;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiLayout extends RelativeLayout {
    private KeypadView mKeypadView;
    private Context mContext;
    private WifiManager mWifiManager;
    private WifiSetting mActivity;

    private LinearLayout mWifiLayout;
    private ListView mLvWifi;
    private ArrayList<WifiListItem> mWifiList;
    private WifiListItem mConnectedWifiItem;
    private ListViewAdapter mListViewAdapter;
    private ProgressBar mPbLoading;
    private TextView mAppTitleTextView;
    private ImageButton mBtnRefresh;
    private ImageButton mOnOffButton;
    private ImageButton mAddNetworkButton;
    private ImageButton mDeleteButton;

    private WifiInfo mLastInfo;
    private NetworkInfo.DetailedState mLastState;
    private ConnectPopup mConnectPopup;
    private SavedSecurityPopup mSavedSecurityPopup;
    private AddNetworkPopup mAddNetworkPopup;
    private SavedConnectPopup mOpenConnectPopup;

    private final Scanner mScanner;
    private boolean mWifiState = false;
    private float mDpRate;

    private final AtomicBoolean mConnected = new AtomicBoolean(false);

    final int MESSAGE_WIFI_REFRESH = 1;
    final int MESSAGE_POPUP_DISMISS = 2;

    private InputMethodManager mInputMethodManager;

    // Combo scans can tack 5-6s to complete - set to 10s
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    public void setActivity(WifiSetting activity) {
        mActivity = activity;
    }

    public WifiLayout(Context context, AttributeSet attrs) {
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
        mListViewAdapter = new ListViewAdapter(mWifiList, mContext);

        mScanner = new Scanner();

        mInputMethodManager = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDpRate = WifiSetting.getDisplayRate(mContext);
        mWifiLayout = findViewById(R.id.wifi_layout);
        mBtnRefresh = findViewById(R.id.btn_1);
        mBtnRefresh.setOnClickListener(mBtnClickListener);
        mBtnRefresh.setVisibility(View.VISIBLE);
        mBtnRefresh.setImageResource(R.drawable.btn_refresh_selector);
        mAppTitleTextView = findViewById(R.id.app_name);
        mAppTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 36.0f * mDpRate);
        mOnOffButton = findViewById(R.id.on_off_button);
        mOnOffButton.setOnClickListener(mBtnClickListener);
        mAddNetworkButton = findViewById(R.id.add_network_button);
        mAddNetworkButton.setOnClickListener(mBtnClickListener);
        mDeleteButton = findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(mBtnClickListener);
        mPbLoading = findViewById(R.id.progress_loading);
        Drawable d = mContext.getResources().getDrawable(R.drawable.ani_mini_progress, null);
        mPbLoading.setIndeterminateDrawable(d);
        mPbLoading.setVisibility(View.GONE);

        mLvWifi	= findViewById(R.id.lv_wifi);
        mLvWifi.setOnTouchListener(mListTouchListener);
        mLvWifi.setAdapter(mListViewAdapter);

        ItemClickListener itemClickListener = new ItemClickListener();
        mLvWifi.setOnItemClickListener(itemClickListener);

        if (mWifiManager == null)
            mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        mWifiState = mWifiManager.isWifiEnabled();

        setSwitchWifiButton(mWifiState);
        mAddNetworkButton.setClickable(mWifiState);
        mDeleteButton.setClickable(mWifiState);

        mKeypadView = new KeypadView(mActivity);
    }

    public void onResume() {
        if(mWifiLayout != null) mWifiLayout.setVisibility(View.VISIBLE);
        if (mWifiManager.isWifiEnabled()) {
            mScanner.resume();
        }
    }

    public void onPause() {
        if (mWifiManager.isWifiEnabled()) {
            mScanner.pause();
        }

        if(mInputMethodManager != null && mConnectPopup != null) {
            mConnectPopup.hideSoftKeyboard();
        }
        if(mKeypadView != null) mKeypadView.onPause();

        if(mConnectPopup != null) mConnectPopup.dismiss();
        if(mSavedSecurityPopup != null) mSavedSecurityPopup.dismiss();
        if(mAddNetworkPopup != null) mAddNetworkPopup.dismiss();
        if(mOpenConnectPopup != null) mOpenConnectPopup.dismiss();
    }

    @Override
    protected void onDetachedFromWindow() {

        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
        }

        super.onDetachedFromWindow();
    }

    public final Comparator mComparator = new Comparator<WifiListItem>() {

        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(WifiListItem obj1, WifiListItem obj2) {

            if (obj1.getState() == NetworkInfo.DetailedState.CONNECTED ||
                    obj1.getState() == NetworkInfo.DetailedState.CONNECTING ||
                    obj1.getState() == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                return -1;
            } else {
                return (obj2.getLevel() <= obj1.getLevel()) ? -1 : 1;
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
        if (mOnOffButton == null || mBtnRefresh == null)
            return ;

        mBtnRefresh.setClickable(value);
        if(value) {
            mOnOffButton.setImageResource(R.drawable.txt_off);
            mBtnRefresh.setImageResource(R.drawable.btn_refresh_selector);
        } else {
            mOnOffButton.setImageResource(R.drawable.txt_on);
            mBtnRefresh.setImageResource(R.drawable.btn_refresh_dis);
        }
    }

    private void refreshWifiList() {

        mListViewAdapter.clear();
        mListViewAdapter.notifyDataSetChanged();
        setLoadingProgressVisible(true);

        if (mWifiState) {
            mScanner.resume();
        }
    }

    private void updateConnectionState(NetworkInfo.DetailedState state) {
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

        try {
            Collections.sort(mWifiList, mComparator);
            if(mConnectedWifiItem != null) {
                mWifiList.remove(mConnectedWifiItem);
                mWifiList.add(0, mConnectedWifiItem);
            }
        } catch (Exception e) {
        }
        mListViewAdapter.notifyDataSetChanged();
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                // show loading progress
                setLoadingProgressVisible(true);

                break;
            case WifiManager.WIFI_STATE_ENABLED:
                // hide loading progress
                setSwitchWifiButton(true);

//			if (mTvWifiItemCnt != null)
//				mTvWifiItemCnt.setText(mContext.getString(R.string.wifi_con_scanning));

                mScanner.resume();
                mWifiState = true;

                break;
            case WifiManager.WIFI_STATE_DISABLING:
                // show loading progress
                setLoadingProgressVisible(true);

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

    public class ItemClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            final WifiListItem item = (WifiListItem)parent.getItemAtPosition(position);
            Log.i("item state : "+item.getState());
            if (item.getState() == NetworkInfo.DetailedState.CONNECTING || item.getState() == NetworkInfo.DetailedState.AUTHENTICATING
                    || item.getState() == NetworkInfo.DetailedState.OBTAINING_IPADDR || item.getState() == NetworkInfo.DetailedState.CONNECTED) {
                // connected item
                createConnectedPopup(item);
            } else {
                if (item.getSecurity() == WifiListItem.SECURITY_NONE) {
                    // open item
                    item.generateOpenNetworkConfig();
                    mOpenConnectPopup = new SavedConnectPopup(mContext);
                    mOpenConnectPopup.setWifiListItem(item);
                    mOpenConnectPopup.setYesText(mContext.getResources().getString(R.string.popup_connect));
                    mOpenConnectPopup.setOnClickListener(new SavedConnectPopup.OnClickListener() {
                        @Override
                        public void onYes() {
                            mWifiManager.connect(item.getConfig(), mConnectListener);
                            mOpenConnectPopup.dismiss();
                        }

                        @Override
                        public void onNo() {
                            mOpenConnectPopup.dismiss();
                        }
                    });
                    mOpenConnectPopup.show();
                } else {
                    if (item.getConfig() != null) {
                        // saved item
                        mSavedSecurityPopup = new SavedSecurityPopup(mContext);
                        mSavedSecurityPopup.setWifiListItem(item);
                        mSavedSecurityPopup.setOnClickListener(new SavedSecurityPopup.OnClickListener() {
                            @Override
                            public void onDoNotSaved() {
                                mWifiManager.forget(item.getNetworkId(), mForgetListener);
                                mSavedSecurityPopup.dismiss();
                            }

                            @Override
                            public void onConnect() {
                                mWifiManager.connect(item.getConfig(), mConnectListener);
                                mSavedSecurityPopup.dismiss();
                            }

                            @Override
                            public void onCancel() {
                                mSavedSecurityPopup.dismiss();
                            }
                        });
                        mSavedSecurityPopup.show();
                    } else {
                        mConnectPopup = new ConnectPopup(mContext, item, mWifiManager, mConnectListener, null/*mInputMethodManager*/);
                        mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        mConnectPopup.setOnClickListener(new ConnectPopup.OnClickListener() {
                            @Override
                            public void onCallKeypad() {
                                mWifiLayout.setVisibility(View.GONE);
                                mKeypadView.showKeypadView(true);
                                mKeypadView.setInputText(mConnectPopup.getPasswordText());
                                mKeypadView.setTopText(item.getSsid() + " WiFi Password");
                                mKeypadView.setOnClickListener(new KeypadView.OnClickListener() {
                                    @Override
                                    public void onSave(String msg) {
                                        mWifiLayout.setVisibility(View.VISIBLE);
                                        mConnectPopup.setPasswordText(msg);
                                        mConnectPopup.show();
                                    }
                                    @Override
                                    public void onBack() {
                                        mWifiLayout.setVisibility(View.VISIBLE);
                                        mConnectPopup.show();
                                    }
                                });
                                mConnectPopup.hide();
                            }
                            @Override
                            public void onShowPassword(boolean show) {
                                if(show) {
                                    mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                } else {
                                    mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                }
                            }
                        });

                        mConnectPopup.show();
                    }
                }
            }
        }
    }

    private View.OnTouchListener mListTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };
    private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == mBtnRefresh) {
                refreshWifiList();
            } else if (view == mOnOffButton) {
                setLoadingProgressVisible(true);
                int wifiApState = mWifiManager.getWifiApState();
                if (!mWifiState && (wifiApState == WifiManager.WIFI_AP_STATE_ENABLING
                        || (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
//                    mWifiManager.setWifiApEnabled(null, false);//TODO: @nowhere
                    Log.d("mWifiManager.setWifiApEnabled(null, false);//TODO: @nowhere");
                }

                if (!mWifiManager.setWifiEnabled(!mWifiState)) {
                    // error
                    setLoadingProgressVisible(false);
                }
                mAddNetworkButton.setClickable(!mWifiState);
                mDeleteButton.setClickable(!mWifiState);

            } else if (view == mAddNetworkButton) {
                if(mWifiState) createAddNetWorkPopup();
            } else if (view == mDeleteButton) {
                if(mConnectedWifiItem != null && mWifiState) createConnectedPopup(mConnectedWifiItem);
            }
        }
    };

    private void createAddNetWorkPopup() {
        mAddNetworkPopup = new AddNetworkPopup(mContext);
        mAddNetworkPopup.show();
        mAddNetworkPopup.setOnClickListener(new AddNetworkPopup.OnClickListener() {
            @Override
            public void onYes(String ssid, String password, int pskType) {
                WifiConfiguration wifiConfig = new WifiConfiguration();

                wifiConfig.SSID = "\"".concat(ssid).concat("\"");
                wifiConfig.status = WifiConfiguration.Status.DISABLED;
                wifiConfig.priority = 40;//TODO: @deprecated

                switch (pskType) {
                    case WifiListItem.SECURITY_NONE:
                        /*Open network*/
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);//TODO: @deprecated
                        wifiConfig.allowedAuthAlgorithms.clear();
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        break;
                    case WifiListItem.SECURITY_WEP:
                        /*WEP*/
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);//TODO: @deprecated
                        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);//TODO: @deprecated
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);//TODO: @deprecated

                        wifiConfig.wepKeys[0] = "\"".concat(password).concat("\"");//TODO: @deprecated
                        wifiConfig.wepTxKeyIndex = 0;//TODO: @deprecated
                        break;
                    case WifiListItem.SECURITY_PSK:
                        /*WPA / WPA2 Security*/
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);//TODO: @deprecated
                        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);//TODO: @deprecated
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                        wifiConfig.preSharedKey = "\"".concat(password).concat("\"");
                        break;
                }
                mWifiManager.addNetwork(wifiConfig);
                mAddNetworkPopup.dismiss();
            }

            @Override
            public void onNo() {
                mAddNetworkPopup.dismiss();
            }

            @Override
            public void onCallKeypad(final int type) {
                mWifiLayout.setVisibility(View.GONE);
                mKeypadView.showKeypadView(true);
                mKeypadView.setTopText(mContext.getResources().getString(R.string.popup_addnetwork_title));
                if(type == mAddNetworkPopup.KEYPAD_TYPE_SSID) {
                    mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }else if(type == mAddNetworkPopup.KEYPAD_TYPE_PASSWORD) {
                    if(mAddNetworkPopup.getShowPassword()) {
                        mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    } else {
                        mKeypadView.setInputBoxType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
                mKeypadView.setOnClickListener(new KeypadView.OnClickListener() {
                    @Override
                    public void onSave(String msg) {
                        mWifiLayout.setVisibility(View.VISIBLE);
                        if(type == mAddNetworkPopup.KEYPAD_TYPE_SSID) {
                            mAddNetworkPopup.setSsidText(msg);
                        }else if(type == mAddNetworkPopup.KEYPAD_TYPE_PASSWORD) {
                            mAddNetworkPopup.setPasswordText(msg);
                        }
                        mAddNetworkPopup.show();
                    }
                    @Override
                    public void onBack() {
                        mWifiLayout.setVisibility(View.VISIBLE);
                        mAddNetworkPopup.show();
                    }
                });
                mAddNetworkPopup.hide();
            }
        });
    }

    private void createConnectedPopup(final WifiListItem item) {
        mOpenConnectPopup = new SavedConnectPopup(mContext);
        mOpenConnectPopup.setWifiListItem(item);
        mOpenConnectPopup.setYesText(mContext.getResources().getString(R.string.popup_do_not_save));
        mOpenConnectPopup.setOnClickListener(new SavedConnectPopup.OnClickListener() {
            @Override
            public void onYes() {
                mWifiManager.forget(item.getNetworkId(), mForgetListener);
                mOpenConnectPopup.dismiss();
            }

            @Override
            public void onNo() {
                mOpenConnectPopup.dismiss();
            }
        });
        mOpenConnectPopup.show();
    }

    private WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {

        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(MESSAGE_POPUP_DISMISS);
        }

        @Override
        public void onFailure(int reason) {
            Log.d("Wifi Connection Failed : " + reason);
        }
    };

    private WifiManager.ActionListener mForgetListener = new WifiManager.ActionListener() {

        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(MESSAGE_POPUP_DISMISS);

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

                MultiMap<String, WifiListItem> apMap = new MultiMap<>();

                final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

                if (configs != null) {
                    for (WifiConfiguration config : configs) {
                        WifiListItem item = new WifiListItem(mContext, config);

                        item.update(mLastInfo, mLastState);
                        apMap.put(item.getSsid(), item);
                    }
                }

                final List<ScanResult> results = mWifiManager.getScanResults();
                if (results != null) {
                    mConnectedWifiItem = null;
                    for (ScanResult result : results) {

                        // ignore hidden and ad-hoc network
                        if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]") || result.SSID.isEmpty())
                            continue;

                        boolean found = false;

                        for (WifiListItem item : apMap.getAll(result.SSID)) {
                            if (item.update(result)) {
                                mWifiList.add(item);
                                if(item.getState() == NetworkInfo.DetailedState.CONNECTED) {
                                    mConnectedWifiItem = item;
                                }
                                found = true;
                            }
                        }

                        if (!found) {
                            WifiListItem item = new WifiListItem(mContext, result);

                            mWifiList.add(item);
                            if(item.getState() == NetworkInfo.DetailedState.CONNECTED) {
                                mConnectedWifiItem = item;
                            }
                            apMap.put(item.getSsid(), item);
                        }
                    }
                }

                int size = mWifiList.size();

                try {
                    Collections.sort(mWifiList, mComparator);
                    if(mConnectedWifiItem != null) {
                        mWifiList.remove(mConnectedWifiItem);
                        mWifiList.add(0, mConnectedWifiItem);
                        mDeleteButton.setClickable(true);
                    } else {
                        mDeleteButton.setClickable(false);
                    }
                } catch (Exception e) {
                }
                mListViewAdapter.notifyDataSetChanged();
            }

            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            }

            else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {//TODO: @deprecated
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);//TODO: @deprecated

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
            if (mWifiManager.startScan()) {//TODO: @deprecated
                mRetry = 0;
            }
            else if (++mRetry >= 3) {
                mRetry = 0;
                Log.e("wifi scan failed..");

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
                case MESSAGE_POPUP_DISMISS:
                    if (mConnectPopup != null) {
                        mConnectPopup.dismiss();
                        mConnectPopup = null;
                    }
                    break;
            }
        }
    };
}
