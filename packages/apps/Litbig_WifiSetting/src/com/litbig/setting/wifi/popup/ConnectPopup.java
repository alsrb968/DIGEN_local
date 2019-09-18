package com.litbig.setting.wifi.popup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.litbig.setting.wifi.R;
import com.litbig.setting.wifi.WifiListItem;

public class ConnectPopup {
    private final float STANDARD_WIDTH = 1024f;
    private final float STANDARD_HEIGHT = 600f;
    private final float EXTRA_HEIGHT = 41f;
    private final float NAVIGATION_HEIGHT = 78.0f;
    private final int SHOW_SOFT_KEYBOARD_Y = 45;
    private final int HIDE_SOFT_KEYBOARD_Y = 45;

    public static interface OnClickListener {
        public void onCallKeypad();
        public void onShowPassword(boolean show);
    }

    private WindowManager mWindowManager = null;
    private OnClickListener mOnClickListener;
    private WifiManager mWifiManager;
    private WifiListItem mWifiItem;
    private InputMethodManager mInputMethodManager;
    private WifiManager.ActionListener mConnectListener;
    private Context mContext;

    private FrameLayout mBackgroundLayout;
    private FrameLayout mPopupLayout;
    private TextView mSignalLevelText;
    private TextView mPskTypeText;
    private TextView mConnectText;
    //	private EditText mPasswordEditText;
    private TextView mPasswordText;
    private CheckBox mNotiPasswordCheckBox;
    private FrameLayout mButtonLayout;
    private ImageButton mConnectButton;
    private ImageButton mCancelButton;

    private float mRateX;
    private float mRateY;
    private boolean mClickableButton = false;

    public ConnectPopup(Context context, WifiListItem item, WifiManager manager, WifiManager.ActionListener listener
            ,InputMethodManager inputmethodmanager) {
        mWifiItem = item;
        mWifiManager = manager;
        mContext = context;
        mConnectListener = listener;
        mInputMethodManager = inputmethodmanager;

        mWindowManager = (WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        float xRate = (float)metrics.widthPixels / STANDARD_WIDTH;
        float yRate = (float)metrics.heightPixels / STANDARD_HEIGHT;
        mRateX = xRate;
        mRateY = yRate;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(getXResolutionChange(STANDARD_WIDTH), getYResolutionChange(STANDARD_HEIGHT),
                WindowManager.LayoutParams.TYPE_BOOT_PROGRESS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        mBackgroundLayout = new FrameLayout(context);
        params.width = getXResolutionChange(STANDARD_WIDTH);
        params.height = getYResolutionChange(STANDARD_HEIGHT+EXTRA_HEIGHT);
        mBackgroundLayout.setBackgroundColor(0xee000000);
        mBackgroundLayout.setClickable(true);
        mBackgroundLayout.setVisibility(View.GONE);
        mWindowManager.addView(mBackgroundLayout, params);

        mPopupLayout = new FrameLayout(context);
        params.width = getXResolutionChange(634);
        params.height = getYResolutionChange(510);
        mPopupLayout.setX(getXResolutionChange(195));
        mPopupLayout.setY(getYResolutionChange(45));
        mPopupLayout.setBackgroundResource(R.drawable.connect_popup);
        mBackgroundLayout.addView(mPopupLayout, params);

        TextView titleText = new TextView(context);
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = getYResolutionChange(74);
        titleText.setX(getXResolutionChange(0));
        titleText.setY(getYResolutionChange(3));
        titleText.setTextColor(0xffffffff);
        titleText.setText(mWifiItem.getSsid());
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        titleText.setGravity(Gravity.CENTER);
        titleText.setSingleLine();
        titleText.setEllipsize(TextUtils.TruncateAt.END);
        mPopupLayout.addView(titleText, params);

        TextView signalTitleText = new TextView(context);
        params.width = getXResolutionChange(260);
        params.height = getYResolutionChange(88);
        signalTitleText.setX(getXResolutionChange(43));
        signalTitleText.setY(getYResolutionChange(77));
        signalTitleText.setTextColor(0xffffffff);
        signalTitleText.setText(context.getResources().getString(R.string.popup_title_signal_strenth));
        signalTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        signalTitleText.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(signalTitleText, params);

        mSignalLevelText = new TextView(context);
        params.width = getXResolutionChange(325);
        params.height = getYResolutionChange(88);
        mSignalLevelText.setX(getXResolutionChange(303));
        mSignalLevelText.setY(getYResolutionChange(77));
        mSignalLevelText.setTextColor(0xffffffff);
        mSignalLevelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mSignalLevelText.setGravity(Gravity.CENTER_VERTICAL);
        int level = mWifiItem.getLevel();
        String[] signal = mContext.getResources().getStringArray(R.array.wifi_signal);
        if(level > -1) mSignalLevelText.setText(signal[level]);
        else mSignalLevelText.setText(signal[0]);
        mPopupLayout.addView(mSignalLevelText, params);

        View line1 = new View(context);
        params.width = getXResolutionChange(628);
        params.height = 1;
        line1.setX(getXResolutionChange(4));
        line1.setY(getYResolutionChange(165));
        line1.setBackgroundColor(0xff3b3b3b);
        mPopupLayout.addView(line1, params);

        TextView pskTypeTitleText = new TextView(context);
        params.width = getXResolutionChange(260);
        params.height = getYResolutionChange(88);
        pskTypeTitleText.setX(getXResolutionChange(43));
        pskTypeTitleText.setY(getYResolutionChange(165));
        pskTypeTitleText.setTextColor(0xffffffff);
        pskTypeTitleText.setText(context.getResources().getString(R.string.popup_title_security));
        pskTypeTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        pskTypeTitleText.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(pskTypeTitleText, params);

        mPskTypeText = new TextView(context);
        params.width = getXResolutionChange(325);
        params.height = getYResolutionChange(88);
        mPskTypeText.setX(getXResolutionChange(303));
        mPskTypeText.setY(getYResolutionChange(165));
        mPskTypeText.setTextColor(0xffffffff);
        mPskTypeText.setText(mWifiItem.getSecurityString(true));
        mPskTypeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mPskTypeText.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(mPskTypeText, params);

        View line2 = new View(context);
        params.width = getXResolutionChange(628);
        params.height = 1;
        line2.setX(getXResolutionChange(4));
        line2.setY(getYResolutionChange(253));
        line2.setBackgroundColor(0xff3b3b3b);
        mPopupLayout.addView(line2, params);

        TextView passwordTitleText = new TextView(context);
        params.width = getXResolutionChange(260);
        params.height = getYResolutionChange(88);
        passwordTitleText.setX(getXResolutionChange(43));
        passwordTitleText.setY(getYResolutionChange(253));
        passwordTitleText.setTextColor(0xffffffff);
        passwordTitleText.setText(context.getResources().getString(R.string.popup_title_password));
        passwordTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        passwordTitleText.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(passwordTitleText, params);

        mPasswordText = new TextView(context);
        params.width = getXResolutionChange(325);
        params.height = getYResolutionChange(88);
        mPasswordText.setX(getXResolutionChange(298));
        mPasswordText.setY(getYResolutionChange(253));
        mPasswordText.setTextColor(0xffffffff);
        mPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mPasswordText.setGravity(Gravity.CENTER_VERTICAL);
        mPasswordText.setBackgroundColor(Color.TRANSPARENT);
        mPasswordText.setSingleLine();
        mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onCallKeypad();
                }
            }
        });
        mPopupLayout.addView(mPasswordText, params);

        int checkboxPadding = getYResolutionChange(20);
        mNotiPasswordCheckBox = new CheckBox(context);
        params.width = getXResolutionChange(76);
        params.height = getYResolutionChange(76);
        mNotiPasswordCheckBox.setX(getXResolutionChange(23));
        mNotiPasswordCheckBox.setY(getYResolutionChange(347));
        mNotiPasswordCheckBox.setPadding(checkboxPadding, checkboxPadding, checkboxPadding, checkboxPadding);
        mNotiPasswordCheckBox.setOnCheckedChangeListener(mCheckedChacgedListener);
        mNotiPasswordCheckBox.setButtonDrawable(R.drawable.checkbox_selector);
        mPopupLayout.addView(mNotiPasswordCheckBox, params);

        TextView showPasswordText = new TextView(context);
        params.width = getXResolutionChange(395);
        params.height = getYResolutionChange(88);
        showPasswordText.setX(getXResolutionChange(93));
        showPasswordText.setY(getYResolutionChange(341));
        showPasswordText.setTextColor(0xffffffff);
        showPasswordText.setText(context.getResources().getString(R.string.popup_title_show_password));
        showPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28.0f * yRate);
        showPasswordText.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(showPasswordText, params);

        View line3 = new View(context);
        params.width = getXResolutionChange(628);
        params.height = 1;
        line3.setX(getXResolutionChange(4));
        line3.setY(getYResolutionChange(341));
        line3.setBackgroundColor(0xff3b3b3b);
        mPopupLayout.addView(line3, params);

        mButtonLayout = new FrameLayout(context);
        params.width = getXResolutionChange(624);
        params.height = getYResolutionChange(74);
        mButtonLayout.setX(getXResolutionChange(4));
        mButtonLayout.setY(getYResolutionChange(432));
        mButtonLayout.setBackgroundColor(Color.BLACK);
        mPopupLayout.addView(mButtonLayout, params);

        mConnectButton = new ImageButton(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        mConnectButton.setImageResource(R.drawable.btn_popup_selector);
        mConnectButton.setBackgroundColor(Color.TRANSPARENT);
        mConnectButton.setOnClickListener(mClickListener);
        mButtonLayout.addView(mConnectButton, params);

        mConnectText = new TextView(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        mConnectText.setTextColor(0xffffffff);
        mConnectText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28 * yRate);
        mConnectText.setText(context.getResources().getString(R.string.popup_connect));
        mConnectText.setGravity(Gravity.CENTER);
        mButtonLayout.addView(mConnectText, params);
        setClickableConnectButton(false);

        mCancelButton = new ImageButton(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        mCancelButton.setX(getXResolutionChange(313));
        mCancelButton.setImageResource(R.drawable.btn_popup_selector);
        mCancelButton.setBackgroundColor(Color.TRANSPARENT);
        mCancelButton.setOnClickListener(mClickListener);
        mButtonLayout.addView(mCancelButton, params);

        TextView cancelText = new TextView(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        cancelText.setX(getXResolutionChange(313));
        cancelText.setTextColor(0xffffffff);
        cancelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28 * yRate);
        cancelText.setText(context.getResources().getString(R.string.popup_cancel));
        cancelText.setGravity(Gravity.CENTER);
        mButtonLayout.addView(cancelText, params);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NAVIGATION_TOUCHEVENT);
        context.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void show() {
        if(mBackgroundLayout != null) mBackgroundLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if(mBackgroundLayout != null) mBackgroundLayout.setVisibility(View.GONE);
    }

    public void dismiss() {
        if(mBackgroundLayout != null && mWindowManager != null) {
            mBackgroundLayout.removeAllViews();
            mBackgroundLayout.setVisibility(View.GONE);
            mWindowManager.removeView(mBackgroundLayout);
            mBackgroundLayout = null;
            mWindowManager = null;
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public void showSoftKeyboard() {
        mPopupLayout.setY(SHOW_SOFT_KEYBOARD_Y);
        if(mInputMethodManager != null) mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideSoftKeyboard() {
        mPopupLayout.setY(HIDE_SOFT_KEYBOARD_Y);
        if(mInputMethodManager != null) mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void setClickableConnectButton(boolean enable) {
        mClickableButton = enable;
        if(enable) {
            mConnectText.setTextColor(0xffffffff);
        } else {
            mConnectText.setTextColor(0xff404040);
        }
    }
    private int convertFrequencyToChannel(int freq) {
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

        config.SSID = "\"".concat(item.getSsid()).concat("\"");
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;

        if(item.getSecurity() == WifiListItem.SECURITY_WEP){
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
        else if(item.getSecurity() == WifiListItem.SECURITY_PSK) {
            if(item.getPskType() == WifiListItem.PskType.WPA || item.getPskType() == WifiListItem.PskType.WPA_WPA2){

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.preSharedKey = "\"".concat(password).concat("\"");
            }
            else if(item.getPskType() == WifiListItem.PskType.WPA2) {

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
        else if(item.getSecurity() == WifiListItem.SECURITY_NONE) {

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

    private int getXResolutionChange(float x) {
        return (int) (x * mRateX);
    }

    private int getYResolutionChange(float y) {
        return (int) (y * mRateY);
    }

    // ----------
    // AndroidView Widget Listeners
    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean isFocused) {
            if (isFocused) {
                showSoftKeyboard();
            } else {
                hideSoftKeyboard();
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == mConnectButton && mClickableButton) {
                setConnect(mWifiItem, mPasswordText.getText().toString());
            } else if (v == mCancelButton) {
                dismiss();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener mCheckedChacgedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean value) {
            if (button == mNotiPasswordCheckBox) {
                if(mOnClickListener != null) mOnClickListener.onShowPassword(value);
                if(value) {
                    mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        }
    };

    public final String ACTION_NAVIGATION_TOUCHEVENT = "com.litbig.intent.action.navigation.touchevent";
    public final String INTENT_NAME_NAVIGATION_MOTIONEVENT = "com.litbig.intent.name.navigation.motionevent";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction;
            if (null != intent) {
                intentAction = intent.getAction();
                if (null != intentAction) {
                    if (intentAction.equals(ACTION_NAVIGATION_TOUCHEVENT)) {
                        MotionEvent event = intent.getExtras().getParcelable(INTENT_NAME_NAVIGATION_MOTIONEVENT);
                        if(event.getY() > 0) {
                            event.setLocation(event.getX(), event.getY() + STANDARD_HEIGHT - NAVIGATION_HEIGHT);
                            int cancelButtonX = (int) (mPopupLayout.getX() + mButtonLayout.getX() + mCancelButton.getX());
                            int cancelButtonY = (int) (mPopupLayout.getY() + mButtonLayout.getY() + mCancelButton.getY());
                            if(cancelButtonX < event.getX() && cancelButtonX + mCancelButton.getWidth() > event.getX() &&
                                    cancelButtonY < event.getY() && cancelButtonY + mCancelButton.getHeight() > event.getY()) {
                                mCancelButton.dispatchTouchEvent(event);
                            }
                            int connectButtonX = (int) (mPopupLayout.getX() + mButtonLayout.getX() + mConnectButton.getX());
                            int connectButtonY = (int) (mPopupLayout.getY() + mButtonLayout.getY() + mConnectButton.getY());
                            if(connectButtonX < event.getX() && connectButtonX + mConnectButton.getWidth() > event.getX() &&
                                    connectButtonY < event.getY() && connectButtonY + mConnectButton.getHeight() > event.getY()) {
                                mConnectButton.dispatchTouchEvent(event);
                            }
                        }
                    }
                }
            }
        }
    };

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setPasswordText(String msg) {
        int msgLength = msg.length();
        if (msgLength > 63) {
            msg = msg.substring(0, 63);
        }
        if(msgLength >= 8) {
            setClickableConnectButton(true);
        } else {
            setClickableConnectButton(false);
        }
        mPasswordText.setText(msg);
    }
    public String getPasswordText() {
        return mPasswordText.getText().toString();
    }
}
