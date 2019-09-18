package com.litbig.app.movie.aidl;

import android.graphics.Bitmap;
import android.view.Surface;

public interface MoviePlayerInterface {
    //keep in sync with the IMovieService.aidl
    void setSurface(Surface surface);
    int getPlayingIndex();
    String getFileFullPath(int index, boolean isNowPlaying);
    int getPlayState();
    int getPlayTimeMS();
    void setPlayTimeMS(int playTimeMS);
    void gripTimeProgressBar();
    void play();
    void pause();
    boolean playIndex(int index, boolean isNowPlaying);
    void playPrev();
    void playNext();
    void startFastForward();
    void stopFastForward();
    void startFastRewind();
    void stopFastRewind();
    int getShuffle();
    int getRepeat();
    void setShuffle();
    void setRepeat();
    void requestList(int listType, String subCategory);
    Bitmap getVideoThumbnail(int index, boolean isNowPlaying, boolean isScale);
    int getNowPlayingCategory();
    String getNowPlayingSubCategory();
    boolean isNowPlayingCategory(int category, String subCategory);
}
