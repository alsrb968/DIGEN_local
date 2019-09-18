package com.litbig.app.movie.activity;

import com.litbig.app.movie.R;
import com.litbig.app.movie.activity.view.AndroidView;
import com.litbig.app.movie.activity.view.MovieView;
import com.litbig.app.movie.aidl.IMovieService;
import com.litbig.app.movie.util.Log;
import com.litbig.mediastorage.MediaStorage;

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

public class MovieActivity extends Activity {
    // ----------
    // Activity behavior
    private static MovieActivity mActivity;
    private MovieView mView;
    private FrameLayout mRootLayout;
    private MovieServiceCallbackHandler mCallbackHandler;
    private MovieServiceCallbackStub mServiceCallback;

    public static MovieActivity getInstance() {
        return mActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        mActivity = this;
        mView = new AndroidView(this); //If you want to use a view other than AndroidView, create a view class inherited from MovieView and replace it instead of AndroidView.
        setContentView(R.layout.movie_view);
        mRootLayout = findViewById(R.id.root_layout);
        registerMediaReceiver();
        mCallbackHandler = new MovieServiceCallbackHandler(this);
        mServiceCallback = new MovieServiceCallbackStub(this);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mVideoThumbnailBuffer = new VideoThumbnailBuffer();
        mView.onCreate();
        
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MediaStorage.isMovieEnable(this)) {
        	boolean result;
        	Intent service = new Intent(IMovieService.class.getName());
        	service.setPackage("com.litbig.app.movie");
        	result = bindService(service, mServiceBinder, Context.BIND_AUTO_CREATE);
            Log.w("Movie Service Binding result: " + result);
            mView.onResume();
        } else {
            Log.w("isMovieEnable() = false");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
        if (null != mService) {
            try {
                mService.unregisterMovieServiceCallback(mServiceCallback);
                unbindService(mServiceBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mView.onDestroy();
        if (null != mVideoThumbnailBuffer) {
            mVideoThumbnailBuffer.release();
            mVideoThumbnailBuffer = null;
        }
        unregisterMediaReceiver();
    }

    @Override
    public void onBackPressed() {
        mView.onBackPressed();
    }

    public MovieView getView() {
        return mView;
    }

    public MovieServiceCallbackHandler getCallbackHandler() {
        return mCallbackHandler;
    }

    public FrameLayout getRootLayout() {
        return mRootLayout;
    }

    //----------
    // ServiceConnection
    private IMovieService mService = null;

    private ServiceConnection mServiceBinder = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IMovieService.Stub.asInterface(service);
            try {
                mService.registerMovieServiceCallback(mServiceCallback);
                mService.setMovieMode();
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

    public IMovieService getService() {
        if (null == mService) {
            Log.w("IMovieService is null");
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
                            switch (intentAction) {
                                case MediaStorage.INTENT_ACTION_MEDIA_SCANNER_STARTED:
                                    mView.onMediaScan(true);
                                    break;
                                case MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED:
                                    if (intent.getBooleanExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, true)) {
                                        if (MediaStorage.isMovieEnable(context)) {
                                            mView.onMediaScan(false);
                                        } else {
                                            finish();
                                        }
                                    }
                                    break;
                                case MediaStorage.INTENT_ACTION_MEDIA_EJECT:
                                    if (MediaStorage.isMovieEnable(context)) {
                                        mView.onMediaScan(false);
                                    } else {
                                        finish();
                                    }
                                    break;
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
    // VideoThumbnailBuffer functions
    private VideoThumbnailBuffer mVideoThumbnailBuffer = null;

    public void toVideoThumbnailBuffer(int index, Bitmap videoThumbnail) {
        mVideoThumbnailBuffer.add(index, videoThumbnail);
    }

    public Bitmap fromVideoThumbnailBuffer(int index) {
        return mVideoThumbnailBuffer.get(index);
    }

    public void clearVideoThumbnailBuffer() {
        mVideoThumbnailBuffer.clear();
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
