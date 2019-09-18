// IPhotoServiceCallback.aidl
package com.litbig.app.photo.aidl;

import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;

import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IPhotoServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	//keep in sync with the PhotoPlayerCallbackInterface.java
	oneway void onTotalCount(int totalCount);
	oneway void onPlayState(int playState);
	oneway void onPhotoInfo(int index, in PhotoInfo info);
	oneway void onImageBitmap(int index, in Bitmap imageBitmap);
	oneway void onShuffleState(int shuffle);
	oneway void onFileInfo(int index, in FileInfo info);
	oneway void onListInfo(in ListInfo info);
}
