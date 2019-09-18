package com.litbig.app.music.aidl;

import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;

import android.graphics.Bitmap;

interface IMusicServiceCallback {
	//keep in sync with the MusicPlayerCallbackInterface.java
	oneway void onTotalCount(int totalCount);
	oneway void onPlayState(int playState);
	oneway void onPlayTimeMS(int playTimeMS);
	oneway void onMusicInfo(int index, in MusicInfo info);
	oneway void onAlbumArt(int index, in Bitmap albumArt);
	oneway void onShuffleState(int shuffle);
	oneway void onRepeatState(int repeat);
	oneway void onScanState(int scan);
	oneway void onListInfo(in ListInfo info);
	oneway void onError(in String error);
}