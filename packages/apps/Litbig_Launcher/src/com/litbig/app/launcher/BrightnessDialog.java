package com.litbig.app.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrightnessDialog extends Dialog{

	// messages for handler
	private final static int MSG_SET_PANEL_HIDE = 10;
	
	private final static int PANEL_AUTO_HIDE_DELAY = 4 * 1000;
	
	private final String FILE_PATH_BACKLIGHT_BRIGHTNESS = "/sys/devices/backlight/leds/lcd-backlight/brightness";
	
	private Context mContext;
	private SeekBar mSbBrightness;
	private TextView mTvCurBrightness;
	
	public BrightnessDialog(Context context) {
		super(context);
		
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.y = 31;
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.brightness_dialog);
		
		mSbBrightness = findViewById(R.id.sb_brightness);
		mTvCurBrightness = findViewById(R.id.tv_cur_brightness);
		
		SeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener();
		mSbBrightness.setOnSeekBarChangeListener(seekBarChangeListener);
	}
	
	@Override
	public void show() {
		super.show();
		
		updateView();

		if (mHandler.hasMessages(MSG_SET_PANEL_HIDE))
			mHandler.removeMessages(MSG_SET_PANEL_HIDE);

		mHandler.sendEmptyMessageDelayed(MSG_SET_PANEL_HIDE, PANEL_AUTO_HIDE_DELAY);
	}
	
	@Override
	public void hide() {
		super.hide();
	}

	class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int value, boolean byUser) {
			// TODO Auto-generated method stub
			
			if (byUser) {
				boolean isNight = isAutoBrightnessEnable() && 
						(getCurTimeHour() >= getNightStartTime() || getCurTimeHour() < getNightFinishTime());
				
				setBrightness(value, !isNight);
				
				if (isNight)
					saveNightBrightness(value);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

			if (mHandler.hasMessages(MSG_SET_PANEL_HIDE))
				mHandler.removeMessages(MSG_SET_PANEL_HIDE);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

			if (mHandler.hasMessages(MSG_SET_PANEL_HIDE))
				mHandler.removeMessages(MSG_SET_PANEL_HIDE);
			
			mHandler.sendEmptyMessageDelayed(MSG_SET_PANEL_HIDE, PANEL_AUTO_HIDE_DELAY);
		}
	}
	
	private boolean isAutoBrightnessEnable() {
		return SystemProperties.getBoolean("persist.sys.auto.brightness", false);
	}
	
	private void saveNightBrightness(int value) {
		SystemProperties.set("persist.sys.night.brightness", "" + value);
	}
	
	private int getNightStartTime() {
		return SystemProperties.getInt("persist.sys.night.start", 18);
	}

	private int getNightFinishTime() {
		return SystemProperties.getInt("persist.sys.night.finish", 8);
	}

	private int getCurTimeHour() {
		Calendar cal = Calendar.getInstance();

		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	private void setBrightness(int brightness, boolean save) {
		int value = brightness * 17;
		
		if(brightness <= 0)
		    value = 5;
		else if (brightness > 15) 
		    value = 255;

		if (save) {
			android.provider.Settings.System.putInt(getContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, value);
		}
		else {
			FileUtil.writeFile(new File(FILE_PATH_BACKLIGHT_BRIGHTNESS), String.valueOf(value));
		}
		
		mTvCurBrightness.setText(String.valueOf(brightness));
	}
	
	private int getCurBrightness() {
		String brightness = FileUtil.readFile(new File(FILE_PATH_BACKLIGHT_BRIGHTNESS));

		if (brightness == null)
			brightness = "0";

		return Integer.parseInt(brightness) / 17;
	}
	
	private void updateView() {
		int curBrightness = getCurBrightness();
		
		mSbBrightness.setProgress(curBrightness);
		mTvCurBrightness.setText(String.valueOf(curBrightness));
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SET_PANEL_HIDE:
				cancel();
				break;
			}
		}
	};
}
