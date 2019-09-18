package com.litbig.app.music.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class FileInfo implements Parcelable {
	private String mFileName;
	private String mParentPath;
	private long mFileSize;

	public FileInfo(String fileName, String parentPath, long fileSize) {
		mFileName = fileName;
		mParentPath = parentPath;
		mFileSize = fileSize;
	}

	public String getFileName() {
		return mFileName;
	}

	public String getParentPath() {
		return mParentPath;
	}

	public long getFileSize() {
		return mFileSize;
	}

	public static final Parcelable.Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
		@Override
		public FileInfo createFromParcel(Parcel src) {
			String fileName = src.readString();
			String mParentPath = src.readString();
			long fileSize = src.readInt();
			return new FileInfo(fileName, mParentPath, fileSize);
		}

		@Override
		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFileName);
		dest.writeString(mParentPath);
		dest.writeLong(mFileSize);
	}
}
