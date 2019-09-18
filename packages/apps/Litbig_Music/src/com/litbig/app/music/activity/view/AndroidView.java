package com.litbig.app.music.activity.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.litbig.app.music.R;
import com.litbig.app.music.activity.MusicActivity;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.util.Log;
import com.litbig.app.music.util.MusicUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidView extends MusicView {
	// ----------
	// AndroidView behavior
	private float mDpRate;
	private LayoutInflater mLayoutInflater;

	public AndroidView(MusicActivity activity) {
		super(activity);
		mDpRate = getDisplayRate();
	}

	@Override
	public void onCreate() {
		mLayoutInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initPlayerView();
		initListView();
	}

	@Override
	public void onResume() {
		mMusicPlayerView.setVisibility(View.VISIBLE);
		mMusicListView.setVisibility(View.GONE);
		mCurrentIndex.setText("0");
		mTotalCount.setText(" of " + "0");
		mTitle.setText("");
		mArtist.setText("");
		mAlbum.setText("");
		mAlbumArt.setImageResource(android.R.color.transparent);
		mListAlbumArt.setImageResource(android.R.color.transparent);
	}

	@Override
	public void onPause() {
		mImageLoader.destroy();
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onBackPressed() {
		if (View.VISIBLE == mMusicListView.getVisibility()) {
			backToPlayerView();
		} else {
			// if (null != mActivity.getService()) {
			// 	try {
			// 		mActivity.getService().finish();
			// 		Log.e("Service finish()");
			// 	} catch (RemoteException e) {
			// 		e.printStackTrace();
			// 	}
			// }
			mActivity.finish();
		}
	}

	@Override
	public void onServiceConnected() {
	}

	@Override
	public void onServiceDisconnected() {
	}

	@Override
	public void onMediaScan(boolean scanning) {
		if (scanning) {
			if (View.VISIBLE == mMusicListView.getVisibility()) {
				backToPlayerView();
			}
		}
		mScanning = scanning;
	}

	private float getDisplayRate() {
		final float DEFAULT_WIDTH = 1024.0f;
		final float DEFAULT_HEIGHT = 600.0f;
		float rate;
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		if (((float)metrics.widthPixels / DEFAULT_WIDTH) <= ((float)metrics.heightPixels / DEFAULT_HEIGHT)) {
			rate = (float)metrics.widthPixels / DEFAULT_WIDTH;
		} else {
			rate = (float)metrics.heightPixels / DEFAULT_HEIGHT;
		}
		return rate;
	}

	// ----------
	// MusicServiceCallback APIs
	@Override
	public void onTotalCount(int totalCount) {
		if (0 < totalCount) {
			mTotalCount.setText(" of " + Integer.valueOf(totalCount).toString());
		}
		if (1 >= totalCount) {
			mShuffleButton.setEnabled(false);
			mRepeatButton.setEnabled(false);
			mScanButton.setEnabled(false);
			mShuffleButton.setAlpha(0.5f);
			mRepeatButton.setAlpha(0.5f);
			mScanButton.setAlpha(0.5f);
		} else {
			mShuffleButton.setEnabled(true);
			mRepeatButton.setEnabled(true);
			mScanButton.setEnabled(true);
			mShuffleButton.setAlpha(1.0f);
			mRepeatButton.setAlpha(1.0f);
			mScanButton.setAlpha(1.0f);
		}
	}

	@Override
	public void onPlayState(int playState) {
		mPlayState = playState;
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_PLAY_STATE_PAUSE);
		switch (playState) {
		case MusicUtils.PlayState.PLAY :
			mCurrentTime.setVisibility(View.VISIBLE);
			mPlayPause.setImageResource(R.drawable.btn_pause_click);
			mListPlayPauseButton.setImageResource(R.drawable.btn_pause_click);
			break;
		case MusicUtils.PlayState.PAUSE :
			mPlayPause.setImageResource(R.drawable.btn_play_click);
			mListPlayPauseButton.setImageResource(R.drawable.btn_play_click);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_PLAY_STATE_PAUSE, mTimerHandler.INTERVAL_FOR_BLINK_TIME);
			break;
		case MusicUtils.PlayState.FAST_FORWARD :
		case MusicUtils.PlayState.FAST_REWIND :
			mCurrentTime.setVisibility(View.VISIBLE);
			mPlayPause.setImageResource(R.drawable.btn_play_click);
			mListPlayPauseButton.setImageResource(R.drawable.btn_play_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) {
		if ((0 < mPlayingDuration) && (mPlayingDuration >= playTimeMS) && (0 <= playTimeMS)) {
			mCurrentTime.setText(makeTimeFormat(playTimeMS));
			mTimeProgress.setProgress(playTimeMS);
		}
	}

	@Override
	public void onMusicInfo(int index, MusicInfo info) {
		Log.d("index = " + index + ", title = " + info.getTitle());
		mPlayingIndex = getPlayingIndex();
		mShowIndex = index;
		if (mPlayingIndex != index) {
			Log.d("index = " + index + ", currentIndex = " + mPlayingIndex);
			mTimerHandler.removeMessages(mTimerHandler.MESSAGE_RETURN_TO_PLAYING);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_RETURN_TO_PLAYING, mTimerHandler.DELAY_FOR_RETURN_TIME);
			showMusicInfo(info);
		} else {
			mPlayingMusicInfo = info;
			mPlayingDuration = info.getTotalTimeMS();
			mTimeProgress.setMax(mPlayingDuration);
			showMusicInfo(info);
			mTotalTime.setText(makeTimeFormat(mPlayingDuration));
			if (View.VISIBLE == mMusicListView.getVisibility()) {
				if (isNowPlayingCategory()) {
					mListAdapter.notifyDataSetChanged();
				}
			}
		}
		mCurrentIndex.setText(Integer.valueOf(index + 1).toString());
	}

	@Override
	public void onAlbumArt(int index, Bitmap albumArt) {
		if (null == albumArt) {
			Log.w("index = " + index + ", albumArt null");
			mAlbumArt.setImageResource(R.drawable.cover_img);
			mListAlbumArt.setImageResource(R.drawable.cover_img);
		} else {
			Log.d("index = " + index + ", albumArt OK");
			mAlbumArt.setImageBitmap(albumArt);
			mListAlbumArt.setImageBitmap(albumArt);
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
		switch (shuffle) {
		case MusicUtils.ShuffleState.OFF :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_off_click);
			break;
		case MusicUtils.ShuffleState.ALL :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_on_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onRepeatState(int repeat) {
		switch (repeat) {
		case MusicUtils.RepeatState.ALL :
			mRepeatButton.setImageResource(R.drawable.btn_repeat_all_click);
			break;
		case MusicUtils.RepeatState.ONE :
			mRepeatButton.setImageResource(R.drawable.btn_repeat_1_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onScanState(int scan) {
		switch (scan) {
		case MusicUtils.ScanState.OFF :
			mScanButton.setImageResource(R.drawable.btn_scan_off_click);
			break;
		case MusicUtils.ScanState.ALL :
			mScanButton.setImageResource(R.drawable.btn_scan_on_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onListInfo(ListInfo info) {
		if (null != info) {
			if ((null == info.getList()) || (0 == info.getList().length)) {
				switch (info.getListType()) {
				case MusicUtils.ListType.NOW_PLAYING :
					requestList(MusicUtils.ListType.ALL, null);
					break;
				case MusicUtils.ListType.ARTIST :
				case MusicUtils.ListType.ALBUM :
				case MusicUtils.ListType.GENRE :
				case MusicUtils.ListType.FOLDER :
//				case MusicUtils.ListType.RECENT :
					requestList(MusicUtils.ListType.NOW_PLAYING, null);
					break;
				case MusicUtils.ListType.ARTIST_TRACK :
					requestList(MusicUtils.ListType.ARTIST, null);
					break;
				case MusicUtils.ListType.ALBUM_TRACK :
					requestList(MusicUtils.ListType.ALBUM, null);
					break;
				case MusicUtils.ListType.GENRE_TRACK :
					requestList(MusicUtils.ListType.GENRE, null);
					break;
				case MusicUtils.ListType.FOLDER_TRACK :
					requestList(MusicUtils.ListType.FOLDER, null);
					break;
				default :
					backToPlayerView();
					break;
				}
			} else {
				mListInfo = info;
				mListAdapter = new ListItemAdapter(mActivity);
				mItemList.setKeywordList(new ArrayList<>(Arrays.asList(mListInfo.getList())));
				mItemList.setAdapter(mListAdapter);
				switch (info.getListType()) {
				case MusicUtils.ListType.NOW_PLAYING :
					setNowPlaying();
					mItemList.setSelection(mPlayingIndex);
					break;
				case MusicUtils.ListType.ALL :
					selectCategoryButton(MusicUtils.Category.ALL);
					mUpstep.setVisibility(View.GONE);
					break;
				case MusicUtils.ListType.ARTIST :
					selectCategoryButton(MusicUtils.Category.ARTIST);
					mUpstep.setVisibility(View.GONE);
					break;
				case MusicUtils.ListType.ALBUM :
					selectCategoryButton(MusicUtils.Category.ALBUM);
					mUpstep.setVisibility(View.GONE);
					break;
				case MusicUtils.ListType.GENRE :
					selectCategoryButton(MusicUtils.Category.GENRE);
					mUpstep.setVisibility(View.GONE);
					break;
				case MusicUtils.ListType.FOLDER :
					selectCategoryButton(MusicUtils.Category.FOLDER);
					mUpstep.setVisibility(View.GONE);
					break;
//				case MusicUtils.ListType.RECENT :
//					selectCategoryButton(MusicUtils.Category.RECENT);
//					mUpstep.setVisibility(View.GONE);
//					break;
				case MusicUtils.ListType.ARTIST_TRACK :
					selectCategoryButton(MusicUtils.Category.ARTIST);
					mUpstep.setVisibility(View.VISIBLE);
					mSubCategoryName.setText(info.getSubCategory());
					break;
				case MusicUtils.ListType.ALBUM_TRACK :
					selectCategoryButton(MusicUtils.Category.ALBUM);
					mUpstep.setVisibility(View.VISIBLE);
					mSubCategoryName.setText(info.getSubCategory());
					break;
				case MusicUtils.ListType.GENRE_TRACK :
					selectCategoryButton(MusicUtils.Category.GENRE);
					mUpstep.setVisibility(View.VISIBLE);
					mSubCategoryName.setText(info.getSubCategory());
					break;
				case MusicUtils.ListType.FOLDER_TRACK :
					String subCategory = info.getSubCategory();
					if (null == subCategory) {
						requestList(MusicUtils.ListType.FOLDER, null);
					} else {
						String folder = subCategory.substring(subCategory.lastIndexOf("/") + 1);
						folder = setCharset(folder);
						selectCategoryButton(MusicUtils.Category.FOLDER);
						mUpstep.setVisibility(View.VISIBLE);
						mSubCategoryName.setText(folder);
					}
					break;
				default :
					break;
				}
			}
		}
	}

	@Override
	public void onError(String error) {
		Toast.makeText(mActivity.getApplicationContext(), error, Toast.LENGTH_LONG).show();
	}

	// ----------
	// Music Player View
	private View mMusicPlayerView;
	private ImageView mAlbumArt;
	private TextView mCurrentIndex;
	private TextView mTotalCount;
	private TextView mTitle;
	private TextView mArtist;
	private TextView mAlbum;
	private ImageButton mListButton;
	private ImageButton mPrevious;
	private ImageButton mPlayPause;
	private ImageButton mNext;
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;
	private ImageButton mScanButton;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private SeekBar mTimeProgress;

	private int mPlayingIndex = 0;
	private int mShowIndex = 0;
	private boolean mPreviewMode = false;
	private MusicInfo mPlayingMusicInfo = null;
	private int mPlayingDuration = 0;
	private int mPlayState = MusicUtils.PlayState.STOP;
	private boolean mScanning = false;
	private boolean mPreviousPressed = false;
	private boolean mNextPressed = false;

	private void initPlayerView() {
		mMusicPlayerView = mLayoutInflater.inflate(R.layout.music_player, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mMusicPlayerView);
		mAlbumArt = mMusicPlayerView.findViewById(R.id.album_image);
		mCurrentIndex = mMusicPlayerView.findViewById(R.id.current_index);
		mCurrentIndex.setText("0");
		mCurrentIndex.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTotalCount = mMusicPlayerView.findViewById(R.id.total_count);
		mTotalCount.setText(" of " + "0");
		mTotalCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTitle = mMusicPlayerView.findViewById(R.id.title_text);
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f * mDpRate);
		mArtist = mMusicPlayerView.findViewById(R.id.artist_text);
		mArtist.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24.0f * mDpRate);
		mAlbum = mMusicPlayerView.findViewById(R.id.album_text);
		mAlbum.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mListButton = mMusicPlayerView.findViewById(R.id.list_button);
		mListButton.setOnClickListener(mClickListener);
		mPrevious = mMusicPlayerView.findViewById(R.id.previous_button);
		mPrevious.setOnTouchListener(mTouchListener);
		mPlayPause = mMusicPlayerView.findViewById(R.id.play_pause_button);
		mPlayPause.setOnClickListener(mClickListener);
		mPlayPause.setImageResource(R.drawable.btn_play_click);
		mNext = mMusicPlayerView.findViewById(R.id.next_button);
		mNext.setOnTouchListener(mTouchListener);
		mShuffleButton = mMusicPlayerView.findViewById(R.id.shuffle_button);
		mShuffleButton.setOnClickListener(mClickListener);
		mRepeatButton = mMusicPlayerView.findViewById(R.id.repeat_button);
		mRepeatButton.setOnClickListener(mClickListener);
		mScanButton = mMusicPlayerView.findViewById(R.id.scan_button);
		mScanButton.setOnClickListener(mClickListener);
		mCurrentTime = mMusicPlayerView.findViewById(R.id.current_time);
		mCurrentTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTimeProgress = mMusicPlayerView.findViewById(R.id.time_progress);
		mTimeProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mTotalTime = mMusicPlayerView.findViewById(R.id.total_time);
		mTotalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
	}

	private void showMusicInfo(MusicInfo info) {
		if (null != info) {
			mTitle.setText(info.getTitle());
			mArtist.setText(info.getArtist());
			mAlbum.setText(info.getAlbum());
			if (mPlayingIndex == mShowIndex) {
				int viewId = R.drawable.btn_play_click;
				switch (getPlayState()) {
				case MusicUtils.PlayState.PLAY :
					viewId = R.drawable.btn_pause_click;
					break;
				case MusicUtils.PlayState.PAUSE :
					viewId = R.drawable.btn_play_click;
					break;
				default :
					break;
				}
				mPrevious.setEnabled(true);
				mPlayPause.setImageResource(viewId);
				mNext.setEnabled(true);
				mTimeProgress.setEnabled(true);
				mCurrentIndex.setTextColor(Color.WHITE);
				mTitle.setTextColor(Color.WHITE);
				mArtist.setTextColor(Color.WHITE);
				mAlbum.setTextColor(Color.parseColor("#CCCCCC"));
			} else if (mPreviewMode) {
				mPrevious.setEnabled(false);
				mPlayPause.setImageResource(R.drawable.btn_play_click);
				mNext.setEnabled(false);
				mTimeProgress.setEnabled(false);
				mCurrentIndex.setTextColor(Color.BLUE);
				mTitle.setTextColor(Color.BLUE);
				mArtist.setTextColor(Color.BLUE);
				mAlbum.setTextColor(Color.parseColor("#0000CC"));
			}
		}
	}

	private String makeTimeFormat(int timeMS) {
		int hour, min, sec;
		int timeS = timeMS / 1000;
		hour = timeS / 3600;
		min = (timeS - (hour * 3600)) / 60;
		sec = timeS - (hour * 3600) - (min * 60);
		StringBuilder timeFormat = new StringBuilder();
		if (hour > 0) {
			timeFormat.append(String.format("%d:", hour));
		}
		if (min < 10) {
			timeFormat.append(String.format("0%d:", min));
		} else {
			timeFormat.append(String.format("%d:", min));
		}
		if (sec < 10) {
			timeFormat.append(String.format("0%d", sec));
		} else {
			timeFormat.append(String.format("%d", sec));
		}
		return timeFormat.toString();
	}

	// ----------
	// Music List View
	private View mMusicListView;
	private ImageView mListAlbumArt;
	private ImageButton mCategoryRecentButton;
	private ImageButton mCategoryAllButton;
	private ImageButton mCategoryAlbumButton;
	private ImageButton mCategoryArtistButton;
	private ImageButton mCategoryFolderButton;
	private ImageButton mListPreviousButton;
	private ImageButton mListPlayPauseButton;
	private ImageButton mListNextButton;
	private LinearLayout mUpstep;
	private TextView mSubCategoryName;
	private KoreanIndexerListView mItemList;

	private ListItemAdapter mListAdapter;
	private ListInfo mListInfo = null;
	private int mNowPlayingCategory = MusicUtils.Category.ALL;

	private void initListView() {
		mMusicListView = mLayoutInflater.inflate(R.layout.music_list, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mMusicListView);
		mListAlbumArt = mMusicListView.findViewById(R.id.list_album_image);
		mCategoryRecentButton = mMusicListView.findViewById(R.id.category_recent_button);
		mCategoryRecentButton.setOnClickListener(mClickListener);
		mCategoryAllButton = mMusicListView.findViewById(R.id.category_all_button);
		mCategoryAllButton.setOnClickListener(mClickListener);
		mCategoryAlbumButton = mMusicListView.findViewById(R.id.category_album_button);
		mCategoryAlbumButton.setOnClickListener(mClickListener);
		mCategoryArtistButton = mMusicListView.findViewById(R.id.category_artist_button);
		mCategoryArtistButton.setOnClickListener(mClickListener);
		mCategoryFolderButton = mMusicListView.findViewById(R.id.category_folder_button);
		mCategoryFolderButton.setOnClickListener(mClickListener);
		mListPreviousButton = mMusicListView.findViewById(R.id.list_previous_button);
		mListPreviousButton.setOnClickListener(mClickListener);
		mListPlayPauseButton = mMusicListView.findViewById(R.id.list_play_pause_button);
		mListPlayPauseButton.setOnClickListener(mClickListener);
		mListPlayPauseButton.setImageResource(R.drawable.btn_play_click);
		mListNextButton = mMusicListView.findViewById(R.id.list_next_button);
		mListNextButton.setOnClickListener(mClickListener);
		mUpstep = mMusicListView.findViewById(R.id.list_upstep);
		mUpstep.setOnClickListener(mClickListener);
		mSubCategoryName = mMusicListView.findViewById(R.id.sub_category);
		mSubCategoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14.0f * mDpRate);
		mItemList = mMusicListView.findViewById(R.id.item_list);
		mItemList.setOnItemClickListener(mItemClickListener);
	}

	private void selectCategoryButton(int category) {
		switch (category) {
		case MusicUtils.Category.ALL :
			mCategoryAllButton.setSelected(true);
			mCategoryAlbumButton.setSelected(false);
			mCategoryArtistButton.setSelected(false);
			mCategoryFolderButton.setSelected(false);
			mCategoryRecentButton.setSelected(false);
			break;
		case MusicUtils.Category.ARTIST :
			mCategoryAllButton.setSelected(false);
			mCategoryAlbumButton.setSelected(false);
			mCategoryArtistButton.setSelected(true);
			mCategoryFolderButton.setSelected(false);
			mCategoryRecentButton.setSelected(false);
			break;
		case MusicUtils.Category.ALBUM :
			mCategoryAllButton.setSelected(false);
			mCategoryAlbumButton.setSelected(true);
			mCategoryArtistButton.setSelected(false);
			mCategoryFolderButton.setSelected(false);
			mCategoryRecentButton.setSelected(false);
			break;
		case MusicUtils.Category.FOLDER :
			mCategoryAllButton.setSelected(false);
			mCategoryAlbumButton.setSelected(false);
			mCategoryArtistButton.setSelected(false);
			mCategoryFolderButton.setSelected(true);
			mCategoryRecentButton.setSelected(false);
			break;
		case MusicUtils.Category._RECENT :
			mCategoryAllButton.setSelected(false);
			mCategoryAlbumButton.setSelected(false);
			mCategoryArtistButton.setSelected(false);
			mCategoryFolderButton.setSelected(false);
			mCategoryRecentButton.setSelected(true);
			break;
		default :
			break;
		}
	}

	private void setNowPlaying() {
		mNowPlayingCategory = getNowPlayingCategory();
		selectCategoryButton(mNowPlayingCategory);
		switch (mNowPlayingCategory) {
		case MusicUtils.ListType.ALL :
//		case MusicUtils.ListType.RECENT :
			mUpstep.setVisibility(View.GONE);
			break;
		case MusicUtils.ListType.ARTIST :
		case MusicUtils.ListType.ALBUM :
		case MusicUtils.ListType.GENRE :
		case MusicUtils.ListType.FOLDER :
			mUpstep.setVisibility(View.VISIBLE);
			String subCategory = getNowPlayingSubCategory();
			String folder = subCategory.substring(subCategory.lastIndexOf("/") + 1);
			folder = setCharset(folder);
			mSubCategoryName.setText(folder);
			break;
		default :
			break;
		}
	}

	private void selectItemToPlay(int index, boolean isNowPlaying) {
		if (playIndex(index, isNowPlaying)) {
			if (View.VISIBLE == mMusicListView.getVisibility()) {
				backToPlayerView();
			}
		} else {
			Toast.makeText(mActivity.getApplicationContext(), mListInfo.getList()[index] + "\n" + mActivity.getString(R.string.error_not_support), Toast.LENGTH_LONG).show();
		}
	}

	private boolean isPlayingItem(int position) {
		boolean isPlaying = false;
		if (null != mListInfo) {
			switch (mListInfo.getListType()) {
			case MusicUtils.ListType.NOW_PLAYING :
				if (mPlayingIndex == position) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.ALL :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MusicUtils.Category.ALL, null))) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.ARTIST :
				if (isNowPlayingCategory(MusicUtils.Category.ARTIST, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.ALBUM :
				if (isNowPlayingCategory(MusicUtils.Category.ALBUM, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.GENRE :
				if (isNowPlayingCategory(MusicUtils.Category.GENRE, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.FOLDER :
				if (isNowPlayingCategory(MusicUtils.Category.FOLDER, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
//			case MusicUtils.ListType.RECENT :
//				if ((mPlayingIndex == position) && (true == isNowPlayingCategory(MusicUtils.Category.RECENT, null))) {
//					isPlaying = true;
//				}
//				break;
			case MusicUtils.ListType.ARTIST_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MusicUtils.Category.ARTIST, mListInfo.getSubCategory()))) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.ALBUM_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MusicUtils.Category.ALBUM, mListInfo.getSubCategory()))) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.GENRE_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MusicUtils.Category.GENRE, mListInfo.getSubCategory()))) {
					isPlaying = true;
				}
				break;
			case MusicUtils.ListType.FOLDER_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MusicUtils.Category.FOLDER, mListInfo.getSubCategory()))) {
					isPlaying = true;
				}
				break;
			default :
				break;
			}
		}
		return isPlaying;
	}

	private boolean isNowPlayingCategory() {
		boolean isPlaying = false;
		if (null != mListInfo) {
			switch (mListInfo.getListType()) {
			case MusicUtils.ListType.NOW_PLAYING :
				isPlaying = true;
				break;
			case MusicUtils.ListType.ALL :
				isPlaying = isNowPlayingCategory(MusicUtils.Category.ALL, null);
				break;
			case MusicUtils.ListType.ARTIST_TRACK :
				isPlaying = isNowPlayingCategory(MusicUtils.Category.ARTIST, mListInfo.getSubCategory());
				break;
			case MusicUtils.ListType.ALBUM_TRACK :
				isPlaying = isNowPlayingCategory(MusicUtils.Category.ALBUM, mListInfo.getSubCategory());
				break;
			case MusicUtils.ListType.GENRE_TRACK :
				isPlaying = isNowPlayingCategory(MusicUtils.Category.GENRE, mListInfo.getSubCategory());
				break;
			case MusicUtils.ListType.FOLDER_TRACK :
				isPlaying = isNowPlayingCategory(MusicUtils.Category.FOLDER, mListInfo.getSubCategory());
				break;
//			case MusicUtils.ListType.RECENT :
//				isPlaying = isNowPlayingCategory(MusicUtils.Category.RECENT, null);
//				break;
			default :
				break;
			}
		}
		return isPlaying;
	}

	private void backToPlayerView() {
		mImageLoader.destroy();
		mMusicPlayerView.setVisibility(View.VISIBLE);
		mMusicListView.setVisibility(View.GONE);
		mItemList.setAdapter(null);
		mListInfo = null;
	}

	// ----------
	// ViewHolder
	private class ViewHolder {
		public LinearLayout folder;
		public LinearLayout item;
		public ImageView albumart;
		public TextView title;
		public TextView artist;
		public TextView info;
		public ImageView playing;
	}

	// ----------
	// ListAdapter
	private class ListItemAdapter extends KoreanIndexerListView.KoreanIndexerAdapter<String> {
	    private ArrayList<String> list;
		public ListItemAdapter(Context context) {
			super(context, new ArrayList<>(Arrays.asList(mListInfo.getList())));
			list = new ArrayList<>(Arrays.asList(mListInfo.getList()));
			mActivity.clearAlbumArtBuffer();
			mImageLoader.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			convertView = ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.icon_list_item, parent, false);
			holder.folder = convertView.findViewById(R.id.list_folder);
			holder.item = convertView.findViewById(R.id.list_item);
			holder.albumart = convertView.findViewById(R.id.list_albumart);
			holder.title = convertView.findViewById(R.id.list_title);
			holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18 * mDpRate);
			holder.artist = convertView.findViewById(R.id.list_artist);
			holder.artist.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14 * mDpRate);
			holder.info = convertView.findViewById(R.id.list_info);
			holder.info.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14 * mDpRate);
			holder.playing = convertView.findViewById(R.id.list_playing);
			convertView.setTag(holder);
			convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(71 * mDpRate)));
			String getInfo = mListInfo.getList()[position];
			switch (mListInfo.getListType()) {
			case MusicUtils.ListType.ALBUM :
				holder.folder.setVisibility(View.VISIBLE);
				holder.item.setVisibility(View.GONE);
				holder.artist.setText(mListInfo.getArtist()[position]);
				holder.info.setText("     " + mListInfo.getFileCount()[position] + " List");
				holder.artist.measure(0, 0);
				holder.info.measure(0, 0);
				int albumTotalWidth = (int)(364 * mDpRate);
				int albumInfoWidth = holder.info.getMeasuredWidth();
				if (albumTotalWidth < (holder.artist.getMeasuredWidth() + albumInfoWidth)) {
					holder.artist.setWidth(albumTotalWidth - albumInfoWidth);
				}
				break;
			case MusicUtils.ListType.ARTIST :
				holder.folder.setVisibility(View.VISIBLE);
				holder.item.setVisibility(View.GONE);
				holder.artist.setText("");
				holder.info.setText(mListInfo.getFileCount()[position] + " List");
				break;
			case MusicUtils.ListType.FOLDER :
				String folder = getInfo.substring(getInfo.lastIndexOf("/") + 1);
				getInfo = setCharset(folder);
				holder.folder.setVisibility(View.VISIBLE);
				holder.item.setVisibility(View.GONE);
				holder.artist.setText("");
				holder.info.setText(mListInfo.getFileCount()[position] + " List");
				break;
			default :
				holder.folder.setVisibility(View.GONE);
				holder.item.setVisibility(View.VISIBLE);
				holder.albumart.setImageResource(R.drawable.cover_img_s);
				mImageLoader.displayImage(position, holder.albumart);
				if (mListInfo.getArtist() != null)
					holder.artist.setText(mListInfo.getArtist()[position]);
				holder.info.setText("     " + makeTimeFormat(mListInfo.getTotalTime()[position]));
				holder.artist.measure(0, 0);
				holder.info.measure(0, 0);
				int trackTotalWidth = (int)(364 * mDpRate);
				int trackInfoWidth = holder.info.getMeasuredWidth();
				if (trackTotalWidth < (holder.artist.getMeasuredWidth() + trackInfoWidth)) {
					holder.artist.setWidth(trackTotalWidth - trackInfoWidth);
				}
				break;
			}
			holder.title.setText(getInfo);
			if (isPlayingItem(position)) {
				holder.playing.setVisibility(View.VISIBLE);
			} else {
				holder.playing.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

        @Override
        public int getCount() {
            return list.size();
        }
	}

	// ----------
	// AndroidView Widget Listeners
	private Button.OnClickListener mClickListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.list_button :
				if (mScanning) {
					Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.media_scanning), Toast.LENGTH_LONG).show();
				} else {
					mMusicPlayerView.setVisibility(View.GONE);
					mMusicListView.setVisibility(View.VISIBLE);
					mUpstep.setVisibility(View.GONE);
					requestList(MusicUtils.ListType.NOW_PLAYING, null);
				}
				break;
			case R.id.play_pause_button :
			case R.id.list_play_pause_button :
				if ((mPreviewMode) && (mPlayingIndex != mShowIndex)) {
					selectItemToPlay(mShowIndex, true);
				} else {
					switch (mPlayState) {
					case MusicUtils.PlayState.PLAY :
						pause();
						break;
					case MusicUtils.PlayState.PAUSE :
						play();
						break;
					default :
						break;
					}
				}
				break;
			case R.id.shuffle_button :
				setShuffle();
				break;
			case R.id.repeat_button :
				setRepeat();
				break;
			case R.id.scan_button :
				setScan();
				break;
			case R.id.list_previous_button :
				playPrev();
				break;
			case R.id.list_next_button :
				playNext();
				break;
			case R.id.category_all_button :
				if ((null != mListInfo) && (MusicUtils.ListType.ALL != mListInfo.getListType())) {
					requestList(MusicUtils.ListType.ALL, null);
				}
				break;
			case R.id.category_album_button :
				if ((null != mListInfo) && (MusicUtils.ListType.ALBUM != mListInfo.getListType())) {
					requestList(MusicUtils.ListType.ALBUM, null);
				}
				break;
			case R.id.category_artist_button :
				if ((null != mListInfo) && (MusicUtils.ListType.ARTIST != mListInfo.getListType())) {
					requestList(MusicUtils.ListType.ARTIST, null);
				}
				break;
			case R.id.category_folder_button :
				if ((null != mListInfo) && (MusicUtils.ListType.FOLDER != mListInfo.getListType())) {
					requestList(MusicUtils.ListType.FOLDER, null);
				}
				break;
			case R.id.category_recent_button:
//				if ((null != mListInfo) && (MusicUtils.ListType.RECENT != mListInfo.getListType())) {
					Log.d("[jacob] Category: " + getNowPlayingCategory()
							+ ", SubCategory: " + getNowPlayingSubCategory());
					int listType = MusicUtils.ListType.ALL;
					switch (getNowPlayingCategory()) {
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
					default :
						break;
					}
					
					requestList(/*MusicUtils.ListType.RECENT*/listType, getNowPlayingSubCategory());
//				}
				break;
			case R.id.list_upstep :
				if (null != mListInfo) {
					switch (mListInfo.getListType()) {
					case MusicUtils.ListType.NOW_PLAYING :
						switch (mNowPlayingCategory) {
						case MusicUtils.Category.ARTIST :
							requestList(MusicUtils.ListType.ARTIST, null);
							break;
						case MusicUtils.Category.ALBUM :
							requestList(MusicUtils.ListType.ALBUM, null);
							break;
						case MusicUtils.Category.GENRE :
							requestList(MusicUtils.ListType.GENRE, null);
							break;
						case MusicUtils.Category.FOLDER :
							requestList(MusicUtils.ListType.FOLDER, null);
							break;
						default :
							break;
						}
						break;
					case MusicUtils.ListType.ARTIST_TRACK :
						requestList(MusicUtils.ListType.ARTIST, null);
						break;
					case MusicUtils.ListType.ALBUM_TRACK :
						requestList(MusicUtils.ListType.ALBUM, null);
						break;
					case MusicUtils.ListType.GENRE_TRACK :
						requestList(MusicUtils.ListType.GENRE, null);
						break;
					case MusicUtils.ListType.FOLDER_TRACK :
						requestList(MusicUtils.ListType.FOLDER, null);
						break;
					default :
						break;
					}
				}
				break;
			default :
				break;
			}
		}
	};

	private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.previous_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mPreviousPressed = true;
						if (!mNextPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
						}
						break;
					case MotionEvent.ACTION_MOVE :
						if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
							break;
						}
					case MotionEvent.ACTION_UP :
						if (mPreviousPressed) {
							mPreviousPressed = false;
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_REWIND);
							switch (mPlayState) {
							case MusicUtils.PlayState.FAST_FORWARD :
								break;
							case MusicUtils.PlayState.FAST_REWIND :
								stopFastRewind();
								if (mNextPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
								}
								break;
							default :
								if (mNextPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
								} else {
									playPrev();
								}
								break;
							}
						}
						break;
					default :
						break;
					}
				}
				break;
			case R.id.next_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mNextPressed = true;
						if (!mPreviousPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
						}
						break;
					case MotionEvent.ACTION_MOVE :
						if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
							break;
						}
					case MotionEvent.ACTION_UP :
						if (mNextPressed) {
							mNextPressed = false;
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_FORWARD);
							switch (mPlayState) {
							case MusicUtils.PlayState.FAST_FORWARD :
								stopFastForward();
								if (mPreviousPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
								}
								break;
							case MusicUtils.PlayState.FAST_REWIND :
								break;
							default :
								if (mPreviousPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
								} else {
									playNext();
								}
								break;
							}
						}
						break;
					default :
						break;
					}
				}
				break;
			default :
				break;
			}
			return false;
		}
	};

	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			switch (seekBar.getId()) {
			case R.id.time_progress :
				if (MusicUtils.PlayState.PAUSE == getPlayState()) {
					mCurrentTime.setText(makeTimeFormat(progress));
				}
				break;
			default :
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			switch (seekBar.getId()) {
			case R.id.time_progress :
				mTimerHandler.removeMessages(mTimerHandler.MESSAGE_PLAY_STATE_PAUSE);
				mCurrentTime.setVisibility(View.VISIBLE);
				gripTimeProgressBar();
				break;
			default :
				break;
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			switch (seekBar.getId()) {
			case R.id.time_progress :
				setPlayTimeMS(seekBar.getProgress());
				break;
			default :
				break;
			}
		}
	};

	private ListView.OnItemClickListener mItemClickListener = new ListView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch (parent.getId()) {
			case R.id.item_list :
				if (null != mListInfo) {
					switch (mListInfo.getListType()) {
					case MusicUtils.ListType.ALL :
					case MusicUtils.ListType.ARTIST_TRACK :
					case MusicUtils.ListType.ALBUM_TRACK :
					case MusicUtils.ListType.GENRE_TRACK :
					case MusicUtils.ListType.FOLDER_TRACK :
//					case MusicUtils.ListType.RECENT :
					case MusicUtils.ListType.NOW_PLAYING :
						selectItemToPlay(position, MusicUtils.ListType.NOW_PLAYING == mListInfo.getListType());
						break;
					case MusicUtils.ListType.ARTIST :
						requestList(MusicUtils.ListType.ARTIST_TRACK, (mListInfo.getList())[position]);
						break;
					case MusicUtils.ListType.ALBUM :
						requestList(MusicUtils.ListType.ALBUM_TRACK, (mListInfo.getList())[position]);
						break;
					case MusicUtils.ListType.GENRE :
						requestList(MusicUtils.ListType.GENRE_TRACK, (mListInfo.getList())[position]);
						break;
					case MusicUtils.ListType.FOLDER :
						requestList(MusicUtils.ListType.FOLDER_TRACK, (mListInfo.getList())[position]);
						break;
					default :
						break;
					}
				}
				break;
			default :
				break;
			}
		}
	};

	// ----------
	// AndroidView UI Timer Handler
	private TimerHandler mTimerHandler = new TimerHandler();

	private class TimerHandler extends Handler {
		public final int MESSAGE_PLAY_STATE_PAUSE = 2001;
		public final int MESSAGE_START_FAST_FORWARD = 2002;
		public final int MESSAGE_START_FAST_REWIND = 2003;
		public final int MESSAGE_RETURN_TO_PLAYING = 2004;

		public final int INTERVAL_FOR_BLINK_TIME = 1000;
		public final int DELAY_FOR_START_FAST = 1500;
		public final int DELAY_FOR_RETURN_TIME = 5000;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_PLAY_STATE_PAUSE :
				switch (mCurrentTime.getVisibility()) {
				case View.VISIBLE :
					mCurrentTime.setVisibility(View.INVISIBLE);
					break;
				case View.INVISIBLE :
					mCurrentTime.setVisibility(View.VISIBLE);
					break;
				default :
					break;
				}
				sendEmptyMessageDelayed(MESSAGE_PLAY_STATE_PAUSE, INTERVAL_FOR_BLINK_TIME);
				break;
			case MESSAGE_START_FAST_FORWARD :
				if (MusicUtils.PlayState.FAST_FORWARD != mPlayState) {
					startFastForward();
				}
				break;
			case MESSAGE_START_FAST_REWIND :
				if (MusicUtils.PlayState.FAST_REWIND != mPlayState) {
					startFastRewind();
				}
				break;
			case MESSAGE_RETURN_TO_PLAYING :
				mPreviewMode = false;
				mShowIndex = mPlayingIndex;
				showMusicInfo(mPlayingMusicInfo);
				mCurrentIndex.setText(Integer.valueOf(mPlayingIndex + 1).toString());
				break;
			default :
				break;
			}
			super.handleMessage(msg);
		}
	}

	// ----------
	// ImageLoader
	private ImageLoader mImageLoader = new ImageLoader();

	private class ImageLoader {
		private HashMap<ImageView, Integer> imageViews = new HashMap<>();
		private ExecutorService executorService;
		private Future<?> runningTaskFuture = null;
		private Handler handler = new Handler();

		public void create() {
			destroy();
			executorService = Executors.newSingleThreadExecutor();
		}

		public void destroy() {
			if ((null != runningTaskFuture) && (!runningTaskFuture.isDone())) {
				runningTaskFuture.cancel(true);
			}
		}

		public void displayImage(int index, ImageView imageView) {
			imageViews.put(imageView, index);
			Bitmap bitmap = mActivity.fromAlbumArtBuffer(index);
			if (null != bitmap) {
				imageView.setImageBitmap(bitmap);
			} else {
				queuePhoto(index, imageView);
			}
		}

		private void queuePhoto(int index, ImageView imageView) {
			PhotoToLoad photoToLoad = new PhotoToLoad(index, imageView);
			runningTaskFuture = executorService.submit(new PhotosLoader(photoToLoad));
		}

		private boolean imageViewReused(PhotoToLoad photoToLoad) {
			int index = imageViews.get(photoToLoad.imageView); 
			if ((0 > index) || (index != photoToLoad.index)) {
				return true;
			}
			return false;
		}

		private class PhotoToLoad {
			public int index;
			public ImageView imageView;

			public PhotoToLoad(int index, ImageView imageView) {
				this.index = index;
				this.imageView = imageView;
			}
		}

		private class PhotosLoader implements Runnable {
			PhotoToLoad photoToLoad;

			public PhotosLoader(PhotoToLoad photoToLoad) {
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				try {
					if ((!imageViewReused(photoToLoad)) && (null != mListInfo)) {
						Bitmap bitmap = getAlbumArt(photoToLoad.index, MusicUtils.ListType.NOW_PLAYING == mListInfo.getListType(), true);
						mActivity.toAlbumArtBuffer(photoToLoad.index, bitmap);
						if (imageViewReused(photoToLoad)) {
							return;
						}
						BitmapDisplayer bd = new BitmapDisplayer(bitmap, photoToLoad);
						handler.post(bd);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		private class BitmapDisplayer implements Runnable {
			Bitmap bitmap;
			PhotoToLoad photoToLoad;

			public BitmapDisplayer(Bitmap bitmap, PhotoToLoad photoToLoad) {
				this.bitmap = bitmap;
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				if ((!imageViewReused(photoToLoad)) && (null != bitmap)) {
					photoToLoad.imageView.setImageBitmap(bitmap);
				}
			} 
		}
	}
}
