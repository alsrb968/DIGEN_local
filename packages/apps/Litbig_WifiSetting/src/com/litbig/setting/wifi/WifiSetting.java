package com.litbig.setting.wifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class WifiSetting extends AppCompatActivity {
//    private MicomSystemManager mMicomSystemManager;
//    private MicomSystemListener mMicomSystemListener;

    private WifiLayout mWifiLayout;
    private ImageButton mBtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

//        try {
//            mMicomSystemManager = new MicomSystemManager(this);
//            registerMicomSystemListener();
//        } catch (NoClassDefFoundError e) {
//            e.printStackTrace();
//        }

        mWifiLayout = findViewById(R.id.wifi_area);
        mWifiLayout.setActivity(this);
        mBtnBack = mWifiLayout.findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(mBtnClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mWifiLayout.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWifiLayout.onPause();
    }

    @Override
    public void onDestroy() {
//        unregisterMicomSystemListener();
        super.onDestroy();
    }

//    private void registerMicomSystemListener() {
//        if(null == mMicomSystemListener) {
//            mMicomSystemListener = new MicomSystemListener() {
//                @Override
//                public void onPowerStatus(boolean status) {
//                    if(status == false) {
//                        finish();
//                    }
//                }
//            };
//
//            if(mMicomSystemManager != null) {
//                mMicomSystemManager.addListener(mMicomSystemListener);
//            } else {
//                Log.e("mMicomSystemManager is null");
//            }
//        }
//    }
//
//    private void unregisterMicomSystemListener() {
//        if(null != mMicomSystemManager) {
//            mMicomSystemManager.removeListener(mMicomSystemListener);
//            mMicomSystemListener = null;
//        }
//    }

    private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == mBtnBack) {
                finish();
            }
        }
    };

    private final static float STANDARD_WIDTH = 1024f;
    private final static float STANDARD_HEIGHT = 600f;
    public static float getDisplayRate(Context context) {
        FloatPoint rate = getXYDensity(context);
        if(rate.x > rate.y) return rate.y;
        else return rate.x;
    }
    static class FloatPoint{
        public float x;
        public float y;
        public FloatPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    public static FloatPoint getXYDensity(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        float x = size.x / STANDARD_WIDTH;
        float y = size.y / STANDARD_HEIGHT;

        return new FloatPoint(x, y);
    }

    private static final String[] permissionList = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_CONFIGURATION,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.INTERNAL_SYSTEM_WINDOW,
    };

    public void checkPermission() {
        for (String permission : permissionList) {
            if (checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
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
                    Log.i("permission Granted: " + permissions[i]);
                } else {
                    finish();
                }
            }
        }
    }
}
