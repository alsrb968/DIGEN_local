package com.litbig.app.photo.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class ListInfo implements Parcelable {
    private int mListType;
    private String mSubCategory;
    private String[] mList;
	private String[] mFilePathList;
    private int[] mFileCount;

	public ListInfo(int listType, String subCategory, String[] list, String[] filePathList,int[] fileCount) {
        mListType = listType;
        mSubCategory = subCategory;
        mList = list;
		mFilePathList = filePathList;
        mFileCount = fileCount;
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

    public int[] getFileCount() {
        return mFileCount;
    }
	
	public String[] getFilePathList() {
		return mFilePathList;
	}

    public static final Parcelable.Creator<ListInfo> CREATOR = new Creator<ListInfo>() {
        @Override
        public ListInfo createFromParcel(Parcel src) {
            int listType = src.readInt();
            String subCategory = src.readString();
            String[] list = src.createStringArray();
			String[] filePathList = src.createStringArray();
            int[] fileCount = src.createIntArray();
			return new ListInfo(listType, subCategory, list, filePathList, fileCount);
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
		dest.writeStringArray(mFilePathList);
        dest.writeIntArray(mFileCount);
    }
}
