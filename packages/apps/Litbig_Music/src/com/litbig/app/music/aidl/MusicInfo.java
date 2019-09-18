package com.litbig.app.music.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Parcelable {
	private int mTotalTimeMS;
	private String mTitle;
	private String mArtist;
	private String mAlbum;
	private String mGenre;

	public MusicInfo(int totalTimeMS, String title, String artist, String album, String genre) {
		mTotalTimeMS = totalTimeMS;
		mTitle = title;
		mArtist = artist;
		mAlbum = album;
		mGenre = genre;
	}

	public int getTotalTimeMS() {
		return mTotalTimeMS;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getArtist() {
		return mArtist;
	}

	public String getAlbum() {
		return mAlbum;
	}

	public String getGenre() {
		return mGenre;
	}

	public static final Parcelable.Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
		@Override
		public MusicInfo createFromParcel(Parcel src) {
			int totalTimeMS = src.readInt();
			String title = src.readString();
			String artist = src.readString();
			String album = src.readString();
			String genre = src.readString();
			return new MusicInfo(totalTimeMS, title, artist, album, genre);
		}

		@Override
		public MusicInfo[] newArray(int size) {
			return new MusicInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mTotalTimeMS);
		dest.writeString(mTitle);
		dest.writeString(mArtist);
		dest.writeString(mAlbum);
		dest.writeString(mGenre);
	}
}
