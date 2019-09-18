package com.litbig.app.movie.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieInfo implements Parcelable {
    private int mTotalTimeMS;
    private int mVideoWidth;
    private int mVideoHeight;
    private String mTitle;

    public MovieInfo(int totalTimeMS, int videoWidth, int videoHeight, String title) {
        mTotalTimeMS = totalTimeMS;
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
        mTitle = title;
    }

    public int getTotalTimeMS() {
        return mTotalTimeMS;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public String getTitle() {
        return mTitle;
    }

    public static final Parcelable.Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel src) {
            int totalTimeMS = src.readInt();
            int videoWidth = src.readInt();
            int videoHeight = src.readInt();
            String title = src.readString();
            return new MovieInfo(totalTimeMS, videoWidth, videoHeight, title);
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mTotalTimeMS);
        dest.writeInt(mVideoWidth);
        dest.writeInt(mVideoHeight);
        dest.writeString(mTitle);
    }
}
