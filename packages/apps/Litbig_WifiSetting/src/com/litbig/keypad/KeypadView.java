package com.litbig.keypad;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.litbig.setting.wifi.R;

public class KeypadView {
    private final float STANDARD_WIDTH = 1024f;
    private final float STANDARD_HEIGHT = 600f;

    public final int KEYPAD_MODE_ENGLISH = 0;
    public final int KEYPAD_MODE_ENGLISH_SHIFT = 1;
    public final int KEYPAD_MODE_SPECIAL_NUM = 2;
    public final int KEYPAD_MODE_SPECIAL = 3;

    public static interface OnClickListener {
        public void onSave(String msg);
        public void onBack();
    }

    private LinearLayout mKeyPadLayout;
    private Activity mActivity;
    private OnClickListener mOnClickListener;
    private WindowManager mWindowManager;

    private EditText mInputTextView;
    private TextView mTopTextView;
    private ImageButton mBackButton;
    private GridLayout mKeypadEnglish;
    private GridLayout mKeypadEnglishShift;
    private GridLayout mKeypadSpecialNumber;
    private GridLayout mKeypadSpecial;
    private Button[] mKeyButton = new Button[124];

    private String mInputString = "";

    public KeypadView(Activity activity) {
        mActivity = activity;
        onCreate();
    }

    public void showKeypadView(boolean show) {
        if (show) {
            mKeyPadLayout.setVisibility(View.VISIBLE);
        } else {
            mKeyPadLayout.setVisibility(View.GONE);
            mInputString = "";
            mInputTextView.setText(mInputString);
        }
    }

    public boolean isKeypadMode() {
        if (mKeyPadLayout.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    private void onCreate() {
        mWindowManager = (WindowManager) mActivity.getSystemService(android.content.Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(metrics);
        float xRate = (float)metrics.widthPixels / STANDARD_WIDTH;
        float yRate = (float)metrics.heightPixels / STANDARD_HEIGHT;

        mKeyPadLayout = mActivity.findViewById(R.id.keypad_layout);
        mKeyPadLayout.setOnTouchListener(mTouchListener);
        mTopTextView = mKeyPadLayout.findViewById(R.id.app_name);
        mTopTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 36.0f * yRate);
        mInputTextView = mKeyPadLayout.findViewById(R.id.keypad_input_textview);
        mInputTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * yRate);
        mBackButton = mKeyPadLayout.findViewById(R.id.btn_back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnClickListener != null) {
                    mOnClickListener.onBack();
                }
                showKeypadView(false);
            }
        });

        mKeypadEnglish = mActivity.findViewById(R.id.keypad_key_english);
        mKeypadEnglish.setVisibility(View.VISIBLE);
        mKeypadEnglishShift = mActivity.findViewById(R.id.keypad_key_english_shift);
        mKeypadSpecialNumber = mActivity.findViewById(R.id.keypad_key_symbol_number);
        mKeypadSpecial = mActivity.findViewById(R.id.keypad_key_symbol);

        for(int i=0; i< mKeyButton.length; i++){
            int k = mActivity.getResources().getIdentifier("key_"+i, "id", mActivity.getPackageName());
            mKeyButton[i] = mKeyPadLayout.findViewById(k);
            mKeyButton[i].setOnClickListener(mKeyClickListener);
            mKeyButton[i].setOnLongClickListener(mKeyLongClickListener);
        }
    }

    public void onPause() {
        mInputString = "";
        showKeypadView(false);
    }

    public void setInputText(String text) {
        mInputString = text;
        mInputTextView.setText(text);
        int position = mInputTextView.length();
        Editable etext = mInputTextView.getText();
        Selection.setSelection(etext, position);
    }
    public void setTopText(String text) {
        mTopTextView.setText(text);
    }
    public void setInputHint(String text) {
        mInputTextView.setHint(text);
    }
    public void setInputBoxType(int type) {
        mInputTextView.setInputType(type);
    }
    public void setKeypad(int mode) {
        mKeypadEnglish.setVisibility(View.GONE);
        mKeypadEnglishShift.setVisibility(View.GONE);
        mKeypadSpecialNumber.setVisibility(View.GONE);
        mKeypadSpecial.setVisibility(View.GONE);
        switch (mode) {
            case KEYPAD_MODE_ENGLISH_SHIFT:
                mKeypadEnglishShift.setVisibility(View.VISIBLE);
                break;
            case KEYPAD_MODE_SPECIAL_NUM:
                mKeypadSpecialNumber.setVisibility(View.VISIBLE);
                break;
            case KEYPAD_MODE_SPECIAL:
                mKeypadSpecial.setVisibility(View.VISIBLE);
                break;
            case KEYPAD_MODE_ENGLISH:
            default:
                mKeypadEnglish.setVisibility(View.VISIBLE);
                break;
        }
    }
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    private View.OnClickListener mKeyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button keyButton = (Button) v;
            String code = keyButton.getText().toString();
            if(code.length() == 1) {
                mInputString += code;
            } else {
                switch (code) {
                    case "del":
                        mLongClickHandler.removeMessages(START_DEL_KEY);
                        if (mInputString.length() > 0)
                            mInputString = mInputString.substring(0, mInputString.length() - 1);
                        break;
                    case "space":
                        mInputString += " ";
                        break;
                    case "save":
                        if (mOnClickListener != null) mOnClickListener.onSave(mInputString);
                        showKeypadView(false);
                        break;
                    case "lower":
                        setKeypad(KEYPAD_MODE_ENGLISH);
                        break;
                    case "upper":
                        setKeypad(KEYPAD_MODE_ENGLISH_SHIFT);
                        break;
                    case "numspc":
                        setKeypad(KEYPAD_MODE_SPECIAL_NUM);
                        break;
                    case "sp":
                        setKeypad(KEYPAD_MODE_SPECIAL);
                        break;
                }
            }
            setInputText(mInputString);
        }
    };
    private View.OnLongClickListener mKeyLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Button keyButton = (Button) v;
            String code = keyButton.getText().toString();
            if(code.length() == 1) {
            } else {
                switch (code) {
                    case "del":
                        mLongClickHandler.sendEmptyMessage(START_DEL_KEY);
                        break;
                    case "space":
                    case "lower":
                    case "sp":
                    case "numspc":
                    case "upper":
                    case "save":
                        break;
                }
            }
            return false;
        }
    };

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    public final int START_DEL_KEY = 1000;
    public final int INTEVAL_DEL_KEY = 100;
    private Handler mLongClickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_DEL_KEY:
                    if(mInputString.length() > 0)
                        mInputString = mInputString.substring(0, mInputString.length()-1);
                    setInputText(mInputString);
                    mLongClickHandler.sendEmptyMessageDelayed(START_DEL_KEY, INTEVAL_DEL_KEY);
                    break;
                default:
                    break;
            }
        }
    };
}
