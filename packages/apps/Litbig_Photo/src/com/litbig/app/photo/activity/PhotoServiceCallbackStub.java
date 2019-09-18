package com.litbig.app.photo.activity;

import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.IPhotoServiceCallback;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;

import android.graphics.Bitmap;
import android.os.Message;
import android.os.RemoteException;

public class PhotoServiceCallbackStub extends IPhotoServiceCallback.Stub {
	private PhotoServiceCallbackHandler mHandler;

	public PhotoServiceCallbackStub(PhotoActivity activity) {
		mHandler = activity.getCallbackHandler();
	}

	@Override
	public void onTotalCount(int totalCount) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_TOTAL_COUNT;
		message.arg1 = totalCount;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPlayState(int playState) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_PLAY_STATE;
		message.arg1 = playState;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPhotoInfo(int index, PhotoInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_PHOTO_INFO;
		message.arg1 = index;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onImageBitmap(int index, Bitmap imageBitmap) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_IMAGE_BITMAP;
		message.arg1 = index;
		message.obj = imageBitmap;
		mHandler.sendMessage(message);
	}

	@Override
	public void onShuffleState(int shuffle) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_SHUFFLE_STATE;
		message.arg1 = shuffle;
		mHandler.sendMessage(message);
	}

	@Override
	public void onFileInfo(int index, FileInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_FILE_INFO;
		message.arg1 = index;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onListInfo(ListInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_LIST_INFO;
		message.obj = info;
		mHandler.sendMessage(message);
	}
}
