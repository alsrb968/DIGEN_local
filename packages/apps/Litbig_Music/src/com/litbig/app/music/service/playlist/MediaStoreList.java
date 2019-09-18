package com.litbig.app.music.service.playlist;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.SparseIntArray;

import com.litbig.app.music.R;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.service.MusicPlaybackService;
import com.litbig.app.music.service.player.FilePlayer;
import com.litbig.app.music.util.MusicUtils;
import com.litbig.mediastorage.MediaStorage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MediaStoreList {
	private MusicPlaybackService mService;
	private FilePlayer mPlayer;

	public MediaStoreList(MusicPlaybackService service) {
		mService = service;
	}

	// ----------
	// MediaStoreList APIs
	public void destroy() {
		if (null != mNowPlayingCursor) {
			mNowPlayingCursor.close();
			mNowPlayingCursor = null;
		}
		if (null != mCategoryTrackCursor) {
			mCategoryTrackCursor.close();
			mCategoryTrackCursor = null;
		}
		if (null != mRecentTrackCursor) {
			mRecentTrackCursor.close();
			mRecentTrackCursor = null;
		}
	}

	public void requestActive() {
		switch (mNowPlayingQueryState) {
		case QUERY_STATE_NONE :
			mPlayer = (FilePlayer)mService.getMusicPlayer();
			mActivePlayer = true;
			requestNowPlaying();
			break;
		case QUERY_STATE_STARTED :
			mActivePlayer = true;
			break;
		case QUERY_STATE_COMPLETED :
			mPlayer.onTotalCount(mNowPlayingTotalCount);
			mPlayer.activePlayer();
			break;
		default :
			break;
		}
	}

	public int getPlayingIndex(boolean refresh) {
		if (refresh) {
			int totalCount = getTotalCount(true);
			Cursor cursor = getCursor(true);
			if (0 < totalCount) {
				String track = mPlayer.getLastTrack();
				if ((null != track) && (!track.isEmpty())) {
					for (int index = 0; index < totalCount; index++) {
						cursor.moveToPosition(index);
						if (track.equals(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))) {
							mPlayingIndex = cursor.getPosition();
							break;
						}
					}
				}
			}
		}
		return mPlayingIndex;
	}

	public int getTotalCount() {
		int totalCount = 0;
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			totalCount = getTotalCount(true);
		}
		return totalCount;
	}

	public MusicInfo getMusicInfo(int index, boolean isNowPlaying) {
		MusicInfo info = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			String genre = getGenre(cursor);
			if ((null == title) || (title.isEmpty())) {
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				title = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
			}
			if ((null == artist) || (artist.isEmpty()) || (artist.equals("<unknown>"))) {
				artist = mService.getString(R.string.unknown);
			}
			if ((null == album) || (album.isEmpty()) || (album.equals("<unknown>"))) {
				album = mService.getString(R.string.unknown);
			}
			if ((null == genre) || (genre.isEmpty()) || (genre.equals("<unknown>"))) {
				genre = mService.getString(R.string.unknown);
			}
			info = new MusicInfo(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)), title, artist, album, genre);
		}
		return info;
	}

	public String getFileFullPath(int index, boolean isNowPlaying) {
		String file = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			file = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
		}
		return file;
	}

	public Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap bitmap = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			MediaMetadataRetriever metaData = new MediaMetadataRetriever();
			try {
				metaData.setDataSource(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
				byte[] artBytes = metaData.getEmbeddedPicture();
				if (null != artBytes) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.RGB_565;
					if (isScale) {
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length, options);
						if ((0 < options.outWidth) && (0 < options.outHeight)) {
							final int defaultWidth = 100;
							final int defaultHeight = 100;
							int sampleSize = 1;
							int outWidth = options.outWidth;
							int outHeight = options.outHeight;
							while ((defaultWidth < outWidth) && (defaultHeight < outHeight)) {
								sampleSize <<= 1;
								outWidth >>= 1;
								outHeight >>= 1;
							}
							options.inSampleSize = sampleSize;
							options.inJustDecodeBounds = false;
							bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length, options);
						}
					} else {
						options.inSampleSize = 1;
						options.inJustDecodeBounds = false;
						bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length, options);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			metaData.release();
		}
		return bitmap;
	}

	public String getTrackFromIndex(int index, boolean isNowPlaying) {
		String track = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			mPlayingIndex = cursor.getPosition();
		}
		return track;
	}

	public String getPrevTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (MusicUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("prev"));
			} else if (0 >= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToLast();
			} else {
				mNowPlayingCursor.moveToPrevious();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			mPlayingIndex = mNowPlayingCursor.getPosition();
		}
		return track;
	}

	public String getNextTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (MusicUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("next"));
			} else if ((mNowPlayingTotalCount - 1) <= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToFirst();
			} else {
				mNowPlayingCursor.moveToNext();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			mPlayingIndex = mNowPlayingCursor.getPosition();
		}
		return track;
	}

	public int getShuffle() {
		return mShuffle;
	}

	public void setShuffle(int shuffle) {
		mShuffle = shuffle;
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			if (MusicUtils.ShuffleState.OFF != shuffle) {
				setShuffleList();
			} else {
				mShuffleList = null;
			}
		}
	}

	public void requestList(int listType, String subCategory) {
		if (!mMakeList) {
			mMakeList = true;
			switch (listType) {
			case MusicUtils.ListType.NOW_PLAYING :
				mCategory = MusicUtils.Category.NOW_PLAYING;
				if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							int[] totalTime = new int[mNowPlayingTotalCount];
							int[] fileCount = new int[mNowPlayingTotalCount];
							for (int index = 0; index < mNowPlayingTotalCount; index++) {
								totalTime[index] = mNowPlayingTotalTimeList.get(index);
								fileCount[index] = 0;
							}
							responseList(new ListInfo(MusicUtils.ListType.NOW_PLAYING, null, mNowPlayingList.toArray(new String[mNowPlayingTotalCount]), mNowPlayingArtistList.toArray(new String[mNowPlayingTotalCount]), totalTime, fileCount));
						}
					}).start();
				} else {
					responseList(new ListInfo(MusicUtils.ListType.NOW_PLAYING, null, null, null, null, null));
				}
				break;
			case MusicUtils.ListType.ALL :
				mCategory = MusicUtils.Category.ALL;
				requestCategoryTrack();
				break;
			case MusicUtils.ListType.ARTIST :
				mCategory = MusicUtils.Category.ARTIST;
				requestArtistCategory();
				break;
			case MusicUtils.ListType.ALBUM :
				mCategory = MusicUtils.Category.ALBUM;
				requestAlbumCategory();
				break;
			case MusicUtils.ListType.GENRE :
				mCategory = MusicUtils.Category.GENRE;
				requestGenreCategory();
				break;
			case MusicUtils.ListType.FOLDER :
				mCategory = MusicUtils.Category.FOLDER;
				requestFolderCategory();
				break;
//			case MusicUtils.ListType.RECENT :
//				mCategory = MusicUtils.Category.RECENT;
//				requestCategoryTrack();
//				break;
			case MusicUtils.ListType.ARTIST_TRACK :
			case MusicUtils.ListType.ALBUM_TRACK :
			case MusicUtils.ListType.GENRE_TRACK :
			case MusicUtils.ListType.FOLDER_TRACK :
				mCategory = getCategory(listType);
				mSubCategory = subCategory;
				requestCategoryTrack();
				break;
			default :
				break;
			}
		}
	}

	public int getNowPlayingCategory() {
		return mNowPlayingCategory;
	}

	public String getNowPlayingSubCategory() {
		String subCategory = null;
		switch (mNowPlayingCategory) {
		case MusicUtils.Category.ALL :
//		case MusicUtils.Category.RECENT :
			break;
		case MusicUtils.Category.ARTIST :
		case MusicUtils.Category.ALBUM :
		case MusicUtils.Category.FOLDER :
			subCategory = mNowPlayingSubCategory;
			break;
		case MusicUtils.Category.GENRE :
			subCategory = getGenre(mNowPlayingCursor);
			break;
		default :
			break;
		}
		return subCategory;
	}

	public boolean isNowPlayingCategory(int category, String subCategory) {
		boolean nowPlaying = false;
		switch (category) {
		case MusicUtils.Category.NOW_PLAYING :
			nowPlaying = true;
			break;
		case MusicUtils.Category.ALL :
//		case MusicUtils.Category.RECENT :
			if (mNowPlayingCategory == category) {
				nowPlaying = true;
			}
			break;
		case MusicUtils.Category.ARTIST :
		case MusicUtils.Category.ALBUM :
		case MusicUtils.Category.FOLDER :
			if ((mNowPlayingCategory == category) && (subCategory.equals(mNowPlayingSubCategory))) {
				nowPlaying = true;
			}
			break;
		case MusicUtils.Category.GENRE :
			String genreName = getGenre(mNowPlayingCursor);
			if ((mNowPlayingCategory == category) && (subCategory.equals(genreName))) {
				nowPlaying = true;
			}
			break;
		default :
			break;
		}
		return nowPlaying;
	}

	public void changeCurrentCategoryToNowPlaying() {
		switch (mCategory) {
		case MusicUtils.Category.ALL :
//		case MusicUtils.Category.RECENT :
			if (mCategory != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
				mService.savePreference(MusicUtils.Preference.CATEGORY, mCategory);
			}
			break;
		case MusicUtils.Category.ARTIST :
			if (mCategory != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
				mService.savePreference(MusicUtils.Preference.CATEGORY, mCategory);
			}
			if (mSubCategory != mService.loadPreference(MusicUtils.Preference.ARTIST_NAME)) {
				mService.savePreference(MusicUtils.Preference.ARTIST_NAME, mSubCategory);
			}
			break;
		case MusicUtils.Category.ALBUM :
			if (mCategory != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
				mService.savePreference(MusicUtils.Preference.CATEGORY, mCategory);
			}
			if (mSubCategory != mService.loadPreference(MusicUtils.Preference.ALBUM_NAME)) {
				mService.savePreference(MusicUtils.Preference.ALBUM_NAME, mSubCategory);
			}
			break;
		case MusicUtils.Category.GENRE :
			if (mCategory != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
				mService.savePreference(MusicUtils.Preference.CATEGORY, mCategory);
			}
			if (Integer.toString(mGenreList.get(mSubCategory)) != mService.loadPreference(MusicUtils.Preference.GENRE_ID)) {
				mService.savePreference(MusicUtils.Preference.GENRE_ID, Integer.toString(mGenreList.get(mSubCategory)));
			}
			break;
		case MusicUtils.Category.FOLDER :
			if (mCategory != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
				mService.savePreference(MusicUtils.Preference.CATEGORY, mCategory);
			}
			if (mSubCategory != mService.loadPreference(MusicUtils.Preference.FOLDER_PATH)) {
				mService.savePreference(MusicUtils.Preference.FOLDER_PATH, mSubCategory);
			}
			break;
		default :
			break;
		}
		mChangeNowPlaying = true;
		requestNowPlaying();
		mPlayer.onTotalCount(getTotalCount(true));
	}

	public void clearPlaylist() {
		mNowPlayingQueryState = QUERY_STATE_NONE;
	}

	public void requestQueryForScanFinish(boolean activePlayer) {
		mScanFinish = true;
		mActivePlayer = activePlayer;
		requestNowPlaying();
	}

	// ----------
	// AsyncQueryHandler
	private class NotifyingAsyncQueryHandler extends AsyncQueryHandler {
		private Context mContext;
		private WeakReference<AsyncQueryListener> mListener;

		private class QueryArgs {
			public Uri uri;
			public String [] projection;
			public String selection;
			public String [] selectionArgs;
			public String orderBy;
		}

		public NotifyingAsyncQueryHandler(Context context, AsyncQueryListener listener) {
			super(context.getContentResolver());
			mContext = context;
			mListener = new WeakReference<>(listener);
		}

		public Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy, boolean async) {
			if (async) {
				// Get 100 results first, which is enough to allow the user to start scrolling, while still being very fast.
				Uri limituri = uri.buildUpon().appendQueryParameter("limit", "100").build();
				QueryArgs args = new QueryArgs();
				args.uri = uri;
				args.projection = projection;
				args.selection = selection;
				args.selectionArgs = selectionArgs;
				args.orderBy = orderBy;
				startQuery(0, args, limituri, projection, selection, selectionArgs, orderBy);
				return null;
			}
			ContentResolver resolver = mContext.getContentResolver();
			if (null == resolver) {
				return null;
			}
			return resolver.query(uri, projection, selection, selectionArgs, orderBy);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if ((0 == token) && (null != cookie) && (null != cursor) && (100 <= cursor.getCount())) {
				QueryArgs args = (QueryArgs)cookie;
				startQuery(1, null, args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
			}
			AsyncQueryListener listener = mListener.get();
			if (null != listener) {
				if ((0 == token) || ((null != cursor) && (100 <= cursor.getCount()))) {
					listener.onQueryComplete(token, cookie, cursor);
				}
			} else if (null != cursor) {
				cursor.close();
			}
		}
	}

	private interface AsyncQueryListener {
		void onQueryComplete(int token, Object cookie, Cursor cursor);
	}

	// ----------
	// shuffle internal functions
	private int mShuffle = MusicUtils.ShuffleState.OFF;
	private ArrayList<Integer> mShuffleList = null;
	private int mShuffleIndex = 0;

	private void makeShuffleList() {
		int index;
		if (null == mShuffleList) {
			mShuffleList = new ArrayList<>();
			for (index = 0; index < mNowPlayingTotalCount; index++) {
				mShuffleList.add(index, index);
			}
			Collections.shuffle(mShuffleList);
			int current = mPlayingIndex;
			int count = 0;
			while (current != mShuffleList.get(count)) {
				count++;
			}
			mShuffleList.set(count, mShuffleList.get(0));
			mShuffleList.set(0, current);
		} else {
			for (index = 0; index < mNowPlayingTotalCount; index++) {
				mShuffleList.set(index, mShuffleList.get(mNowPlayingTotalCount + index));
			}
			for (index = mNowPlayingTotalCount; index < mShuffleList.size(); index++) {
				mShuffleList.remove(index);
			}
		}
		ArrayList<Integer> shuffleList = new ArrayList<>();
		for (index = 0; index < mNowPlayingTotalCount; index++) {
			shuffleList.add(index, index);
		}
		Collections.shuffle(shuffleList);
		for (index = 0; index < mNowPlayingTotalCount; index++) {
			mShuffleList.add(mNowPlayingTotalCount + index, shuffleList.get(index));
		}
	}

	private void setShuffleList() {
		mShuffleList = null;
		makeShuffleList();
		mShuffleIndex = 0;
	}

	private int setShuffleIndex(String direction) {
		int select = 0;
		int index = mPlayingIndex;
		if ((MusicUtils.ShuffleState.OFF != mShuffle) && (0 <= mPlayingIndex) && (mNowPlayingTotalCount > index)) {
			select = index;
			mShuffleIndex = 0;
			while (index != mShuffleList.get(mShuffleIndex)) {
				mShuffleIndex++;
			}
			if (direction.equals("prev")) {
				mShuffleIndex--;
				if (0 > mShuffleIndex) {
					if (1 < mNowPlayingTotalCount) {
						mShuffleIndex = mNowPlayingTotalCount - 1;
					} else {
						mShuffleIndex = 0;
					}
				}
				select = mShuffleList.get(mShuffleIndex);
			} else if (direction.equals("next")) {
				mShuffleIndex++;
				if (mNowPlayingTotalCount <= mShuffleIndex) {
					makeShuffleList();
					mShuffleIndex = 0;
				}
				select = mShuffleList.get(mShuffleIndex);
			}
		}
		return select;
	}

	// ----------
	// NowPlayingList internal functions
	private final int QUERY_STATE_NONE = 0;
	private final int QUERY_STATE_STARTED = 1;
	private final int QUERY_STATE_COMPLETED = 2;
	
	private int mNowPlayingQueryState = QUERY_STATE_NONE;
	private boolean mRetryQuery = false;
	private int mNowPlayingCategory = MusicUtils.Category.ALL;
	private String mNowPlayingSubCategory = null;
	private ArrayList<String> mNowPlayingList = new ArrayList<>();
	private ArrayList<String> mNowPlayingArtistList = new ArrayList<>();
	private ArrayList<Integer> mNowPlayingTotalTimeList = new ArrayList<>();
	private Cursor mNowPlayingCursor = null;
	private int mNowPlayingTotalCount = 0;

	private void requestNowPlaying() {
		boolean directQuery = false;
		if (null == Looper.myLooper()) {
			directQuery = true;
		}
		mNowPlayingList.clear();
		mNowPlayingArtistList.clear();
		mNowPlayingTotalTimeList.clear();
		mNowPlayingQueryState = QUERY_STATE_STARTED;
		loadNowPlayingCategory();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID,	MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs = null;
		String orderBy = getOrderbyFromLocalization(MediaStore.Audio.Media.TITLE);
		ArrayList<String> args = new ArrayList<>();
		if (MusicUtils.Category.FOLDER == mNowPlayingCategory) {
			where.append(" AND " + MediaStore.Audio.Media.DATA + " like ?");
			args.add(mNowPlayingSubCategory + "%");
			selectionArgs = args.toArray(new String[args.size()]);
		} /*else if (MusicUtils.Category.RECENT == mNowPlayingCategory) {
			if (mService.getRecentTrackList().size() > 0) {
				where += " AND " + MediaStore.Audio.Media.TITLE + " like ?";
				args.add(mService.getRecentTrackList().get(0));
				for (int index = 1; index < mService.getRecentTrackList().size(); index++) {
					Log.d("[jacob] RecentTrackList: " + mService.getRecentTrackList().get(index));
					where += " OR " + MediaStore.Audio.Media.TITLE + " like ?";
					args.add(mService.getRecentTrackList().get(index));
				}
				selectionArgs = args.toArray(new String[args.size()]);
			}
		}*/ else {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
					args.add(enableStorage.get(index) + "%");
				}
				where.append(")");
			}
			switch (mNowPlayingCategory) {
			case MusicUtils.Category.ALL :
//			case MusicUtils.Category.RECENT :
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.ARTIST :
				where.append(" AND " + MediaStore.Audio.Media.ARTIST + "=?");
				args.add(mNowPlayingSubCategory);
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.ALBUM :
				where.append(" AND " + MediaStore.Audio.Media.ALBUM + "=?");
				args.add(mNowPlayingSubCategory);
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.GENRE :
				uri = MediaStore.Audio.Genres.Members.getContentUri("external", Integer.parseInt(mNowPlayingSubCategory));
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			default :
				break;
			}
		}
		if (directQuery) {
			Cursor cursor = mService.getContentResolver().query(uri, projection, where.toString(), selectionArgs, orderBy);
			if ((null != cursor) && (0 < cursor.getCount())) {
				mNowPlayingQueryState = QUERY_STATE_COMPLETED;
				mNowPlayingCursor = cursor;
				mNowPlayingTotalCount = mNowPlayingCursor.getCount();
				mPlayer.onTotalCount(mNowPlayingTotalCount);
				for (int count = 0; count < mNowPlayingTotalCount; count++) {
					mNowPlayingCursor.moveToPosition(count);
					String track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
					if ((null == track) || (track.isEmpty())) {
						String name = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
						track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
					}
					mNowPlayingList.add(track);
					String artist = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					if ((null == artist) || (artist.isEmpty()) || (artist.equals("<unknown>"))) {
						artist = mService.getString(R.string.unknown);
					}
					mNowPlayingArtistList.add(artist);
					mNowPlayingTotalTimeList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				}
				processAfterQuery();
				mRetryQuery = false;
			} else if ((MusicUtils.Category.ALL != mNowPlayingCategory) && (!mRetryQuery)) {
				mRetryQuery = true;
				mService.savePreference(MusicUtils.Preference.CATEGORY, MusicUtils.Category.ALL);
				requestNowPlaying();
			} else {
				mNowPlayingQueryState = QUERY_STATE_NONE;
				mNowPlayingCursor = null;
				mNowPlayingTotalCount = 0;
				mActivePlayer = false;
				mService.clearPreference();
				mRetryQuery = false;
			}
		} else {
			(new NotifyingAsyncQueryHandler(mService, mNowPlayingListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
		}
	}

	private AsyncQueryListener mNowPlayingListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if ((null != cursor) && (0 < cursor.getCount())) {
				if ((0 < token) && (100 >= cursor.getCount())) {
					processAfterQuery();
				} else {
					mNowPlayingQueryState = QUERY_STATE_COMPLETED;
					mNowPlayingCursor = cursor;
					mNowPlayingTotalCount = mNowPlayingCursor.getCount();
					mPlayer.onTotalCount(mNowPlayingTotalCount);
					int count = 0;
					if ((0 < token) && (100 < mNowPlayingTotalCount)) {
						count = 100;
					}
					for (; count < mNowPlayingTotalCount; count++) {
						mNowPlayingCursor.moveToPosition(count);
						String track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
						if ((null == track) || (track.isEmpty())) {
							String name = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mNowPlayingList.add(track);
						String artist = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
						if ((null == artist) || (artist.isEmpty()) || (artist.equals("<unknown>"))) {
							artist = mService.getString(R.string.unknown);
						}
						mNowPlayingArtistList.add(artist);
						mNowPlayingTotalTimeList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
					}
					if ((0 < token) || (100 > mNowPlayingTotalCount)) {
						processAfterQuery();
					}
				}
				mRetryQuery = false;
			} else if ((MusicUtils.Category.ALL != mNowPlayingCategory) && (!mRetryQuery)) {
				mRetryQuery = true;
				mService.savePreference(MusicUtils.Preference.CATEGORY, MusicUtils.Category.ALL);
				requestNowPlaying();
			} else {
				mNowPlayingQueryState = QUERY_STATE_NONE;
				mNowPlayingCursor = null;
				mNowPlayingTotalCount = 0;
				mActivePlayer = false;
				mService.clearPreference();
				mRetryQuery = false;
			}
		}
	};

	// ----------
	// CategoryTrackList internal functions
	private ArrayList<String> mCategoryTrackList = new ArrayList<>();
	private ArrayList<String> mCategoryTrackArtistList = new ArrayList<>();
	private ArrayList<Integer> mCategoryTrackTotalTimeList = new ArrayList<>();
	private Cursor mCategoryTrackCursor = null;
	private Cursor mRecentTrackCursor = null;
	private int mCategoryTrackTotalCount = 0;
	private int mCategory = MusicUtils.Category.ALL;
	private String mSubCategory = null;

	private void requestCategoryTrack() {
		mCategoryTrackList.clear();
		mCategoryTrackArtistList.clear();
		mCategoryTrackTotalTimeList.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID,	MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs = null;

		String orderBy = getOrderbyFromLocalization(MediaStore.Audio.Media.TITLE);
		String name = MediaStore.Audio.Media.TITLE;

		ArrayList<String> args = new ArrayList<>();
		if (MusicUtils.Category.FOLDER == mCategory) {
			where.append(" AND " + MediaStore.Audio.Media.DATA + " like ?");
			args.add(mSubCategory + "%");
			selectionArgs = args.toArray(new String[args.size()]);
		} /*else if (MusicUtils.Category.RECENT == mCategory) {
			if (mService.getRecentTrackList().size() > 0) {
				where += " AND " + MediaStore.Audio.Media.TITLE + " like ?";
				args.add(mService.getRecentTrackList().get(0));
				for (int index = 1; index < mService.getRecentTrackList().size(); index++) {
					Log.d("[jacob] RecentTrackList: " + mService.getRecentTrackList().get(index));
					where += " OR " + MediaStore.Audio.Media.TITLE + " like ?";
					args.add(mService.getRecentTrackList().get(index));
				}
				selectionArgs = args.toArray(new String[args.size()]);
			}
		}*/ else {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
					args.add(enableStorage.get(index) + "%");
				}
				where.append(")");
			}
			switch (mCategory) {
			case MusicUtils.Category.ALL :
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.ARTIST :
				where.append(" AND " + MediaStore.Audio.Media.ARTIST + "=?");
				args.add(mSubCategory);
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.ALBUM :
				where.append(" AND " + MediaStore.Audio.Media.ALBUM + "=?");
				args.add(mSubCategory);
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			case MusicUtils.Category.GENRE :
				uri = MediaStore.Audio.Genres.Members.getContentUri("external", mGenreList.get(mSubCategory));
				selectionArgs = args.toArray(new String[args.size()]);
				break;
			default :
				break;
			}
		}
		(new NotifyingAsyncQueryHandler(mService, mCategoryTrackListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
	}

	private AsyncQueryListener mCategoryTrackListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if ((null != cursor) && (0 < cursor.getCount())) {
				if ((0 == token) || (100 < cursor.getCount())) {
					mCategoryTrackCursor = cursor;
					mRecentTrackCursor = cursor;
					mCategoryTrackTotalCount = mCategoryTrackCursor.getCount();
					int count = 0;
					if ((0 < token) && (100 < mCategoryTrackTotalCount)) {
						count = 100;
					}
					for (; count < mCategoryTrackTotalCount; count++) {
						mCategoryTrackCursor.moveToPosition(count);
						String track = mCategoryTrackCursor.getString(mCategoryTrackCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
						if ((null == track) || (track.isEmpty())) {
							String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mCategoryTrackList.add(track);
						String artist = mCategoryTrackCursor.getString(mCategoryTrackCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
						if ((null == artist) || (artist.isEmpty()) || (artist.equals("<unknown>"))) {
							artist = mService.getString(R.string.unknown);
						}
						mCategoryTrackArtistList.add(artist);
						mCategoryTrackTotalTimeList.add(mCategoryTrackCursor.getInt(mCategoryTrackCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
					}
				}
				if ((0 < token) || (100 > mCategoryTrackTotalCount)) {
					int[] totalTime = new int[mCategoryTrackTotalCount];
					int[] fileCount = new int[mCategoryTrackTotalCount];
					for (int index = 0; index < mCategoryTrackTotalCount; index++) {
						totalTime[index] = mCategoryTrackTotalTimeList.get(index);
						fileCount[index] = 0;
					}
					responseList(new ListInfo(getListType(), mSubCategory, mCategoryTrackList.toArray(new String[mCategoryTrackTotalCount]), mCategoryTrackArtistList.toArray(new String[mCategoryTrackTotalCount]), totalTime, fileCount));
				}
			} else {
				mCategoryTrackCursor = null;
				mCategoryTrackTotalCount = 0;
				responseList(new ListInfo(getListType(), mSubCategory, null, null, null, null));
			}
		}
	};

	private String getOrderbyFromLocalization(String name) {
//		Locale systemLocale = mService.getResources().getConfiguration().locale;
		String orderBy = "";
//		if (systemLocale.getLanguage().contains("ko")) {
			orderBy = "case"+
					" when substr("+ name +", 1) BETWEEN '"+mService.getString(R.string.list_indexer_korean_init)+"' AND '"+mService.getString(R.string.list_indexer_korean_last)+"' then 1 "+
					" when substr("+ name +", 1) BETWEEN 'A' AND '[' then 2 "+  
					" when substr("+ name +", 1) BETWEEN 'a' AND '{' then 3 "+  
					" when substr("+ name +", 1) BETWEEN '0' AND ':' then 4 "+  
					" else 5 end, "+ name + " ASC";
//		} else {
//		}
		return orderBy;
	}
	private int getListType() {
		int listType = MusicUtils.ListType.ALL;
		switch (mCategory) {
		case MusicUtils.Category.ALL :
			listType = MusicUtils.ListType.ALL;
			break;
		case MusicUtils.Category.ARTIST :
			listType = MusicUtils.ListType.ARTIST_TRACK;
			break;
		case MusicUtils.Category.ALBUM :
			listType = MusicUtils.ListType.ALBUM_TRACK;
			break;
		case MusicUtils.Category.GENRE :
			listType = MusicUtils.ListType.GENRE_TRACK;
			break;
		case MusicUtils.Category.FOLDER :
			listType = MusicUtils.ListType.FOLDER_TRACK;
			break;
//		case MusicUtils.Category.RECENT :
//			listType = MusicUtils.ListType.RECENT;
//			break;
		default :
			break;
		}
		return listType;
	}
	private int getCategory(int listType) {
		int category = MusicUtils.ListType.ALL;
		switch (listType) {
		case MusicUtils.ListType.ALL :
			category = MusicUtils.Category.ALL;
			break;
		case MusicUtils.ListType.ARTIST :
		case MusicUtils.ListType.ARTIST_TRACK :
			category = MusicUtils.Category.ARTIST;
			break;
		case MusicUtils.ListType.ALBUM :
		case MusicUtils.ListType.ALBUM_TRACK :
			category = MusicUtils.Category.ALBUM;
			break;
		case MusicUtils.ListType.GENRE :
		case MusicUtils.ListType.GENRE_TRACK :
			category = MusicUtils.Category.GENRE;
			break;
		case MusicUtils.ListType.FOLDER_TRACK :
			category = MusicUtils.Category.FOLDER;
			break;
//		case MusicUtils.ListType.RECENT :
//			category = MusicUtils.Category.RECENT;
//			break;
		default :
			break;
		}
		return category;
	}

	// ----------
	// CategoryList internal functions
	private ArrayList<String> mCategoryList = new ArrayList<>();
	private ArrayList<String> mCategoryArtistList = new ArrayList<>();
	private SparseIntArray mCategoryFileCount = new SparseIntArray();
	private HashMap<String, Integer> mGenreList = new HashMap<>();
	private HashMap<String, Integer> mFolderList = new HashMap<>();

	private void requestArtistCategory() {
		mCategoryList.clear();
		mCategoryFileCount.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs;
		String orderBy = getOrderbyFromLocalization(MediaStore.Audio.Media.ARTIST);
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
			where.append(")");
		}
		selectionArgs = args.toArray(new String[args.size()]);
		(new NotifyingAsyncQueryHandler(mService, mArtistQueryListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
	}

	private AsyncQueryListener mArtistQueryListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (null != cursor) {
				final int totalCount = cursor.getCount();
				if (0 < totalCount) {
					if ((0 == token) || (100 < totalCount)) {
						int count = 0;
						if ((0 < token) && (100 < totalCount)) {
							count = 100;
						}
						for (; count < totalCount; count++) {
							cursor.moveToPosition(count);
							String category = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
							if ((null != category) && (!category.isEmpty()) && (!category.equals("<unknown>"))) {
								int list = 0;
								while (list < mCategoryList.size()) {
									if (category.equals(mCategoryList.get(list))) {
										mCategoryFileCount.put(list, mCategoryFileCount.get(list) + 1);
										break;
									}
									list++;
								}
								if (list == mCategoryList.size()) {
									mCategoryList.add(category);
									mCategoryFileCount.put(list, 1);
								}
							}
						}
					}
				}
				cursor.close();
				if ((0 < token) || (100 > totalCount)) {
					int size = mCategoryList.size();
					int[] totalTime = new int[size];
					int[] fileCount = new int[size];
					for (int index = 0; index < size; index++) {
						totalTime[index] = 0;
						fileCount[index] = mCategoryFileCount.get(index);
					}
					responseList(new ListInfo(MusicUtils.ListType.ARTIST, null, mCategoryList.toArray(new String[size]), null, totalTime, fileCount));
				}
			} else {
				responseList(new ListInfo(MusicUtils.ListType.ARTIST, null, null, null, null, null));
			}
		}
	};

	private void requestAlbumCategory() {
		mCategoryList.clear();
		mCategoryArtistList.clear();
		mCategoryFileCount.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs;
		String orderBy = getOrderbyFromLocalization(MediaStore.Audio.Media.ALBUM);
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
			where.append(")");
		}
		selectionArgs = args.toArray(new String[args.size()]);
		(new NotifyingAsyncQueryHandler(mService, mAlbumQueryListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
	}

	private AsyncQueryListener mAlbumQueryListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (null != cursor) {
				final int totalCount = cursor.getCount();
				if (0 < totalCount) {
					if ((0 == token) || (100 < totalCount)) {
						int count = 0;
						if ((0 < token) && (100 < totalCount)) {
							count = 100;
						}
						for (; count < totalCount; count++) {
							cursor.moveToPosition(count);
							String category = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
							if ((null != category) && (!category.isEmpty()) && (!category.equals("<unknown>"))) {
								int list = 0;
								while (list < mCategoryList.size()) {
									if (category.equals(mCategoryList.get(list))) {
										mCategoryFileCount.put(list, mCategoryFileCount.get(list) + 1);
										break;
									}
									list++;
								}
								if (list == mCategoryList.size()) {
									mCategoryList.add(category);
									String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
									if ((null == artist) || (artist.isEmpty()) || (artist.equals("<unknown>"))) {
										artist = mService.getString(R.string.unknown);
									}
									mCategoryArtistList.add(artist);
									mCategoryFileCount.put(list, 1);
								}
							}
						}
					}
				}
				cursor.close();
				if ((0 < token) || (100 > totalCount)) {
					int size = mCategoryList.size();
					int[] totalTime = new int[size];
					int[] fileCount = new int[size];
					for (int index = 0; index < size; index++) {
						totalTime[index] = 0;
						fileCount[index] = mCategoryFileCount.get(index);
					}
					responseList(new ListInfo(MusicUtils.ListType.ALBUM, null, mCategoryList.toArray(new String[size]), mCategoryArtistList.toArray(new String[size]), totalTime, fileCount));
				}
			} else {
				responseList(new ListInfo(MusicUtils.ListType.ALBUM, null, null, null, null, null));
			}
		}
	};

	private void requestGenreCategory() {
		mCategoryList.clear();
		mCategoryFileCount.clear();
		mGenreList.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs;
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
			where.append(")");
		}
		selectionArgs = args.toArray(new String[args.size()]);
		(new NotifyingAsyncQueryHandler(mService, mGenreQueryListener)).doQuery(uri, projection, where.toString(), selectionArgs, null, true);
	}

	private AsyncQueryListener mGenreQueryListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (null != cursor) {
				final int totalCount = cursor.getCount();
				if (0 < totalCount) {
					if ((0 == token) || (100 < totalCount)) {
						int count = 0;
						if ((0 < token) && (100 < totalCount)) {
							count = 100;
						}
						for (; count < totalCount; count++) {
							cursor.moveToPosition(count);
							Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
							String[] projection = new String[] {
								MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID
							};
							Cursor genreCursor = mService.getContentResolver().query(uri, projection, null, null, null);
							if (null != genreCursor) {
								if (0 < genreCursor.getCount()) {
									genreCursor.moveToFirst();
									String category = genreCursor.getString(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME));
									int genreId = genreCursor.getInt(genreCursor.getColumnIndex(MediaStore.Audio.Genres._ID));
									if ((null != category) && (!category.isEmpty()) && (!category.equals("<unknown>"))) {
										int list = 0;
										while (list < mCategoryList.size()) {
											if (category.equals(mCategoryList.get(list))) {
												mCategoryFileCount.put(genreId, mCategoryFileCount.get(genreId) + 1);
												break;
											}
											list++;
										}
										if (list == mCategoryList.size()) {
											mCategoryList.add(category);
											mCategoryFileCount.put(genreId, 1);
											mGenreList.put(category, genreId);
										}
									}
								}
								genreCursor.close();
							}
						}
					}
				}
				cursor.close();
				if ((0 < token) || (100 > totalCount)) {
					Collections.sort(mCategoryList);
					int size = mCategoryList.size();
					int[] totalTime = new int[size];
					int[] fileCount = new int[size];
					for (int index = 0; index < size; index++) {
						totalTime[index] = 0;
						fileCount[index] = mCategoryFileCount.get(mGenreList.get(mCategoryList.get(index)));
					}
					responseList(new ListInfo(MusicUtils.ListType.GENRE, null, mCategoryList.toArray(new String[size]), null, totalTime, fileCount));
				}
			} else {
				responseList(new ListInfo(MusicUtils.ListType.GENRE, null, null, null, null, null));
			}
		}
	};

	private void requestFolderCategory() {
		mCategoryList.clear();
		mCategoryFileCount.clear();
		mFolderList.clear();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA
		};
		StringBuilder where = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + "=1");
		String[] selectionArgs;
		String orderBy = getOrderbyFromLocalization(MediaStore.Audio.Media.TITLE)/*MediaStore.Audio.Media.DATA*/;
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(" AND (" + MediaStore.Audio.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Audio.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
			where.append(")");
		}
		selectionArgs = args.toArray(new String[args.size()]);
		(new NotifyingAsyncQueryHandler(mService, mFolderQueryListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
	}

	private AsyncQueryListener mFolderQueryListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (null != cursor) {
				final int totalCount = cursor.getCount();
				if (0 < totalCount) {
					if ((0 == token) || (100 < totalCount)) {
						int count = 0;
						if ((0 < token) && (100 < totalCount)) {
							count = 100;
						}
						for (; count < totalCount; count++) {
							cursor.moveToPosition(count);
							String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
							String category = data.substring(0, data.lastIndexOf("/"));
							int list = 0;
							for (int index = 0; index < mCategoryList.size(); index++) {
								if (category.startsWith(mCategoryList.get(index))) {
									mCategoryFileCount.put(index, mCategoryFileCount.get(index) + 1);
								}
							}
							while (list < mCategoryList.size()) {
								if (category.equals(mCategoryList.get(list))) {
									break;
								}
								list++;
							}
							if (list == mCategoryList.size()) {
								mCategoryList.add(category);
								mCategoryFileCount.put(list, 1);
								mFolderList.put(category, list);
							}
						}
					}
				}
				cursor.close();
				if ((0 < token) || (100 > totalCount)) {
					Collections.sort(mCategoryList, new IDAscCompare());
					int size = mCategoryList.size();
					int[] totalTime = new int[size];
					int[] fileCount = new int[size];
					for (int index = 0; index < size; index++) {
						totalTime[index] = 0;
						fileCount[index] = mCategoryFileCount.get(mFolderList.get(mCategoryList.get(index)));
					}
					responseList(new ListInfo(MusicUtils.ListType.FOLDER, null, mCategoryList.toArray(new String[size]), null, totalTime, fileCount));
				}
			} else {
				responseList(new ListInfo(MusicUtils.ListType.FOLDER, null, null, null, null, null));
			}
		}
	};

	private class IDAscCompare implements Comparator<String> {
		@Override
		public int compare(String arg0, String arg1) {
			return arg0.substring(arg0.lastIndexOf("/") + 1).compareTo(arg1.substring(arg1.lastIndexOf("/") + 1));
		}
 	}

	// ----------
	// MediaStoreList internal functions
	private int mPlayingIndex = 0;
	private boolean mActivePlayer = false;
	private boolean mChangeNowPlaying = false;
	private boolean mScanFinish = false;
	private boolean mMakeList = false;

	private void processAfterQuery() {
		if (mScanFinish) {
			if (mActivePlayer) {
				mPlayer.activePlayer();
				mActivePlayer = false;
			} else {
				getPlayingIndex(true);
				mPlayer.onMusicInfo(mPlayingIndex, getMusicInfo(mPlayingIndex, true));
				mPlayer.onAlbumArt(mPlayingIndex, getAlbumArt(mPlayingIndex, true, false));
			}
			requestList(MusicUtils.ListType.NOW_PLAYING, null);
			mScanFinish = false;
		} else if (mActivePlayer) {
			mPlayer.activePlayer();
			mActivePlayer = false;
		} else if (mChangeNowPlaying) {
			getPlayingIndex(true);
			mChangeNowPlaying = false;
		}
		if (MusicUtils.ShuffleState.OFF != mShuffle) {
			setShuffleList();
		}
	}

	private void loadNowPlayingCategory() {
		mNowPlayingCategory = (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY);
		switch (mNowPlayingCategory) {
		case MusicUtils.Category.ARTIST :
			mNowPlayingSubCategory = (String)mService.loadPreference(MusicUtils.Preference.ARTIST_NAME);
			break;
		case MusicUtils.Category.ALBUM :
			mNowPlayingSubCategory = (String)mService.loadPreference(MusicUtils.Preference.ALBUM_NAME);
			break;
		case MusicUtils.Category.GENRE :
			mNowPlayingSubCategory = (String)mService.loadPreference(MusicUtils.Preference.GENRE_ID);
			break;
		case MusicUtils.Category.FOLDER :
			mNowPlayingSubCategory = (String)mService.loadPreference(MusicUtils.Preference.FOLDER_PATH);
			break;
		default :
			break;
		}
	}

	private int getTotalCount(boolean isNowPlaying) {
		if (mChangeNowPlaying) {
			isNowPlaying = false;
		}
		return (isNowPlaying) ? mNowPlayingTotalCount : mCategoryTrackTotalCount;
	}

	private Cursor getCursor(boolean isNowPlaying) {
		if (mChangeNowPlaying) {
			isNowPlaying = false;
		}
		return (isNowPlaying) ? mNowPlayingCursor : mCategoryTrackCursor;
	}

	private String getGenre(Cursor cursor) {
		String genre = mService.getString(R.string.unknown);
		Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		String[] projection = new String[] {
			MediaStore.Audio.Genres.NAME
		};
		Cursor genreCursor = mService.getContentResolver().query(uri, projection, null, null, null);
		if (null != genreCursor) {
			if (0 < genreCursor.getCount()) {
				genreCursor.moveToFirst();
				genre = genreCursor.getString(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME));
			}
			genreCursor.close();
		}
		return genre;
	}

	private void responseList(ListInfo info) {
		mPlayer.onListInfo(info);
		mMakeList = false;
	}
}
