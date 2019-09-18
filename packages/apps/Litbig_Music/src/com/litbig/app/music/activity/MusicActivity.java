package com.litbig.app.music.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.litbig.app.music.R;
import com.litbig.app.music.activity.view.AndroidView;
import com.litbig.app.music.activity.view.MusicView;
import com.litbig.app.music.aidl.IMusicService;
import com.litbig.app.music.util.Log;
import com.litbig.mediastorage.MediaStorage;

public class MusicActivity extends Activity {
	// ----------
	// Activity behavior
	private static MusicActivity mActivity;
	private MusicView mView;
	private FrameLayout mRootLayout;
	private MusicServiceCallbackStub mServiceCallback;

	public static MusicActivity getInstance() {
		return mActivity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("[jacob]");
		super.onCreate(savedInstanceState);
		overridePendingTransition(0, 0);
		mActivity = this;
		mView = new AndroidView(this); //If you want to use a view other than AndroidView, create a view class inherited from MusicView and replace it instead of AndroidView.
		setContentView(R.layout.music_view);
		mRootLayout = findViewById(R.id.root_layout);
		registerMediaReceiver();
		mServiceCallback = new MusicServiceCallbackStub(this);
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mAlbumArtBuffer = new AlbumArtBuffer();
		mView.onCreate();
		
		checkPermission();
	}

	@Override
	protected void onResume() {
		Log.i("[jacob]");
		super.onResume();
		if (MediaStorage.isMusicEnable(this)) {
        	boolean result = false;
        	Intent service = new Intent(IMusicService.class.getName());
        	service.setPackage("com.litbig.app.music");
        	result = bindService(service, mServiceBinder, Context.BIND_AUTO_CREATE);
            Log.w("Music Service Binding result: " + result);
			mView.onResume();
		} else {
			Log.w("isMusicEnable() = false");
			finish();
		}
	}

	@Override
	protected void onPause() {
		Log.i("[jacob]");
		super.onPause();
		mView.onPause();
		if (null != mService) {
			try {
				mService.unregisterMusicServiceCallback(mServiceCallback);
				unbindService(mServiceBinder);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
		Log.i("[jacob]");
		super.onDestroy();
		mView.onDestroy();
		if (null != mAlbumArtBuffer) {
			mAlbumArtBuffer.release();
			mAlbumArtBuffer = null;
		}
		unregisterMediaReceiver();
	}

	@Override
	public void onBackPressed() {
		Log.i("[jacob]");
		mView.onBackPressed();
	}

	public MusicView getView() {
		return mView;
	}

	public FrameLayout getRootLayout() {
		return mRootLayout;
	}

	//----------
	// ServiceConnection
	private IMusicService mService = null;

	private ServiceConnection mServiceBinder = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IMusicService.Stub.asInterface(service);
			try {
				mService.registerMusicServiceCallback(mServiceCallback);
				mService.setMusicMode(true);
				mView.onServiceConnected();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mView.onServiceDisconnected();
			mService = null;
		}
	};

	public IMusicService getService() {
		if (null == mService) {
			Log.w("IMusicService is null");
		}
		return mService;
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
							if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_STARTED)) {
								mView.onMediaScan(true);
							} else if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED)) {
								if (intent.getBooleanExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, true)) {
									if (MediaStorage.isMusicEnable(context)) {
										mView.onMediaScan(false);
									} else {
										finish();
									}
								}
							} else if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_EJECT)) {
								if (MediaStorage.isMusicEnable(context)) {
									mView.onMediaScan(false);
								} else {
									finish();
								}
							}
						}
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_STARTED);
			intentFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
			intentFilter.addAction(MediaStorage.INTENT_ACTION_MEDIA_EJECT);
			registerReceiver(mMediaReceiver, intentFilter);
		}
	}

	private void unregisterMediaReceiver() {
		if (null != mMediaReceiver) {
			unregisterReceiver(mMediaReceiver);
			mMediaReceiver = null;
		}
	}

	//----------
	// Audio Volume control functions
	private AudioManager mAudioManager;

	public int getSystemMediaMaxVolume() {
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	public int getSystemMediaVolume() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	public void setSystemMediaVolume(int volume) {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
	}

	//----------
	// AlbumArtBuffer functions
	private AlbumArtBuffer mAlbumArtBuffer = null;

	public void toAlbumArtBuffer(int index, Bitmap albumArt) {
		mAlbumArtBuffer.add(index, albumArt);
	}

	public Bitmap fromAlbumArtBuffer(int index) {
		return mAlbumArtBuffer.get(index);
	}

	public void clearAlbumArtBuffer() {
		mAlbumArtBuffer.clear();
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
