package com.litbig.app.music.service.player;

import com.litbig.app.music.aidl.IMusicServiceCallback;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.aidl.MusicPlayerCallbackInterface;
import com.litbig.app.music.aidl.MusicPlayerInterface;
import com.litbig.app.music.service.MusicPlaybackService;
import com.litbig.app.music.service.MusicServiceCallback;

import android.graphics.Bitmap;

public abstract class MusicPlayer implements MusicPlayerInterface, MusicPlayerCallbackInterface {
	protected MusicPlaybackService mService;
	private MusicServiceCallback<IMusicServiceCallback> mCallback;

	public MusicPlayer(MusicPlaybackService service) {
		mService = service;
		mCallback = service.getCallback();
	}

	public abstract void active(boolean start);
	public abstract void inactive();
	public abstract void destroy();
	public abstract void setVolume(float volume);

	// ----------
	// MusicPlayerCallbackInterface APIs
	@Override
	public void onTotalCount(int totalCount) {
		mCallback.onTotalCount(totalCount);
	}

	@Override
	public void onPlayState(int playState) {
		mCallback.onPlayState(playState);
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) {
		mCallback.onPlayTimeMS(playTimeMS);
	}

	@Override
	public void onMusicInfo(int index, MusicInfo info) {
		mCallback.onMusicInfo(index, info);
	}

	@Override
	public void onAlbumArt(int index, Bitmap albumArt) {
		mCallback.onAlbumArt(index, albumArt);
	}

	@Override
	public void onShuffleState(int shuffle) {
		mCallback.onShuffleState(shuffle);
	}

	@Override
	public void onRepeatState(int repeat) {
		mCallback.onRepeatState(repeat);
	}

	@Override
	public void onScanState(int scan) {
		mCallback.onScanState(scan);
	}

	@Override
	public void onListInfo(ListInfo info) {
		mCallback.onListInfo(info);
	}

	@Override
	public void onError(String error) {
		mCallback.onError(error);
	}
}
