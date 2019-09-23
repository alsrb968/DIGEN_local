package com.litbig.mediastorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

import com.litbig.app.music.util.Log;

public class MediaStorage {
	public static String getRootDirectoryName() {
		return "/storage/";
	}
    private static Context mContext = null;

    public static final String AUTHORITY = "com.litbig.mediastorage";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Preference {
        public static final String MOUNTEDSTORAGE = "MOUNTED_STORAGE";
        public static final String MUSICENABLE = "MUSIC_ENABLE";
        public static final String MOVIEENABLE = "MOVIE_ENABLE";
        public static final String PHOTOENABLE = "PHOTO_ENABLE";
        public static final String ENABLESTORAGE = "ENABLE_STORAGE";
    }

    public static List<String> getPreference(String pref) {
        Log.d("Auth get: " + pref);

        ContentResolver cr = mContext.getContentResolver();
        Uri reqUri = Uri.parse(CONTENT_URI + "/" + pref);
        Uri uri = cr.insert(reqUri, new ContentValues());

        List<String> authValues = null;
        try {
            authValues = uri.getPathSegments();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("NullPointerException");
        }
        if (authValues == null) {
            Log.d("authValues is Null");
            return null;
        } else {
            Log.d("authValues: " + authValues.toString());
        }
        String preference = authValues.get(0);
        ArrayList<String> values = new ArrayList<>();
        for (int i = 1; i < authValues.size(); i++) {
            values.add(authValues.get(i));
        }

        return values;
    }

	public static final String INTENT_ACTION_MEDIA_PREPARED = "com.litbig.intent.action.MEDIA_PREPARED";
	public static final String INTENT_ACTION_MEDIA_SCANNER_STARTED = "com.litbig.intent.action.MEDIA_SCANNER_STARTED";
	public static final String INTENT_ACTION_MEDIA_SCANNER_FINISHED = "com.litbig.intent.action.MEDIA_SCANNER_FINISHED";
	public static final String INTENT_ACTION_MEDIA_EJECT = "com.litbig.intent.action.MEDIA_EJECT";
	public static final String INTENT_EXTRA_MEDIA_STORAGE = "com.litbig.intent.extra.MEDIA_STORAGE";
	public static final String INTENT_EXTRA_MEDIA_SCAN_COMPLETE = "com.litbig.intent.extra.MEDIA_SCAN_COMPLETE";

	public static ArrayList<String> getMountedStorage(Context context) {
        if (mContext == null) mContext = context;

        List<String> prefValues = getPreference(Preference.MOUNTEDSTORAGE);
        ArrayList<String> mountedStorage = new ArrayList<>();

		StringBuilder storage = new StringBuilder();
        if(prefValues != null) {
            for (String s : prefValues) {
            	if (s.equals("storage")) {
            		if (storage.length() > 0) {
            			mountedStorage.add(storage.toString());
					}
            		storage = new StringBuilder();
				}
            	storage.append("/").append(s);
			}
        }
		if (storage.length() > 0) {
			mountedStorage.add(storage.toString());
		}
		Log.i("MOUNTEDSTORAGE: " + mountedStorage.toString());
		return /*loadMountedStorage(context)*/mountedStorage;
	}

	public static ArrayList<String> getEnableStorage(Context context) {
        if (mContext == null) mContext = context;

		List<String> prefValues = getPreference(Preference.ENABLESTORAGE);
		ArrayList<String> enableStorage = new ArrayList<String>();

		StringBuilder storage = new StringBuilder();
		if(prefValues != null) {
			for (String s : prefValues) {
				if (s.equals("storage")) {
					if (storage.length() > 0) {
						enableStorage.add(storage.toString());
					}
					storage = new StringBuilder();
				}
				storage.append("/").append(s);
			}
		}
		if (storage.length() > 0) {
			enableStorage.add(storage.toString());
		}
		Log.i("ENABLESTORAGE: " + enableStorage.toString());
		return enableStorage;
	}

	public static boolean isMusicEnable(Context context) {
        if (mContext == null) mContext = context;

		boolean enable = false;

		List<String> prefValues = getPreference(Preference.MUSICENABLE);
		if(prefValues != null) {
			Log.i("MUSICENABLE: " + prefValues.toString());
			if (prefValues.get(0).equals("true")) {
                enable = true;
			}
		}

		return enable;
	}

	public static boolean isMovieEnable(Context context) {
        if (mContext == null) mContext = context;

		boolean enable = false;

		List<String> prefValues = getPreference(Preference.MOVIEENABLE);
		if(prefValues != null) {
			Log.i("MOVIEENABLE: " + prefValues.toString());
			if (prefValues.get(0).equals("true")) {
				enable = true;
			}
		}

		return enable;
	}

	public static boolean isPhotoEnable(Context context) {
        if (mContext == null) mContext = context;

		boolean enable = false;

		List<String> prefValues = getPreference(Preference.PHOTOENABLE);
		if(prefValues != null) {
			Log.i("PHOTOENABLE: " + prefValues.toString());
			if (prefValues.get(0).equals("true")) {
				enable = true;
			}
		}

		return enable;
	}

	private static SharedPreferences getSharedPreference(Context context) {
        if (mContext == null) mContext = context;

		SharedPreferences pref;
		try {
            pref = (context.createPackageContext("com.litbig.app.launcher", 0)).getSharedPreferences("MediaStorage", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			pref = context.getSharedPreferences("MediaStorage", Context.MODE_PRIVATE);
		}
		return pref;
	}

	private static ArrayList<String> loadMountedStorage(Context context) {
        if (mContext == null) mContext = context;

		ArrayList<String> mountedStorage = new ArrayList<String>();
		String loadString = getSharedPreference(context).getString("mounted_storage", null);
		if (null != loadString) {
//			android.util.Log.i("Litbig_Media", "loadMountedStorage : " + loadString);
			List<String> list = Arrays.asList(TextUtils.split(loadString, ","));
			mountedStorage.addAll(list);
		}
		return mountedStorage;
	}

	private static void logMountedStorage(Context context, ArrayList<String> mountedStorage) {
        if (mContext == null) mContext = context;
        
//		for (int index = 0; index < mountedStorage.size(); index++) {
//			String storagePath = mountedStorage.get(index);
//			String mountedSotrageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
//			android.util.Log.i("Litbig_Media", "MountedStorage(" + storagePath + ") "
//					+ ": enableMusic = " + getSharedPreference(context).getBoolean(mountedSotrageName + "_enableMusic", false)
//					+ ", enableMovie = " + getSharedPreference(context).getBoolean(mountedSotrageName + "_enableMovie", false)
//					+ ", enablePhoto = " + getSharedPreference(context).getBoolean(mountedSotrageName + "_enablePhoto", false)
//					+ ", scanFinish = " + getSharedPreference(context).getBoolean(mountedSotrageName + "_scanFinish", false));
//		}
	}
}
