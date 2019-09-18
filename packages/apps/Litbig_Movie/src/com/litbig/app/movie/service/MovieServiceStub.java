package com.litbig.app.movie.service;

import com.litbig.app.movie.aidl.IMovieService;
import com.litbig.app.movie.aidl.IMovieServiceCallback;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.view.Surface;

public class MovieServiceStub extends IMovieService.Stub {
	private MoviePlaybackService mService;

	public MovieServiceStub(MoviePlaybackService service) {
		mService = service;
	}

	@Override
	public boolean registerMovieServiceCallback(IMovieServiceCallback callback) throws RemoteException {
		return mService.registerCallback(callback);
	}

	@Override
	public boolean unregisterMovieServiceCallback(IMovieServiceCallback callback) throws RemoteException {
		return mService.unregisterCallback(callback);
	}

	@Override
	public void setMovieMode() throws RemoteException {
		mService.setMovieMode();
	}

	@Override
	public void showList(boolean show) throws RemoteException {
		mService.showList(show);
	}

	@Override
	public void setSurface(Surface surface) throws RemoteException {
		mService.getMoviePlayer().setSurface(surface);
	}

	@Override
	public int getPlayingIndex() throws RemoteException {
		return mService.getMoviePlayer().getPlayingIndex();
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getMoviePlayer().getFileFullPath(index, isNowPlaying);
	}

	@Override
	public int getPlayState() throws RemoteException {
		return mService.getMoviePlayer().getPlayState();
	}

	@Override
	public int getPlayTimeMS() throws RemoteException {
		return mService.getMoviePlayer().getPlayTimeMS();
	}

	@Override
	public void setPlayTimeMS(int playTimeMS) throws RemoteException {
		mService.getMoviePlayer().setPlayTimeMS(playTimeMS);
	}

	@Override
	public void gripTimeProgressBar() throws RemoteException {
		mService.getMoviePlayer().gripTimeProgressBar();
	}

	@Override
	public void play() throws RemoteException {
		mService.getMoviePlayer().play();
	}

	@Override
	public void pause() throws RemoteException {
		mService.getMoviePlayer().pause();
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getMoviePlayer().playIndex(index, isNowPlaying);
	}

	@Override
	public void playPrev() throws RemoteException {
		mService.getMoviePlayer().playPrev();
	}

	@Override
	public void playNext() throws RemoteException {
		mService.getMoviePlayer().playNext();
	}

	@Override
	public void startFastForward() throws RemoteException {
		mService.getMoviePlayer().startFastForward();
	}

	@Override
	public void stopFastForward() throws RemoteException {
		mService.getMoviePlayer().stopFastForward();
	}

	@Override
	public void startFastRewind() throws RemoteException {
		mService.getMoviePlayer().startFastRewind();
	}

	@Override
	public void stopFastRewind() throws RemoteException {
		mService.getMoviePlayer().stopFastRewind();
	}

	@Override
	public int getShuffle() throws RemoteException {
		return mService.getMoviePlayer().getShuffle();
	}

	@Override
	public int getRepeat() throws RemoteException {
		return mService.getMoviePlayer().getRepeat();
	}

	@Override
	public void setShuffle() throws RemoteException {
		mService.getMoviePlayer().setShuffle();
	}

	@Override
	public void setRepeat() throws RemoteException {
		mService.getMoviePlayer().setRepeat();
	}

	@Override
	public void requestList(int listType, String subCategory) throws RemoteException {
		mService.getMoviePlayer().requestList(listType, subCategory);
	}

	@Override
	public Bitmap getVideoThumbnail(int index, boolean isNowPlaying, boolean isScale) throws RemoteException {
		return mService.getMoviePlayer().getVideoThumbnail(index, isNowPlaying, isScale);
	}

	@Override
	public int getNowPlayingCategory() throws RemoteException {
		return mService.getMoviePlayer().getNowPlayingCategory();
	}

	@Override
	public String getNowPlayingSubCategory() throws RemoteException {
		return mService.getMoviePlayer().getNowPlayingSubCategory();
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) throws RemoteException {
		return mService.getMoviePlayer().isNowPlayingCategory(category, subCategory);
	}
}
