/* //device/content/providers/media/src/com/android/providers/media/MediaScannerReceiver.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.providers.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MediaScannerReceiver extends BroadcastReceiver {
    private final static String TAG = "MediaScannerReceiver";
    private static boolean mBootComplete = false;
    private static ArrayList<String> mScanningPath = new ArrayList<String>();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();
        Log.d(TAG, "my action: " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Scan internal only.
            scan(context, MediaProvider.INTERNAL_VOLUME);
			scan(context, Environment.getRootDirectory() + "/media");
            scan(context, Environment.getExternalStorageDirectory().toString());
            mBootComplete = true;
            if (!mScanningPath.isEmpty()) {
                scan(context, mScanningPath.get(0));
            }
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            scanTranslatable(context);
        } else if (MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            String path = intent.getStringExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_STORAGE);
            Log.d(TAG, "INTENT_ACTION_MEDIA_SCANNER_FINISHED path : " + path);
            Log.d(TAG, "(!mScanningPath.isEmpty()) : " + (!mScanningPath.isEmpty()));
            Log.d(TAG, "mScanningPath.size() : " + mScanningPath.size());
            for(String ss : mScanningPath) {
                Log.d(TAG, "1mScanningPath.get() : " + ss);
            }
            if (!mScanningPath.isEmpty()) { 
                if(mScanningPath.get(0).contains(path)) {
                	Log.d(TAG, "path : " + path);
                    mScanningPath.remove(0);
                    for(String ss : mScanningPath) {
                        Log.d(TAG, "2mScanningPath.get() : " + ss);
                    }
                }
            }
            Log.d(TAG, "(!mScanningPath.isEmpty()) : " + (!mScanningPath.isEmpty()));
            if ((mBootComplete) && (!mScanningPath.isEmpty())) {
                scan(context, mScanningPath.get(0));
            }
        } else {
            if (uri.getScheme().equals("file")) {
                // handle intents related to external storage
                String path = uri.getPath();
                String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
                String legacyPath = Environment.getLegacyExternalStorageDirectory().getPath();

                try {
                    path = new File(path).getCanonicalPath();
                } catch (IOException e) {
                    Log.e(TAG, "couldn't canonicalize " + path);
                    return;
                }
                if (path.startsWith(legacyPath)) {
                    path = externalStoragePath + path.substring(legacyPath.length());
                }

                Log.d(TAG, "action: " + action + " path: " + path);
                if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    // scan whenever any volume is mounted
                    scan(context, MediaProvider.EXTERNAL_VOLUME);
                	boolean isScanEnable = SystemProperties.getBoolean("tcc.solution.scan.enable", true);
                	
                	if( isScanEnable ) {
                        Log.i(TAG, "ACTION_MEDIA_MOUNTED BootComplete is " + mBootComplete + "/ ScanningPath : " + ((false == mScanningPath.isEmpty()) ? mScanningPath.get(0) : "null"));
                        if (false == path.equals(Environment.getExternalStorageDirectory().toString())) {
                            mScanningPath.add(path);
                        }
                        if ((mBootComplete) && (1 == mScanningPath.size())) {
                            scan(context, path);
                        }
                	}
                	else {
                		Log.i(TAG, "tcc.solution.scan.enable is " + isScanEnable + ". skip media scanning for " + path);
                		SystemProperties.set("tcc.solution.scan.need", "true");
                	}
                } else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                    Log.d(TAG, "ACTION_MEDIA_EJECT path : " + path + "/ ScanningPath : " + ((false == mScanningPath.isEmpty()) ? mScanningPath.get(0) : "null"));
                    if (mScanningPath.contains(path)) {
                        mScanningPath.remove(path);
                    }
                    if ((mBootComplete) && (false == mScanningPath.isEmpty())) {
                        scan(context, mScanningPath.get(0));
                    }
                } else if (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE.equals(action) &&
                        path != null && path.startsWith(externalStoragePath + "/")) {
                    scanFile(context, path);
                }
            }
        }
    }

    private void scan(Context context, String volume) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }

    private void scanFile(Context context, String path) {
        Bundle args = new Bundle();
        args.putString("filepath", path);
        context.startService(
                new Intent(context, MediaScannerService.class).putExtras(args));
    }

    private void scanTranslatable(Context context) {
        final Bundle args = new Bundle();
        args.putBoolean(MediaStore.RETRANSLATE_CALL, true);
        context.startService(new Intent(context, MediaScannerService.class).putExtras(args));
    }
}
