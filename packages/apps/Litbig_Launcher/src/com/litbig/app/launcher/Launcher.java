package com.litbig.app.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.Manifest;
import android.annotation.NonNull;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.litbig.app.music.activity.MusicActivity;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.util.Log;
import com.litbig.app.music.util.MusicUtils;
import com.litbig.mediastorage.MediaScanReceiver;
import com.litbig.mediastorage.MediaStorage;
import android.provider.Settings;


public class Launcher extends MusicActivity {
	private final String FILE_PATH_LCD_BRIGHTNESS 	= "/sys/class/tcc_dispman/tcc_dispman/color_enhance_lcd_brightness";
	private final String FILE_PATH_LCD_CONTRAST 	= "/sys/class/tcc_dispman/tcc_dispman/color_enhance_lcd_contrast";
	private final String FILE_PATH_LCD_HUE 			= "/sys/class/tcc_dispman/tcc_dispman/color_enhance_lcd_hue";

	private final int PLAY_TIME_MAX_LENGTH = 312;

	private ImageButton mBtnApps, mBtnAudio, mBtnDmb, mBtnImage, mBtnMiracast,
	mBtnAirplay, mBtnVideo, mBtnSetting, mBtnWifi, mBtnBluetooth, mBtnBrightness;

	private int mWifiState;

	private ImageView mIvAlbumCover;
	private RelativeLayout mRlAudioControl;
	private FrameLayout mFlAudioTime;
	private ImageButton mBtnAudioPrev, mBtnAudioPlay, mBtnAudioPause, mBtnAudioNext;
	private TextView mTvAudioTitle;

	private TextView mTvTime, mTvTimeDivision;
	private TextView mTvDate, mTvDay;

	private Context mContext;

	private final String AUDIO_APPLICATION_PACKAGE_NAME 	= "com.litbig.app.music";
	private final String DMB_APPLICATION_PACKAGE_NAME 		= "com.litbig.app.dmb";
	private final String IMAGE_APPLICATION_PACKAGE_NAME 	= "com.litbig.app.photo";
	private final String MIRACAST_APPLICATION_PACKAGE_NAME 	= "com.litbig.wfd";
	private final String AIRPLAY_APPLICATION_PACKAGE_NAME 	= "com.litbig.airplay";
	private final String VIDEO_APPLICATION_PACKAGE_NAME 	= "com.litbig.app.movie";
	private final String SETTING_APPLICATION_PACKAGE_NAME 	= "com.litbig.app.setting";

	private boolean mWifiProcessing, mBluetoothProcessing;
	private int mPlayingDuration = 0;

	private Toast mToast = null;
//	private BrightnessChangeHandler mAutoBrightnessHandler;

//	private BrightnessDialog mBrightnessDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("onCreate");

		mContext = this;

		setContentView(R.layout.main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		checkPermission();

		mBtnApps		= findViewById(R.id.btn_apps);
		mBtnAudio		= findViewById(R.id.btn_audio);
		mBtnDmb			= findViewById(R.id.btn_dmb);
		mBtnImage		= findViewById(R.id.btn_image);
		mBtnMiracast	= findViewById(R.id.btn_miracast);
		mBtnAirplay		= findViewById(R.id.btn_airplay);
		mBtnVideo		= findViewById(R.id.btn_video);
		mBtnBrightness	= findViewById(R.id.btn_brightness);

		mIvAlbumCover		= findViewById(R.id.iv_album_cover);
		mRlAudioControl		= findViewById(R.id.control_audio);
		mFlAudioTime		= findViewById(R.id.progress_audio_time);
		mBtnAudioPrev		= findViewById(R.id.btn_audio_prev);
		mBtnAudioPlay		= findViewById(R.id.btn_audio_play);
		mBtnAudioPause		= findViewById(R.id.btn_audio_pause);
		mBtnAudioNext		= findViewById(R.id.btn_audio_next);
		mTvAudioTitle		= findViewById(R.id.tv_audio_title);

		mTvTime 			= findViewById(R.id.tv_time);
		mTvTimeDivision 	= findViewById(R.id.tv_time_division);
		mTvDate 			= findViewById(R.id.tv_date);
		mTvDay 				= findViewById(R.id.tv_day);

		mBtnSetting 	= findViewById(R.id.btn_setting);
		mBtnWifi		= findViewById(R.id.btn_wifi);
		mBtnBluetooth	= findViewById(R.id.btn_bluetooth);

		BtnClickListener btnClickListener = new BtnClickListener();
		mBtnApps.setOnClickListener(btnClickListener);
		mBtnAudio.setOnClickListener(btnClickListener);
		mBtnDmb.setOnClickListener(btnClickListener);
		mBtnImage.setOnClickListener(btnClickListener);
		mBtnMiracast.setOnClickListener(btnClickListener);
		mBtnAirplay.setOnClickListener(btnClickListener);
		mBtnVideo.setOnClickListener(btnClickListener);
		mBtnBrightness.setOnClickListener(btnClickListener);

		mBtnAudioPrev.setOnClickListener(btnClickListener);
		mBtnAudioPlay.setOnClickListener(btnClickListener);
		mBtnAudioPause.setOnClickListener(btnClickListener);
		mBtnAudioNext.setOnClickListener(btnClickListener);

		mBtnSetting.setOnClickListener(btnClickListener);
		mBtnWifi.setOnClickListener(btnClickListener);
		mBtnBluetooth.setOnClickListener(btnClickListener);

		BtnLongClickListener btnLongClickListener = new BtnLongClickListener();

		mBtnWifi.setOnLongClickListener(btnLongClickListener);
		mBtnBluetooth.setOnLongClickListener(btnLongClickListener);

		mRlAudioControl.setVisibility(View.GONE);

		IntentFilter wifiFilter = new IntentFilter();
		wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		wifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mWifiIntentReceiver, wifiFilter);

		IntentFilter mediaFilter = new IntentFilter();
		mediaFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_PREPARED);
		mediaFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
		mediaFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_EJECT);
		registerReceiver(mMediaIntentReceiver, mediaFilter);

		IntentFilter btFilter = new IntentFilter();
		btFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		btFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		btFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(mBtIntentReceiver, btFilter);

		IntentFilter tzFilter = new IntentFilter();
		tzFilter.addAction(Intent.ACTION_TIME_TICK);
		tzFilter.addAction(Intent.ACTION_TIME_CHANGED);
		tzFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(mTimeIntentReceiver, tzFilter);


//		mAutoBrightnessHandler = new BrightnessChangeHandler(mContext);
//		mBrightnessDialog = new BrightnessDialog(mContext);

		mWifiProcessing = false;
		mBluetoothProcessing = false;

		applyDisplaySetting();

		try {
			Settings.Secure.putString(mContext.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS,
					"com.litbig.app.keyboard/.SoftKeyboard");
			Settings.Secure.putString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD,
					"com.litbig.app.keyboard/.SoftKeyboard");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void applyDisplaySetting() {

		int brightness = SystemProperties.getInt("persist.sys.lcd.brightness", -1);
		int contrast = SystemProperties.getInt("persist.sys.lcd.contrast", -1);
		int hue = SystemProperties.getInt("persist.sys.lcd.hue", -1);

		if (brightness != -1) {
			writeFile(new File(FILE_PATH_LCD_BRIGHTNESS), String.valueOf(brightness));
		}

		if (contrast != -1) {
			writeFile(new File(FILE_PATH_LCD_CONTRAST), String.valueOf(contrast));
		}

		if (hue != -1) {
			writeFile(new File(FILE_PATH_LCD_HUE), String.valueOf(hue));
		}
	}

	private boolean writeFile(File file, String content) {

		if (file != null && file.exists() && content != null) {

			try {
				BufferedWriter bufferdWriter = new BufferedWriter(new FileWriter(file));

				try {
					bufferdWriter.write(content);
					bufferdWriter.flush();
					bufferdWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}

		return false;
	}

	@Override
	public void onResume(){
		super.onResume();

		updateClock();

		Log.d("onResume");

		overridePendingTransition(R.anim.ani_fade_in, R.anim.ani_fade_out);
		new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (View.VISIBLE != mRlAudioControl.getVisibility()) {
					bindService();
				}
			}
		}.sendEmptyMessageDelayed(0, 1000);

//		mAutoBrightnessHandler.resume();
	}

	@Override
	public void onPause(){
		super.onPause();

		Log.d("onPause");

		overridePendingTransition(R.anim.ani_fade_in, R.anim.ani_fade_out);
	}

	@Override
	public void onStop(){
		super.onStop();

		Log.d("onStop");
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		Log.d("onDestroy");
	}

	@Override
	public void onBackPressed() {
		Log.d("onBackPressed");
	}

	@Override
	protected void showAudioControl(boolean show) {
		if (show) {
			mRlAudioControl.setVisibility(View.VISIBLE);
		} else {
			mIvAlbumCover.setImageResource(R.drawable.menu_02_cover);
			mRlAudioControl.setVisibility(View.GONE);
			mPlayingDuration = 0;
			setAudioProgress(0);
			mTvAudioTitle.setText("");
		}
	}

	private void updateClock() {
		Calendar cal = Calendar.getInstance();

		if (mTvTime == null || mTvDate == null || mTvTimeDivision == null || mTvDay == null)
			return ;

		boolean is24 = DateFormat.is24HourFormat(mContext);
		SimpleDateFormat sdf;
		if(!is24) {
			mTvTimeDivision.setText(cal.get(Calendar.AM_PM) == Calendar.PM ? "pm " : "am ");
			sdf = new SimpleDateFormat("hh:mm", Locale.US);
		} else {
			sdf = new SimpleDateFormat("HH:mm", Locale.US);
		}

		mTvTime.setText(sdf.format(Calendar.getInstance().getTime()));

		mTvDate.setText((cal.get(Calendar.MONTH) + 1) + getString(R.string.month) + " " + cal.get(Calendar.DAY_OF_MONTH) + getString(R.string.day));

		String[] formats = mContext.getResources().getStringArray(R.array.day_of_week);
		mTvDay.setText(String.format(formats[cal.get(Calendar.DAY_OF_WEEK) - 1]));
	}

	class BtnClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {

			if (v == mBtnApps) {
				showApps();
			}

			else if (v == mBtnAudio) {
				if (MediaScanReceiver.mMediaPrepared && MediaStorage.isMusicEnable(getActivity())) {
					if(!startApplication(AUDIO_APPLICATION_PACKAGE_NAME))
						Log.e("Music App Start Failed!");
				} else {
					String message;
					if (!MediaScanReceiver.mMediaPrepared) {
						message = getString(R.string.storage_not_prepared);
					} else if (MediaScanReceiver.mMediaScanning) {
						message = getString(R.string.media_scanning);
					} else {
						message = getString(R.string.no_playable_file);
					}
					showToast(message);
					Log.w("isMusicEnable() = false");
				}
			}

			else if (v == mBtnDmb) {
				if (MediaScanReceiver.mMediaPrepared) {
					if(!startApplication(DMB_APPLICATION_PACKAGE_NAME))
						Log.e("DMB App Start Failed!");
				} else {
					showToast(getString(R.string.storage_not_prepared));
				}
			}

			else if (v == mBtnImage) {
				if (MediaScanReceiver.mMediaPrepared && MediaStorage.isPhotoEnable(getActivity())) {
					if(!startApplication(IMAGE_APPLICATION_PACKAGE_NAME))
						Log.e("Image App Start Failed!");
				} else {
					String message;
					if (!MediaScanReceiver.mMediaPrepared) {
						message = getString(R.string.storage_not_prepared);
					} else if (MediaScanReceiver.mMediaScanning) {
						message = getString(R.string.media_scanning);
					} else {
						message = getString(R.string.no_playable_file);
					}
					showToast(message);
					Log.w("isPhotoEnable() = false");
				}
			}

			else if (v == mBtnMiracast) {

				if(!startApplication(MIRACAST_APPLICATION_PACKAGE_NAME))
					Log.e("WFDSink App Start Failed!");
			}

			else if (v == mBtnAirplay) {

				if(!startApplication(AIRPLAY_APPLICATION_PACKAGE_NAME))
					Log.e("Airplay App Start Failed!");
			}

			else if (v == mBtnVideo) {
				if (MediaScanReceiver.mMediaPrepared && MediaStorage.isMovieEnable(getActivity())) {
					if(!startApplication(VIDEO_APPLICATION_PACKAGE_NAME))
						Log.e("Movie App Start Failed!");
				} else {
					String message;
					if (!MediaScanReceiver.mMediaPrepared) {
						message = getString(R.string.storage_not_prepared);
					} else if (MediaScanReceiver.mMediaScanning) {
						message = getString(R.string.media_scanning);
					} else {
						message = getString(R.string.no_playable_file);
					}
					showToast(message);
					Log.w("isMovieEnable() = false");
				}
			}

			else if (v == mBtnSetting) {

				if(!startApplication(SETTING_APPLICATION_PACKAGE_NAME))
					Log.e("Setting App Start Failed!");
			}

			else if (v == mBtnAudioPrev) {

				playPrev();
			}

			else if (v == mBtnAudioPlay) {

				play();
			}

			else if (v == mBtnAudioPause) {

				pause();
			}

			else if (v == mBtnAudioNext) {

				playNext();
			}

			else if (v == mBtnWifi) {
				if (mWifiProcessing)
					return ;

				WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
				wm.setWifiEnabled(!wm.isWifiEnabled());
			}

			else if (v == mBtnBluetooth) {
				if (mBluetoothProcessing)
					return ;

				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if(adapter.getState() == BluetoothAdapter.STATE_ON)
					adapter.disable();
				else if(adapter.getState() == BluetoothAdapter.STATE_OFF)
					adapter.enable();
			}

			else if (v == mBtnBrightness) {
//				mBrightnessDialog.show();
			}
		}
	}

	public class BtnLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View view) {
			if (view == mBtnWifi) {
				startSettingApplicationByMode("wifi");
			}

			else if (view == mBtnBluetooth) {
				startSettingApplicationByMode("bluetooth");
			}

			return false;
		}

	}

	private boolean startApplication(String packageName) {
		Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

		if (intent != null) {
			runActivity(intent);
			return true;
		}

		return false;
	}

	private boolean startSettingApplicationByMode(String mode) {
		Intent intent = getPackageManager().getLaunchIntentForPackage(SETTING_APPLICATION_PACKAGE_NAME);

		if (intent != null) {
			intent.putExtra("mode", mode);
			runActivity(intent);
			return true;
		}

		return false;
	}

	private void showApps() {
		Intent intent = new Intent(this, HomeScreen.class);
		runActivity(intent);
	}

	private void runActivity(Intent intent) {
		if (!mRunApplication) {
			mRunApplication = true;
			startActivity(intent);
		}
	}

	private void handleWifiStateChanged(int state) {
		switch(state) {
		case WifiManager.WIFI_STATE_DISABLED:
			mWifiProcessing = false;
			mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_dis);
			break;
		case WifiManager.WIFI_STATE_DISABLING:
		case WifiManager.WIFI_STATE_ENABLING:
				mWifiProcessing = true;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			mWifiProcessing = false;
			mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_0);
			break;
		}
	}

	private void updateWifiConnectionState(boolean connected) {
		if(connected){
			updateWifiSignalState();
		}

		else {
			handleWifiStateChanged(mWifiState);
		}
	}

	private void updateWifiSignalState() {
		WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);

		int signal;

		signal = WifiManager.calculateSignalLevel(manager.getConnectionInfo().getRssi(), 5);

		ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if(!info.isConnected()) {
			mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_0);
		}

		else {
			if(manager.isWifiEnabled() && (manager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED)) {
				switch(signal) {
				case 4:
					mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_4);
					break;
				case 3:
					mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_3);
					break;
				case 2:
					mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_2);
					break;
				case 1:
					mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_1);
					break;
				case 0:
					mBtnWifi.setBackgroundResource(R.drawable.btn_wifi_0);
					break;
				}
			}
		}
	}

	private void setAudioProgress(int width) {
		RelativeLayout.LayoutParams audioProgress = (RelativeLayout.LayoutParams)mFlAudioTime.getLayoutParams();
		audioProgress.width = width;
		mFlAudioTime.setLayoutParams(audioProgress);
	}

	private void showToast(String message) {
		if (null != mToast) {
			mToast.cancel();
		}
		mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
		mToast.show();
	}

	private BroadcastReceiver mWifiIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				handleWifiStateChanged(mWifiState);
			}

			else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
				updateWifiSignalState();
			}

			else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				updateWifiConnectionState(info.isConnected());
			}
		}

	};

	private BroadcastReceiver mMediaIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			Log.d(action.substring(action.lastIndexOf(".") + 1));

			if (action.equals(MediaStorage.INTENT_ACTION_MEDIA_PREPARED)) {
				if (null != mToast) {
					mToast.cancel();
				}
			}

			else if (action.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED)) {
				if (intent.getBooleanExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, true)) {
					if (MediaStorage.isMusicEnable(getActivity())) {
						if ((null != getMusicService()) && (View.VISIBLE != mRlAudioControl.getVisibility())) {
							try {
								getMusicService().setMusicMode(false);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					} else {
						showAudioControl(false);
					}
				}
			}

			else if (action.equals(MediaStorage.INTENT_ACTION_MEDIA_EJECT)) {
				if (!MediaStorage.isMusicEnable(getActivity())) {
					showAudioControl(false);
				}
			}
		}
	};

	private BroadcastReceiver mBtIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context conext, Intent intent) {

			final String action = intent.getAction();

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				switch (state) {
				case BluetoothAdapter.STATE_OFF:
					mBluetoothProcessing = false;
					mBtnBluetooth.setBackgroundResource(R.drawable.btn_bt_dis);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
				case BluetoothAdapter.STATE_TURNING_ON:
						mBluetoothProcessing = true;
					break;
				case BluetoothAdapter.STATE_ON:
					mBluetoothProcessing = false;
					mBtnBluetooth.setBackgroundResource(R.drawable.btn_bt_on);
					break;
				}
			}

			else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,
						BluetoothAdapter.STATE_DISCONNECTED);
				if (state == BluetoothAdapter.STATE_CONNECTED) {
					mBtnBluetooth.setBackgroundResource(R.drawable.btn_bt_conn);
				} else {
					mBtnBluetooth.setBackgroundResource(R.drawable.btn_bt_on);
				}

			}            
		}
	};

	private BroadcastReceiver mTimeIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateClock();
		}
	};


	// ----------
	// MusicServiceCallback APIs
	@Override
	public void onTotalCount(int totalCount) {
	}

	@Override
	public void onPlayState(int playState) {
		switch (playState) {
		case MusicUtils.PlayState.PLAY :
			mBtnAudioPlay.setVisibility(View.GONE);
			mBtnAudioPause.setVisibility(View.VISIBLE);
			showAudioControl(true);
			break;
		case MusicUtils.PlayState.PAUSE :
			mBtnAudioPlay.setVisibility(View.VISIBLE);
			mBtnAudioPause.setVisibility(View.GONE);
			showAudioControl(true);
			break;
		default :
			break;
		}
		Log.d("playState = " + ((playState == MusicUtils.PlayState.PLAY) ? "PLAY" : (playState == MusicUtils.PlayState.PAUSE) ? "PAUSE" : ""));
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) {
		if ((0 < mPlayingDuration) && (mPlayingDuration >= playTimeMS) && (0 <= playTimeMS)) {
			setAudioProgress(playTimeMS * PLAY_TIME_MAX_LENGTH / mPlayingDuration);
			//			Log.w(playTimeMS + "/" + mPlayingDuration);
		}
	}

	@Override
	public void onMusicInfo(int index, MusicInfo info) {
		mPlayingDuration = info.getTotalTimeMS();
		if (0 < mPlayingDuration) {
			setAudioProgress(getPlayTimeMS() * PLAY_TIME_MAX_LENGTH / mPlayingDuration);
		}
		String title = info.getTitle();
		mTvAudioTitle.setText(title);
		mTvAudioTitle.setSelected(true);
		Log.d("index = " + index + ", title = " + title);
	}

	@Override
	public void onAlbumArt(int index, Bitmap albumArt) {
		if (null == albumArt) {
			mIvAlbumCover.setImageResource(R.drawable.cover_img);
			Log.w("index = " + index + ", albumArt null");
		} else {
			mIvAlbumCover.setImageBitmap(albumArt);
			Log.d("index = " + index + ", albumArt OK");
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
	}

	@Override
	public void onRepeatState(int repeat) {
	}

	@Override
	public void onScanState(int scan) {
	}

	@Override
	public void onListInfo(ListInfo info) {
	}

	@Override
	public void onError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
	}

	private static final String[] permissionList = {
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
	};

	public void checkPermission() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
			return;
		
		for (String permission : permissionList) {
			int chk = checkCallingOrSelfPermission(permission);
			if (chk == PackageManager.PERMISSION_DENIED) {
				requestPermissions(permissionList, 0);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 0) {
			for (int i = 0; i < grantResults.length; i++) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					
				} else {
					finish();
				}
			}
		}
	}
}
