package com.litbig.app.photo.service;

import com.litbig.app.photo.aidl.IPhotoService;
import com.litbig.app.photo.aidl.IPhotoServiceCallback;
import com.litbig.app.photo.service.player.FilePlayer;
import com.litbig.app.photo.service.player.PhotoPlayer;
import com.litbig.app.photo.util.Log;
import com.litbig.app.photo.util.PhotoUtils;
import com.litbig.mediastorage.MediaStorage;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

public class PhotoPlaybackService extends Service {
    //----------
    // Service behavior
    private PhotoServiceStub mServiceStub;

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceStub = new PhotoServiceStub(this);
        registerMediaReceiver();
        createSharedPreference();
        createPhotoPlayer();
        Log.i("Photo Service Create");
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IPhotoService.class.getName().equals(intent.getAction())) {
            return mServiceStub;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.destroy();
        unregisterMediaReceiver();
        Log.w("Photo Service Destroy");
    }

    //----------
    // MovieServiceCallback functions
    private final PhotoServiceCallback<IPhotoServiceCallback> mServiceCallback = new PhotoServiceCallback<>();

    public boolean registerCallback(IPhotoServiceCallback callback) {
        if (null != callback) {
            return mServiceCallback.register(callback);
        }
        return false;
    }

    public boolean unregisterCallback(IPhotoServiceCallback callback) {
        if (null != callback) {
            return mServiceCallback.unregister(callback);
        }
        return false;
    }

    public PhotoServiceCallback<IPhotoServiceCallback> getCallback() {
        return mServiceCallback;
    }

    //----------
    // PhotoPlayer functions
    private PhotoPlayer mPlayer = null;

    private void createPhotoPlayer() {
        mPlayer = new FilePlayer(this);
    }

    public void setPhotoMode() {
        Log.d("PhotoMode");
        mPlayer.active();
    }

    public PhotoPlayer getPhotoPlayer() {
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
                                    if (MediaStorage.isPhotoEnable(context)) {
                                        ((FilePlayer)mPlayer).changeStorage();
                                    } else {
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

    // ----------
    // SharedPreference functions
    private SharedPreferences mPref;

    private void createSharedPreference() {
        mPref = getSharedPreferences("PhotoPlayer", Context.MODE_PRIVATE);
    }

    public void savePreference(int key, Object value) {
        SharedPreferences.Editor prefEdit = mPref.edit();
        switch (key) {
            case PhotoUtils.Preference.PLAY_FILE :
                if (value instanceof String) {
                    prefEdit.putString("play_file", (String)value);
                } else {
                    Log.e("savePreference error!");
                }
                break;
            case PhotoUtils.Preference.SHUFFLE :
                if (value instanceof Integer) {
                    prefEdit.putInt("shuffle", (Integer)value);
                } else {
                    Log.e("savePreference error!");
                }
                break;
            case PhotoUtils.Preference.CATEGORY :
                if (value instanceof Integer) {
                    prefEdit.putInt("category", (Integer)value);
                } else {
                    Log.e("savePreference error!");
                }
                break;
            case PhotoUtils.Preference.FOLDER_PATH :
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
            case PhotoUtils.Preference.PLAY_FILE :
                load = mPref.getString("play_file", "");
                break;
            case PhotoUtils.Preference.SHUFFLE :
                load = mPref.getInt("shuffle", PhotoUtils.ShuffleState.OFF);
                break;
            case PhotoUtils.Preference.CATEGORY :
                load = mPref.getInt("category", PhotoUtils.Category.ALL);
                break;
            case PhotoUtils.Preference.FOLDER_PATH :
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
        prefEdit.putInt("shuffle", PhotoUtils.ShuffleState.OFF);
        prefEdit.putInt("category", PhotoUtils.Category.ALL);
        prefEdit.putString("sub_category", "");
        prefEdit.apply();
    }
}
