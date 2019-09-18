package com.litbig.app.photo.service;

import com.litbig.app.photo.aidl.IPhotoService;
import com.litbig.app.photo.aidl.IPhotoServiceCallback;

import android.graphics.Bitmap;
import android.os.RemoteException;

public class PhotoServiceStub extends IPhotoService.Stub {
	private PhotoPlaybackService mService;

	public PhotoServiceStub(PhotoPlaybackService service) {
		mService = service;
	}

	@Override
	public boolean registerPhotoServiceCallback(IPhotoServiceCallback callback) throws RemoteException {
		return mService.registerCallback(callback);
	}

	@Override
	public boolean unregisterPhotoServiceCallback(IPhotoServiceCallback callback) throws RemoteException {
		return mService.unregisterCallback(callback);
	}

	@Override
	public void setPhotoMode() throws RemoteException {
		mService.setPhotoMode();
	}

	@Override
	public int getPlayingIndex() throws RemoteException {
		return mService.getPhotoPlayer().getPlayingIndex();
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getPhotoPlayer().getFileFullPath(index, isNowPlaying);
	}

	@Override
	public int getPlayState() throws RemoteException {
		return mService.getPhotoPlayer().getPlayState();
	}

	@Override
	public int getPlayIntervalMS() throws RemoteException {
		return mService.getPhotoPlayer().getPlayIntervalMS();
	}

	@Override
	public void setPlayIntervalMS(int intervalMS) throws RemoteException {
		mService.getPhotoPlayer().setPlayIntervalMS(intervalMS);
	}

	@Override
	public void play() throws RemoteException {
		mService.getPhotoPlayer().play();
	}

	@Override
	public void pause() throws RemoteException {
		mService.getPhotoPlayer().pause();
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) throws RemoteException {
		return mService.getPhotoPlayer().playIndex(index, isNowPlaying);
	}

	@Override
	public void playPrev() throws RemoteException {
		mService.getPhotoPlayer().playPrev();
	}

	@Override
	public void playNext() throws RemoteException {
		mService.getPhotoPlayer().playNext();
	}

	@Override
	public int getShuffle() throws RemoteException {
		return mService.getPhotoPlayer().getShuffle();
	}

	@Override
	public void setShuffle() throws RemoteException {
		mService.getPhotoPlayer().setShuffle();
	}

	@Override
	public void requestList(int listType, String subCategory) throws RemoteException {
		mService.getPhotoPlayer().requestList(listType, subCategory);
	}

	@Override
	public Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale) throws RemoteException {
		return mService.getPhotoPlayer().getImageBitmap(index, isNowPlaying, isScale);
	}

	@Override
	public int getNowPlayingCategory() throws RemoteException {
		return mService.getPhotoPlayer().getNowPlayingCategory();
	}

	@Override
	public String getNowPlayingSubCategory() throws RemoteException {
		return mService.getPhotoPlayer().getNowPlayingSubCategory();
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) throws RemoteException {
		return mService.getPhotoPlayer().isNowPlayingCategory(category, subCategory);
	}
}
