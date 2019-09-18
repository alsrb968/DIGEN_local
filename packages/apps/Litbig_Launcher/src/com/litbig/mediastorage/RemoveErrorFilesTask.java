package com.litbig.mediastorage;

import android.content.Context;
import android.provider.MediaStore;

class RemoveErrorFilesTask implements Runnable {
	private Context mContext;

	public RemoveErrorFilesTask(Context context) {
		mContext = context;
	}

	@Override
	public void run() {
		int index;
		for (index = 0; index < MediaErrorFiles.audio.size(); index++) {
			mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "=?", new String[] {MediaErrorFiles.audio.get(index)});
		}
		for (index = 0; index < MediaErrorFiles.video.size(); index++) {
			mContext.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "=?", new String[] {MediaErrorFiles.video.get(index)});
		}
		for (index = 0; index < MediaErrorFiles.image.size(); index++) {
			mContext.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[] {MediaErrorFiles.image.get(index)});
		}
		MediaErrorFiles.audio.clear();
		MediaErrorFiles.video.clear();
		MediaErrorFiles.image.clear();
	}
}
