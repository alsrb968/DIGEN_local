package com.litbig.app.movie.service;

import java.util.List;

import com.litbig.app.movie.activity.MovieActivity;
import com.litbig.app.movie.aidl.IMovieService;
import com.litbig.app.movie.aidl.IMovieServiceCallback;
import com.litbig.app.movie.receiver.AVRCPReceiver;
import com.litbig.app.movie.service.player.FilePlayer;
import com.litbig.app.movie.service.player.MoviePlayer;
import com.litbig.app.movie.util.Log;
import com.litbig.app.movie.util.MovieUtils;
import com.litbig.mediastorage.MediaStorage;

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

public class MoviePlaybackService extends Service {
	//----------
	// Service behavior
	private MovieServiceStub mServiceStub;

	@Override
	public void onCreate() {
		super.onCreate();
		mServiceStub = new MovieServiceStub(this);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		registerMediaReceiver();
		registerAVRCPReceiver();
		createSharedPreference();
		createMoviePlayer();
		Log.i("Movie Service Create");
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (IMovieService.class.getName().equals(intent.getAction())) {
			return mServiceStub;
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		abandonAudioFocus();
		mPlayer.destroy();
		unregisterAVRCPReceiver();
		unregisterMediaReceiver();
		Log.w("Movie Service Destroy");
	}

	//----------
	// MovieServiceCallback functions
	private final MovieServiceCallback<IMovieServiceCallback> mServiceCallback = new MovieServiceCallback<IMovieServiceCallback>();

	public boolean registerCallback(IMovieServiceCallback callback) {
		if (null != callback) {
			return mServiceCallback.register(callback);
		}
		return false;
	}

	public boolean unregisterCallback(IMovieServiceCallback callback) {
		if (null != callback) {
			return mServiceCallback.unregister(callback);
		}
		return false;
	}

	public MovieServiceCallback<IMovieServiceCallback> getCallback() {
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
				mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), AVRCPReceiver.class.getName()));
				mAudioFocus = false;
				break;
			case AudioManager.AUDIOFOCUS_GAIN :
				mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), AVRCPReceiver.class.getName()));
				mAudioFocus = true;
				break;
			default :
				break;
			}
			Log.d("Movie AudioFocusChange = " + mAudioFocus);
		}
	};

	public void abandonAudioFocus() {
		if (mAudioFocus) {
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
			mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), AVRCPReceiver.class.getName()));
			mAudioFocus = false;
			Log.d("Movie AudioFocusChange = " + mAudioFocus);
		}
	}

	//----------
	// MoviePlayer functions
	private MoviePlayer mPlayer = null;

	private void createMoviePlayer() {
		mPlayer = new FilePlayer(this);
	}

	public void setMovieMode() {
		Log.d("MovieMode");
		if (!mAudioFocus) {
			if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
				mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), AVRCPReceiver.class.getName()));
				mAudioFocus = true;
				Log.d("Movie AudioFocusChange = " + mAudioFocus);
			}
		}
		mPlayer.active();
	}

	public MoviePlayer getMoviePlayer() {
		return mPlayer;
	}

	// ----------
	// ListView functions
	private boolean mListVisible = false;
	private boolean mPauseForList = false;

	public void showList(boolean show) {
		mListVisible = show;
		if (show) {
			if (MovieUtils.PlayState.PLAY == mPlayer.getPlayState()) {
				mPauseForList = true;
				mPlayer.pause();
			}
		} else if (mPauseForList) {
			mPauseForList = false;
			mPlayer.play();
		}
	}

	public boolean isPauseForList() {
		return mPauseForList;
	}

	//----------
	// BroadcastReceiver
	private BroadcastReceiver mMediaReceiver = null;
	private BroadcastReceiver mAVRCPReceiver = null;

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
									if (MediaStorage.isMovieEnable(context)) {
										((FilePlayer)mPlayer).changeStorage();
									} else {
										abandonAudioFocus();
										mPlayer.inactive();
										((FilePlayer)mPlayer).clearPlaylist();
									}
								}
							}
						}
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
			registerReceiver(mMediaReceiver, intentFilter);
		}
	}

	private void unregisterMediaReceiver() {
		if (null != mMediaReceiver) {
			unregisterReceiver(mMediaReceiver);
			mMediaReceiver = null;
		}
	}

	private void registerAVRCPReceiver() {
		if (null == mAVRCPReceiver) {
			mAVRCPReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (null != intent) {
						String intentAction = intent.getAction();
						if (null != intentAction) {
							if ((mAudioFocus) && (MediaStorage.isMovieEnable(context))) {
								if (intentAction.equals(AVRCPReceiver.ACTION_MEDIA_BUTTON)) {
									KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
									if (KeyEvent.ACTION_UP == event.getAction()) {
										switch (event.getKeyCode()) {
										case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE :
											if (!mListVisible) {
												if (MovieUtils.PlayState.PLAY == mPlayer.getPlayState()) {
													mPlayer.pause();
												} else {
													mPlayer.play();
												}
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_STOP :
											ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
											List<RunningTaskInfo> activityList = am.getRunningTasks(1);
											ComponentName topActivity = activityList.get(0).topActivity;
											abandonAudioFocus();
											mPlayer.inactive();
											if (topActivity.getClassName().equals("com.litbig.app.movie.activity.MovieActivity")) {
												MovieActivity.getInstance().finish();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_NEXT :
											if (!mListVisible) {
												mPlayer.playNext();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_PREVIOUS :
											if (!mListVisible) {
												mPlayer.playPrev();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_PLAY :
											if (!mListVisible) {
												mPlayer.play();
											}
											break;
										case KeyEvent.KEYCODE_MEDIA_PAUSE :
											if (!mListVisible) {
												mPlayer.pause();
											}
											break;
										default :
											Log.i("ACTION_MEDIA_BUTTON : " + event.getKeyCode());
											break;
										}
									}
								} else if (intentAction.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
									Log.i("ACTION_AUDIO_BECOMING_NOISY");
									//mPlayer.pause();
								}
							}
						}
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(AVRCPReceiver.ACTION_MEDIA_BUTTON);
			intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
			registerReceiver(mAVRCPReceiver, intentFilter);
		}
	}

	private void unregisterAVRCPReceiver() {
		if (null != mAVRCPReceiver) {
			unregisterReceiver(mAVRCPReceiver);
			mAVRCPReceiver = null;
		}
	}

	// ----------
	// SharedPreference functions
	private SharedPreferences mPref;

	private void createSharedPreference() {
		mPref = getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE);
	}

	public void savePreference(int key, Object value) {
		SharedPreferences.Editor prefEdit = mPref.edit();
		switch (key) {
		case MovieUtils.Preference.PLAY_FILE :
			if (value instanceof String) {
				prefEdit.putString("play_file", (String)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MovieUtils.Preference.PLAY_TIME :
			if (value instanceof Integer) {
				prefEdit.putInt("play_time", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MovieUtils.Preference.SHUFFLE :
			if (value instanceof Integer) {
				prefEdit.putInt("shuffle", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MovieUtils.Preference.REPEAT :
			if (value instanceof Integer) {
				prefEdit.putInt("repeat", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MovieUtils.Preference.CATEGORY :
			if (value instanceof Integer) {
				prefEdit.putInt("category", (Integer)value);
			} else {
				Log.e("savePreference error!");
			}
			break;
		case MovieUtils.Preference.FOLDER_PATH :
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
		case MovieUtils.Preference.PLAY_FILE :
			load = mPref.getString("play_file", "");
			break;
		case MovieUtils.Preference.PLAY_TIME :
			load = mPref.getInt("play_time", 0);
			break;
		case MovieUtils.Preference.SHUFFLE :
			load = mPref.getInt("shuffle", MovieUtils.ShuffleState.OFF);
			break;
		case MovieUtils.Preference.REPEAT :
			load = mPref.getInt("repeat", MovieUtils.RepeatState.ALL);
			break;
		case MovieUtils.Preference.CATEGORY :
			load = mPref.getInt("category", MovieUtils.Category.ALL);
			break;
		case MovieUtils.Preference.FOLDER_PATH :
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
		prefEdit.putInt("shuffle", MovieUtils.ShuffleState.OFF);
		prefEdit.putInt("repeat", MovieUtils.RepeatState.ALL);
		prefEdit.putInt("category", MovieUtils.Category.ALL);
		prefEdit.putString("sub_category", "");
		prefEdit.apply();
	}
}
