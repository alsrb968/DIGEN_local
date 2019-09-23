package com.litbig.app.music.aidl;

import android.graphics.Bitmap;

public interface MusicPlayerCallbackInterface {
	//keep in sync with the IMusicServiceCallback.aidl
	void onTotalCount(int totalCount);
	void onPlayState(int playState);
	void onPlayTimeMS(int playTimeMS);
	void onMusicInfo(int index, MusicInfo info);
	void onAlbumArt(int index, Bitmap albumArt);
	void onShuffleState(int shuffle);
	void onRepeatState(int repeat);
	void onScanState(int scan);
	void onListState(int listState);
	void onListInfo(ListInfo info);
	void onError(String error);
}
