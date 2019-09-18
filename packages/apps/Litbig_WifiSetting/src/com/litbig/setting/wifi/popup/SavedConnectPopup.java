package com.litbig.setting.wifi.popup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.litbig.setting.wifi.R;
import com.litbig.setting.wifi.WifiListItem;

public class SavedConnectPopup {
    private final float STANDARD_WIDTH = 1024f;
    private final float STANDARD_HEIGHT = 600f;

    public static interface OnClickListener {
        public void onYes();
        public void onNo();
    }

    private WindowManager mWindowManager;
    private OnClickListener mOnClickListener;
    private Context mContext;

    private FrameLayout mBackgroundLayout;
    private FrameLayout mPopupLayout;
    private TextView mSignalLevelText;
    private TextView mPskTypeText;
    private TextView mYesText;
    private FrameLayout mButtonLayout;

    private float mRateX;
    private float mRateY;
    private TextView mTitleText;

    public SavedConnectPopup(Context context) {
        mContext = context;

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
        params.height = getYResolutionChange(STANDARD_HEIGHT);
        mBackgroundLayout.setBackgroundColor(0xee000000);
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
        mTitleText.setTextColor(0xffffffff);
        mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mTitleText.setGravity(Gravity.CENTER);
        mTitleText.setSingleLine();
        mTitleText.setEllipsize(TextUtils.TruncateAt.END);
        mPopupLayout.addView(mTitleText, params);

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
        mPskTypeText.setText(context.getResources().getString(R.string.wifi_security_open));
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

        mButtonLayout = new FrameLayout(context);
        params.width = getXResolutionChange(624);
        params.height = getYResolutionChange(74);
        mButtonLayout.setX(getXResolutionChange(4));
        mButtonLayout.setY(getYResolutionChange(255));
        mButtonLayout.setBackgroundColor(Color.BLACK);
        mPopupLayout.addView(mButtonLayout, params);

        ImageButton yesButton = new ImageButton(context);
        params.width = getXResolutionChange(311);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        yesButton.setImageResource(R.drawable.btn_popup_selector);
        yesButton.setBackgroundColor(Color.TRANSPARENT);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onYes();
                } else {
                    dismiss();
                }
            }
        });
        mButtonLayout.addView(yesButton, params);

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
    }

    public void show() {
        if(mBackgroundLayout != null) mBackgroundLayout.setVisibility(View.VISIBLE);
    }
    public void hide() {
        if(mBackgroundLayout != null) mBackgroundLayout.setVisibility(View.INVISIBLE);
    }

    public void dismiss() {
        if(mBackgroundLayout != null && mWindowManager != null) {
            mBackgroundLayout.removeAllViews();
            mBackgroundLayout.setVisibility(View.GONE);
            mWindowManager.removeView(mBackgroundLayout);
            mBackgroundLayout = null;
            mWindowManager = null;
        }
    }
    private int getXResolutionChange(float x) {
        return (int) (x * mRateX);
    }

    private int getYResolutionChange(float y) {
        return (int) (y * mRateY);
    }

    public void setWifiListItem(WifiListItem item) {
        if(item != null) {
            int level = item.getLevel();
            String[] signal = mContext.getResources().getStringArray(R.array.wifi_signal);
            if(level > -1) mSignalLevelText.setText(signal[level]);
            else mSignalLevelText.setText(signal[0]);
            mTitleText.setText(item.getSsid());
            mPskTypeText.setText(item.getSecurityString(false));
        }
    }
    public void setYesText(String yes) {
        mYesText.setText(yes);
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
}
