package com.litbig.app.music.activity;

import com.litbig.app.music.aidl.IMusicService;
import com.litbig.app.music.aidl.MusicPlayerCallbackInterface;
import com.litbig.app.music.aidl.MusicPlayerInterface;
import com.litbig.app.music.util.Log;
import com.litbig.app.music.util.MusicUtils;
import com.litbig.mediastorage.MediaScanReceiver;
import com.litbig.mediastorage.MediaStorage;

import android.Manifest;
import android.annotation.NonNull;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;


public abstract class MusicActivity extends Activity implements MusicPlayerInterface, MusicPlayerCallbackInterface {
	// ----------
	// Activity behavior
	private MusicActivity mActivity = this;
	private MusicServiceCallbackStub mServiceCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("[jacob]");
		super.onCreate(savedInstanceState);
		mServiceCallback = new MusicServiceCallbackStub(this);
		startService();
		checkPermission();
	}

	@Override
	protected void onStart() {
		Log.i("[jacob]");
		super.onStart();
		bindService();
		mRunApplication = false;
		showAudioControl(false);
	}

	@Override
	protected void onResume() {
		Log.i("[jacob]");
		super.onResume();
		if (mRunApplication) {
			if ((null != mService) && (MediaScanReceiver.mMediaPrepared) && (MediaStorage.isMusicEnable(mActivity))) {
				try {
					mService.setMusicMode(false);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mRunApplication = false;
		}
	}

	@Override
	protected void onStop() {
		Log.i("[jacob]");
		super.onStop();
		if (null != mService) {
			try {
				mService.unregisterMusicServiceCallback(mServiceCallback);
				unbindService(mServiceBinder);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public MusicActivity getActivity() {
		return mActivity;
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
				if ((MediaScanReceiver.mMediaPrepared) && (MediaStorage.isMusicEnable(mActivity))) {
					mService.setMusicMode(true);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};

	protected IMusicService getMusicService() {
		return getService();
	}

	private IMusicService getService() {
		if (null == mService) {
			Log.w("IMusicService is null");
		}
		return mService;
	}

	protected void startService() {
		Intent intent = new Intent(getApplicationContext(), IMusicService.class);
		intent.setPackage("com.litbig.app.music.service.MusicPlaybackService");
		startService(intent);
	}

	protected void bindService() {
		boolean result;
    	Intent service = new Intent(IMusicService.class.getName());
    	service.setPackage("com.litbig.app.music");
    	result = bindService(service, mServiceBinder, Context.BIND_AUTO_CREATE);
        Log.w("Music Service Binding result: " + result);
	}

	// ----------
	// MusicService APIs
	@Override
	public int getPlayingIndex() {
		int index = -1;
		if (null != getService()) {
			try {
				index = getService().getPlayingIndex();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) {
		String file = null;
		if (null != getService()) {
			try {
				file = getService().getFileFullPath(index, isNowPlaying);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	@Override
	public int getPlayState() {
		int playState = MusicUtils.PlayState.STOP;
		if (null != getService()) {
			try {
				playState = getService().getPlayState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return playState;
	}

	@Override
	public int getPlayTimeMS() {
		int playTime = 0;
		if (null != getService()) {
			try {
				playTime = getService().getPlayTimeMS();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return playTime;
	}

	@Override
	public void setPlayTimeMS(int playTimeMS) {
		if (null != getService()) {
			try {
				getService().setPlayTimeMS(playTimeMS);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void gripTimeProgressBar() {
		if (null != getService()) {
			try {
				getService().gripTimeProgressBar();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void play() {
		if (null != getService()) {
			try {
				getService().play();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void pause() {
		if (null != getService()) {
			try {
				getService().pause();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) {
		boolean success = false;
		if (null != getService()) {
			try {
				success = getService().playIndex(index, isNowPlaying);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	@Override
	public void playPrev() {
		if (null != getService()) {
			try {
				getService().playPrev();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void playNext() {
		if (null != getService()) {
			try {
				getService().playNext();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startFastForward() {
		if (null != getService()) {
			try {
				getService().startFastForward();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopFastForward() {
		if (null != getService()) {
			try {
				getService().stopFastForward();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startFastRewind() {
		if (null != getService()) {
			try {
				getService().startFastRewind();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopFastRewind() {
		if (null != getService()) {
			try {
				getService().stopFastRewind();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getShuffle() {
		int shuffle = MusicUtils.ShuffleState.OFF;
		if (null != getService()) {
			try {
				shuffle = getService().getShuffle();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return shuffle;
	}

	@Override
	public int getRepeat() {
		int repeat = MusicUtils.RepeatState.OFF;
		if (null != getService()) {
			try {
				repeat = getService().getRepeat();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return repeat;
	}

	@Override
	public int getScan() {
		int scan = MusicUtils.ScanState.OFF;
		if (null != getService()) {
			try {
				scan = getService().getScan();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return scan;
	}

	@Override
	public void setShuffle() {
		if (null != getService()) {
			try {
				getService().setShuffle();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setRepeat() {
		if (null != getService()) {
			try {
				getService().setRepeat();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setScan() {
		if (null != getService()) {
			try {
				getService().setScan();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void requestList(int listType, String subCategory) {
		if (null != getService()) {
			try {
				getService().requestList(listType, subCategory);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap albumArt = null;
		if (null != getService()) {
			try {
				albumArt = getService().getAlbumArt(index, isNowPlaying, isScale);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return albumArt;
	}

	@Override
	public int getNowPlayingCategory() {
		int category = MusicUtils.Category.ALL;
		if (null != getService()) {
			try {
				category = getService().getNowPlayingCategory();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return category;
	}

	@Override
	public String getNowPlayingSubCategory() {
		String subCategory = null;
		if (null != getService()) {
			try {
				subCategory = getService().getNowPlayingSubCategory();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return subCategory;
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) {
		boolean nowPlaying = false;
		if (null != getService()) {
			try {
				nowPlaying = getService().isNowPlayingCategory(category, subCategory);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return nowPlaying;
	}

	// ----------
	// MusicActivity APIs
	protected boolean mRunApplication = false;

	protected abstract void showAudioControl(boolean show);
	
	
	
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
