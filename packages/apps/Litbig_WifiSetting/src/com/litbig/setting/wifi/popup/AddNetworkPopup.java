package com.litbig.setting.wifi.popup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.litbig.setting.wifi.R;
import com.litbig.setting.wifi.WifiListItem;

public class AddNetworkPopup {
    private final float STANDARD_WIDTH = 1024f;
    private final float STANDARD_HEIGHT = 600f;

    public final int KEYPAD_TYPE_SSID = 0;
    public final int KEYPAD_TYPE_PASSWORD = 1;

    public static interface OnClickListener {
        public void onYes(String ssid, String password, int pskType);
        public void onNo();
        public void onCallKeypad(int type);
    }

    private WindowManager mWindowManager;
    private OnClickListener mOnClickListener;
    private Context mContext;

    private FrameLayout mBackgroundLayout;
    private FrameLayout mPopupLayout;
    private TextView mTitleText;
    private TextView mSsidTextView;
    private FrameLayout mButtonLayout;
    private TextView mSsidInputTextView;
    private TextView mYesText;
    private TextView mPskTypeText;
    private FrameLayout mPasswordLayout;
    private TextView mPasswordText;
    private TextView mPasswordTitleText;
    private CheckBox mNotiPasswordCheckBox;
    private FrameLayout mDropButtonBackgroundLayout;
    private FrameLayout mDropButtonLayout;

    private float xRate;
    private float yRate;
    private int mPskType = WifiListItem.SECURITY_NONE;
    private ImageButton mYesButton;

    public AddNetworkPopup(Context context) {
        mContext = context;
        mWindowManager = (WindowManager)context.getSystemService(android.content.Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        xRate = (float)metrics.widthPixels / STANDARD_WIDTH;
        yRate = (float)metrics.heightPixels / STANDARD_HEIGHT;
        mWindowManager = (WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(getXResolutionChange(STANDARD_WIDTH),
                getYResolutionChange(STANDARD_HEIGHT), WindowManager.LayoutParams.TYPE_BOOT_PROGRESS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        mBackgroundLayout = new FrameLayout(context);
        params.width = getXResolutionChange(STANDARD_WIDTH);
        params.height = getYResolutionChange(STANDARD_HEIGHT);
        mBackgroundLayout.setBackgroundColor(0xee000000);
        mBackgroundLayout.setFitsSystemWindows(true);
        mBackgroundLayout.setClickable(true);
        mBackgroundLayout.setVisibility(View.GONE);
        mWindowManager.addView(mBackgroundLayout, params);

        mPopupLayout = new FrameLayout(context);
        params.width = getXResolutionChange(634);
        params.height = getYResolutionChange(333);
        mPopupLayout.setX(getXResolutionChange(195));
        mPopupLayout.setY(getYResolutionChange(134));
        mPopupLayout.setBackgroundResource(R.drawable.open_connect_popup);
        mBackgroundLayout.addView(mPopupLayout, params);

        mTitleText = new TextView(context);
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = getYResolutionChange(74);
        mTitleText.setX(getXResolutionChange(0));
        mTitleText.setY(getYResolutionChange(3));
        mTitleText.setText(context.getResources().getString(R.string.popup_addnetwork_title));
        mTitleText.setTextColor(0xffffffff);
        mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mTitleText.setGravity(Gravity.CENTER);
        mTitleText.setSingleLine();
        mTitleText.setEllipsize(TextUtils.TruncateAt.END);
        mPopupLayout.addView(mTitleText, params);

        mSsidTextView = new TextView(context);
        params.width = getXResolutionChange(260);
        params.height = getYResolutionChange(88);
        mSsidTextView.setX(getXResolutionChange(43));
        mSsidTextView.setY(getYResolutionChange(77));
        mSsidTextView.setTextColor(0xffffffff);
        mSsidTextView.setText(context.getResources().getString(R.string.popup_addnetwork_message));
        mSsidTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mSsidTextView.setGravity(Gravity.CENTER_VERTICAL);
        mPopupLayout.addView(mSsidTextView, params);

        mSsidInputTextView = new TextView(context);
        params.width = getXResolutionChange(325);
        params.height = getYResolutionChange(88);
        mSsidInputTextView.setX(getXResolutionChange(303));
        mSsidInputTextView.setY(getYResolutionChange(77));
        mSsidInputTextView.setTextColor(0xffffffff);
        mSsidInputTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mSsidInputTextView.setGravity(Gravity.CENTER_VERTICAL);
        mSsidInputTextView.setBackgroundColor(Color.TRANSPARENT);
        mSsidInputTextView.setSingleLine();
        mSsidInputTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onCallKeypad(KEYPAD_TYPE_SSID);
                }
            }
        });
        mPopupLayout.addView(mSsidInputTextView, params);

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
        mPskTypeText.setText(context.getResources().getString(R.string.wifi_security_open));
        mPskTypeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mPskTypeText.setGravity(Gravity.CENTER_VERTICAL);
        mPskTypeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    if(mDropButtonBackgroundLayout.getVisibility() != View.VISIBLE)
                        mDropButtonBackgroundLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mPopupLayout.addView(mPskTypeText, params);

        ImageButton dropButton = new ImageButton(context);
        params.width = getXResolutionChange(22);
        params.height = getYResolutionChange(15);
        dropButton.setX(getXResolutionChange(569));
        dropButton.setY(getYResolutionChange(202));
        dropButton.setImageResource(R.drawable.img_arrow);
        dropButton.setBackgroundColor(Color.TRANSPARENT);
        mPopupLayout.addView(dropButton, params);

        mPasswordLayout = new FrameLayout(context);
        params.width = getXResolutionChange(628);
        params.height = getYResolutionChange(178);
        mPasswordLayout.setY(getYResolutionChange(88+165));
        mPasswordLayout.setVisibility(View.GONE);
        mPopupLayout.addView(mPasswordLayout, params);

        View line2 = new View(context);
        params.width = getXResolutionChange(628);
        params.height = 1;
        line2.setX(getXResolutionChange(4));
        line2.setY(getYResolutionChange(0));
        line2.setBackgroundColor(0xff3b3b3b);
        mPasswordLayout.addView(line2, params);

        mPasswordTitleText = new TextView(context);
        params.width = getXResolutionChange(260);
        params.height = getYResolutionChange(88);
        mPasswordTitleText.setX(getXResolutionChange(43));
        mPasswordTitleText.setY(getYResolutionChange(1));
        mPasswordTitleText.setTextColor(0xffffffff);
        mPasswordTitleText.setText(context.getResources().getString(R.string.popup_title_password));
        mPasswordTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mPasswordTitleText.setGravity(Gravity.CENTER_VERTICAL);
        mPasswordLayout.addView(mPasswordTitleText, params);

        mPasswordText = new TextView(context);
        params.width = getXResolutionChange(325);
        params.height = getYResolutionChange(88);
        mPasswordText.setX(getXResolutionChange(298));
        mPasswordText.setY(getYResolutionChange(1));
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
                    mOnClickListener.onCallKeypad(KEYPAD_TYPE_PASSWORD);
                }
            }
        });
        mPasswordLayout.addView(mPasswordText, params);

        View line3 = new View(context);
        params.width = getXResolutionChange(628);
        params.height = 1;
        line3.setX(getXResolutionChange(4));
        line3.setY(getYResolutionChange(89));
        line3.setBackgroundColor(0xff3b3b3b);
        mPasswordLayout.addView(line3, params);

        int checkboxPadding = getYResolutionChange(20);
        mNotiPasswordCheckBox = new CheckBox(context);
        params.width = getXResolutionChange(76);
        params.height = getYResolutionChange(76);
        mNotiPasswordCheckBox.setX(getXResolutionChange(23));
        mNotiPasswordCheckBox.setY(getYResolutionChange(96));
        mNotiPasswordCheckBox.setPadding(checkboxPadding, checkboxPadding, checkboxPadding, checkboxPadding);
        mNotiPasswordCheckBox.setOnCheckedChangeListener(mCheckedChacgedListener);
        mNotiPasswordCheckBox.setButtonDrawable(R.drawable.checkbox_selector);
        mPasswordLayout.addView(mNotiPasswordCheckBox, params);

        TextView showPasswordText = new TextView(context);
        params.width = getXResolutionChange(395);
        params.height = getYResolutionChange(88);
        showPasswordText.setX(getXResolutionChange(93));
        showPasswordText.setY(getYResolutionChange(90));
        showPasswordText.setTextColor(0xffffffff);
        showPasswordText.setText(context.getResources().getString(R.string.popup_title_show_password));
        showPasswordText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28.0f * yRate);
        showPasswordText.setGravity(Gravity.CENTER_VERTICAL);
        mPasswordLayout.addView(showPasswordText, params);

        mButtonLayout = new FrameLayout(context);
        params.width = getXResolutionChange(624);
        params.height = getYResolutionChange(74);
        mButtonLayout.setX(getXResolutionChange(4));
        mButtonLayout.setY(getYResolutionChange(255));
        mButtonLayout.setBackgroundColor(Color.BLACK);
        mPopupLayout.addView(mButtonLayout, params);

        mYesButton = new ImageButton(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        mYesButton.setImageResource(R.drawable.btn_popup_selector);
        mYesButton.setBackgroundColor(Color.TRANSPARENT);
        mYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onYes(mSsidInputTextView.getText().toString(), "",mPskType);
                } else {
                    dismiss();
                }
            }
        });
        mButtonLayout.addView(mYesButton, params);

        mYesText = new TextView(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        mYesText.setTextColor(0xffffffff);
        mYesText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28 * yRate);
        mYesText.setText(context.getResources().getString(R.string.popup_connect));
        mYesText.setGravity(Gravity.CENTER);
        mButtonLayout.addView(mYesText, params);

        ImageButton noButton = new ImageButton(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        noButton.setX(getXResolutionChange(313));
        noButton.setImageResource(R.drawable.btn_popup_selector);
        noButton.setBackgroundColor(Color.TRANSPARENT);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onNo();
                } else {
                    dismiss();
                }
            }
        });
        mButtonLayout.addView(noButton, params);

        TextView noText = new TextView(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        noText.setX(getXResolutionChange(313));
        noText.setTextColor(0xffffffff);
        noText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28 * yRate);
        noText.setText(context.getResources().getString(R.string.popup_cancel));
        noText.setGravity(Gravity.CENTER);
        mButtonLayout.addView(noText, params);

        mDropButtonBackgroundLayout = new FrameLayout(context);
        params.width = getXResolutionChange(STANDARD_WIDTH);
        params.height = getYResolutionChange(STANDARD_HEIGHT);
        mDropButtonBackgroundLayout.setBackgroundColor(0xee000000);
        mDropButtonBackgroundLayout.setClickable(true);
        mDropButtonBackgroundLayout.setVisibility(View.GONE);
        mDropButtonBackgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDropButtonBackgroundLayout.setVisibility(View.GONE);
            }
        });
        mBackgroundLayout.addView(mDropButtonBackgroundLayout, params);

        mDropButtonLayout = new FrameLayout(context);
        params.width = getXResolutionChange(300);
        params.height = getYResolutionChange(198);
        mDropButtonLayout.setX(getXResolutionChange(519));
        mDropButtonLayout.setY(getYResolutionChange(311));
        mDropButtonLayout.setBackgroundResource(R.drawable.bg_menu_popup);
        mDropButtonBackgroundLayout.addView(mDropButtonLayout, params);

        Button openButton = new Button(context);
        params.width = getXResolutionChange(298);
        params.height = getYResolutionChange(66);
        openButton.setX(getXResolutionChange(0));
        openButton.setY(getYResolutionChange(0));
        openButton.setBackgroundResource(R.drawable.btn_popup_s);
        openButton.setPadding(getXResolutionChange(22), 0, 0, 0);
        openButton.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        openButton.setText(context.getResources().getString(R.string.wifi_security_open));
        openButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26.0f * yRate);
        openButton.setTextColor(0xffffffff);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPskType(WifiListItem.SECURITY_NONE);
                mDropButtonBackgroundLayout.setVisibility(View.GONE);
            }
        });
        mDropButtonLayout.addView(openButton, params);

        View dropLine1 = new View(context);
        params.width = getXResolutionChange(294);
        params.height = 1;
        dropLine1.setX(getXResolutionChange(2));
        dropLine1.setY(getYResolutionChange(66));
        dropLine1.setBackgroundColor(0xff333333);
        mDropButtonLayout.addView(dropLine1, params);

        Button wepButton = new Button(context);
        params.width = getXResolutionChange(298);
        params.height = getYResolutionChange(64);
        wepButton.setX(getXResolutionChange(0));
        wepButton.setY(getYResolutionChange(67));
        wepButton.setBackgroundResource(R.drawable.btn_popup_s);
        wepButton.setPadding(getXResolutionChange(22), 0, 0, 0);
        wepButton.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        wepButton.setText(context.getResources().getString(R.string.wifi_security_wep));
        wepButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26.0f * yRate);
        wepButton.setTextColor(0xffffffff);
        wepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPskType(WifiListItem.SECURITY_WEP);
                mDropButtonBackgroundLayout.setVisibility(View.GONE);
            }
        });
        mDropButtonLayout.addView(wepButton, params);

        View dropLine2 = new View(context);
        params.width = getXResolutionChange(294);
        params.height = 1;
        dropLine2.setX(getXResolutionChange(2));
        dropLine2.setY(getYResolutionChange(131));
        dropLine2.setBackgroundColor(0xff333333);
        mDropButtonLayout.addView(dropLine2, params);

        Button wpaButton = new Button(context);
        params.width = getXResolutionChange(298);
        params.height = getYResolutionChange(64);
        wpaButton.setX(getXResolutionChange(0));
        wpaButton.setY(getYResolutionChange(132));
        wpaButton.setBackgroundResource(R.drawable.btn_popup_s);
        wpaButton.setPadding(getXResolutionChange(22), 0, 0, 0);
        wpaButton.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        wpaButton.setText(context.getResources().getString(R.string.wifi_security_wpa_wpa2));
        wpaButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26.0f * yRate);
        wpaButton.setTextColor(0xffffffff);
        wpaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPskType(WifiListItem.SECURITY_PSK);
                mDropButtonBackgroundLayout.setVisibility(View.GONE);
            }
        });
        mDropButtonLayout.addView(wpaButton, params);

        setSsidText(null);
        setPskType(WifiListItem.SECURITY_NONE);
    }

    public void show() {
        if(mBackgroundLayout != null) mBackgroundLayout.setVisibility(View.VISIBLE);
    }
    public void hide() {
        if(mBackgroundLayout != null) {
            mBackgroundLayout.setVisibility(View.INVISIBLE);
            mDropButtonBackgroundLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void dismiss() {
        if(mBackgroundLayout != null && mWindowManager != null) {
            mBackgroundLayout.removeAllViews();
            mBackgroundLayout.setVisibility(View.GONE);
            mDropButtonBackgroundLayout.setVisibility(View.GONE);
            mWindowManager.removeView(mBackgroundLayout);
            mBackgroundLayout = null;
            mWindowManager = null;
        }
    }

    public boolean isShowing() {
        return (View.VISIBLE == mBackgroundLayout.getVisibility());
    }

    private int getXResolutionChange(float x) {
        return (int) (x * yRate);
    }

    private int getYResolutionChange(float y) {
        return (int) (y * yRate);
    }

    public void setSsidText(String text) {
        if(text != null && !text.isEmpty()) {
            mSsidInputTextView.setText(text);
        } else {
            mSsidInputTextView.setText("");
        }
        enableConnectButton();
    }
    public void setPasswordText(String msg) {
        int msgLength = msg.length();
        if (msgLength > 63) {
            msg = msg.substring(0, 63);
        }
        mPasswordText.setText(msg);
        enableConnectButton();
    }

    private void enableConnectButton() {
        String ssid = mSsidInputTextView.getText().toString();
        String password = mPasswordText.getText().toString();

        if(mPskType == WifiListItem.SECURITY_NONE && !ssid.isEmpty()) {
            setClickableConnectButton(true);
        } else {
            if(password.length() >= 8 && !ssid.isEmpty()) {
                setClickableConnectButton(true);
            } else {
                setClickableConnectButton(false);
            }
        }
    }

    private void setClickableConnectButton(boolean enable) {
        if(enable) {
            mYesText.setTextColor(0xffffffff);
            mYesButton.setClickable(true);
            mYesButton.setImageResource(R.drawable.btn_popup_selector);
        } else {
            mYesText.setTextColor(0xff404040);
            mYesButton.setClickable(false);
            mYesButton.setImageResource(R.drawable.btn_popup_n);
        }
    }
    public void setWifiItem(WifiListItem item) {
        if(item != null) {
            mPskTypeText.setText(item.getSecurityString(true));
        }
    }
    public void setPskType(int pskType) {
        mPskType = pskType;
        switch (pskType) {
            case WifiListItem.SECURITY_NONE:
                mPskTypeText.setText(mContext.getResources().getString(R.string.wifi_security_open));
                setSecurityMode(false);
                break;
            case WifiListItem.SECURITY_WEP:
                mPskTypeText.setText(mContext.getResources().getString(R.string.wifi_security_wep));
                setSecurityMode(true);
                break;
            case WifiListItem.SECURITY_PSK:
                mPskTypeText.setText(mContext.getResources().getString(R.string.wifi_security_wpa_wpa2));
                setSecurityMode(true);
                break;
            default:
                break;
        }
        enableConnectButton();
    }

    public void setSecurityMode(boolean security) {
        if(security) {
            ViewGroup.LayoutParams params = mPopupLayout.getLayoutParams();
            params.height = getYResolutionChange(510);
            mPopupLayout.setY(getYResolutionChange(45));
            mPopupLayout.setBackgroundResource(R.drawable.connect_popup);
            mPopupLayout.setLayoutParams(params);
            mButtonLayout.setY(getYResolutionChange(255+178));
            mDropButtonLayout.setY(getYResolutionChange(311-90));
            mPasswordLayout.setVisibility(View.VISIBLE);
        }else {
            ViewGroup.LayoutParams params = mPopupLayout.getLayoutParams();
            params.height = getYResolutionChange(333);
            mPopupLayout.setY(getYResolutionChange(134));
            mPopupLayout.setBackgroundResource(R.drawable.open_connect_popup);
            mPopupLayout.setLayoutParams(params);
            mButtonLayout.setY(getYResolutionChange(255));
            mDropButtonLayout.setY(getYResolutionChange(311));
            mPasswordLayout.setVisibility(View.GONE);
        }
    }
    public boolean getShowPassword() {
        return mNotiPasswordCheckBox.isChecked();
    }
    private CompoundButton.OnCheckedChangeListener mCheckedChacgedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean value) {
            if (button == mNotiPasswordCheckBox) {
                if(value) {
                    mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    mPasswordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        }
    };

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
}
