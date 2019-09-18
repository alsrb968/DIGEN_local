// IPhotoService.aidl
package com.litbig.app.photo.aidl;

import com.litbig.app.photo.aidl.IPhotoServiceCallback;

import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IPhotoService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	boolean registerPhotoServiceCallback(IPhotoServiceCallback callback);
	boolean unregisterPhotoServiceCallback(IPhotoServiceCallback callback);
	void setPhotoMode();
	//keep in sync with the PhotoPlayerInterface.java
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
	void requestList(int listType, in String subCategory);
	Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale);
	int getNowPlayingCategory();
	String getNowPlayingSubCategory();
	boolean isNowPlayingCategory(int category, in String subCategory);
}
