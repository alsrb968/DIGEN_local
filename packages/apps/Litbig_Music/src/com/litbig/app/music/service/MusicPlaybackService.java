package com.litbig.app.music.service;

import java.util.ArrayList;
import java.util.List;

import com.litbig.app.music.activity.MusicActivity;
import com.litbig.app.music.aidl.IMusicService;
import com.litbig.app.music.aidl.IMusicServiceCallback;
import com.litbig.app.music.receiver.MediaButtonReceiver;
import com.litbig.app.music.service.player.FilePlayer;
import com.litbig.app.music.service.player.MusicPlayer;
import com.litbig.app.music.util.Log;
import com.litbig.app.music.util.MusicUtils;
import com.litbig.mediastorage.MediaStorage;
import com.litbig.systemmanager.SystemKey;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.view.KeyEvent;

public class MusicPlaybackService extends Service {
	//----------
	// Service behavior
	private MusicServiceStub mServiceStub;

	@Override
	public void onCreate() {
		super.onCreate();
		mServiceStub = new MusicServiceStub(this);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		registerMediaReceiver();
		createSharedPreference();
		createMusicPlayer();
		Log.i("Music Service Create");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (IMusicService.class.getName().equals(intent.getAction())) {
			return mServiceStub;
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		abandonAudioFocus();
		mPlayer.destroy();
		unregisterMediaReceiver();
		Log.w("Music Service Destroy");
	}

	//----------
	// MusicServiceCallback functions
	private final MusicServiceCallback<IMusicServiceCallback> mServiceCallback = new MusicServiceCallback<IMusicServiceCallback>();

	public boolean registerCallback(IMusicServiceCallback callback) {
		if (null != callback) {
			return mServiceCallback.register(callback);
		}
		return false;
	}

	public boolean unregisterCallback(IMusicServiceCallback callback) {
		if (null != callback) {
			return mServiceCallback.unregister(callback);
		}
		return false;
	}

	public MusicServiceCallback<IMusicServiceCallback> getCallback() {
		return mServiceCallback;
	}

	//----------
	// AudioFocus Manager
	private AudioManager mAudioManager;
	private boolean mAudioFocus = false;

	private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS :
				mPlayer.inactive();
				mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonReceiver.class.getName()));
				mAudioFocus = false;
				break;
			case AudioManager.AUDIOFOCUS_GAIN :
				mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonReceiver.class.getName()));
				mAudioFocus = true;
				break;
			default :
				break;
			}
			Log.d("Music AudioFocusChange = " + mAudioFocus);
		}
	};

	void setAudioFocus() {
		if (!mAudioFocus) {
			if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
				mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonReceiver.class.getName()));
				mAudioFocus = true;
				Log.d("Music AudioFocusChange = " + mAudioFocus);
			}
		}
	}

	private void abandonAudioFocus() {
		if (mAudioFocus) {
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
			mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), MediaButtonReceiver.class.getName()));
			mAudioFocus = false;
			Log.d("Music AudioFocusChange = " + mAudioFocus);
		}
	}

	//----------
	// MusicPlayer functions
	private MusicPlayer mPlayer = null;

	private void createMusicPlayer() {
		mPlayer = new FilePlayer(this);
	}

	public void setMusicMode(boolean start) {
		Log.d("MusicMode(" + start + ")");
		if (start) {
			setAudioFocus();
		}
		mPlayer.active(start);
	}

	public MusicPlayer getMusicPlayer() {
		return mPlayer;
	}

	//----------
	// BroadcastReceiver
	private BroadcastReceiver mMediaReceiver = null;

	private void registerMediaReceiver() {
		if (null == mMediaReceiver) {
			mMediaReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (null != intent) {
						String intentAction = intent.getAction();
						if (null != intentAction) {
							if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED)) {
								if (intent.getBooleanExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, true)) {
									if (MediaStorage.isMusicEnable(context)) {
										((FilePlayer)mPlayer).changeStorage();
									} else {
										mPlayer.inactive();
										((FilePlayer)mPlayer).clearPlaylist();
									}
								}
							} else if ((mAudioFocus) && (MediaStorage.isMusicEnable(context))) {
								if (intentAction.equals(MediaButtonReceiver.ACTION_MEDIA_BUTTON)) {
									KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
									if (KeyEvent.ACTION_UP == event.getAction()) {
										switch (event.getKeyCode()) {
										case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE :
											if (MusicUtils.PlayState.PLAY == mPlayer.getPlayState()) {
												mPlayer.pause();
											} else {
												mPlayer.play();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_STOP :
											ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
											List<RunningTaskInfo> activityList = am.getRunningTasks(1);
											ComponentName topActivity = activityList.get(0).topActivity;
											mPlayer.pause();
											if (topActivity.getClassName().equals("com.litbig.app.music.activity.MusicActivity")) {
												MusicActivity.getInstance().finish();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_NEXT :
											mPlayer.playNext();
											break;
										case KeyEvent.KEYCODE_MEDIA_PREVIOUS :
											mPlayer.playPrev();
											break;
										case KeyEvent.KEYCODE_MEDIA_PLAY :
											mPlayer.play();
											break;
										case KeyEvent.KEYCODE_MEDIA_PAUSE :
											mPlayer.pause();
											break;
										default :
											Log.i("ACTION_MEDIA_BUTTON : " + event.getKeyCode());
											break;
										}
									}
								} else if (intentAction.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
									Log.i("ACTION_AUDIO_BECOMING_NOISY");
									//mPlayer.pause();
								} else if (intentAction.equals(SystemKey.INTENT_ACTION_SYSTEM_KEY_CLICK)) {
									switch (intent.getIntExtra(SystemKey.INTENT_EXTRA_SYSTEM_KEY_CODE, -1)) {
									case SystemKey.KeyCode.PLAY :
										mPlayer.play();
										break;
									case SystemKey.KeyCode.PAUSE :
										mPlayer.pause();
										break;
									case SystemKey.KeyCode.PREV :
										mPlayer.playPrev();
										break;
									case SystemKey.KeyCode.NEXT :
										mPlayer.playNext();
										break;
									default :
										break;
									}
								}
							}
						}
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
			intentFilter.addAction(MediaButtonReceiver.ACTION_MEDIA_BUTTON);
			intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
			intentFilter.addAction(SystemKey.INTENT_ACTION_SYSTEM_KEY_CLICK);
			registerReceiver(mMediaReceiver, intentFilter);
		}
	}

	private void unregisterMediaReceiver() {
		if (null != mMediaReceiver) {
			unregisterReceiver(mMediaReceiver);
			mMediaReceiver = null;
		}
	}

	// ----------
	// SharedPreference functions
	private SharedPreferences mPref;

	private void createSharedPreference() {
		mPref = getSharedPreferences("MusicPlayer", Context.MODE_PRIVATE);
	}

	public void savePreference(int key, Object value) {
		SharedPreferences.Editor prefEdit = mPref.edit();
		switch (key) {
		case MusicUtils.Preference.PLAY_FILE :
			if (value instanceof String) {
				prefEdit.putString("play_file", (String)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.PLAY_TIME :
			if (value instanceof Integer) {
				prefEdit.putInt("play_time", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.SHUFFLE :
			if (value instanceof Integer) {
				prefEdit.putInt("shuffle", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.REPEAT :
			if (value instanceof Integer) {
				prefEdit.putInt("repeat", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.SCAN :
			if (value instanceof Integer) {
				prefEdit.putInt("scan", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.CATEGORY :
			if (value instanceof Integer) {
				prefEdit.putInt("category", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MusicUtils.Preference.ARTIST_NAME :
		case MusicUtils.Preference.ALBUM_NAME :
		case MusicUtils.Preference.GENRE_ID :
		case MusicUtils.Preference.FOLDER_PATH :
			if (value instanceof String) {
				prefEdit.putString("sub_category", (String)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		default :
			break;
		}
		prefEdit.apply();
	}

	public Object loadPreference(int key) {
		Object load = null;
		switch (key) {
		case MusicUtils.Preference.PLAY_FILE :
			load = mPref.getString("play_file", "");
			break;
		case MusicUtils.Preference.PLAY_TIME :
			load = mPref.getInt("play_time", 0);
			break;
		case MusicUtils.Preference.SHUFFLE :
			load = mPref.getInt("shuffle", MusicUtils.ShuffleState.OFF);
			break;
		case MusicUtils.Preference.REPEAT :
			load = mPref.getInt("repeat", MusicUtils.RepeatState.ALL);
			break;
		case MusicUtils.Preference.SCAN :
			load = mPref.getInt("scan", MusicUtils.ScanState.OFF);
			break;
		case MusicUtils.Preference.CATEGORY :
			load = mPref.getInt("category", MusicUtils.Category.ALL);
			break;
		case MusicUtils.Preference.ARTIST_NAME :
		case MusicUtils.Preference.ALBUM_NAME :
		case MusicUtils.Preference.GENRE_ID :
		case MusicUtils.Preference.FOLDER_PATH :
			load = mPref.getString("sub_category", "");
			break;
		default :
			break;
		}
		return load;
	}

	public void clearPreference() {
		SharedPreferences.Editor prefEdit = mPref.edit();
		prefEdit.putString("play_file", "");
		prefEdit.putInt("play_time", 0);
		prefEdit.putInt("shuffle", MusicUtils.ShuffleState.OFF);
		prefEdit.putInt("repeat", MusicUtils.RepeatState.ALL);
		prefEdit.putInt("scan", MusicUtils.ScanState.OFF);
		prefEdit.putInt("category", MusicUtils.Category.ALL);
		prefEdit.putString("sub_category", "");
		prefEdit.apply();
	}

	private ArrayList<String> mRecentTrackList = new ArrayList<String>();
	
	public ArrayList<String> getRecentTrackList() {
		return mRecentTrackList;
	}

	public void addRecentTrackList(String track) {
		mRecentTrackList.remove(track);
		mRecentTrackList.add(0, track);
	}
}
