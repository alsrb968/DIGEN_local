package com.litbig.mediastorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class MediaStorage {
	public static final String INTENT_ACTION_MEDIA_PREPARED = "com.litbig.intent.action.MEDIA_PREPARED";
	public static final String INTENT_ACTION_MEDIA_SCANNER_STARTED = "com.litbig.intent.action.MEDIA_SCANNER_STARTED";
	public static final String INTENT_ACTION_MEDIA_SCANNER_FINISHED = "com.litbig.intent.action.MEDIA_SCANNER_FINISHED";
	public static final String INTENT_ACTION_MEDIA_EJECT = "com.litbig.intent.action.MEDIA_EJECT";
	public static final String INTENT_EXTRA_MEDIA_STORAGE = "com.litbig.intent.extra.MEDIA_STORAGE";
	public static final String INTENT_EXTRA_MEDIA_SCAN_COMPLETE = "com.litbig.intent.extra.MEDIA_SCAN_COMPLETE";
	public static final String INTENT_EXTRA_MEDIA_SCAN_STORAGE = "com.litbig.intent.extra.MEDIA_SCAN_STORAGE";

	public static ArrayList<String> getMountedStorage(Context context) {
		return loadMountedStorage(context);
	}

	public static ArrayList<String> getEnableStorage(Context context) {
		ArrayList<String> enableStorage = new ArrayList<String>();
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		logMountedStorage(context, mountedStorage);
		for (int index = 0; index < mountedStorage.size(); index++) {
			String storagePath = mountedStorage.get(index);
			String mountedStorageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
			boolean scanFinish = getSharedPreference(context).getBoolean(mountedStorageName + "_scanFinish", false);
			if (true == scanFinish) {
				enableStorage.add(storagePath);
			}
		}
		return enableStorage;
	}

	public static boolean isMusicEnable(Context context) {
		boolean enable = false;
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		logMountedStorage(context, mountedStorage);
		for (int index = 0; index < mountedStorage.size(); index++) {
			String storagePath = mountedStorage.get(index);
			String mountedStorageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
			android.util.Log.i("Litbig_Media", "mountedStorageName : " + mountedStorageName);
			boolean enableMusic = getSharedPreference(context).getBoolean(mountedStorageName + "_enableMusic", false);
			boolean scanFinish = getSharedPreference(context).getBoolean(mountedStorageName + "_scanFinish", false);
			if ((true == enableMusic) && (true == scanFinish)) {
				enable = true;
			}
		}
		return enable;
	}

	public static boolean isMovieEnable(Context context) {
		boolean enable = false;
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		logMountedStorage(context, mountedStorage);
		for (int index = 0; index < mountedStorage.size(); index++) {
			String storagePath = mountedStorage.get(index);
			String mountedStorageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
			boolean enableMovie = getSharedPreference(context).getBoolean(mountedStorageName + "_enableMovie", false);
			boolean scanFinish = getSharedPreference(context).getBoolean(mountedStorageName + "_scanFinish", false);
			if ((true == enableMovie) && (true == scanFinish)) {
				enable = true;
			}
		}
		return enable;
	}

	public static boolean isPhotoEnable(Context context) {
		boolean enable = false;
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		logMountedStorage(context, mountedStorage);
		for (int index = 0; index < mountedStorage.size(); index++) {
			String storagePath = mountedStorage.get(index);
			String mountedStorageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
			boolean enablePhoto = getSharedPreference(context).getBoolean(mountedStorageName + "_enablePhoto", false);
			boolean scanFinish = getSharedPreference(context).getBoolean(mountedStorageName + "_scanFinish", false);
			if ((true == enablePhoto) && (true == scanFinish)) {
				enable = true;
			}
		}
		return enable;
	}

	static void addMountedStorage(Context context, String storagePath) {
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		if (false == mountedStorage.contains(storagePath)) {
			mountedStorage.add(storagePath);
			saveMountedStorage(context, mountedStorage);
		}
		SharedPreferences.Editor prefEdit = getSharedPreference(context).edit();
		String mountedSotrageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
		prefEdit.putBoolean(mountedSotrageName + "_enableMusic", false);
		prefEdit.putBoolean(mountedSotrageName + "_enableMovie", false);
		prefEdit.putBoolean(mountedSotrageName + "_enablePhoto", false);
		prefEdit.putBoolean(mountedSotrageName + "_scanFinish", false);
		prefEdit.commit();
		logMountedStorage(context, loadMountedStorage(context));
	}

	static void removeMountedStorage(Context context, String storagePath) {
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		mountedStorage.remove(storagePath);
		saveMountedStorage(context, mountedStorage);
		logMountedStorage(context, loadMountedStorage(context));
	}

	static void setMediaEnable(Context context, String storagePath, boolean musicEnable, boolean movieEnable, boolean photoEnable) {
		SharedPreferences.Editor prefEdit = getSharedPreference(context).edit();
		String mountedSotrageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
		prefEdit.putBoolean(mountedSotrageName + "_enableMusic", musicEnable);
		prefEdit.putBoolean(mountedSotrageName + "_enableMovie", movieEnable);
		prefEdit.putBoolean(mountedSotrageName + "_enablePhoto", photoEnable);
		prefEdit.commit();
		logMountedStorage(context, loadMountedStorage(context));
	}

	static void setMediaScanFinish(Context context, String storagePath, boolean scanFinish) {
		SharedPreferences.Editor prefEdit = getSharedPreference(context).edit();
		String mountedSotrageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
		prefEdit.putBoolean(mountedSotrageName + "_scanFinish", scanFinish);
		prefEdit.commit();
		logMountedStorage(context, loadMountedStorage(context));
	}

	static boolean isEnableStorage(Context context, String storagePath) {
		boolean enable = false;
		ArrayList<String> mountedStorage = loadMountedStorage(context);
		String mountedSotrageName = storagePath.substring(storagePath.lastIndexOf("/") + 1);
		boolean scanFinish = getSharedPreference(context).getBoolean(mountedSotrageName + "_scanFinish", false);
		if ((true == mountedStorage.contains(storagePath)) && (true == scanFinish)) {
			enable = true;
		}
		return enable;
	}

	private static SharedPreferences getSharedPreference(Context context) {
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
		ArrayList<String> mountedStorage = new ArrayList<String>();
		String loadString = getSharedPreference(context).getString("mounted_storage", null);
		if (null != loadString) {
//			android.util.Log.i("Litbig_Media", "loadMountedStorage : " + loadString);
			List<String> list = Arrays.asList(TextUtils.split(loadString, ","));
			for (int index = 0; index < list.size(); index++) {
				mountedStorage.add(list.get(index));
			}
		}
		return mountedStorage;
	}

	private static void saveMountedStorage(Context context, ArrayList<String> mountedStorage) {
		List<String> list = new ArrayList<String>();
		for (int index = 0; index < mountedStorage.size(); index++) {
			list.add(mountedStorage.get(index));
		}
		SharedPreferences.Editor prefEdit = getSharedPreference(context).edit();
		if (0 < list.size()) {
			String saveString = TextUtils.join(",", list);
			prefEdit.putString("mounted_storage", saveString);
//			android.util.Log.i("Litbig_Media", "saveMountedStorage : " + saveString);
		} else {
			prefEdit.putString("mounted_storage", null);
		}
		prefEdit.commit();
	}

	private static void logMountedStorage(Context context, ArrayList<String> mountedStorage) {
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
