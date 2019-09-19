package com.litbig.app.photo.activity.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.litbig.app.photo.R;
import com.litbig.app.photo.activity.PhotoActivity;
import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;
import com.litbig.app.photo.util.Log;
import com.litbig.app.photo.util.PhotoUtils;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AndroidView extends PhotoView {
	// ----------
	// AndroidView behavior
	private float mDpRate;
	private LayoutInflater mLayoutInflater;

	public AndroidView(PhotoActivity activity) {
		super(activity);
		mDpRate = getDisplayRate();
		mItemView = mActivity.getSharedPreferences("PhotoPlayer", Context.MODE_PRIVATE).getInt("item_view", ItemView.LIST);
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
		mPhotoPlayerView.setVisibility(View.VISIBLE);
		mPhotoListView.setVisibility(View.GONE);
		mCurrentIndex.setText("0");
		mTotalCount.setText(" of " + "0");
		mTitle.setText("");
		mImageView.setImageResource(android.R.color.transparent);
		showInfoAndController(true);
	}

	@Override
	public void onPause() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_FORWARD);
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_REWIND);
		mImageLoader.destroy();
		pause();
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onBackPressed() {
		if (View.VISIBLE == mPhotoListView.getVisibility()) {
			backToPlayerView();
		} else {
			mActivity.finish();
		}
	}

	@Override
	public void onServiceConnected() {
		setItemView();
	}

	@Override
	public void onServiceDisconnected() {
	}

	@Override
	public void onMediaScan(boolean scanning) {
		if (scanning) {
			if (View.VISIBLE == mPhotoListView.getVisibility()) {
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
	// PhotoServiceCallback APIs
	@Override
	public void onTotalCount(int totalCount) {
		if (0 < totalCount) {
			mTotalCount.setText(" of " + Integer.valueOf(totalCount).toString());
		}
		if (1 >= totalCount) {
			mShuffleButton.setEnabled(false);
			mShuffleButton.setAlpha(0.5f);
		} else {
			mShuffleButton.setEnabled(true);
			mShuffleButton.setAlpha(1.0f);
		}
	}

	@Override
	public void onPlayState(int playState) {
		mPlayState = playState;
		switch (playState) {
		case PhotoUtils.PlayState.PLAY :
			mPlayPause.setImageResource(R.drawable.btn_pause_click);
			break;
		case PhotoUtils.PlayState.PAUSE :
			mPlayPause.setImageResource(R.drawable.btn_play_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onPhotoInfo(int index, PhotoInfo info) {
		if (mPlayingIndex != index) {
			if (mPreviewMode) {
				Log.d("previewIndex = " + index + ", title = " + info.getTitle());
				mShowIndex = index;
				mTimerHandler.removeMessages(mTimerHandler.MESSAGE_RETURN_TO_PLAYING);
				mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_RETURN_TO_PLAYING, mTimerHandler.DELAY_FOR_RETURN_TIME);
				showPhotoInfo(info);
				mCurrentIndex.setText(Integer.valueOf(index + 1).toString());
			}
		} else {
			Log.d("playingIndex = " + index + ", title = " + info.getTitle());
			mShowIndex = index;
			mPlayingPhotoInfo = info;
			showPhotoInfo(info);
			mCurrentIndex.setText(Integer.valueOf(index + 1).toString());
			if (View.VISIBLE == mPhotoListView.getVisibility()) {
				if (isNowPlayingCategory()) {
					mListAdapter.notifyDataSetChanged();
					mGridAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onImageBitmap(int index, Bitmap imageBitmap) {
		if ((null != imageBitmap) && (!imageBitmap.isRecycled())) {
			Log.d("index = " + index);
			mPlayingIndex = index;
			Bitmap.Config config = imageBitmap.getConfig();
			if (null == config) {
				config = Bitmap.Config.RGB_565;
			}
			Bitmap bitmap = imageBitmap.copy(config, imageBitmap.isMutable());
			mImageView.setImageBitmap(bitmap);
		} else {
			Log.d("Bitmap is null");
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
		switch (shuffle) {
		case PhotoUtils.ShuffleState.OFF :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_off_click);
			break;
		case PhotoUtils.ShuffleState.ALL :
			mShuffleButton.setImageResource(R.drawable.btn_shuffle_on_click);
			break;
		default :
			break;
		}
	}

	@Override
	public void onFileInfo(int index, FileInfo info) {
		Log.d("onFileInfo parent = " + info.getParentPath() + ", title = " + info.getFileName());
		String filePath = info.getParentPath() + "/" + info.getFileName();
		if(filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase().equals("gif")) {
			Glide.with(mActivity.getApplicationContext()).load(filePath).into(mImageView);
		}
	}

	@Override
	public void onListInfo(ListInfo info) {
		if (null != info) {
			if ((null == info.getList()) || (0 == info.getList().length)) {
				switch (info.getListType()) {
				case PhotoUtils.ListType.NOW_PLAYING :
					requestList(PhotoUtils.ListType.ALL, null);
					break;
				case PhotoUtils.ListType.FOLDER :
					requestList(PhotoUtils.ListType.NOW_PLAYING, null);
					break;
				case PhotoUtils.ListType.FOLDER_TRACK :
					requestList(PhotoUtils.ListType.FOLDER, null);
					break;
				default :
					backToPlayerView();
					break;
				}
			} else {
				mListInfo = info;
				mListAdapter = new ListItemAdapter(mActivity);
				mItemList.setAdapter(mListAdapter);
				mGridAdapter = new GridItemAdapter(mActivity);
				mItemGrid.setAdapter(mGridAdapter);
				switch (info.getListType()) {
				case PhotoUtils.ListType.NOW_PLAYING :
					setNowPlaying();
					mItemList.setSelection(mPlayingIndex);
					mItemGrid.setSelection(mPlayingIndex);
					break;
				case PhotoUtils.ListType.ALL :
					selectCategoryButton(PhotoUtils.Category.ALL);
					mUpstep.setVisibility(View.GONE);
					break;
				case PhotoUtils.ListType.FOLDER :
					selectCategoryButton(PhotoUtils.Category.FOLDER);
					mUpstep.setVisibility(View.GONE);
					break;
				case PhotoUtils.ListType.FOLDER_TRACK :
					String subCategory = info.getSubCategory();
					if (null == subCategory) {
						requestList(PhotoUtils.ListType.FOLDER, null);
					} else {
						String folder = subCategory.substring(subCategory.lastIndexOf("/") + 1);
						folder = setCharset(folder);
						selectCategoryButton(PhotoUtils.Category.FOLDER);
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

	// ----------
	// Photo Player View
	private View mPhotoPlayerView;
	private FrameLayout mImageBack;
	private ImageView mImageView;
	private FrameLayout mTouchScreen;
	private LinearLayout mInfoAndController;
	private TextView mCurrentIndex;
	private TextView mTotalCount;
	private TextView mTitle;
	private ImageButton mListButton;
	private ImageButton mPrevious;
	private ImageButton mPlayPause;
	private ImageButton mNext;
	private ImageButton mShuffleButton;

	private int mPlayingIndex = 0;
	private int mShowIndex = 0;
	private boolean mPreviewMode = false;
	private PhotoInfo mPlayingPhotoInfo = null;
	private int mPlayState = PhotoUtils.PlayState.PAUSE;
	private boolean mScanning = false;
	private boolean mPreviousPressed = false;
	private boolean mNextPressed = false;
	private GestureDetector mSwipeDetector;

	private void initPlayerView() {
		mPhotoPlayerView = mLayoutInflater.inflate(R.layout.photo_player, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mPhotoPlayerView);
		mImageBack = mPhotoPlayerView.findViewById(R.id.image_back);
		mImageView = mPhotoPlayerView.findViewById(R.id.image_view);
		mTouchScreen = mPhotoPlayerView.findViewById(R.id.touch_screen);
		mTouchScreen.setOnTouchListener(mTouchListener);
		mInfoAndController = mPhotoPlayerView.findViewById(R.id.info_and_controller);
		mCurrentIndex = mPhotoPlayerView.findViewById(R.id.current_index);
		mCurrentIndex.setText("0");
		mCurrentIndex.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTotalCount = mPhotoPlayerView.findViewById(R.id.total_count);
		mTotalCount.setText(" of " + "0");
		mTotalCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mTitle = mPhotoPlayerView.findViewById(R.id.title_text);
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18.0f * mDpRate);
		mListButton = mPhotoPlayerView.findViewById(R.id.list_button);
		mListButton.setOnClickListener(mClickListener);
		mPrevious = mPhotoPlayerView.findViewById(R.id.previous_button);
		mPrevious.setOnTouchListener(mTouchListener);
		mPlayPause = mPhotoPlayerView.findViewById(R.id.play_pause_button);
		mPlayPause.setOnClickListener(mClickListener);
		mPlayPause.setImageResource(R.drawable.btn_play_click);
		mNext = mPhotoPlayerView.findViewById(R.id.next_button);
		mNext.setOnTouchListener(mTouchListener);
		mShuffleButton = mPhotoPlayerView.findViewById(R.id.shuffle_button);
		mShuffleButton.setOnClickListener(mClickListener);
		mSwipeDetector = new GestureDetector(mActivity, mSwipeListener);
	}

	private void showPhotoInfo(PhotoInfo info) {
		if (null != info) {
			mTitle.setText(info.getTitle());
			if (mPlayingIndex == mShowIndex) {
				int viewId = R.drawable.btn_play_click;
				switch (getPlayState()) {
				case PhotoUtils.PlayState.PLAY :
					viewId = R.drawable.btn_pause_click;
					break;
				case PhotoUtils.PlayState.PAUSE :
					viewId = R.drawable.btn_play_click;
					break;
				default :
					break;
				}
				mPrevious.setEnabled(true);
				mPlayPause.setImageResource(viewId);
				mNext.setEnabled(true);
				mCurrentIndex.setTextColor(Color.WHITE);
				mTitle.setTextColor(Color.WHITE);
			} else if (mPreviewMode) {
				mPrevious.setEnabled(false);
				mPlayPause.setImageResource(R.drawable.btn_play_click);
				mNext.setEnabled(false);
				mCurrentIndex.setTextColor(Color.BLUE);
				mTitle.setTextColor(Color.BLUE);
			}
		}
	}

	private void showInfoAndController(boolean show) {
		ViewGroup.LayoutParams backDimension = mImageBack.getLayoutParams();
		FrameLayout.LayoutParams backMargin;
		ViewGroup.LayoutParams imageDimension = mImageView.getLayoutParams();
		FrameLayout.LayoutParams imageMargin;
		ViewGroup.LayoutParams touchDimension = mTouchScreen.getLayoutParams();
		FrameLayout.LayoutParams touchMargin;
		if (show) {
			int imageHeight = (int)(366 * mDpRate);
			int headerHeight = (int)(56 * mDpRate);
			int footerHeight = (int)(178 * mDpRate);
			mInfoAndController.setVisibility(View.VISIBLE);
			mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			backDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			backDimension.height = imageHeight;
			backMargin = new FrameLayout.LayoutParams(backDimension);
			backMargin.setMargins(0, headerHeight, 0, footerHeight);
			imageDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			imageDimension.height = imageHeight;
			imageMargin = new FrameLayout.LayoutParams(imageDimension);
			imageMargin.setMargins(0, headerHeight, 0, footerHeight);
			touchDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			touchDimension.height = imageHeight;
			touchMargin = new FrameLayout.LayoutParams(touchDimension);
			touchMargin.setMargins(0, headerHeight, 0, footerHeight);
		} else {
			mInfoAndController.setVisibility(View.GONE);
			if (View.GONE == mPhotoListView.getVisibility()) {
				mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
			}
			backDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			backDimension.height = ViewGroup.LayoutParams.MATCH_PARENT;
			backMargin = new FrameLayout.LayoutParams(backDimension);
			backMargin.setMargins(0, 0, 0, 0);
			imageDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			imageDimension.height = ViewGroup.LayoutParams.MATCH_PARENT;
			imageMargin = new FrameLayout.LayoutParams(imageDimension);
			imageMargin.setMargins(0, 0, 0, 0);
			touchDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
			touchDimension.height = ViewGroup.LayoutParams.MATCH_PARENT;
			touchMargin = new FrameLayout.LayoutParams(touchDimension);
			touchMargin.setMargins(0, 0, 0, 0);
		}
		mImageBack.setLayoutParams(backMargin);
		mImageView.setLayoutParams(imageMargin);
		mTouchScreen.setLayoutParams(touchMargin);
	}

	// ----------
	// Photo List View
	private View mPhotoListView;
//	private ImageButton mCategoryAllButton;
//	private ImageButton mCategoryFolderButton;
	private ImageButton mGridChangeView;
	private ImageButton mListChangeView;
	private LinearLayout mUpstep;
	private TextView mSubCategoryName;
	private ListView mItemList;
	private GridView mItemGrid;

	private ListItemAdapter mListAdapter;
	private GridItemAdapter mGridAdapter;
	private ListInfo mListInfo = null;
	private int mItemView;
	private int mNowPlayingCategory = PhotoUtils.Category.ALL;

	private void initListView() {
		mPhotoListView = mLayoutInflater.inflate(R.layout.photo_list, mActivity.getRootLayout(), false);
		mActivity.getRootLayout().addView(mPhotoListView);
//		mCategoryAllButton = mPhotoListView.findViewById(R.id.category_all_button);
//		mCategoryAllButton.setOnClickListener(mClickListener);
//		mCategoryFolderButton = mPhotoListView.findViewById(R.id.category_folder_button);
//		mCategoryFolderButton.setOnClickListener(mClickListener);
		mGridChangeView = mPhotoListView.findViewById(R.id.grid_change_view);
		mGridChangeView.setOnClickListener(mClickListener);
		mListChangeView = mPhotoListView.findViewById(R.id.list_change_view);
		mListChangeView.setOnClickListener(mClickListener);
		mUpstep = mPhotoListView.findViewById(R.id.list_upstep);
		mUpstep.setOnClickListener(mClickListener);
		mSubCategoryName = mPhotoListView.findViewById(R.id.sub_category);
		mSubCategoryName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14.0f * mDpRate);
		mItemList = mPhotoListView.findViewById(R.id.item_list);
		mItemList.setOnItemClickListener(mItemClickListener);
		mItemGrid = mPhotoListView.findViewById(R.id.item_grid);
		mItemGrid.setOnItemClickListener(mItemClickListener);
		mItemGrid.setPadding((int)(60.0f * mDpRate), 0, (int)(34.0f * mDpRate), (int)(20.0f * mDpRate));
	}

	private void selectCategoryButton(int type) {
		switch (type) {
		case PhotoUtils.Category.ALL :
//			mCategoryAllButton.setSelected(true);
//			mCategoryFolderButton.setSelected(false);
			break;
		case PhotoUtils.Category.FOLDER :
//			mCategoryAllButton.setSelected(false);
//			mCategoryFolderButton.setSelected(true);
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
		case PhotoUtils.ListType.ALL :
			mUpstep.setVisibility(View.GONE);
			break;
		case PhotoUtils.ListType.FOLDER :
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
			if (View.VISIBLE == mPhotoListView.getVisibility()) {
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
			case PhotoUtils.ListType.NOW_PLAYING :
				if (mPlayingIndex == position) {
					isPlaying = true;
				}
				break;
			case PhotoUtils.ListType.ALL :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(PhotoUtils.Category.ALL, null))) {
					isPlaying = true;
				}
				break;
			case PhotoUtils.ListType.FOLDER :
				if (isNowPlayingCategory(PhotoUtils.Category.FOLDER, mListInfo.getList()[position])) {
					isPlaying = true;
				}
				break;
			case PhotoUtils.ListType.FOLDER_TRACK :
				if ((mPlayingIndex == position) && (isNowPlayingCategory(PhotoUtils.Category.FOLDER, mListInfo.getSubCategory()))) {
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
			case PhotoUtils.ListType.NOW_PLAYING :
				isPlaying = true;
				break;
			case PhotoUtils.ListType.ALL :
				isPlaying = isNowPlayingCategory(PhotoUtils.Category.ALL, null);
				break;
			case PhotoUtils.ListType.FOLDER_TRACK :
				isPlaying = isNowPlayingCategory(PhotoUtils.Category.FOLDER, mListInfo.getSubCategory());
				break;
			default :
				break;
			}
		}
		return isPlaying;
	}

	private void backToPlayerView() {
		mImageLoader.destroy();
		mPhotoPlayerView.setVisibility(View.VISIBLE);
		mPhotoListView.setVisibility(View.GONE);
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
		public ImageView bitmap;
		public TextView title;
		public TextView info;
		public ImageView playing;
	}

	private class GridViewHolder {
		public ImageView bitmap;
		public LinearLayout folder;
		public ImageView folderIcon;
		public TextView title;
	}

	// ----------
	// ListAdapter
	private class ListItemAdapter extends ArrayAdapter<String> {
		public ListItemAdapter(Context context) {
			super(context, R.layout.icon_list_item, mListInfo.getList());
			mActivity.clearImageBitmapBuffer();
			mImageLoader.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListViewHolder holder = new ListViewHolder();
			convertView = ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.icon_list_item, parent, false);
			holder.folder = convertView.findViewById(R.id.list_folder);
			holder.item = convertView.findViewById(R.id.list_item);
			holder.bitmap = convertView.findViewById(R.id.list_bitmap);
			holder.title = convertView.findViewById(R.id.list_title);
			holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18 * mDpRate);
			holder.info = convertView.findViewById(R.id.list_info);
			holder.info.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14 * mDpRate);
			holder.playing = convertView.findViewById(R.id.list_playing);
			convertView.setTag(holder);
			convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(71 * mDpRate)));
			String getInfo = mListInfo.getList()[position];
			String filePath = mListInfo.getFilePathList()[position];
			switch (mListInfo.getListType()) {
			case PhotoUtils.ListType.FOLDER :
				String folder = getInfo.substring(getInfo.lastIndexOf("/") + 1);
				getInfo = setCharset(folder);
				holder.folder.setVisibility(View.VISIBLE);
				holder.item.setVisibility(View.GONE);
				holder.info.setText(mListInfo.getFileCount()[position] + " List");
				break;
			default :
				holder.folder.setVisibility(View.GONE);
				holder.item.setVisibility(View.VISIBLE);
				holder.bitmap.setImageResource(R.drawable.img_thumb);
				if(filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase().equals("gif")) {
					Glide.with(mActivity.getApplicationContext()).load(filePath).into(holder.bitmap);
				}else {
					mImageLoader.displayImage(position, holder.bitmap);
				}
				holder.info.setVisibility(View.GONE);
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
	}

	// ----------
	// GridAdapter
	private class GridItemAdapter extends ArrayAdapter<String> {
		public GridItemAdapter(Context context) {
			super(context, R.layout.icon_grid_item, mListInfo.getList());
			mActivity.clearImageBitmapBuffer();
			mImageLoader.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GridViewHolder holder = new GridViewHolder();
			convertView = ((LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.icon_grid_item, parent, false);
			holder.bitmap = convertView.findViewById(R.id.grid_bitmap);
			holder.folder = convertView.findViewById(R.id.grid_folder_layout);
			holder.folderIcon = convertView.findViewById(R.id.grid_folder_icon);
			holder.title = convertView.findViewById(R.id.grid_title);
			holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12 * mDpRate);
			boolean isPlaying = isPlayingItem(position);
			convertView.setTag(holder);
			convertView.setLayoutParams(new LayoutParams((int)(186 * mDpRate), (int)(140 * mDpRate)));
			String getInfo = mListInfo.getList()[position];
			String filePath = mListInfo.getFilePathList()[position];
			switch (mListInfo.getListType()) {
			case PhotoUtils.ListType.FOLDER :
				String folder = getInfo.substring(getInfo.lastIndexOf("/") + 1);
				getInfo = setCharset(folder);
				//holder.bitmap.setImageResource(R.drawable.img_thumb);
				//holder.folder.setVisibility(View.VISIBLE);
				//holder.title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				holder.title.setGravity(Gravity.CENTER);
				holder.folder.setVisibility(View.GONE);
				if (isPlaying) {
					//holder.folderIcon.setImageResource(R.drawable.ic_folder_img);
					holder.bitmap.setImageResource(R.drawable.ic_folder_click);
					holder.title.setTextColor(Color.parseColor("#AE00FF"));
				} else {
					//holder.folderIcon.setImageResource(R.drawable.ic_folder_basic);
					//holder.title.setTextColor(Color.parseColor("#FFD07F"));
					holder.bitmap.setImageResource(R.drawable.ic_folder_click);
					holder.title.setTextColor(Color.WHITE);
				}
				break;
			default :
				holder.bitmap.setImageResource(R.drawable.img_thumb);
				if(filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase().equals("gif")) {
					Glide.with(mActivity.getApplicationContext()).load(filePath).into(holder.bitmap);
				}else {
					mImageLoader.displayImage(position, holder.bitmap);
				}
				holder.title.setGravity(Gravity.CENTER);
				holder.folder.setVisibility(View.GONE);
				if (isPlaying) {
					holder.title.setTextColor(Color.parseColor("#AE00FF"));
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
					mPhotoPlayerView.setVisibility(View.GONE);
					mPhotoListView.setVisibility(View.VISIBLE);
					mUpstep.setVisibility(View.GONE);
					requestList(PhotoUtils.ListType.NOW_PLAYING, null);
				}
				break;
			case R.id.play_pause_button :
				if ((mPreviewMode) && (mPlayingIndex != mShowIndex)) {
					selectItemToPlay(mShowIndex, true);
				} else {
					switch (mPlayState) {
					case PhotoUtils.PlayState.PLAY :
						showInfoAndController(true);
						pause();
						break;
					case PhotoUtils.PlayState.PAUSE :
						showInfoAndController(false);
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
//			case R.id.category_all_button :
//				if ((null != mListInfo) && (PhotoUtils.ListType.ALL != mListInfo.getListType())) {
//					requestList(PhotoUtils.ListType.ALL, null);
//				}
//				break;
//			case R.id.category_folder_button :
//				if ((null != mListInfo) && (PhotoUtils.ListType.FOLDER != mListInfo.getListType())) {
//					requestList(PhotoUtils.ListType.FOLDER, null);
//				}
//				break;
			case R.id.grid_change_view :
				mItemView = ItemView.GRID;
				setItemView();
				SharedPreferences.Editor prefEdit = mActivity.getSharedPreferences("PhotoPlayer", Context.MODE_PRIVATE).edit();
				prefEdit.putInt("item_view", mItemView);
				prefEdit.apply();
				break;
			case R.id.list_change_view :
				mItemView = ItemView.LIST;
				setItemView();
				SharedPreferences.Editor prefEdit1 = mActivity.getSharedPreferences("PhotoPlayer", Context.MODE_PRIVATE).edit();
				prefEdit1.putInt("item_view", mItemView);
				prefEdit1.apply();
				break;
			case R.id.list_upstep :
				if (null != mListInfo) {
					switch (mListInfo.getListType()) {
					case PhotoUtils.ListType.NOW_PLAYING :
						if (mNowPlayingCategory == PhotoUtils.Category.FOLDER) {
							requestList(PhotoUtils.ListType.FOLDER, null);
						}
						break;
					case PhotoUtils.ListType.FOLDER_TRACK :
						requestList(PhotoUtils.ListType.FOLDER, null);
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
				if (View.VISIBLE == mPhotoPlayerView.getVisibility()) {
					switch (mPlayState) {
					case PhotoUtils.PlayState.PLAY :
						if (MotionEvent.ACTION_UP == event.getAction()) {
							pause();
							showInfoAndController(true);
						}
						break;
					case PhotoUtils.PlayState.PAUSE :
						if ((!mSwipeDetector.onTouchEvent(event)) && (MotionEvent.ACTION_UP == event.getAction())) {
							if (View.VISIBLE == mInfoAndController.getVisibility()) {
								showInfoAndController(false);
							} else {
								showInfoAndController(true);
							}
						}
						break;
					default :
						break;
					}
					return true;
				}
				break;
			case R.id.previous_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mPreviousPressed = true;
						if (!mNextPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
						} else if (PhotoUtils.PlayState.FAST_FORWARD != mPlayState) {
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_FORWARD);
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
							case PhotoUtils.PlayState.FAST_FORWARD :
								break;
							case PhotoUtils.PlayState.FAST_REWIND :
								mPlayState = PhotoUtils.PlayState.PAUSE;
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
			case R.id.next_button :
				if (mPlayingIndex == mShowIndex) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mNextPressed = true;
						if (!mPreviousPressed) {
							mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
						} else if (PhotoUtils.PlayState.FAST_REWIND != mPlayState) {
							mTimerHandler.removeMessages(mTimerHandler.MESSAGE_START_FAST_REWIND);
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
							case PhotoUtils.PlayState.FAST_FORWARD :
								mPlayState = PhotoUtils.PlayState.PAUSE;
								if (mPreviousPressed) {
									mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
								}
								break;
							case PhotoUtils.PlayState.FAST_REWIND :
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
			default :
				break;
			}
			return false;
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
					case PhotoUtils.ListType.ALL :
					case PhotoUtils.ListType.FOLDER_TRACK :
					case PhotoUtils.ListType.NOW_PLAYING :
						selectItemToPlay(position, PhotoUtils.ListType.NOW_PLAYING == mListInfo.getListType());
						break;
					case PhotoUtils.ListType.FOLDER :
						requestList(PhotoUtils.ListType.FOLDER_TRACK, (mListInfo.getList())[position]);
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

	private GestureDetector.SimpleOnGestureListener mSwipeListener = new GestureDetector.SimpleOnGestureListener() {
		private static final int SWIPE_MIN_DISTANCE = 100;
		private static final int SWIPE_MAX_OFF_PATH = 50;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if ((PhotoUtils.PlayState.PAUSE == mPlayState) && (SWIPE_THRESHOLD_VELOCITY < Math.abs(velocityX)) && (SWIPE_MAX_OFF_PATH > Math.abs(e1.getY() - e2.getY()))) {
				if (SWIPE_MIN_DISTANCE < (e1.getX() - e2.getX())) {
					playNext();
					return true;
				} else if (SWIPE_MIN_DISTANCE < (e2.getX() - e1.getX())) {
					playPrev();
					return true;
				}
			}
			return false;
		}
	};

	// ----------
	// AndroidView UI Timer Handler
	private TimerHandler mTimerHandler = new TimerHandler();

	private class TimerHandler extends Handler {
		public final int MESSAGE_START_FAST_FORWARD = 2001;
		public final int MESSAGE_START_FAST_REWIND = 2002;
		public final int MESSAGE_RETURN_TO_PLAYING = 2003;

		public final int DELAY_FOR_START_FAST = 1000;
		public final int DELAY_FOR_RETURN_TIME = 5000;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_START_FAST_FORWARD :
				mPlayState = PhotoUtils.PlayState.FAST_FORWARD;
				playNext();
				mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_FORWARD, mTimerHandler.DELAY_FOR_START_FAST);
				break;
			case MESSAGE_START_FAST_REWIND :
				mPlayState = PhotoUtils.PlayState.FAST_REWIND;
				playPrev();
				mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_START_FAST_REWIND, mTimerHandler.DELAY_FOR_START_FAST);
				break;
			case MESSAGE_RETURN_TO_PLAYING :
				if (mPreviewMode) {
					mPreviewMode = false;
					mShowIndex = mPlayingIndex;
					showPhotoInfo(mPlayingPhotoInfo);
					mCurrentIndex.setText(Integer.valueOf(mPlayingIndex + 1).toString());
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
		private HashMap<ImageView, Integer> imageViews = new HashMap<ImageView, Integer>();
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
			Bitmap bitmap = mActivity.fromImageBitmapBuffer(index);
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
						Bitmap bitmap = getImageBitmap(photoToLoad.index, PhotoUtils.ListType.NOW_PLAYING == mListInfo.getListType(), true);
						mActivity.toImageBitmapBuffer(photoToLoad.index, bitmap);
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
