package com.litbig.app.photo.aidl;

import android.graphics.Bitmap;

public interface PhotoPlayerCallbackInterface {
	//keep in sync with the IPhotoServiceCallback.aidl
	void onTotalCount(int totalCount);
	void onPlayState(int playState);
	void onPhotoInfo(int index, PhotoInfo info);
	void onImageBitmap(int index, Bitmap imageBitmap);
	void onShuffleState(int shuffle);
	void onFileInfo(int index, FileInfo info);
	void onListInfo(ListInfo info);
}
