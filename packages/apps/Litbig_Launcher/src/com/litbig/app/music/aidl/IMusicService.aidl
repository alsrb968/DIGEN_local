package com.litbig.app.music.aidl;

import com.litbig.app.music.aidl.IMusicServiceCallback;

import android.graphics.Bitmap;

interface IMusicService {
	boolean registerMusicServiceCallback(IMusicServiceCallback callback);
	boolean unregisterMusicServiceCallback(IMusicServiceCallback callback);
	void setMusicMode(boolean start);
	void finish();
	//keep in sync with the MusicPlayerInterface.java
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
	void requestList(int listType, in String subCategory);
	Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale);
	int getNowPlayingCategory();
	String getNowPlayingSubCategory();
	boolean isNowPlayingCategory(int category, in String subCategory);
}