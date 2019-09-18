package com.litbig.app.photo.service.playlist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;
import com.litbig.app.photo.service.PhotoPlaybackService;
import com.litbig.app.photo.service.player.FilePlayer;
import com.litbig.app.photo.util.Log;
import com.litbig.app.photo.util.PhotoUtils;
import com.litbig.mediastorage.MediaStorage;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class MediaStoreList {
	private PhotoPlaybackService mService;
	private FilePlayer mPlayer;

	public MediaStoreList(PhotoPlaybackService service) {
		mService = service;
	}

	// ----------
	// MediaFilePlayListInterface APIs
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
			mPlayer = (FilePlayer)mService.getPhotoPlayer();
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
						if (track.equals(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))) {
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

	public PhotoInfo getPhotoInfo(int index, boolean isNowPlaying) {
		PhotoInfo info = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
			if ((null == title) || (title.isEmpty())) {
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				title = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
			}
			info = new PhotoInfo(
					cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)),
					cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)),
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
			file = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		}
		return file;
	}

	public Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap bitmap = null;
		if ((isNowPlaying) && (!isScale)) {
			bitmap = mImageCache.get(index);
			if (null == bitmap) {
				bitmap = getBitmap(index, isNowPlaying, isScale);
				mImageCache.put(index, bitmap);
			}
			mImageCacheHandler.sendEmptyMessage(0);
		} else {
			bitmap = getBitmap(index, isNowPlaying, isScale);
		}
		return bitmap;
	}

	public String getTrackFromIndex(int index, boolean isNowPlaying) {
		String track = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			track = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			mPlayingIndex = cursor.getPosition();
		}
		return track;
	}

	public String getPrevTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (PhotoUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("prev"));
			} else if (0 >= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToLast();
			} else {
				mNowPlayingCursor.moveToPrevious();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Images.Media.DATA));
			mPlayingIndex = mNowPlayingCursor.getPosition();
		}
		return track;
	}

	public String getNextTrack() {
		String track = mPlayer.getLastTrack();
		if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
			mNowPlayingCursor.moveToPosition(mPlayingIndex);
			if (PhotoUtils.ShuffleState.OFF != mShuffle) {
				mNowPlayingCursor.moveToPosition(setShuffleIndex("next"));
			} else if ((mNowPlayingTotalCount - 1) <= mNowPlayingCursor.getPosition()) {
				mNowPlayingCursor.moveToFirst();
			} else {
				mNowPlayingCursor.moveToNext();
			}
			track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Images.Media.DATA));
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
			if (PhotoUtils.ShuffleState.OFF != shuffle) {
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
			case PhotoUtils.ListType.NOW_PLAYING :
				mCategory = PhotoUtils.Category.NOW_PLAYING;
				if (QUERY_STATE_COMPLETED == mNowPlayingQueryState) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							int[] fileCount = new int[mNowPlayingTotalCount];
							for (int index = 0; index < mNowPlayingTotalCount; index++) {
								fileCount[index] = 0;
							}
							responseList(new ListInfo(PhotoUtils.ListType.NOW_PLAYING, null, mNowPlayingList.toArray(new String[mNowPlayingTotalCount]),
									mNowPlayingPathList.toArray(new String[mNowPlayingTotalCount]) ,fileCount));
						}
					}).start();
				} else {
					responseList(new ListInfo(PhotoUtils.ListType.NOW_PLAYING, null, null, null, null));
				}
				break;
			case PhotoUtils.ListType.ALL :
				mCategory = PhotoUtils.Category.ALL;
				requestCategoryTrack();
				break;
			case PhotoUtils.ListType.FOLDER :
				mCategory = PhotoUtils.Category.FOLDER;
				requestFolderCategory();
				break;
			case PhotoUtils.ListType.FOLDER_TRACK :
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
		case PhotoUtils.Category.ALL :
			break;
		case PhotoUtils.Category.FOLDER :
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
		case PhotoUtils.Category.NOW_PLAYING :
			nowPlaying = true;
			break;
		case PhotoUtils.Category.ALL :
			if (mNowPlayingCategory == category) {
				nowPlaying = true;
			}
			break;
		case PhotoUtils.Category.FOLDER :
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
		case PhotoUtils.Category.ALL :
			if (mCategory != (Integer)mService.loadPreference(PhotoUtils.Preference.CATEGORY)) {
				mService.savePreference(PhotoUtils.Preference.CATEGORY, mCategory);
			}
			break;
		case PhotoUtils.Category.FOLDER :
			if (mCategory != (Integer)mService.loadPreference(PhotoUtils.Preference.CATEGORY)) {
				mService.savePreference(PhotoUtils.Preference.CATEGORY, mCategory);
			}
			if (mSubCategory != mService.loadPreference(PhotoUtils.Preference.FOLDER_PATH)) {
				mService.savePreference(PhotoUtils.Preference.FOLDER_PATH, mSubCategory);
			}
			break;
		default :
			break;
		}
		clearImageCache();
		mChangeNowPlaying = true;
		requestNowPlaying();
		mPlayer.onTotalCount(getTotalCount(true));
	}

	public void clearPlaylist() {
		clearImageCache();
		mNowPlayingQueryState = QUERY_STATE_NONE;
	}

	public void requestQueryForScanFinish(boolean activePlayer) {
		clearImageCache();
		mScanFinish = true;
		mActivePlayer = activePlayer;
		requestNowPlaying();
	}

	// ----------
	// ImageCache internal functions
	private SparseArray<Bitmap> mImageCache = new SparseArray<>();
	private boolean mImageCacheThreadRunning = false;

	private Handler mImageCacheHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mImageCacheThreadRunning) {
				mImageCacheHandler.removeMessages(0);
				mImageCacheHandler.sendEmptyMessage(0);
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						mImageCacheThreadRunning = true;
						int index;
						int firstIndex = mPlayingIndex;
						int lastIndex = mPlayingIndex;
						if (1 < mNowPlayingTotalCount) {
							index = ((mNowPlayingTotalCount - 1) <= mPlayingIndex) ? 0 : (mPlayingIndex + 1);
							if (null == mImageCache.get(index)) {
								mImageCache.put(index, getBitmap(index, true, false));
							}
						}
						if (2 < mNowPlayingTotalCount) {
							index = (0 == mPlayingIndex) ? (mNowPlayingTotalCount - 1) : (mPlayingIndex - 1);
							if (null == mImageCache.get(index)) {
								mImageCache.put(index, getBitmap(index, true, false));
							}
						}
						if (3 < mNowPlayingTotalCount) {
							index = ((mNowPlayingTotalCount - 1) <= mPlayingIndex) ? 1 : ((mNowPlayingTotalCount - 2) <= mPlayingIndex) ? 0 : (mPlayingIndex + 2);
							if (null == mImageCache.get(index)) {
								mImageCache.put(index, getBitmap(index, true, false));
							}
							lastIndex = index;
						}
						if (4 < mNowPlayingTotalCount) {
							index = (0 == mPlayingIndex) ? (mNowPlayingTotalCount - 2) : (1 == mPlayingIndex) ? (mNowPlayingTotalCount - 1) : (mPlayingIndex - 2);
							if (null == mImageCache.get(index)) {
								mImageCache.put(index, getBitmap(index, true, false));
							}
							firstIndex = index;
						}
						if (5 < mImageCache.size()) {
							if (firstIndex > lastIndex) {
								for (index = (lastIndex + 1); index < firstIndex; index++) {
									Bitmap bitmap = mImageCache.get(index);
									if (null != bitmap) {
										if (!bitmap.isRecycled()) {
											bitmap.recycle();
										}
										bitmap = null;
										mImageCache.remove(index);
									}
								}
							} else {
								for (index = 0; index < firstIndex; index++) {
									Bitmap bitmap = mImageCache.get(index);
									if (null != bitmap) {
										if (!bitmap.isRecycled()) {
											bitmap.recycle();
										}
										bitmap = null;
										mImageCache.remove(index);
									}
								}
								for (index = (lastIndex + 1); index < mNowPlayingTotalCount; index++) {
									Bitmap bitmap = mImageCache.get(index);
									if (null != bitmap) {
										if (!bitmap.isRecycled()) {
											bitmap.recycle();
										}
										bitmap = null;
										mImageCache.remove(index);
									}
								}
							}
						}
						Log.d("totalSize = " + mNowPlayingTotalCount + ", firstIndex = " + firstIndex + ", playingIndex = " + mPlayingIndex + ", lastIndex = " + lastIndex + ", mImageCache.size() = " + mImageCache.size());
						mImageCacheThreadRunning = false;
					}
				}).start();
			}
		}
	};

	private void clearImageCache() {
		Log.i("clearImageCache");
		for (int index = 0; index < mNowPlayingTotalCount; index++) {
			Bitmap bitmap = mImageCache.get(index);
			if (null != bitmap) {
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
				}
				bitmap = null;
			}
		}
		mImageCache.clear();
	}

	private Bitmap getBitmap(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap bitmap = null;
		int totalCount = getTotalCount(isNowPlaying);
		Cursor cursor = getCursor(isNowPlaying);
		if ((null != cursor) && (0 <= index) && (totalCount > index)) {
			cursor.moveToPosition(index);
			String track = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			if(track.substring(track.lastIndexOf('.') + 1).toLowerCase().equals("gif")) {
				return null;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(track, options);
			if ((0 < options.outWidth) && (0 < options.outHeight)) {
				if (isScale) {
					final int defaultWidth = 350;
					final int defaultHeight = 200;
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
					bitmap = BitmapFactory.decodeFile(track, options);
				} else if ((3000 < options.outWidth) || (3000 < options.outHeight)) {
					final int defaultWidth = 3000;
					final int defaultHeight = 3000;
					int sampleSize = 1;
					int outWidth = options.outWidth;
					int outHeight = options.outHeight;
					while ((defaultWidth < outWidth) || (defaultHeight < outHeight)) {
						sampleSize <<= 1;
						outWidth >>= 1;
						outHeight >>= 1;
					}
					options.inSampleSize = sampleSize;
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeFile(track, options);
				} else {
					options.inSampleSize = 1;
					options.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeFile(track, options);
				}
			}
		}
		return bitmap;
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
	private int mShuffle = PhotoUtils.ShuffleState.OFF;
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
		if ((PhotoUtils.ShuffleState.OFF != mShuffle) && (0 <= mPlayingIndex) && (mNowPlayingTotalCount > index)) {
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
	private int mNowPlayingCategory = PhotoUtils.Category.ALL;
	private String mNowPlayingSubCategory = null;
	private ArrayList<String> mNowPlayingList = new ArrayList<>();
	private ArrayList<String> mNowPlayingPathList = new ArrayList<>();
	private Cursor mNowPlayingCursor = null;
	private int mNowPlayingTotalCount = 0;

	private void requestNowPlaying() {
		mNowPlayingList.clear();
		mNowPlayingQueryState = QUERY_STATE_STARTED;
		loadNowPlayingCategory();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE,
			MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT,
			MediaStore.Images.Media.DATA
		};
		StringBuilder where = new StringBuilder();
		String[] selectionArgs = null;
		String orderBy = /*MediaStore.Images.Media.TITLE*/MediaStore.Images.Media._ID;
		ArrayList<String> args = new ArrayList<>();
		if (PhotoUtils.Category.FOLDER == mNowPlayingCategory) {
			where.append(MediaStore.Images.Media.DATA + " like ?");
			args.add(mNowPlayingSubCategory + "%");
			selectionArgs = args.toArray(new String[args.size()]);
		} else if (PhotoUtils.Category.ALL == mNowPlayingCategory) {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where.append(MediaStore.Images.Media.DATA + " like ?");
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where.append(" OR " + MediaStore.Images.Media.DATA + " like ?");
					args.add(enableStorage.get(index) + "%");
				}
			}
			selectionArgs = args.toArray(new String[args.size()]);
		} else {
			where = null;
		}
		(new NotifyingAsyncQueryHandler(mService, mNowPlayingListener)).doQuery(uri, projection, where.toString(), selectionArgs, orderBy, true);
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
						String track = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Images.Media.TITLE));
						String name = mNowPlayingCursor.getString(mNowPlayingCursor.getColumnIndex(MediaStore.Images.Media.DATA));
						if ((null == track) || (track.isEmpty())) {
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mNowPlayingPathList.add(name);
						mNowPlayingList.add(track);
					}
					if ((0 < token) || (100 > mNowPlayingTotalCount)) {
						processAfterQuery();
					}
				}
				mRetryQuery = false;
			} else if ((PhotoUtils.Category.ALL != mNowPlayingCategory) && (!mRetryQuery)) {
				mRetryQuery = true;
				mService.savePreference(PhotoUtils.Preference.CATEGORY, PhotoUtils.Category.ALL);
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
	private ArrayList<String> mCategoryFilePathList = new ArrayList<>();
	private Cursor mCategoryTrackCursor = null;
	private int mCategoryTrackTotalCount = 0;
	private int mCategory = PhotoUtils.Category.ALL;
	private String mSubCategory = null;

	private void requestCategoryTrack() {
		mCategoryTrackList.clear();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE,
			MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT,
			MediaStore.Images.Media.DATA
		};
		String where = "";
		String[] selectionArgs = null;
		String orderBy = /*MediaStore.Images.Media.TITLE*/MediaStore.Images.Media._ID;
		ArrayList<String> args = new ArrayList<>();
		if (PhotoUtils.Category.FOLDER == mCategory) {
			where += MediaStore.Images.Media.DATA + " like ?";
			args.add(mSubCategory + "%");
			selectionArgs = args.toArray(new String[args.size()]);
		} else if (PhotoUtils.Category.ALL == mCategory) {
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			if (0 < enableStorage.size()) {
				where += MediaStore.Images.Media.DATA + " like ?";
				args.add(enableStorage.get(0) + "%");
				for (int index = 1; index < enableStorage.size(); index++) {
					where += " OR " + MediaStore.Images.Media.DATA + " like ?";
					args.add(enableStorage.get(index) + "%");
				}
			}
			selectionArgs = args.toArray(new String[args.size()]);
		} else {
			where = null;
		}
		(new NotifyingAsyncQueryHandler(mService, mCategoryTrackListener)).doQuery(uri, projection, where, selectionArgs, orderBy, true);
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
						String track = mCategoryTrackCursor.getString(mCategoryTrackCursor.getColumnIndex(MediaStore.Images.Media.TITLE));
						String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
						if ((null == track) || (track.isEmpty())) {
							track = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
						}
						mCategoryFilePathList.add(name);
						mCategoryTrackList.add(track);
					}
				}
				if ((0 < token) || (100 > mCategoryTrackTotalCount)) {
					int[] fileCount = new int[mCategoryTrackTotalCount];
					for (int index = 0; index < mCategoryTrackTotalCount; index++) {
						fileCount[index] = 0;
					}
					responseList(new ListInfo(getListType(), mSubCategory, mCategoryTrackList.toArray(new String[mCategoryTrackTotalCount]),
							mCategoryFilePathList.toArray(new String[mCategoryTrackTotalCount]), fileCount));
				}
			} else {
				mCategoryTrackCursor = null;
				mCategoryTrackTotalCount = 0;
				responseList(new ListInfo(getListType(), mSubCategory, null, null, null));
			}
		}
	};

	private int getListType() {
		int listType = PhotoUtils.ListType.ALL;
		switch (mCategory) {
		case PhotoUtils.Category.ALL :
			listType = PhotoUtils.ListType.ALL;
			break;
		case PhotoUtils.Category.FOLDER :
			listType = PhotoUtils.ListType.FOLDER_TRACK;
			break;
		default :
			break;
		}
		return listType;
	}

	// ----------
	// CategoryList internal functions
	private ArrayList<String> mCategoryList = new ArrayList<>();
	private ArrayList<String> mCategoryPathList = new ArrayList<>();
	private SparseIntArray mCategoryFileCount = new SparseIntArray();
	private HashMap<String, Integer> mFolderList = new HashMap<>();

	private void requestFolderCategory() {
		mCategoryList.clear();
		mCategoryFileCount.clear();
		mFolderList.clear();
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] {
			MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA
		};
		StringBuilder where = new StringBuilder();
		String[] selectionArgs;
		String orderBy = /*MediaStore.Images.Media.DATA*/MediaStore.Images.Media._ID;
		ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
		ArrayList<String> args = new ArrayList<>();
		if (0 < enableStorage.size()) {
			where.append(MediaStore.Images.Media.DATA + " like ?");
			args.add(enableStorage.get(0) + "%");
			for (int index = 1; index < enableStorage.size(); index++) {
				where.append(" OR " + MediaStore.Images.Media.DATA + " like ?");
				args.add(enableStorage.get(index) + "%");
			}
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
							String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
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
								mCategoryPathList.add(data);
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
					int[] fileCount = new int[size];
					for (int index = 0; index < size; index++) {
						fileCount[index] = mCategoryFileCount.get(mFolderList.get(mCategoryList.get(index)));
					}
					responseList(new ListInfo(PhotoUtils.ListType.FOLDER, null, mCategoryList.toArray(new String[size]), mCategoryPathList.toArray(new String[size]), fileCount));
				}
			} else {
				responseList(new ListInfo(PhotoUtils.ListType.FOLDER, null, null, null, null));
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
				mPlayer.onImageBitmap(mPlayingIndex, getImageBitmap(mPlayingIndex, true, false));
				mPlayer.onPhotoInfo(mPlayingIndex, getPhotoInfo(mPlayingIndex, true));
			}
			requestList(PhotoUtils.ListType.NOW_PLAYING, null);
			mScanFinish = false;
		} else if (mActivePlayer) {
			mPlayer.activePlayer();
			mActivePlayer = false;
		} else if (mChangeNowPlaying) {
			getPlayingIndex(true);
			mChangeNowPlaying = false;
		}
		if (PhotoUtils.ShuffleState.OFF != mShuffle) {
			setShuffleList();
		}
	}

	private void loadNowPlayingCategory() {
		mNowPlayingCategory = (Integer)mService.loadPreference(PhotoUtils.Preference.CATEGORY);
		switch (mNowPlayingCategory) {
		case PhotoUtils.Category.FOLDER :
			mNowPlayingSubCategory = (String)mService.loadPreference(PhotoUtils.Preference.FOLDER_PATH);
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
