package com.litbig.app.music.aidl;

import android.os.Parcel;
import android.os.Parcelable;

import com.litbig.app.music.util.Log;

import java.util.Arrays;

public class ListInfo implements Parcelable {
	private int mListType;
	private String mSubCategory;
	private String[] mList;
	private String[] mArtist;
	private int[] mTotalTime;
	private int[] mFileCount;
	private boolean[] mIsFolder;
	private int mFolderCount;

	public ListInfo(int listType, String subCategory, String[] list, String[] artist, int[] totalTime, int[] fileCount, boolean[] isFolder, int folderCount) {
		mListType = listType;
		mSubCategory = subCategory;
		mList = list;
		mArtist = artist;
		mTotalTime = totalTime;
		mFileCount = fileCount;
		mIsFolder = isFolder;
		mFolderCount = folderCount;
		Log.v("[jacob] lt: " + mListType);
		Log.v("[jacob] sub: " + mSubCategory);
		Log.v("[jacob] list: " + (mList!=null ? Arrays.toString(mList) : "null"));
		Log.v("[jacob] art: " + (mArtist!=null ? Arrays.toString(mArtist) : "null"));
		Log.v("[jacob] tt: " + (mTotalTime!=null ? Arrays.toString(mTotalTime) : "null"));
		Log.v("[jacob] filec: " + (mFileCount!=null ? Arrays.toString(mFileCount) : "null"));
		Log.v("[jacob] isf: " + (mIsFolder!=null ? Arrays.toString(mIsFolder) : "null"));
		Log.v("[jacob] folderc: " + mFolderCount);
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

	public String[] getArtist() {
		return mArtist;
	}

	public int[] getTotalTime() {
		return mTotalTime;
	}

	public int[] getFileCount() {
		return mFileCount;
	}

	public boolean[] getIsFolder() {
		return mIsFolder;
	}

	public int getFolderCount() {
		return mFolderCount;
	}

	public static final Parcelable.Creator<ListInfo> CREATOR = new Creator<ListInfo>() {
		@Override
		public ListInfo createFromParcel(Parcel src) {
			int listType = src.readInt();
			String subCategory = src.readString();
			String[] list = src.createStringArray();
			String[] artist = src.createStringArray();
			int[] totalTime = src.createIntArray();
			int[] fileCount = src.createIntArray();
			boolean[] isFolder = src.createBooleanArray();
			int folderCount = src.readInt();
			return new ListInfo(listType, subCategory, list, artist, totalTime, fileCount, isFolder, folderCount);
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
		dest.writeStringArray(mArtist);
		dest.writeIntArray(mTotalTime);
		dest.writeIntArray(mFileCount);
		dest.writeBooleanArray(mIsFolder);
		dest.writeInt(mFolderCount);
	}
}
