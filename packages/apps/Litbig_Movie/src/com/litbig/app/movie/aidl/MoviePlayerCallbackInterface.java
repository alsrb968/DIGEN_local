package com.litbig.app.movie.aidl;

import android.graphics.Bitmap;

public interface MoviePlayerCallbackInterface {
    //keep in sync with the IMovieServiceCallback.aidl
    void onTotalCount(int totalCount);
    void onPlayState(int playState);
    void onPlayTimeMS(int playTimeMS);
    void onMovieInfo(int index, MovieInfo info);
    void onVideoThumbnail(int index, Bitmap videoThumbnail);
    void onShuffleState(int shuffle);
    void onRepeatState(int repeat);
    void onSubtitle(String text);
    void onListInfo(ListInfo info);
    void onError(String error);
}
