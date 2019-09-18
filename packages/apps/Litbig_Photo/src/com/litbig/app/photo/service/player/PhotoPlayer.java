package com.litbig.app.photo.service.player;

import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.IPhotoServiceCallback;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;
import com.litbig.app.photo.aidl.PhotoPlayerCallbackInterface;
import com.litbig.app.photo.aidl.PhotoPlayerInterface;
import com.litbig.app.photo.service.PhotoPlaybackService;
import com.litbig.app.photo.service.PhotoServiceCallback;

import android.graphics.Bitmap;

public abstract class PhotoPlayer implements PhotoPlayerInterface, PhotoPlayerCallbackInterface {
	protected PhotoPlaybackService mService;
	private PhotoServiceCallback<IPhotoServiceCallback> mCallback;

	public PhotoPlayer(PhotoPlaybackService service) {
		mService = service;
		mCallback = service.getCallback();
	}

	public abstract void active();
	public abstract void inactive();
	public abstract void destroy();

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
	public void onPhotoInfo(int index, PhotoInfo info) {
		mCallback.onPhotoInfo(index, info);
	}

	@Override
	public void onImageBitmap(int index, Bitmap imageBitmap) {
		mCallback.onImageBitmap(index, imageBitmap);
	}

	@Override
	public void onShuffleState(int shuffle) {
		mCallback.onShuffleState(shuffle);
	}

	@Override
	public void onFileInfo(int index, FileInfo info) {
		mCallback.onFileInfo(index, info);
	}

	@Override
	public void onListInfo(ListInfo info) {
		mCallback.onListInfo(info);
	}
}
