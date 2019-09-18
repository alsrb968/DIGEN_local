package com.litbig.app.movie.service.player;

import com.litbig.app.movie.aidl.IMovieServiceCallback;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;
import com.litbig.app.movie.aidl.MoviePlayerCallbackInterface;
import com.litbig.app.movie.aidl.MoviePlayerInterface;
import com.litbig.app.movie.service.MoviePlaybackService;
import com.litbig.app.movie.service.MovieServiceCallback;

import android.graphics.Bitmap;

public abstract class MoviePlayer implements MoviePlayerInterface, MoviePlayerCallbackInterface {
	protected MoviePlaybackService mService;
	private MovieServiceCallback<IMovieServiceCallback> mCallback;

	public MoviePlayer(MoviePlaybackService service) {
		mService = service;
		mCallback = service.getCallback();
	}

	public abstract void active();
	public abstract void inactive();
	public abstract void destroy();
	public abstract void setVolume(float volume);

	// ----------
	// MoviePlayerCallbackInterface APIs
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
	public void onMovieInfo(int index, MovieInfo info) {
		mCallback.onMovieInfo(index, info);
	}

	@Override
	public void onVideoThumbnail(int index, Bitmap videoThumbnail) {
		mCallback.onVideoThumbnail(index, videoThumbnail);
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
	public void onSubtitle(String text) {
		mCallback.onSubtitle(text);
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
