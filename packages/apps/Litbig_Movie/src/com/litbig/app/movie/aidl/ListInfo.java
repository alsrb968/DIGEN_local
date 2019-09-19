package com.litbig.app.movie.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class ListInfo implements Parcelable {
    private int mListType;
    private String mSubCategory;
    private String[] mList;
    private int[] mTotalTime;
    private int[] mFileCount;
    private int[] mWidth;
    private int[] mHeight;

    public ListInfo(int listType, String subCategory, String[] list, int[] totalTime, int[] fileCount, int[] width, int[] height) {
        mListType = listType;
        mSubCategory = subCategory;
        mList = list;
        mTotalTime = totalTime;
        mFileCount = fileCount;
        mWidth = width;
        mHeight = height;
    }

    public int getListType() {
        return mListType;
    }

    public String getSubCategory() {
        return mSubCategory;
    }

    public String[] getList() {
        return mList;
    }

    public int[] getTotalTime() {
        return mTotalTime;
    }

    public int[] getFileCount() {
        return mFileCount;
    }

    public int[] getWidth() {
        return mWidth;
    }

    public int[] getHeight() {
        return mHeight;
    }

    public static final Parcelable.Creator<ListInfo> CREATOR = new Creator<ListInfo>() {
        @Override
        public ListInfo createFromParcel(Parcel src) {
            int listType = src.readInt();
            String subCategory = src.readString();
            String[] list = src.createStringArray();
            int[] totalTime = src.createIntArray();
            int[] fileCount = src.createIntArray();
            int[] width = src.createIntArray();
            int[] height = src.createIntArray();
            return new ListInfo(listType, subCategory, list, totalTime, fileCount, width, height);
        }

        @Override
        public ListInfo[] newArray(int size) {
            return new ListInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mListType);
        dest.writeString(mSubCategory);
        dest.writeStringArray(mList);
        dest.writeIntArray(mTotalTime);
        dest.writeIntArray(mFileCount);
        dest.writeIntArray(mWidth);
        dest.writeIntArray(mHeight);
    }
}
