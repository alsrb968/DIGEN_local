package com.litbig.app.music.aidl;

import android.graphics.Bitmap;

public interface MusicPlayerInterface {
	//keep in sync with the IMusicService.aidl
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
	int getScan();
	void setShuffle();
	void setRepeat();
	void setScan();
	void requestList(int listType, String subCategory);
	Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale);
	int getNowPlayingCategory();
	String getNowPlayingSubCategory();
	boolean isNowPlayingCategory(int category, String subCategory);
}
