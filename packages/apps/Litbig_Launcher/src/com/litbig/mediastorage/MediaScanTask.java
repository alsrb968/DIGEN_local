package com.litbig.mediastorage;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
//import android.media.MediaExtractor;
import android.media.MediaFile;
//import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;

class MediaScanTask implements Runnable {
	private static final String TAG = "Litbig_Media";

	private Context mContext;
	private String mPath;
	private boolean mStopTask = false;
	private boolean mMusicEnable = false;
	private boolean mMovieEnable = false;
	private boolean mPhotoEnable = false;

	public MediaScanTask(Context context, String path) {
		mContext = context;
		mPath = path;
	}

	public void stopTask() {
		mStopTask = true;
	}

	public String getPath() {
		return mPath;
	}

	@Override
	public void run() {
		Log.d(TAG, "MediaScanTask start");
		Queue<File> folderQueue = new LinkedList<File>();
		folderQueue.add(new File(mPath + "/"));
		while ((false == mStopTask) && (false == folderQueue.isEmpty())) {
			File[] files = folderQueue.poll().listFiles();
			if ((null != files) && (0 < files.length)) {
				for (File file : files) {
					if (false == mStopTask) {
						if (true == file.isHidden()) {
							continue;
						} else if (true == file.isDirectory()) {
							folderQueue.add(file);
						} else if (0 < file.length()) {
							checkMediaFile(file);
						}
					} else {
						break;
					}
				}
			}
		}
		if (null != folderQueue) {
			folderQueue.clear();
			folderQueue = null;
		}
		MediaStorage.setMediaEnable(mContext, mPath, mMusicEnable, mMovieEnable, mPhotoEnable);
		if (true == MediaStorage.isEnableStorage(mContext, mPath)) {
			Intent sendIntent = new Intent(MediaStorage.INTENT_ACTION_MEDIA_SCANNER_FINISHED);
			sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_COMPLETE, MediaScanReceiver.mMediaScanningStorage.isEmpty());
			sendIntent.putExtra(MediaStorage.INTENT_EXTRA_MEDIA_SCAN_STORAGE, mPath.substring(9));
			mContext.sendBroadcast(sendIntent);
		}
		Log.d(TAG, "setMediaEnable(" + mPath + ", " + mMusicEnable + ", " + mMovieEnable + ", " + mPhotoEnable + ")");
		for (int index = 0; index < MediaScanReceiver.mMediaScanTask.size(); index++) {
			MediaScanTask mediaScanTask = MediaScanReceiver.mMediaScanTask.get(index);
			if (true == mediaScanTask.getPath().equals(mPath)) {
				MediaScanReceiver.mMediaScanTask.remove(mediaScanTask);
			}
		}
	}

	private void checkMediaFile(File file) {
		if ((true == file.exists()) && (true == file.isFile())) {
			int fileType = 0;
			MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(file.getName());
			if (null != mediaFileType) {
				fileType = mediaFileType.fileType;
			}
			if (true == MediaFile.isAudioFileType(fileType)) {
				if (true == isMediaFile(file.getAbsolutePath(), fileType)) {
					mMusicEnable = true;
				}
			} else if (true == MediaFile.isVideoFileType(fileType)) {
				if (true == isMediaFile(file.getAbsolutePath(), fileType)) {
					mMovieEnable = true;
				}
			} else if (true == MediaFile.isImageFileType(fileType)) {
				if (true == isMediaFile(file.getAbsolutePath(), fileType)) {
					mPhotoEnable = true;
				}
			}
		}
	}

	private boolean isMediaFile(String path, int fileType) {
		boolean result = false;
		if (true == MediaFile.isAudioFileType(fileType)) {
			MediaMetadataRetriever metaData = new MediaMetadataRetriever();
			try {
				metaData.setDataSource(path);
				result = true;
			} catch (Exception e) {
				MediaErrorFiles.audio.add(path);
				e.printStackTrace();
			}
			metaData.release();
		} else if (true == MediaFile.isVideoFileType(fileType)) {
			MediaMetadataRetriever metaData = new MediaMetadataRetriever();
			try {
				metaData.setDataSource(path);
/*
				// test FourCC
				String fourcc = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FOURCC);
				int metaInfoFourCC = 0;
				if(fourcc != null && fourcc.equals("") == false)
					metaInfoFourCC = Integer.parseInt(fourcc);

				Log.d(TAG, "******************************************");
				Log.d(TAG, "   file name : " + path.substring(path.lastIndexOf("/")+1));
				Log.d(TAG, "   fourcc: " + metaInfoFourCC + ",   0x" + Integer.toHexString(metaInfoFourCC));

				String fourcc_str = "";
				fourcc_str += (char)((metaInfoFourCC>> 0)&0xff);
				fourcc_str += (char)((metaInfoFourCC>> 8)&0xff);
				fourcc_str += (char)((metaInfoFourCC>>16)&0xff);
				fourcc_str += (char)((metaInfoFourCC>>24)&0xff);
				Log.d(TAG, "   fourcc_str: " + fourcc_str);
				Log.d(TAG ,"******************************************");

				// test audio getFormat
				MediaExtractor extractor = new MediaExtractor();
				extractor.setDataSource(path);
				MediaFormat format = extractor.getTrackFormat(1);
				String mime = format.getString(MediaFormat.KEY_MIME);
				Log.d(TAG, "KEY_MIME : "+mime);
*/
				result = true;
			} catch (Exception e) {
				MediaErrorFiles.video.add(path);
				e.printStackTrace();
			}
			metaData.release();
		} else if (true == MediaFile.isImageFileType(fileType)) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			if ((0 >= options.outWidth) || (0 >= options.outHeight)) {
				MediaErrorFiles.image.add(path);
			} else {
				result = true;
			}
		}
		return result;
	}
}
