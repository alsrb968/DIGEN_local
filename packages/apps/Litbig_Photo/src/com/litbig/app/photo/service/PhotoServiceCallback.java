package com.litbig.app.photo.service;

import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.IPhotoServiceCallback;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;
import com.litbig.app.photo.aidl.PhotoPlayerCallbackInterface;

import android.graphics.Bitmap;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class PhotoServiceCallback<E extends IPhotoServiceCallback> extends RemoteCallbackList<E> implements PhotoPlayerCallbackInterface {
	private boolean mCallbackProcessing = false;

	private void checkCallbackProcessing() {
		while (mCallbackProcessing) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mCallbackProcessing = true;
	}

	@Override
	public void onTotalCount(int totalCount) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onTotalCount(totalCount);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onPlayState(int playState) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onPlayState(playState);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onPhotoInfo(int index, PhotoInfo info) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
			 	IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onPhotoInfo(index, info);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onImageBitmap(int index, Bitmap imageBitmap) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onImageBitmap(index, imageBitmap);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onShuffleState(shuffle);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onFileInfo(int index, FileInfo info) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onFileInfo(index, info);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onListInfo(ListInfo info) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IPhotoServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onListInfo(info);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}
}
