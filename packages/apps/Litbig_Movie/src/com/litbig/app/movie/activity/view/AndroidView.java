package com.litbig.app.movie.activity.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.litbig.app.movie.R;
import com.litbig.app.movie.activity.MovieActivity;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;
import com.litbig.app.movie.util.Log;
import com.litbig.app.movie.util.MovieUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OutlineTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidView extends MovieView {
	// ----------
	// AndroidView behavior
	private float mDpRate;
	private int mDpWidth;
	private int mDpHeight;
	private LayoutInflater mLayoutInflater;

	public AndroidView(MovieActivity activity) {
		super(activity);
		mDpRate = getDisplayRate();
		Log.d("mDpRate: " + mDpRate);
		mItemView = mActivity.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE).getInt("item_view", ItemView.LIST);
		mScreenScaleType = mActivity.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE).getInt("scale_type", MovieUtils.ScreenScaleType.FIT_XY);
	}

	@Override
	public void onCreate() {
		mLayoutInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initPlayerView();
		initListView();
		mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	@Override
	public void onResume() {
		mMoviePlayerView.setVisibility(View.VISIBLE);
		mMovieListView.setVisibility(View.GONE);
		mCurrentIndex.setText("0");
		mTotalCount.setText(" of " + "0");
		mTitle.setText("");
		showInfoAndController(true);
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
		if (View.VISIBLE == mMovieListView.getVisibility()) {
			backToPlayerView();
		} else {
			mActivity.finish();
		}
	}

	@Override
	public void onServiceConnected() {
		setSurface(mVideoSurfaceView.getHolder().getSurface());
		setItemView();
		restartHideTimer();
	}

	@Override
	public void onServiceDisconnected() {
	}

	@Override
	public void onMediaScan(boolean scanning) {
		if (scanning) {
			if (View.VISIBLE == mMovieListView.getVisibility()) {
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
		mDpWidth = metrics.widthPixels;
		mDpHeight = metrics.heightPixels;
		if (((float)mDpWidth / DEFAULT_WIDTH) <= ((float)mDpHeight / DEFAULT_HEIGHT)) {
			rate = (float)mDpWidth / DEFAULT_WIDTH;
		} else {
			rate = (float)mDpHeight / DEFAULT_HEIGHT;
		}
		return rate;
	}

	// ----------
	// MovieServiceCallback APIs
	@Override
	public void onTotalCount(int totalCount) {
		if (0 < totalCount) {
			mTotalCount.setText(" of " + Integer.valueOf(totalCount).toString());
		}
		if (1 >= totalCount) {
			mShuffleButton.setEnabled(false);
			mRepeatButton.setEnabled(false);
			mShuffleButton.setAlpha(0.5f);
			mRepeatButton.setAlpha(0.5f);
		} else {
			mShuffleButton.setEnabled(true);
			mRepeatButton.setEnabled(true);
			mShuffleButton.setAlpha(1.0f);
			mRepeatButton.setAlpha(1.0f);
		}
	}

	@Override
	public void onPlayState(int playState) {
		mPlayState = playState;
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_PLAY_STATE_PAUSE);
		switch (playState) {
		case MovieUtils.PlayState.PLAY :
			mCurrentTime.setVisibility(View.VISIBLE);
			mPlayPause.setImageResource(R.drawable.btn_pause_click);
			break;
		case MovieUtils.PlayState.PAUSE :
			mPlayPause.setImageResource(R.drawable.btn_play_click);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_PLAY_STATE_PAUSE, mTimerHandler.INTERVAL_FOR_BLINK_TIME);
			break;
		case MovieUtils.PlayState.FAST_FORWARD :
		case MovieUtils.PlayState.FAST_REWIND :
			mCurrentTime.setVisibility(View.VISIBLE);
			mPlayPause.setImageResource(R.drawable.btn_play_click);
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
	public void onMovieInfo(int index, MovieInfo info) {
		Log.d("index = " + index + ", title = " + info.getTitle());
		mPlayingIndex = getPlayingIndex();
		mShowIndex = index;
		if (mPlayingIndex != index) {
			Log.d("index = " + index + ", currentIndex = " + mPlayingIndex);
			mTimerHandler.removeMessages(mTimerHandler.MESSAGE_RETURN_TO_PLAYING);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_RETURN_TO_PLAYING, mTimerHandler.DELAY_FOR_RETURN_TIME);
			showMovieInfo(info);
		} else {
			scaleSurfaceView(info.getVideoWidth(), info.getVideoHeight());
			mPlayingMovieInfo = info;
			mPlayingDuration = info.getTotalTimeMS();
			mTimeProgress.setMax(mPlayingDuration);
			showMovieInfo(info);
			mTotalTime.setText(makeTimeFormat(mPlayingDuration));
			if (View.VISIBLE == mMovieListView.getVisibility()) {
				if (isNowPlayingCategory()) {
					mListAdapter.notifyDataSetChanged();
					mGridAdapter.notifyDataSetChanged();
				}
			}
		}
		mCurrentIndex.setText(Integer.valueOf(index + 1).toString());
	}

	@Override
	public void onVideoThumbnail(int index, Bitmap videoThumbnail) {
		if (null == videoThumbnail) {
			Log.w("index = " + index + ", videoThumbnail null");
		} else {
			Log.d("index = " + index + ", videoThumbnail OK");
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
		switch (shuffle) {
		case MovieUtils.ShuffleState.OFF :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_off_click);
			break;
		case MovieUtils.ShuffleState.ALL :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_on_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onRepeatState(int repeat) {
		switch (repeat) {
		case MovieUtils.RepeatState.ALL :
			mRepeatButton.setImageResource(R.drawable.btn_repeat_all_click);
			break;
		case MovieUtils.RepeatState.ONE :
			mRepeatButton.setImageResource(R.drawable.btn_repeat_1_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onSubtitle(String text) {
		mSubtitle.setText(text);
	}

	@Override
	public void onListInfo(ListInfo info) {
		if (null != info) {
			if ((null == info.getList()) || (0 == info.getList().length)) {
				switch (info.getListType()) {
				case MovieUtils.ListType.NOW_PLAYING :
					requestList(MovieUtils.ListType.ALL, null);
					break;
				case MovieUtils.ListType.FOLDER :
					requestList(MovieUtils.ListType.NOW_PLAYING, null);
					break;
				case MovieUtils.ListType.FOLDER_TRACK :
					requestList(MovieUtils.ListType.FOLDER, null);
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
				mGridAdapter = new GridItemAdapter(mActivity);
				mItemGrid.setAdapter(mGridAdapter);
				switch (info.getListType()) {
				case MovieUtils.ListType.NOW_PLAYING :
					setNowPlaying();
					mItemList.setSelection(mPlayingIndex);
					mItemGrid.setSelection(mPlayingIndex);
					break;
				case MovieUtils.ListType.ALL :
					selectCategoryButton(MovieUtils.Category.ALL);
					mUpstep.setVisibility(View.GONE);
					break;
				case MovieUtils.ListType.FOLDER :
					selectCategoryButton(MovieUtils.Category.FOLDER);
					mUpstep.setVisibility(View.GONE);
					break;
				case MovieUtils.ListType.FOLDER_TRACK :
					String subCategory = info.getSubCategory();
					if (null == subCategory) {
						requestList(MovieUtils.ListType.FOLDER, null);
					} else {
						String folder = subCategory.substring(subCategory.lastIndexOf("/") + 1);
						folder = setCharset(folder);
						selectCategoryButton(MovieUtils.Category.FOLDER);
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
	// Movie Player View
	private View mMoviePlayerView;
	private SurfaceView mVideoSurfaceView;
	private FrameLayout mTouchScreen;
	private OutlineTextView mSubtitle;
	private FrameLayout mInfoAndController;
	private TextView mCurrentIndex;
	private TextView mTotalCount;
	private TextView mTitle;
	private ImageButton mListButton;
	private ImageButton mPrevious;
	private ImageButton mPlayPause;
	private ImageButton mNext;
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private SeekBar mTimeProgress;
	private Animation mAppearAnimation;
	private Animation mDisappearAnimation;

	private int mPlayingIndex = 0;
	private int mShowIndex = 0;
	private boolean mPreviewMode = false;
	private MovieInfo mPlayingMovieInfo = null;
	private int mScreenScaleType = MovieUtils.ScreenScaleType.FIT_XY;
	private int mPlayingDuration = 0;
	private int mPlayState = MovieUtils.PlayState.STOP;
	private boolean mScanning = false;
	private boolean mListPressed = false;
	private boolean mPreviousPressed = false;
	private boolean mPlayPressed = false;
	private boolean mNextPressed = false;
	private boolean mShufflePressed = false;
	private boolean mRepeatPressed = false;
	private boolean mStopHideTimer = false;
	private boolean mAnimationRunning = false;

	private void initPlayerView() {
		mMoviePlayerView = mLayoutInflater.inflate(R.layout.movie_player, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mMoviePlayerView);
		mVideoSurfaceView = mMoviePlayerView.findViewById(R.id.video_surface);
		mVideoSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
		mTouchScreen = mMoviePlayerView.findViewById(R.id.touch_screen);
		mTouchScreen.setOnTouchListener(mTouchListener);
		mSubtitle = mMoviePlayerView.findViewById(R.id.subtitle_text);
		mSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40.0f * mDpRate);
		mSubtitle.setTextColor(Color.WHITE);
		mSubtitle.setOutlineColor(Color.BLACK);
		mInfoAndController = mMoviePlayerView.findViewById(R.id.info_and_controller);
		mCurrentIndex = mMoviePlayerView.findViewById(R.id.current_index);
		mCurrentIndex.setText("0");
		mCurrentIndex.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTotalCount = mMoviePlayerView.findViewById(R.id.total_count);
		mTotalCount.setText(" of " + "0");
		mTotalCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTitle = mMoviePlayerView.findViewById(R.id.title_text);
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mListButton = mMoviePlayerView.findViewById(R.id.list_button);
		mListButton.setOnClickListener(mClickListener);
		mListButton.setOnTouchListener(mTouchListener);
		mPrevious = mMoviePlayerView.findViewById(R.id.previous_button);
		mPrevious.setOnClickListener(mClickListener);
		mPrevious.setOnTouchListener(mTouchListener);
		mPlayPause = mMoviePlayerView.findViewById(R.id.play_pause_button);
		mPlayPause.setOnClickListener(mClickListener);
		mPlayPause.setOnTouchListener(mTouchListener);
		mPlayPause.setImageResource(R.drawable.btn_play_click);
		mNext = mMoviePlayerView.findViewById(R.id.next_button);
		mNext.setOnClickListener(mClickListener);
		mNext.setOnTouchListener(mTouchListener);
		mShuffleButton = mMoviePlayerView.findViewById(R.id.shuffle_button);
		mShuffleButton.setOnClickListener(mClickListener);
		mShuffleButton.setOnTouchListener(mTouchListener);
		mRepeatButton = mMoviePlayerView.findViewById(R.id.repeat_button);
		mRepeatButton.setOnClickListener(mClickListener);
		mRepeatButton.setOnTouchListener(mTouchListener);
		mCurrentTime = mMoviePlayerView.findViewById(R.id.current_time);
		mCurrentTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTimeProgress = mMoviePlayerView.findViewById(R.id.time_progress);
		mTimeProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mTotalTime = mMoviePlayerView.findViewById(R.id.total_time);
		mTotalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mAppearAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.appear);
		mAppearAnimation.setAnimationListener(mAnimationListener);
		mDisappearAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.disappear);
		mDisappearAnimation.setAnimationListener(mAnimationListener);
	}

	private void showMovieInfo(MovieInfo info) {
		if (null != info) {
			mTitle.setText(info.getTitle());
			if (mPlayingIndex == mShowIndex) {
				int viewId = R.drawable.btn_play_click;
				switch (getPlayState()) {
				case MovieUtils.PlayState.PLAY :
					viewId = R.drawable.btn_pause_click;
					break;
				case MovieUtils.PlayState.PAUSE :
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
			} else if (mPreviewMode) {
				mPrevious.setEnabled(false);
				mPlayPause.setImageResource(R.drawable.btn_play_click);
				mNext.setEnabled(false);
				mTimeProgress.setEnabled(false);
				mCurrentIndex.setTextColor(Color.BLUE);
				mTitle.setTextColor(Color.BLUE);
			}
		}
	}

	private void showInfoAndController(boolean show) {
		FrameLayout.LayoutParams margin = (FrameLayout.LayoutParams)mSubtitle.getLayoutParams();
		if (show) {
			margin.setMargins(0, 0, 0, (int)(170 * mDpRate));
			mSubtitle.setLayoutParams(margin);
			mInfoAndController.setVisibility(View.VISIBLE);
			mInfoAndController.startAnimation(mAppearAnimation);
			mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			restartHideTimer();
		} else {
			margin.setMargins(0, 0, 0, (int)(10 * mDpRate));
			mSubtitle.setLayoutParams(margin);
			mInfoAndController.startAnimation(mDisappearAnimation);
			mInfoAndController.setVisibility(View.GONE);
			if (View.GONE == mMovieListView.getVisibility()) {
				mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
			}
		}
	}

	private void scaleSurfaceView(int videoWidth, int videoHeight) {
		FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams)mVideoSurfaceView.getLayoutParams();
		switch (mScreenScaleType) {
		case MovieUtils.ScreenScaleType.CENTER_INSIDE :
			if ((videoWidth <= mDpWidth) && (videoHeight <= mDpHeight)) {
				layout.width = videoWidth;
				layout.height = videoHeight;
				layout.gravity = Gravity.CENTER;
			} else if ((videoWidth * mDpHeight) > (videoHeight * mDpWidth)) {
				layout.width = FrameLayout.LayoutParams.MATCH_PARENT;
				layout.height = videoHeight * mDpWidth / videoWidth;
				layout.gravity = Gravity.CENTER_VERTICAL;
			} else if ((videoWidth * mDpHeight) < (videoHeight * mDpWidth)) {
				layout.width = videoWidth * mDpHeight / videoHeight;
				layout.height = FrameLayout.LayoutParams.MATCH_PARENT;
				layout.gravity = Gravity.CENTER_HORIZONTAL;
			} else {
				layout.width = FrameLayout.LayoutParams.MATCH_PARENT;
				layout.height = FrameLayout.LayoutParams.MATCH_PARENT;
				layout.gravity = Gravity.FILL;
			}
			break;
		case MovieUtils.ScreenScaleType.FIT_XY :
			layout.width = FrameLayout.LayoutParams.MATCH_PARENT;
			layout.height = FrameLayout.LayoutParams.MATCH_PARENT;
			layout.gravity = Gravity.FILL;
			break;
		default :
			break;
		}
		mVideoSurfaceView.setLayoutParams(layout);
	}

	private void setScaleType() {
		switch (mScreenScaleType) {
		case MovieUtils.ScreenScaleType.CENTER_INSIDE :
			mScreenScaleType = MovieUtils.ScreenScaleType.FIT_XY;
			break;
		case MovieUtils.ScreenScaleType.FIT_XY :
			mScreenScaleType = MovieUtils.ScreenScaleType.CENTER_INSIDE;
			break;
		default :
			break;
		}
		scaleSurfaceView(mPlayingMovieInfo.getVideoWidth(), mPlayingMovieInfo.getVideoHeight());
		SharedPreferences.Editor prefEdit = mActivity.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE).edit();
		prefEdit.putInt("scale_type", mScreenScaleType);
		prefEdit.apply();
	}

	private void restartHideTimer() {
		if (!mStopHideTimer) {
			mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL, mTimerHandler.DELAY_FOR_HIDE_PLAYER_VIEW);
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

	private boolean isPressed() {
		return (mListPressed || mPreviousPressed || mPlayPressed || mNextPressed || mShufflePressed || mRepeatPressed);
	}

	// ----------
	// SurfaceHolder Callback
	private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d("surfaceChanged : width = " + w + ", height = " + h);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("surfaceCreated");
			setSurface(holder.getSurface());
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d("surfaceDestroyed");
			setSurface(null);
		}
	};

	// ----------
	// Movie List View
	private View mMovieListView;
	private ImageButton mCategoryAllButton;
	private ImageButton mCategoryFolderButton;
	private ImageButton mGridChangeView;
	private ImageButton mListChangeView;
	private LinearLayout mUpstep;
	private TextView mSubCategoryName;
	private KoreanIndexerListView mItemList;
	private GridView mItemGrid;

	private ListItemAdapter mListAdapter;
	private GridItemAdapter mGridAdapter;
	private ListInfo mListInfo = null;
	private int mItemView = ItemView.LIST;
	private int mNowPlayingCategory = MovieUtils.Category.ALL;

	private void initListView() {
		mMovieListView = mLayoutInflater.inflate(R.layout.movie_list, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mMovieListView);
		mCategoryAllButton = mMovieListView.findViewById(R.id.category_all_button);
		mCategoryAllButton.setOnClickListener(mClickListener);
		mCategoryFolderButton = mMovieListView.findViewById(R.id.category_folder_button);
		mCategoryFolderButton.setOnClickListener(mClickListener);
		mGridChangeView = mMovieListView.findViewById(R.id.grid_change_view);
		mGridChangeView.setOnClickListener(mClickListener);
		mListChangeView = mMovieListView.findViewById(R.id.list_change_view);
		mListChangeView.setOnClickListener(mClickListener);
		mUpstep = mMovieListView.findViewById(R.id.list_upstep);
		mUpstep.setOnClickListener(mClickListener);
		mSubCategoryName = mMovieListView.findViewById(R.id.sub_category);
		mSubCategoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14.0f * mDpRate);
		mItemList = mMovieListView.findViewById(R.id.item_list);
		mItemList.setOnItemClickListener(mItemClickListener);
		mItemGrid = mMovieListView.findViewById(R.id.item_grid);
		mItemGrid.setOnItemClickListener(mItemClickListener);
		mItemGrid.setPadding((int)(60.0f * mDpRate), 0, (int)(34.0f * mDpRate), (int)(20.0f * mDpRate));
	}

	private void selectCategoryButton(int category) {
		switch (category) {
		case MovieUtils.Category.ALL :
			mCategoryAllButton.setSelected(true);
			mCategoryFolderButton.setSelected(false);
			break;
		case MovieUtils.Category.FOLDER :
			mCategoryAllButton.setSelected(false);
			mCategoryFolderButton.setSelected(true);
			break;
		default :
			break;
		}
	}

	private void setItemView() {
		if (ItemView.LIST == mItemView) {
			mGridChangeView.setSelected(false);
			mListChangeView.setSelected(true);
			mItemList.setVisibility(View.VISIBLE);
			mItemGrid.setVisibility(View.GONE);
		} else {
			mGridChangeView.setSelected(true);
			mListChangeView.setSelected(false);
			mItemList.setVisibility(View.GONE);
			mItemGrid.setVisibility(View.VISIBLE);
		}
	}

	private void setNowPlaying() {
		mNowPlayingCategory = getNowPlayingCategory();
		selectCategoryButton(mNowPlayingCategory);
		switch (mNowPlayingCategory) {
		case MovieUtils.ListType.ALL :
			mUpstep.setVisibility(View.GONE);
			break;
		case MovieUtils.ListType.FOLDER :
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
			if (View.VISIBLE == mMovieListView.getVisibility()) {
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
			case MovieUtils.ListType.NOW_PLAYING :
				if (mPlayingIndex == position) {
					isPlaying = true;
				}
				break;
			case MovieUtils.ListType.ALL :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MovieUtils.Category.ALL, null))) {
					isPlaying = true;
				}
				break;
			case MovieUtils.ListType.FOLDER :
				if (isNowPlayingCategory(MovieUtils.Category.FOLDER, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
			case MovieUtils.ListType.FOLDER_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(MovieUtils.Category.FOLDER, mListInfo.getSubCategory()))) {
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
			case MovieUtils.ListType.NOW_PLAYING :
				isPlaying = true;
				break;
			case MovieUtils.ListType.ALL :
				isPlaying = isNowPlayingCategory(MovieUtils.Category.ALL, null);
				break;
			case MovieUtils.ListType.FOLDER_TRACK :
				isPlaying = isNowPlayingCategory(MovieUtils.Category.FOLDER, mListInfo.getSubCategory());
				break;
			default :
				break;
			}
		}
		return isPlaying;
	}

	private void backToPlayerView() {
		mImageLoader.destroy();
		try {
			mActivity.getService().showList(false);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		mMoviePlayerView.setVisibility(View.VISIBLE);
		mMovieListView.setVisibility(View.GONE);
		mItemList.setAdapter(null);
		mItemGrid.setAdapter(null);
		mListInfo = null;
		showInfoAndController(true);
	}

	// ----------
	// ItemView
	private class ItemView {
		public static final int LIST = 0;
		public static final int GRID = 1;
	}

	// ----------
	// ViewHolder
	private class ListViewHolder {
		public LinearLayout folder;
		public LinearLayout item;
		public ImageView thumbnail;
		public TextView title;
		public TextView info;
		public ImageView playing;
	}

	private class GridViewHolder {
		public ImageView thumbnail;
		public LinearLayout folder;
		public ImageView folderIcon;
		public TextView title;
	}

	// ----------
	// ListAdapter
	private class ListItemAdapter extends KoreanIndexerListView.KoreanIndexerAdapter<String> {
		private ArrayList<String> list;
		public ListItemAdapter(Context context) {
			super(context, new ArrayList<>(Arrays.asList(mListInfo.getList())));
			list = new ArrayList<>(Arrays.asList(mListInfo.getList()));
			mActivity.clearVideoThumbnailBuffer();
			mImageLoader.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListViewHolder holder = new ListViewHolder();
			convertView = ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.icon_list_item, parent, false);
			holder.folder = convertView.findViewById(R.id.list_folder);
			holder.item = convertView.findViewById(R.id.list_item);
			holder.thumbnail = convertView.findViewById(R.id.list_thumbnail);
			holder.title = convertView.findViewById(R.id.list_title);
			holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18 * mDpRate);
			holder.info = convertView.findViewById(R.id.list_info);
			holder.info.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14 * mDpRate);
			holder.playing = convertView.findViewById(R.id.list_playing);
			convertView.setTag(holder);
			convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(71 * mDpRate)));
			String getInfo = mListInfo.getList()[position];
			switch (mListInfo.getListType()) {
			case MovieUtils.ListType.FOLDER :
				String folder = getInfo.substring(getInfo.lastIndexOf("/") + 1);
				getInfo = setCharset(folder);
				holder.folder.setVisibility(View.VISIBLE);
				holder.item.setVisibility(View.GONE);
				holder.info.setText(mListInfo.getFileCount()[position] + " List");
				break;
			default :
				holder.folder.setVisibility(View.GONE);
				holder.item.setVisibility(View.VISIBLE);
				holder.thumbnail.setImageResource(R.drawable.img_thumb);
				mImageLoader.displayImage(position, holder.thumbnail);
				holder.info.setText(makeTimeFormat(mListInfo.getTotalTime()[position]));
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
	// GridAdapter
	private class GridItemAdapter extends ArrayAdapter<String> {
		public GridItemAdapter(Context context) {
			super(context, R.layout.icon_grid_item, mListInfo.getList());
			mActivity.clearVideoThumbnailBuffer();
			mImageLoader.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GridViewHolder holder = new GridViewHolder();
			convertView = ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.icon_grid_item, parent, false);
			holder.thumbnail = convertView.findViewById(R.id.grid_thumbnail);
			holder.folder = convertView.findViewById(R.id.grid_folder_layout);
			holder.folderIcon = convertView.findViewById(R.id.grid_folder_icon);
			holder.title = convertView.findViewById(R.id.grid_title);
			holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12 * mDpRate);
			boolean isPlaying = isPlayingItem(position);
			convertView.setTag(holder);
			convertView.setLayoutParams(new LayoutParams((int)(186 * mDpRate), (int)(140 * mDpRate)));
			String getInfo = mListInfo.getList()[position];
			switch (mListInfo.getListType()) {
			case MovieUtils.ListType.FOLDER :
				String folder = getInfo.substring(getInfo.lastIndexOf("/") + 1);
				getInfo = setCharset(folder);
				//holder.thumbnail.setImageResource(R.drawable.img_thumb);
				//holder.folder.setVisibility(View.VISIBLE);
				//holder.title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				holder.title.setGravity(Gravity.CENTER);
				holder.folder.setVisibility(View.GONE);
				if (isPlaying) {
					//holder.folderIcon.setImageResource(R.drawable.ic_folder_video);
					holder.thumbnail.setImageResource(R.drawable.ic_folder_click);
					holder.title.setTextColor(Color.parseColor("#E81867"));
				} else {
					//holder.folderIcon.setImageResource(R.drawable.ic_folder_basic);
					//holder.title.setTextColor(Color.parseColor("#FFD07F"));
					holder.thumbnail.setImageResource(R.drawable.ic_folder_click);
					holder.title.setTextColor(Color.WHITE);
				}
				break;
			default :
				holder.thumbnail.setImageResource(R.drawable.img_thumb);
				mImageLoader.displayImage(position, holder.thumbnail);
				holder.title.setGravity(Gravity.CENTER);
				holder.folder.setVisibility(View.GONE);
				if (isPlaying) {
					holder.title.setTextColor(Color.parseColor("#E81867"));
				} else {
					holder.title.setTextColor(Color.WHITE);
				}
				break;
			}
			holder.title.setText(getInfo);
			return convertView;
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
					if (MovieUtils.PlayState.PLAY == mPlayState) {
						try {
							mActivity.getService().showList(true);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					mMoviePlayerView.setVisibility(View.GONE);
					mMovieListView.setVisibility(View.VISIBLE);
					mUpstep.setVisibility(View.GONE);
					requestList(MovieUtils.ListType.NOW_PLAYING, null);
				}
				break;
			case R.id.play_pause_button :
				if ((mPreviewMode) && (mPlayingIndex != mShowIndex)) {
					selectItemToPlay(mShowIndex, true);
				} else {
					switch (mPlayState) {
					case MovieUtils.PlayState.PLAY :
						pause();
						break;
					case MovieUtils.PlayState.PAUSE :
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
			case R.id.category_all_button :
				if ((null != mListInfo) && (MovieUtils.ListType.ALL != mListInfo.getListType())) {
					requestList(MovieUtils.ListType.ALL, null);
				}
				break;
			case R.id.category_folder_button :
				if ((null != mListInfo) && (MovieUtils.ListType.FOLDER != mListInfo.getListType())) {
					requestList(MovieUtils.ListType.FOLDER, null);
				}
				break;
			case R.id.grid_change_view :
				mItemView = ItemView.GRID;
				setItemView();
				SharedPreferences.Editor prefEdit = mActivity.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE).edit();
				prefEdit.putInt("item_view", mItemView);
				prefEdit.apply();
				break;
			case R.id.list_change_view :
				mItemView = ItemView.LIST;
				setItemView();
				SharedPreferences.Editor prefEdit1 = mActivity.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE).edit();
				prefEdit1.putInt("item_view", mItemView);
				prefEdit1.apply();
				break;
			case R.id.list_upstep :
				if (null != mListInfo) {
					switch (mListInfo.getListType()) {
					case MovieUtils.ListType.NOW_PLAYING :
						if (mNowPlayingCategory == MovieUtils.Category.FOLDER) {
							requestList(MovieUtils.ListType.FOLDER, null);
						}
						break;
					case MovieUtils.ListType.FOLDER_TRACK :
						requestList(MovieUtils.ListType.FOLDER, null);
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
			case R.id.touch_screen :
				if ((View.VISIBLE == mMoviePlayerView.getVisibility()) && (!mAnimationRunning)) {
					if (MotionEvent.ACTION_UP == event.getAction()) {
						if (View.VISIBLE == mInfoAndController.getVisibility()) {
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
							if (MovieUtils.PlayState.PLAY == getPlayState()) {
								showInfoAndController(false);
							}
						} else {
							showInfoAndController(true);
						}
					}
					return true;
				}
				break;
			case R.id.list_button :
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					mListPressed = true;
					mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
					mStopHideTimer = true;
					break;
				case MotionEvent.ACTION_MOVE :
					if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
						break;
					}
				case MotionEvent.ACTION_UP :
					if (mListPressed) {
						mListPressed = false;
						if (!isPressed()) {
							mStopHideTimer = false;
							restartHideTimer();
						}
					}
					break;
				default :
					break;
				}
				break;
			case R.id.previous_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mPreviousPressed = true;
						mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
						if (!mNextPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
						}
						mStopHideTimer = true;
						break;
					case MotionEvent.ACTION_MOVE :
						if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
							break;
						}
					case MotionEvent.ACTION_UP :
						if (mPreviousPressed) {
							mPreviousPressed = false;
							if (!isPressed()) {
								mStopHideTimer = false;
								restartHideTimer();
							}
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_REWIND);
							switch (mPlayState) {
							case MovieUtils.PlayState.FAST_FORWARD :
								break;
							case MovieUtils.PlayState.FAST_REWIND :
								stopFastRewind();
								if (mNextPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
								}
								break;
							default :
								if (mNextPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
								} else if (MotionEvent.ACTION_UP == event.getAction()) {
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
			case R.id.play_pause_button :
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					mPlayPressed = true;
					mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
					mStopHideTimer = true;
					break;
				case MotionEvent.ACTION_MOVE :
					if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
						break;
					}
				case MotionEvent.ACTION_UP :
					if (mPlayPressed) {
						mPlayPressed = false;
						if (!isPressed()) {
							mStopHideTimer = false;
							restartHideTimer();
						}
					}
					break;
				default :
					break;
				}
				break;
			case R.id.next_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mNextPressed = true;
						mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
						if (!mPreviousPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
						}
						mStopHideTimer = true;
						break;
					case MotionEvent.ACTION_MOVE :
						if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
							break;
						}
					case MotionEvent.ACTION_UP :
						if (mNextPressed) {
							mNextPressed = false;
							if (!isPressed()) {
								mStopHideTimer = false;
								restartHideTimer();
							}
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_FORWARD);
							switch (mPlayState) {
							case MovieUtils.PlayState.FAST_FORWARD :
								stopFastForward();
								if (mPreviousPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
								}
								break;
							case MovieUtils.PlayState.FAST_REWIND :
								break;
							default :
								if (mPreviousPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
								} else if (MotionEvent.ACTION_UP == event.getAction()) {
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
			case R.id.shuffle_button :
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					mShufflePressed = true;
					mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
					mStopHideTimer = true;
					break;
				case MotionEvent.ACTION_MOVE :
					if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
						break;
					}
				case MotionEvent.ACTION_UP :
					if (mShufflePressed) {
						mShufflePressed = false;
						if (!isPressed()) {
							mStopHideTimer = false;
							restartHideTimer();
						}
					}
					break;
				default :
					break;
				}
				break;
			case R.id.repeat_button :
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					mRepeatPressed = true;
					mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
					mStopHideTimer = true;
					break;
				case MotionEvent.ACTION_MOVE :
					if ((0 < event.getX()) && (event.getX() < v.getWidth()) && (0 < event.getY()) && (event.getY() < v.getHeight())) {
						break;
					}
				case MotionEvent.ACTION_UP :
					if (mRepeatPressed) {
						mRepeatPressed = false;
						if (!isPressed()) {
							mStopHideTimer = false;
							restartHideTimer();
						}
					}
					break;
				default :
					break;
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
				if (MovieUtils.PlayState.PAUSE == getPlayState()) {
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
				mTimerHandler.removeMessages(mTimerHandler.MESSAGE_HIDE_INFO_AND_CONTROL);
				mStopHideTimer = true;
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
				mStopHideTimer = false;
				restartHideTimer();
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
			case R.id.item_grid :
				if (null != mListInfo) {
					switch (mListInfo.getListType()) {
					case MovieUtils.ListType.ALL :
					case MovieUtils.ListType.FOLDER_TRACK :
					case MovieUtils.ListType.NOW_PLAYING :
						selectItemToPlay(position, MovieUtils.ListType.NOW_PLAYING == mListInfo.getListType());
						break;
					case MovieUtils.ListType.FOLDER :
						requestList(MovieUtils.ListType.FOLDER_TRACK, (mListInfo.getList())[position]);
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

	private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(Animation v) {
			mAnimationRunning = true;
			mListButton.setEnabled(false);
		}

		@Override
		public void onAnimationRepeat(Animation v) {
		}

		@Override
		public void onAnimationEnd(Animation v) {
			mListButton.setEnabled(true);
			mAnimationRunning = false;
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
		public final int MESSAGE_HIDE_INFO_AND_CONTROL = 2005;

		public final int INTERVAL_FOR_BLINK_TIME = 1000;
		public final int DELAY_FOR_START_FAST = 1500;
		public final int DELAY_FOR_RETURN_TIME = 5000;
		public final int DELAY_FOR_HIDE_PLAYER_VIEW = 5000;

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
				if (MovieUtils.PlayState.FAST_FORWARD != mPlayState) {
					startFastForward();
				}
				break;
			case MESSAGE_START_FAST_REWIND :
				if (MovieUtils.PlayState.FAST_REWIND != mPlayState) {
					startFastRewind();
				}
				break;
			case MESSAGE_RETURN_TO_PLAYING :
				mPreviewMode = false;
				mShowIndex = mPlayingIndex;
				showMovieInfo(mPlayingMovieInfo);
				mCurrentIndex.setText(Integer.valueOf(mPlayingIndex + 1).toString());
				break;
			case MESSAGE_HIDE_INFO_AND_CONTROL :
				if (MovieUtils.PlayState.PLAY == getPlayState()) {
					showInfoAndController(false);
				}
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
			Bitmap bitmap = mActivity.fromVideoThumbnailBuffer(index);
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
						Bitmap bitmap = getVideoThumbnail(photoToLoad.index, MovieUtils.ListType.NOW_PLAYING == mListInfo.getListType(), true);
						mActivity.toVideoThumbnailBuffer(photoToLoad.index, bitmap);
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
