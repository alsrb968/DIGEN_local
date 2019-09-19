package com.litbig.app.movie.service.playlist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.litbig.app.movie.R;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;
import com.litbig.app.movie.service.MoviePlaybackService;
import com.litbig.app.movie.service.player.FilePlayer;
import com.litbig.app.movie.util.MovieUtils;
import com.litbig.mediastorage.MediaStorage;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.SparseIntArray;

public class MediaStoreList {
	private MoviePlaybackService mService;
	private FilePlayer mPlayer;

	public MediaStoreList(MoviePlaybackService service) {
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
	}

	public void requestActive() {
		switch (mNowPlayingQueryState) {
		case QUERY_STATE_NONE :
			mPlayer = (FilePlayer)mService.getMoviePlayer();
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
						if (track.equals(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)))) {
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

	public MovieInfo getMovieInfo(int index, boolean isNowPlaying) {
		MovieInfo info = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
			if ((null == title) || (title.isEmpty())) {
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
				title = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
			}
			info = new MovieInfo(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)),
					cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)),
					cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)),
					title);
		}
		return info;
	}

	public String getFileFullPath(int index, boolean isNowPlaying) {
		String file = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			file = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
		}
		return file;
	}

	public Bitmap getVideoThumbnail(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap bitmap = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = false;
			if (isScale) {
//				bitmap = ThumbnailUtils.createVideoThumbnail(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)), MediaStore.Video.Thumbnails.MICRO_KIND);
				bitmap = MediaStore.Video.Thumbnails.getThumbnail(mService.getContentResolver(), (long)id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
			} else {
//				bitmap = ThumbnailUtils.createVideoThumbnail(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)), MediaStore.Video.Thumbnails.MINI_KIND);
				bitmap = MediaStore.Video.Thumbnails.getThumbnail(mService.getContentResolver(), (long)id, MediaStore.Video.Thumbnails.MINI_KIND, options);
			}
		}
		return bitmap;
	}

	public String getTrackFromIndex(int index, boolean isNowPlaying) {
		String track = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			track = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
			mPlayingIndex = cursor.getPosition();
		}
		return track;
	}

	public String getPrevTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (MovieUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("prev"));
			} else if (0 >= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToLast();
			} else {
				mNowPlayingCursor.moveToPrevious();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DATA));
			mPlayingIndex = mNowPlayingCursor.getPosition();
		}
		return track;
	}

	public String getNextTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (MovieUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("next"));
			} else if ((mNowPlayingTotalCount - 1) <= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToFirst();
			} else {
				mNowPlayingCursor.moveToNext();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DATA));
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
			if (MovieUtils.ShuffleState.OFF != shuffle) {
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
			case MovieUtils.ListType.NOW_PLAYING :
				mCategory = MovieUtils.Category.NOW_PLAYING;
				if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							int[] totalTime = new int[mNowPlayingTotalCount];
							int[] fileCount = new int[mNowPlayingTotalCount];
							int[] width = new int[mNowPlayingTotalCount];
							int[] height = new int[mNowPlayingTotalCount];
							for (int index = 0; index < mNowPlayingTotalCount; index++) {
								totalTime[index] = mNowPlayingTotalTimeList.get(index);
								fileCount[index] = 0;
								width[index] = mNowPlayingWidthList.get(index);
								height[index] = mNowPlayingHeightList.get(index);
							}
							responseList(new ListInfo(MovieUtils.ListType.NOW_PLAYING, null, mNowPlayingList.toArray(new String[mNowPlayingTotalCount]), totalTime, fileCount, width, height));
						}
					}).start();
				} else {
					responseList(new ListInfo(MovieUtils.ListType.NOW_PLAYING, null, null, null, null, null, null));
				}
				break;
			case MovieUtils.ListType.ALL :
				mCategory = MovieUtils.Category.ALL;
				requestCategoryTrack();
				break;
			case MovieUtils.ListType.FOLDER :
				mCategory = MovieUtils.Category.FOLDER;
				requestFolderCategory();
				break;
			case MovieUtils.ListType.FOLDER_TRACK :
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
		case MovieUtils.Category.ALL :
			break;
		case MovieUtils.Category.FOLDER :
			subCategory = mNowPlayingSubCategory;
			break;
		default :
			break;
		}
		return subCategory;
	}

	public boolean isNowPlayingCategory(int category, String subCategory) {
		boolean nowPlaying = false;
		switch (category) {
		case MovieUtils.Category.NOW_PLAYING :
			nowPlaying = true;
			break;
		case MovieUtils.Category.ALL :
			if (mNowPlayingCategory == category) {
				nowPlaying = true;
			}
			break;
		case MovieUtils.Category.FOLDER :
			if ((mNowPlayingCategory == category) && (subCategory.equals(mNowPlayingSubCategory))) {
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
		case MovieUtils.Category.ALL :
			if (mCategory != (Integer)mService.loadPreference(MovieUtils.Preference.CATEGORY)) {
				mService.savePreference(MovieUtils.Preference.CATEGORY, mCategory);
			}
			break;
		case MovieUtils.Category.FOLDER :
			if (mCategory != (Integer)mService.loadPreference(MovieUtils.Preference.CATEGORY)) {
				mService.savePreference(MovieUtils.Preference.CATEGORY, mCategory);
			}
			if (mSubCategory != mService.loadPreference(MovieUtils.Preference.FOLDER_PATH)) {
				mService.savePreference(MovieUtils.Preference.FOLDER_PATH, mSubCategory);
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
			Uri uri;
			String [] projection;
			String selection;
			String [] selectionArgs;
			String orderBy;
		}

		NotifyingAsyncQueryHandler(Context context, AsyncQueryListener listener) {
			super(context.getContentResolver());
			mContext = context;
			mListener = new WeakReference<>(listener);
		}

		Cursor doQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy, boolean async) {
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
	private int mShuffle = MovieUtils.ShuffleState.OFF;
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
		if ((MovieUtils.ShuffleState.OFF != mShuffle) && (0 <= mPlayingIndex) && (mNowPlayingTotalCount > index)) {
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
	private int mNowPlayingCategory = MovieUtils.Category.ALL;
	private String mNowPlayingSubCategory = null;
	private ArrayList<String> mNowPlayingList = new ArrayList<>();
	private ArrayList<Integer> mNowPlayingTotalTimeList = new ArrayList<>();
	private ArrayList<Integer> mNowPlayingWidthList = new ArrayList<>();
	private ArrayList<Integer> mNowPlayingHeightList = new ArrayList<>();
	private Cursor mNowPlayingCursor = null;
	private int mNowPlayingTotalCount = 0;

	private void requestNowPlaying() {
		boolean directQuery = false;
		if (null == Looper.myLooper()) {
			directQuery = true;
		}
		mNowPlayingList.clear();
		mNowPlayingTotalTimeList.clear();
		mNowPlayingQueryState = QUERY_STATE_STARTED;
		loadNowPlayingCategory();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
			MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT,
			MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION
		};
		StringBuilder where = new StringBuilder();
		String[] selectionArgs = null;
		String orderBy = getOrderByFromLocalization(MediaStore.Video.Media.TITLE);
		ArrayList<String> args = new ArrayList<>();
		if (MovieUtils.Category.FOLDER == mNowPlayingCategory) {
			where.append(MediaStore.Video.Media.DATA + " like ?");
			args.add(mNowPlayingSubCategory + "%");
			selectionArgs = args.toArray(new String[0]);
		} else if (MovieUtils.Category.ALL == mNowPlayingCategory) {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where.append(MediaStore.Video.Media.DATA + " like ?");
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where.append(" OR " + MediaStore.Video.Media.DATA + " like ?");
					args.add(enableStorage.get(index) + "%");
				}
			}
			selectionArgs = args.toArray(new String[0]);
		} else {
			where = null;
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
					String track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.TITLE));
					if ((null == track) || (track.isEmpty())) {
						String name = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DATA));
						track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
					}
					mNowPlayingList.add(track);
					mNowPlayingTotalTimeList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
					mNowPlayingWidthList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.WIDTH)));
					mNowPlayingHeightList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)));
				}
				processAfterQuery();
				mRetryQuery = false;
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
						String track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.TITLE));
						if ((null == track) || (track.isEmpty())) {
							String name = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DATA));
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mNowPlayingList.add(track);
						mNowPlayingTotalTimeList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
						mNowPlayingWidthList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.WIDTH)));
						mNowPlayingHeightList.add(mNowPlayingCursor.getInt(mNowPlayingCursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)));
					}
					if ((0 < token) || (100 > mNowPlayingTotalCount)) {
						processAfterQuery();
					}
				}
				mRetryQuery = false;
			} else if ((MovieUtils.Category.ALL != mNowPlayingCategory) && (!mRetryQuery)) {
				mRetryQuery = true;
				mService.savePreference(MovieUtils.Preference.CATEGORY, MovieUtils.Category.ALL);
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
	private ArrayList<Integer> mCategoryTrackTotalTimeList = new ArrayList<>();
	private ArrayList<Integer> mCategoryTrackWidthList = new ArrayList<>();
	private ArrayList<Integer> mCategoryTrackHeightList = new ArrayList<>();
	private Cursor mCategoryTrackCursor = null;
	private int mCategoryTrackTotalCount = 0;
	private int mCategory = MovieUtils.Category.ALL;
	private String mSubCategory = null;

	private void requestCategoryTrack() {
		mCategoryTrackList.clear();
		mCategoryTrackTotalTimeList.clear();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
			MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT,
			MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION
		};
		StringBuilder where = new StringBuilder();
		String[] selectionArgs = null;
		String orderBy = getOrderByFromLocalization(MediaStore.Video.Media.TITLE);
		ArrayList<String> args = new ArrayList<>();
		if (MovieUtils.Category.FOLDER == mCategory) {
			where.append(MediaStore.Video.Media.DATA + " like ?");
			args.add(mSubCategory + "%");
			selectionArgs = args.toArray(new String[0]);
		} else if (MovieUtils.Category.ALL == mCategory) {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where.append(MediaStore.Video.Media.DATA + " like ?");
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where.append(" OR " + MediaStore.Video.Media.DATA + " like ?");
					args.add(enableStorage.get(index) + "%");
				}
			}
			selectionArgs = args.toArray(new String[0]);
		} else {
			where = null;
		}
		(new NotifyingAsyncQueryHandler(mService, mCategoryTrackListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
	}

	private AsyncQueryListener mCategoryTrackListener = new AsyncQueryListener() {
		@Override
		public void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if ((null != cursor) && (0 < cursor.getCount())) {
				if ((0 == token) || (100 < cursor.getCount())) {
					mCategoryTrackCursor = cursor;
					mCategoryTrackTotalCount = mCategoryTrackCursor.getCount();
					int count = 0;
					if ((0 < token) && (100 < mCategoryTrackTotalCount)) {
						count = 100;
					}
					for (; count < mCategoryTrackTotalCount; count++) {
						mCategoryTrackCursor.moveToPosition(count);
						String track = mCategoryTrackCursor.getString(mCategoryTrackCursor.getColumnIndex(MediaStore.Video.Media.TITLE));
						if ((null == track) || (track.isEmpty())) {
							String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mCategoryTrackList.add(track);
						mCategoryTrackTotalTimeList.add(mCategoryTrackCursor.getInt(mCategoryTrackCursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
						mCategoryTrackWidthList.add(mCategoryTrackCursor.getInt(mCategoryTrackCursor.getColumnIndex(MediaStore.Video.Media.WIDTH)));
						mCategoryTrackHeightList.add(mCategoryTrackCursor.getInt(mCategoryTrackCursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)));
					}
				}
				if ((0 < token) || (100 > mCategoryTrackTotalCount)) {
					int[] totalTime = new int[mCategoryTrackTotalCount];
					int[] fileCount = new int[mCategoryTrackTotalCount];
					int[] width = new int[mCategoryTrackTotalCount];
					int[] height = new int[mCategoryTrackTotalCount];
					for (int index = 0; index < mCategoryTrackTotalCount; index++) {
						totalTime[index] = mCategoryTrackTotalTimeList.get(index);
						fileCount[index] = 0;
						width[index] = mCategoryTrackWidthList.get(index);
						height[index] = mCategoryTrackHeightList.get(index);
					}
					responseList(new ListInfo(getListType(), mSubCategory, mCategoryTrackList.toArray(new String[mCategoryTrackTotalCount]), totalTime, fileCount, width, height));
				}
			} else {
				mCategoryTrackCursor = null;
				mCategoryTrackTotalCount = 0;
				responseList(new ListInfo(getListType(), mSubCategory, null, null, null, null, null));
			}
		}
	};

	private String getOrderByFromLocalization(String name) {
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
		int listType = MovieUtils.ListType.ALL;
		switch (mCategory) {
		case MovieUtils.Category.ALL :
			listType = MovieUtils.ListType.ALL;
			break;
		case MovieUtils.Category.FOLDER :
			listType = MovieUtils.ListType.FOLDER_TRACK;
			break;
		default :
			break;
		}
		return listType;
	}

	// ----------
	// CategoryList internal functions
	private ArrayList<String> mCategoryList = new ArrayList<>();
	private SparseIntArray mCategoryFileCount = new SparseIntArray();
	private HashMap<String, Integer> mFolderList = new HashMap<>();

	private void requestFolderCategory() {
		mCategoryList.clear();
		mCategoryFileCount.clear();
		mFolderList.clear();
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA
		};
		StringBuilder where = new StringBuilder();
		String[] selectionArgs;
		String orderBy = getOrderByFromLocalization(MediaStore.Video.Media.DATA);
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(MediaStore.Video.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Video.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
		}
		selectionArgs = args.toArray(new String[0]);
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
							String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
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
					int[] width = new int[size];
					int[] height = new int[size];
					for (int index = 0; index < size; index++) {
						totalTime[index] = 0;
						fileCount[index] = mCategoryFileCount.get(mFolderList.get(mCategoryList.get(index)));
						width[index] = 0;
						height[index] = 0;
					}
					responseList(new ListInfo(MovieUtils.ListType.FOLDER, null, mCategoryList.toArray(new String[size]), totalTime, fileCount, width, height));
				}
			} else {
				responseList(new ListInfo(MovieUtils.ListType.FOLDER, null, null, null, null, null, null));
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
				mPlayer.onMovieInfo(mPlayingIndex, getMovieInfo(mPlayingIndex, true));
				mPlayer.onVideoThumbnail(mPlayingIndex, getVideoThumbnail(mPlayingIndex, true, false));
			}
			requestList(MovieUtils.ListType.NOW_PLAYING, null);
			mScanFinish = false;
		} else if (mActivePlayer) {
			mPlayer.activePlayer();
			mActivePlayer = false;
		} else if (mChangeNowPlaying) {
			getPlayingIndex(true);
			mChangeNowPlaying = false;
		}
		if (MovieUtils.ShuffleState.OFF != mShuffle) {
			setShuffleList();
		}
	}

	private void loadNowPlayingCategory() {
		mNowPlayingCategory = (Integer)mService.loadPreference(MovieUtils.Preference.CATEGORY);
		switch (mNowPlayingCategory) {
		case MovieUtils.Category.FOLDER :
			mNowPlayingSubCategory = (String)mService.loadPreference(MovieUtils.Preference.FOLDER_PATH);
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

	private void responseList(ListInfo info) {
		mPlayer.onListInfo(info);
		mMakeList = false;
	}
}
