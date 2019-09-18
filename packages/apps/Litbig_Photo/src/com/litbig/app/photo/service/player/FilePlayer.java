package com.litbig.app.photo.service.player;

import java.io.File;
import java.util.ArrayList;

import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.service.PhotoPlaybackService;
import com.litbig.app.photo.service.playlist.MediaStoreList;
import com.litbig.app.photo.util.Log;
import com.litbig.app.photo.util.PhotoUtils;
import com.litbig.mediastorage.MediaStorage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

public class FilePlayer extends PhotoPlayer {
	private MediaStoreList mList;

	public FilePlayer(PhotoPlaybackService service) {
		super(service);
		mList = new MediaStoreList(service);
	}

	// ----------
	// PhotoPlayerInterface APIs
	@Override
	public int getPlayingIndex() {
		return mList.getPlayingIndex(false);
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) {
		return mList.getFileFullPath(index, isNowPlaying);
	}

	@Override
	public int getPlayState() {
		return mPlayState;
	}

	@Override
	public int getPlayIntervalMS() {
		return mPlayInterval;
	}

	@Override
	public void setPlayIntervalMS(int intervalMS) {
		mPlayInterval = intervalMS;
	}

	@Override
	public void play() {
		startPlay(false);
		mPlayState = PhotoUtils.PlayState.PLAY;
		onPlayState(mPlayState);
		mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_PLAY_SLIDESHOW, mPlayInterval);
	}

	@Override
	public void pause() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_PLAY_SLIDESHOW);
		mPlayState = PhotoUtils.PlayState.PAUSE;
		onPlayState(mPlayState);
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) {
		boolean success = false;
		mErrorFiles.clear();
		success = setTrack(mList.getTrackFromIndex(index, isNowPlaying));
		if (success) {
			if (!isNowPlaying) {
				mList.changeCurrentCategoryToNowPlaying();
				setShuffle(PhotoUtils.ShuffleState.OFF);
			}
			startPlay(false);
		} else {
			setTrack(mLastTrack);
		}
		return success;
	}

	@Override
	public void playPrev() {
		mErrorFiles.clear();
		while (!setTrack(mList.getPrevTrack())) {
			if (mErrorFiles.size() >= mList.getTotalCount()) {
				break;
			}
		}
		startPlay(false);
	}

	@Override
	public void playNext() {
		mErrorFiles.clear();
		while (!setTrack(mList.getNextTrack())) {
			if (mErrorFiles.size() >= mList.getTotalCount()) {
				break;
			}
		}
		startPlay(false);
	}

	@Override
	public int getShuffle() {
		return mList.getShuffle();
	}

	@Override
	public void setShuffle() {
		switch (mList.getShuffle()) {
		case PhotoUtils.ShuffleState.OFF :
			setShuffle(PhotoUtils.ShuffleState.ALL);
			break;
		case PhotoUtils.ShuffleState.ALL :
			setShuffle(PhotoUtils.ShuffleState.OFF);
			break;
		default :
			break;
		}
	}

	@Override
	public void requestList(int listType, String subCategory) {
		mList.requestList(listType, subCategory);
	}

	@Override
	public Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale) {
		return mList.getImageBitmap(index, isNowPlaying, isScale);
	}

	@Override
	public int getNowPlayingCategory() {
		return mList.getNowPlayingCategory();
	}

	@Override
	public String getNowPlayingSubCategory() {
		return mList.getNowPlayingSubCategory();
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) {
		return mList.isNowPlayingCategory(category, subCategory);
	}

	// ----------
	// PhotoPlayer APIs
	private boolean mActive = false;
	private int mPlayState = PhotoUtils.PlayState.PAUSE;

	@Override
	public void active() {
		if (!mActive) {
			mLastTrack = (String)mService.loadPreference(PhotoUtils.Preference.PLAY_FILE);
			mList.requestActive();
			mList.setShuffle((Integer)mService.loadPreference(PhotoUtils.Preference.SHUFFLE));
		} else {
			onTotalCount(mList.getTotalCount());
			startPlay(false);
		}
		onShuffleState(mList.getShuffle());
	}

	@Override
	public void inactive() {
		if (mActive) {
			Log.i("Player inactive : Track = " + mLastTrack);
			mActive = false;
		}
	}

	@Override
	public void destroy() {
		inactive();
		mList.destroy();
	}

	// ----------
	// UpdatePlayTime Handler
	private int mPlayInterval = 3000;
	private boolean mBitmapThreadRunning = false;
	private TimerHandler mTimerHandler = new TimerHandler();

	private class TimerHandler extends Handler {
		public final int MESSAGE_PLAY_SLIDESHOW = 101;
		public final int MESSAGE_REQUEST_BITMAP = 102;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_PLAY_SLIDESHOW :
				removeMessages(MESSAGE_PLAY_SLIDESHOW);
				playNext();
				sendEmptyMessageDelayed(MESSAGE_PLAY_SLIDESHOW, mPlayInterval);
				break;
			case MESSAGE_REQUEST_BITMAP :
				if (mBitmapThreadRunning) {
					removeMessages(MESSAGE_REQUEST_BITMAP);
					Message message = new Message();
					message.what = mTimerHandler.MESSAGE_REQUEST_BITMAP;
					message.arg1 = msg.arg1;
					sendMessage(message);
				} else {
					final boolean refresh = (1 == msg.arg1);
					new Thread(new Runnable() {
						@Override
						public void run() {
							mBitmapThreadRunning = true;
							int playingIndex = mList.getPlayingIndex(refresh);
							onImageBitmap(playingIndex, mList.getImageBitmap(playingIndex, true, false));
							onPhotoInfo(playingIndex, mList.getPhotoInfo(playingIndex, true));
							onFileInfo(playingIndex, getFileInfo(mList.getTrackFromIndex(playingIndex, true)));
							Log.d("Start Play : Track = " + mLastTrack + ", Index = " + playingIndex);
							mBitmapThreadRunning = false;
						}
					}).start();
				}
				break;
			default :
				break;
			}
			super.handleMessage(msg);
		}
	};

	// FilePlayer APIs
	public boolean activePlayer() {
		boolean success = false;
		mErrorFiles.clear();
		if (setTrack(mLastTrack)) {
			if (1 == mList.getTotalCount()) {
				setShuffle(PhotoUtils.ShuffleState.OFF);
			}
			startPlay(true);
			success = true;
			Log.d("Player active : Track = " + mLastTrack);
		} else {
			mService.clearPreference();
			int index = 0;
			while (!setTrack(mList.getTrackFromIndex(index, true))) {
				index++;
				if (mErrorFiles.size() >= mList.getTotalCount()) {
					break;
				}
			}
			if (mList.getTotalCount() > index) {
				startPlay(false);
				success = true;
				Log.d("Player active : Track = " + mLastTrack);
			}
		}
		return success;
	}

	public String getLastTrack() {
		return mLastTrack;
	}

	public void changeStorage() {
		if ((mActive) && (null != mLastTrack) && (!mLastTrack.isEmpty())) {
			Log.d("changeStorage");
			boolean ejectedPlaying = true;
			ArrayList<String> enableStorage = MediaStorage.getEnableStorage(mService);
			for (int index = 0; index < enableStorage.size(); index++) {
				if (mLastTrack.startsWith(enableStorage.get(index))) {
					ejectedPlaying = false;
				}
			}
			if (ejectedPlaying) {
				mLastTrack = null;
				stopPlay();
				if (PhotoUtils.Category.ALL != (Integer)mService.loadPreference(PhotoUtils.Preference.CATEGORY)) {
					mService.clearPreference();
				} else if (1 >= mList.getTotalCount()) {
					mService.clearPreference();
				}
			}
			mList.requestQueryForScanFinish(ejectedPlaying);
		}
	}

	public void clearPlaylist() {
		mList.clearPlaylist();
	}

	// ----------
	// FilePlayer internal functions
	private String mLastTrack;
	private ArrayList<String> mErrorFiles = new ArrayList<>();

	private boolean setTrack(String track) {
		boolean success = false;
		mActive = true;
		if ((null != track) && (!track.isEmpty())) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(track, options);
			if ((0 < options.outWidth) && (0 < options.outHeight)) {
				mLastTrack = track;
				mService.savePreference(PhotoUtils.Preference.PLAY_FILE, mLastTrack);
				success = true;
			} else {
				Log.w("setTrack Error : " + track);
				mErrorFiles.add(track);
				if (mErrorFiles.size() >= mList.getTotalCount()) {
					Log.e("setTrack Error : all track error");
				}
			}
		}
		return success;
	}

	private void startPlay(boolean refresh) {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_REQUEST_BITMAP);
		Message message = new Message();
		message.what = mTimerHandler.MESSAGE_REQUEST_BITMAP;
		message.arg1 = (refresh) ? 1 : 0;
		mTimerHandler.sendMessage(message);
	}

	private void stopPlay() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_PLAY_SLIDESHOW);
	}

	private void setShuffle(int shuffle) {
		mList.setShuffle(shuffle);
		onShuffleState(mList.getShuffle());
		mService.savePreference(PhotoUtils.Preference.SHUFFLE, mList.getShuffle());
	}

	private FileInfo getFileInfo(String file) {
		if (null != file) {
			return new FileInfo(file.substring(file.lastIndexOf('/') + 1, file.length()), file.substring(0, file.lastIndexOf('/')), (new File(file)).length());
		} else {
			return null;
		}
	}
}
