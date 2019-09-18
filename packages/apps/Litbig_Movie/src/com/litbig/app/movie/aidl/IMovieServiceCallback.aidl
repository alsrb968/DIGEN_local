// IMovieServiceCallback.aidl
package com.litbig.app.movie.aidl;

import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;

import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IMovieServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
	//keep in sync with the MoviePlayerCallbackInterface.java
	oneway void onTotalCount(int totalCount);
	oneway void onPlayState(int playState);
	oneway void onPlayTimeMS(int playTimeMS);
	oneway void onMovieInfo(int index, in MovieInfo info);
	oneway void onVideoThumbnail(int index, in Bitmap videoThumbnail);
	oneway void onShuffleState(int shuffle);
	oneway void onRepeatState(int repeat);
	oneway void onSubtitle(in String text);
	oneway void onListInfo(in ListInfo info);
	oneway void onError(in String error);
}
