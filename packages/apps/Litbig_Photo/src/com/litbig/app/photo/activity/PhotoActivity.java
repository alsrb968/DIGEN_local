package com.litbig.app.photo.activity;

import com.litbig.app.photo.R;
import com.litbig.app.photo.activity.view.AndroidView;
import com.litbig.app.photo.activity.view.PhotoView;
import com.litbig.app.photo.aidl.IPhotoService;
import com.litbig.app.photo.util.Log;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

public class PhotoActivity extends Activity {
    // ----------
    // Activity behavior
    private PhotoView mView;
    private FrameLayout mRootLayout;
    private PhotoServiceCallbackHandler mCallbackHandler;
    private PhotoServiceCallbackStub mServiceCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        mView = new AndroidView(this); //If you want to use a view other than AndroidView, create a view class inherited from PhotoView and replace it instead of AndroidView.
        setContentView(R.layout.photo_view);
        mRootLayout = findViewById(R.id.root_layout);
        registerFinishReceiver();
        mCallbackHandler = new PhotoServiceCallbackHandler(this);
        mServiceCallback = new PhotoServiceCallbackStub(this);
        mImageBitmapBuffer = new ImageBitmapBuffer();
        mView.onCreate();
        
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MediaStorage.isPhotoEnable(this)) {
        	boolean result = false;
        	Intent service = new Intent(IPhotoService.class.getName());
        	service.setPackage("com.litbig.app.photo");
            bindService(service, mServiceBinder, Context.BIND_AUTO_CREATE);
            Log.w("Photo Service Binding result: " + result);
            mView.onResume();
        } else {
            Log.w("isPhotoEnable() = false");
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
        if (null != mService) {
            try {
                mService.unregisterPhotoServiceCallback(mServiceCallback);
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
        if (null != mImageBitmapBuffer) {
            mImageBitmapBuffer.release();
            mImageBitmapBuffer = null;
        }
        unregisterFinishReceiver();
    }

    @Override
    public void onBackPressed() {
        mView.onBackPressed();
    }

    public PhotoView getView() {
        return mView;
    }

    public PhotoServiceCallbackHandler getCallbackHandler() {
        return mCallbackHandler;
    }

    public FrameLayout getRootLayout() {
        return mRootLayout;
    }

    //----------
    // ServiceConnection
    private IPhotoService mService = null;

    private ServiceConnection mServiceBinder = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IPhotoService.Stub.asInterface(service);
            try {
                mService.registerPhotoServiceCallback(mServiceCallback);
                mService.setPhotoMode();
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

    public IPhotoService getService() {
        if (null == mService) {
            Log.w("IPhotoService is null");
        }
        return mService;
    }

    //----------
    // BroadcastReceiver
    private BroadcastReceiver mFinishReceiver = null;

    private void registerFinishReceiver() {
        if (null == mFinishReceiver) {
            mFinishReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (null != intent) {
                        String intentAction = intent.getAction();
                        if (null != intentAction) {
                            if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_STARTED)) {
                                mView.onMediaScan(true);
                            } else if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED)) {
                                if (intent.getBooleanExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, true)) {
                                    if (MediaStorage.isPhotoEnable(context)) {
                                        mView.onMediaScan(false);
                                    } else {
                                        finish();
                                    }
                                }
                            } else if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_EJECT)) {
                                if (MediaStorage.isPhotoEnable(context)) {
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
            registerReceiver(mFinishReceiver, intentFilter);
        }
    }

    private void unregisterFinishReceiver() {
        if (null != mFinishReceiver) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }

    //----------
    // ImageBitmapBuffer functions
    private ImageBitmapBuffer mImageBitmapBuffer = null;

    public void toImageBitmapBuffer(int index, Bitmap imageBitmap) {
        mImageBitmapBuffer.add(index, imageBitmap);
    }

    public Bitmap fromImageBitmapBuffer(int index) {
        return mImageBitmapBuffer.get(index);
    }

    public void clearImageBitmapBuffer() {
        mImageBitmapBuffer.clear();
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
