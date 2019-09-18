package com.litbig.app.photo.aidl;

import android.graphics.Bitmap;

public interface PhotoPlayerInterface {
	//keep in sync with the IPhotoService.aidl
	int getPlayingIndex();
	String getFileFullPath(int index, boolean isNowPlaying);
	int getPlayState();
	int getPlayIntervalMS();
	void setPlayIntervalMS(int intervalMS);
	void play();
	void pause();
	boolean playIndex(int index, boolean isNowPlaying);
	void playPrev();
	void playNext();
	int getShuffle();
	void setShuffle();
	void requestList(int listType, String subCategory);
	Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale);
	int getNowPlayingCategory();
	String getNowPlayingSubCategory();
	boolean isNowPlayingCategory(int category, String subCategory);
}
