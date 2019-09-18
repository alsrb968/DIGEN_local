package com.litbig.app.photo.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoInfo implements Parcelable {
    private int mImageWidth;
    private int mImageHeight;
    private String mTitle;

    public PhotoInfo(int imageWidth, int imageHeight, String title) {
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mTitle = title;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    public String getTitle() {
        return mTitle;
    }

    public static final Parcelable.Creator<PhotoInfo> CREATOR = new Creator<PhotoInfo>() {
        @Override
        public PhotoInfo createFromParcel(Parcel src) {
            int imageWidth = src.readInt();
            int imageHeight = src.readInt();
            String title = src.readString();
            return new PhotoInfo(imageWidth, imageHeight, title);
        }

        @Override
        public PhotoInfo[] newArray(int size) {
            return new PhotoInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mImageWidth);
        dest.writeInt(mImageHeight);
        dest.writeString(mTitle);
    }
}
