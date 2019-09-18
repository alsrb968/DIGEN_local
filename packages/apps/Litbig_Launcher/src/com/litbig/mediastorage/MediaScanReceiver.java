package com.litbig.mediastorage;

import java.io.File;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class MediaScanReceiver extends BroadcastReceiver {
	static ArrayList<String> mMediaScanningStorage = new ArrayList<String>();
	static ArrayList<MediaScanTask> mMediaScanTask = new ArrayList<MediaScanTask>();
	public static boolean mMediaPrepared = false;
	public static boolean mMediaScanning = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String data = intent.getDataString().substring(7);
		com.litbig.app.music.util.Log.d(action.substring(action.lastIndexOf(".") + 1) + " : " + data);
		if (true == action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			MediaStorage.addMountedStorage(context, data);
			mMediaScanningStorage.add(data);
		} else if (true == action.equals(Intent.ACTION_MEDIA_EJECT)) {
			for (int index = 0; index < mMediaScanTask.size(); index++) {
				MediaScanTask mediaScanTask = mMediaScanTask.get(index);
				if (true == mediaScanTask.getPath().equals(data)) {
					mediaScanTask.stopTask();
				}
			}
			MediaStorage.removeMountedStorage(context, data);
			mMediaScanningStorage.remove(data);
			Intent sendIntent = new Intent(MediaStorage.INTENT_ACTION_MEDIA_EJECT);
			sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_STORAGE, data);
			context.sendBroadcast(sendIntent);
		} else if (true == action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
			if (true == data.equals(Environment.getRootDirectory() + "/media")) {
				ArrayList<String> mountedStorage = MediaStorage.getMountedStorage(context);
				for (int index = 0; index < mountedStorage.size(); index++) {
					String storagePath = mountedStorage.get(index);
					File[] files = (new File(storagePath)).listFiles();
					if ((null == files) || (0 == files.length)) {
						MediaStorage.removeMountedStorage(context, storagePath);
					}
				}
				Intent sendIntent = new Intent(MediaStorage.INTENT_ACTION_MEDIA_PREPARED);
				context.sendBroadcast(sendIntent);
				mMediaPrepared = true;
			} else if (mMediaScanningStorage.contains(data)) {
				MediaScanTask mediaScanTask = new MediaScanTask(context, data);
				Thread scanThread = new Thread(mediaScanTask);
				scanThread.start();
				mMediaScanTask.add(mediaScanTask);
				Intent sendIntent = new Intent(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_STARTED);
				sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_STORAGE, data);
				context.sendBroadcast(sendIntent);
				mMediaScanning = true;
			}
		} else if (true == action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			if (true == data.startsWith("/storage/")) {
				if (mMediaScanningStorage.contains(data)) {
					mMediaScanningStorage.remove(data);
					MediaStorage.setMediaScanFinish(context, data, true);
				}
				if (true == mMediaScanningStorage.isEmpty()) {
					Thread deleteThread = new Thread(new RemoveErrorFilesTask(context));
					deleteThread.start();
				}
				boolean mediaScanComplate = true;
				for (int index = 0; index < mMediaScanTask.size(); index++) {
					if (true == mMediaScanTask.get(index).getPath().equals(data)) {
						mediaScanComplate = false;
					}
				}
				if (true == mediaScanComplate) {
					Intent sendIntent = new Intent(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
					sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, mMediaScanningStorage.isEmpty());
					sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_STORAGE, data.substring(9));
					context.sendBroadcast(sendIntent);
				}
				mMediaScanning = false;
			}
		}
	}
}
