// IMovieService.aidl
package com.litbig.app.movie.aidl;

import com.litbig.app.movie.aidl.IMovieServiceCallback;

import android.graphics.Bitmap;
import android.view.Surface;

// Declare any non-default types here with import statements

interface IMovieService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	boolean registerMovieServiceCallback(IMovieServiceCallback callback);
	boolean unregisterMovieServiceCallback(IMovieServiceCallback callback);
	void setMovieMode();
	void showList(boolean show);
	//keep in sync with the MoviePlayerInterface.java
	void setSurface(in Surface surface);
	int getPlayingIndex();
	String getFileFullPath(int index, boolean isNowPlaying);
	int getPlayState();
	int getPlayTimeMS();
	void setPlayTimeMS(int playTimeMS);
	void gripTimeProgressBar();
	void play();
	void pause();
	boolean playIndex(int index, boolean isNowPlaying);
	void playPrev();
	void playNext();
	void startFastForward();
	void stopFastForward();
	void startFastRewind();
	void stopFastRewind();
	int getShuffle();
	int getRepeat();
	void setShuffle();
	void setRepeat();
	void requestList(int listType, in String subCategory);
	Bitmap getVideoThumbnail(int index, boolean isNowPlaying, boolean isScale);
	int getNowPlayingCategory();
	String getNowPlayingSubCategory();
	boolean isNowPlayingCategory(int category, in String subCategory);
}
