package com.litbig.app.music.service;

import com.litbig.app.music.aidl.IMusicService;
import com.litbig.app.music.aidl.IMusicServiceCallback;

import android.graphics.Bitmap;
import android.os.RemoteException;

public class MusicServiceStub extends IMusicService.Stub {
	private MusicPlaybackService mService;

	public MusicServiceStub(MusicPlaybackService service) {
		mService = service;
	}

	@Override
	public boolean registerMusicServiceCallback(IMusicServiceCallback callback) throws RemoteException {
		return mService.registerCallback(callback);
	}

	@Override
	public boolean unregisterMusicServiceCallback(IMusicServiceCallback callback) throws RemoteException {
		return mService.unregisterCallback(callback);
	}

	@Override
	public void setMusicMode(boolean start) throws RemoteException {
		mService.setMusicMode(start);
	}

	@Override
	public void finish() throws RemoteException {
		mService.getMusicPlayer().pause();
	}

	@Override
	public int getPlayingIndex() throws RemoteException {
		return mService.getMusicPlayer().getPlayingIndex();
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getMusicPlayer().getFileFullPath(index, isNowPlaying);
	}

	@Override
	public int getPlayState() throws RemoteException {
		return mService.getMusicPlayer().getPlayState();
	}

	@Override
	public int getPlayTimeMS() throws RemoteException {
		return mService.getMusicPlayer().getPlayTimeMS();
	}

	@Override
	public void setPlayTimeMS(int playTimeMS) throws RemoteException {
		mService.getMusicPlayer().setPlayTimeMS(playTimeMS);
	}

	@Override
	public void gripTimeProgressBar() throws RemoteException {
		mService.getMusicPlayer().gripTimeProgressBar();
	}

	@Override
	public void play() throws RemoteException {
		mService.setAudioFocus();
		mService.getMusicPlayer().play();
	}

	@Override
	public void pause() throws RemoteException {
		mService.getMusicPlayer().pause();
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getMusicPlayer().playIndex(index, isNowPlaying);
	}

	@Override
	public void playPrev() throws RemoteException {
		mService.setAudioFocus();
		mService.getMusicPlayer().playPrev();
	}

	@Override
	public void playNext() throws RemoteException {
		mService.setAudioFocus();
		mService.getMusicPlayer().playNext();
	}

	@Override
	public void startFastForward() throws RemoteException {
		mService.getMusicPlayer().startFastForward();
	}

	@Override
	public void stopFastForward() throws RemoteException {
		mService.getMusicPlayer().stopFastForward();
	}

	@Override
	public void startFastRewind() throws RemoteException {
		mService.getMusicPlayer().startFastRewind();
	}

	@Override
	public void stopFastRewind() throws RemoteException {
		mService.getMusicPlayer().stopFastRewind();
	}

	@Override
	public int getShuffle() throws RemoteException {
		return mService.getMusicPlayer().getShuffle();
	}

	@Override
	public int getRepeat() throws RemoteException {
		return mService.getMusicPlayer().getRepeat();
	}

	@Override
	public int getScan() throws RemoteException {
		return mService.getMusicPlayer().getScan();
	}

	@Override
	public void setShuffle() throws RemoteException {
		mService.getMusicPlayer().setShuffle();
	}

	@Override
	public void setRepeat() throws RemoteException {
		mService.getMusicPlayer().setRepeat();
	}

	@Override
	public void setScan() throws RemoteException {
		mService.getMusicPlayer().setScan();
	}

	@Override
	public void requestList(int listType, String subCategory) throws RemoteException {
		mService.getMusicPlayer().requestList(listType, subCategory);
	}

	@Override
	public Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale) throws RemoteException {
		return mService.getMusicPlayer().getAlbumArt(index, isNowPlaying, isScale);
	}

	@Override
	public int getNowPlayingCategory() throws RemoteException {
		return mService.getMusicPlayer().getNowPlayingCategory();
	}

	@Override
	public String getNowPlayingSubCategory() throws RemoteException {
		return mService.getMusicPlayer().getNowPlayingSubCategory();
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) throws RemoteException {
		return mService.getMusicPlayer().isNowPlayingCategory(category, subCategory);
	}
}
