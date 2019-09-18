package com.litbig.app.launcher;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;

public class BrightnessChangeHandler extends Handler{
	
	private final int AUTO_BRIGHTNESS_CHECK_DELAY = 60 * 1000;
	
	private Context mContext;
	
	public BrightnessChangeHandler(Context context) {
		mContext = context;
	}
	
	void resume() {
		if (hasMessages(0)) {
			removeMessages(0);
		}

		sendEmptyMessage(0);
	}

	void pause() {
		removeMessages(0);
	}

	@Override
	public void handleMessage(Message msg) {
		if (SystemProperties.get("sys.screen.lock", "0").equals("1"))
			return ;

		int brightness = getNormalBrightness() * 17;

		if (isAutoBrightnessEnable()) {

			if ((getCurTimeHour() >= getNightStartTime()) || 
					(getCurTimeHour() < getNightFinishTime())) {
				// night mode
				brightness = getNightBrightness();

				if (brightness == 0)
					brightness = 5;
				else
					brightness = brightness * 17;
			}
		}

		if (brightness <= 0)
			brightness = 5;

		// change backlight brightness
		try {
			Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", "echo " + brightness + " > /sys/devices/backlight/leds/lcd-backlight/brightness"});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sendEmptyMessageDelayed(0, AUTO_BRIGHTNESS_CHECK_DELAY);
	}

	private boolean isAutoBrightnessEnable() {
		return SystemProperties.getBoolean("persist.sys.auto.brightness", false);
	}

	private int getNightBrightness() {
		return SystemProperties.getInt("persist.sys.night.brightness", getNormalBrightness());
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

	private int getNormalBrightness() {
		int brightness = 0;

		try {
			brightness = android.provider.Settings.System.getInt(mContext.getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}

		return brightness / 17;
	}
}
