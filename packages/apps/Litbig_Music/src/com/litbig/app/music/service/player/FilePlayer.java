package com.litbig.app.music.service.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.litbig.app.music.R;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.service.MusicPlaybackService;
import com.litbig.app.music.service.playlist.MediaStoreList;
import com.litbig.app.music.util.Log;
import com.litbig.app.music.util.MusicUtils;
import com.litbig.mediastorage.MediaStorage;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

public class FilePlayer extends MusicPlayer {
	private MediaPlayer mPlayer = null;
	private MediaStoreList mList;

	public FilePlayer(MusicPlaybackService service) {
		super(service);
		mList = new MediaStoreList(service);
	}

	// ----------
	// PlayDirection
	private class PlayDirection {
		public static final int PREV = -1;
		public static final int NEXT = 1;
	}

	// ----------
	// MusicPlayerInterface APIs
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
		if (null != mPlayer) {
			if (mPlayer.isPlaying()) {
				mPlayState = MusicUtils.PlayState.PLAY;
			} else if (MusicUtils.PlayState.STOP == mPlayState) {
				mPlayState = MusicUtils.PlayState.PAUSE;
			}
		} else {
			mPlayState = MusicUtils.PlayState.STOP;
		}
		return mPlayState;
	}

	@Override
	public int getPlayTimeMS() {
		int time = 0;
		if (null != mPlayer) {
			time = mPlayer.getCurrentPosition();
		}
		return time;
	}

	@Override
	public void setPlayTimeMS(int playTimeMS) {
		if (null != mPlayer) {
			if ((0 > playTimeMS) || (mPlayer.getDuration() <= playTimeMS)) {
				playTimeMS = 0;
			}
			mPlayer.seekTo(playTimeMS);
			startPlay();
		}
	}

	@Override
	public void gripTimeProgressBar() {
		if (null != mPlayer) {
			if (mPlayer.isPlaying()) {
				pausePlay();
			}
		}
	}

	@Override
	public void play() {
		if (null != mPlayer) {
			if (!mPlayer.isPlaying()) {
				startPlay();
			}
		}
	}

	@Override
	public void pause() {
		if (null != mPlayer) {
			if (mPlayer.isPlaying()) {
				pausePlay();
				if (!mPlayer.isPlaying()) {
					mPlayState = MusicUtils.PlayState.PAUSE;
					onPlayState(mPlayState);
				}
			}
		}
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) {
		boolean success;
		int position = 0;
		mErrorFiles.clear();
		mPlayDirection = PlayDirection.NEXT;
		if (null != mPlayer) {
			position = mPlayer.getCurrentPosition();
		}
		success = setTrack(mList.getTrackFromIndex(index, isNowPlaying));
		if (success) {
			if (!isNowPlaying) {
				mList.changeCurrentCategoryToNowPlaying();
				setShuffle(MusicUtils.ShuffleState.OFF);
				setRepeat(MusicUtils.RepeatState.ALL);
				setScan(MusicUtils.ScanState.OFF);
			}
			changePlay();
		} else {
			setTrack(mLastTrack);
			mPlayer.seekTo(position);
			startPlay();
		}
		return success;
	}

	@Override
	public void playPrev() {
		if (null != mPlayer) {
			final int boundary = 10000;
			if (boundary < mPlayer.getCurrentPosition()) {
				mPlayer.seekTo(0);
				startPlay();
			} else {
				prevPlay();
			}
		} else {
			prevPlay();
		}
	}

	@Override
	public void playNext() {
		mErrorFiles.clear();
		mPlayDirection = PlayDirection.NEXT;
		while (!setTrack(mList.getNextTrack())) {
			if (mErrorFiles.size() >= mList.getTotalCount()) {
				break;
			}
		}
		changePlay();
	}

	@Override
	public void startFastForward() {
		if (null != mPlayer) {
			if (mPlayer.isPlaying()) {
				pausePlay();
			}
			mFastTimeMS = mPlayer.getCurrentPosition();
			mTimerHandler.removeMessages(mTimerHandler.MESSAGE_FAST_FORWARD);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_FAST_FORWARD, mTimerHandler.INTERVAL_FOR_FAST_TIMER);
			mPlayState = MusicUtils.PlayState.FAST_FORWARD;
			onPlayState(mPlayState);
		}
	}

	@Override
	public void stopFastForward() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_FAST_FORWARD);
		setPlayTimeMS(mFastTimeMS);
	}

	@Override
	public void startFastRewind() {
		if (null != mPlayer) {
			if (mPlayer.isPlaying()) {
				pausePlay();
			}
			mFastTimeMS = mPlayer.getCurrentPosition();
			mTimerHandler.removeMessages(mTimerHandler.MESSAGE_FAST_REWIND);
			mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_FAST_REWIND, mTimerHandler.INTERVAL_FOR_FAST_TIMER);
			mPlayState = MusicUtils.PlayState.FAST_REWIND;
			onPlayState(mPlayState);
		}
	}

	@Override
	public void stopFastRewind() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_FAST_REWIND);
		setPlayTimeMS(mFastTimeMS);
	}

	@Override
	public int getShuffle() {
		return mList.getShuffle();
	}

	@Override
	public int getRepeat() {
		return mRepeat;
	}

	@Override
	public int getScan() {
		return mScan;
	}

	@Override
	public void setShuffle() {
		switch (mList.getShuffle()) {
		case MusicUtils.ShuffleState.OFF :
			setShuffle(MusicUtils.ShuffleState.ALL);
			setRepeat(MusicUtils.RepeatState.ALL);
			setScan(MusicUtils.ScanState.OFF);
			break;
		case MusicUtils.ShuffleState.ALL :
			setShuffle(MusicUtils.ShuffleState.OFF);
			break;
		default :
			break;
		}
	}

	@Override
	public void setRepeat() {
		switch (mRepeat) {
		case MusicUtils.RepeatState.OFF:
			setRepeat(MusicUtils.RepeatState.ALL);
			break;
		case MusicUtils.RepeatState.ALL :
			setShuffle(MusicUtils.ShuffleState.OFF);
			setRepeat(MusicUtils.RepeatState.ONE);
			setScan(MusicUtils.ScanState.OFF);
			break;
		case MusicUtils.RepeatState.ONE :
			setRepeat(MusicUtils.RepeatState.OFF);
			break;
		default :
			break;
		}
	}

	@Override
	public void setScan() {
		switch (mScan) {
		case MusicUtils.ScanState.OFF :
			setShuffle(MusicUtils.ShuffleState.OFF);
			setRepeat(MusicUtils.RepeatState.ALL);
			setScan(MusicUtils.ScanState.ALL);
			break;
		case MusicUtils.ScanState.ALL :
			setScan(MusicUtils.ScanState.OFF);
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
	public Bitmap getAlbumArt(int index, boolean isNowPlaying, boolean isScale) {
		return mList.getAlbumArt(index, isNowPlaying, isScale);
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
	// MusicPlayer APIs
	private boolean mStart;

	@Override
	public void active(boolean start) {
		if (null == mPlayer) {
			mStart = start;
			mLastTrack = (String)mService.loadPreference(MusicUtils.Preference.PLAY_FILE);
			mList.requestActive();
			mList.setShuffle((Integer)mService.loadPreference(MusicUtils.Preference.SHUFFLE));
			mRepeat = (Integer)mService.loadPreference(MusicUtils.Preference.REPEAT);
			mScan = (Integer)mService.loadPreference(MusicUtils.Preference.SCAN);
		} else {
			if ((start) && (!mPlayer.isPlaying())) {
				startPlay();
			}
			onTotalCount(mList.getTotalCount());
			updateMusicInfo(false);
			onPlayState(getPlayState());
			onPlayTimeMS(mPlayer.getCurrentPosition());
		}
		onShuffleState(mList.getShuffle());
		onRepeatState(mRepeat);
		onScanState(mScan);
	}

	@Override
	public void inactive() {
		if (null != mPlayer) {
			Log.i("Player inactive : Track = " + mLastTrack + ", PlayTime = " + mPlayer.getCurrentPosition());
			stopPlay();
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override
	public void destroy() {
		inactive();
		mList.destroy();
	}

	@Override
	public void setVolume(float volume) {
		if (null != mPlayer) {
			mPlayer.setVolume(volume, volume);
		}
	}

	// ----------
	// UpdatePlayTime Handler
	private int mFastTimeMS;
	private boolean mBitmapThreadRunning = false;
	private TimerHandler mTimerHandler = new TimerHandler();

	private class TimerHandler extends Handler {
		public final int MESSAGE_TIME_PROGRESS = 101;
		public final int MESSAGE_FAST_FORWARD = 102;
		public final int MESSAGE_FAST_REWIND = 103;
		public final int MESSAGE_REQUEST_BITMAP = 104;

		private final int INTERVAL_FOR_UPDATE_TIME = 100;
		public final int INTERVAL_FOR_FAST_TIMER = 100;
		public final int INTERVAL_FOR_SCAN_TIMER = 5000;

		private int mPrevTime = 0;

		@Override
		public void handleMessage(Message msg) {
			if (null != mPlayer) {
				switch (msg.what) {
				case MESSAGE_TIME_PROGRESS :
					if (mPlayer.isPlaying()) {
						sendEmptyMessageDelayed(MESSAGE_TIME_PROGRESS, INTERVAL_FOR_UPDATE_TIME);
						int currentTime = mPlayer.getCurrentPosition();
						if ((mPrevTime > currentTime) || (0 < ((currentTime / 1000) - (mPrevTime / 1000)))) {
							onPlayTimeMS(currentTime);
							mService.savePreference(MusicUtils.Preference.PLAY_TIME, mPlayer.getCurrentPosition());
							if ((MusicUtils.ScanState.ALL == mScan) && (INTERVAL_FOR_SCAN_TIMER <= currentTime)) {
								playNext();
							}
						}
						mPrevTime = currentTime;
					} else if (MusicUtils.PlayState.PLAY == mPlayState) {
						mTimerHandler.sendEmptyMessageDelayed(mTimerHandler.MESSAGE_TIME_PROGRESS, 1000);
					}
					break;
				case MESSAGE_FAST_FORWARD :
					mFastTimeMS += 1000;
					if (mPlayer.getDuration() < mFastTimeMS) {
						mErrorFiles.clear();
						while (!setTrack(mList.getNextTrack())) {
							if (mErrorFiles.size() >= mList.getTotalCount()) {
								break;
							}
						}
						mFastTimeMS = 0;
						updateMusicInfo(false);
					}
					sendEmptyMessageDelayed(MESSAGE_FAST_FORWARD, INTERVAL_FOR_FAST_TIMER);
					onPlayTimeMS(mFastTimeMS);
					break;
				case MESSAGE_FAST_REWIND :
					mFastTimeMS -= 1000;
					if (0 > mFastTimeMS) {
						mErrorFiles.clear();
						while (!setTrack(mList.getPrevTrack())) {
							if (mErrorFiles.size() >= mList.getTotalCount()) {
								break;
							}
						}
						mFastTimeMS = mPlayer.getDuration() - 1;
						updateMusicInfo(false);
					}
					sendEmptyMessageDelayed(MESSAGE_FAST_REWIND, INTERVAL_FOR_FAST_TIMER);
					onPlayTimeMS(mFastTimeMS);
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
								
								MusicInfo musicInfo = mList.getMusicInfo(playingIndex, true);
								Log.d("[jacob] Title = " + musicInfo.getTitle());
								mService.addRecentTrackList(musicInfo.getTitle());
								
								onMusicInfo(playingIndex, musicInfo);
								onAlbumArt(playingIndex, mList.getAlbumArt(playingIndex, true, false));
								mBitmapThreadRunning = false;
							}
						}).start();
					}
					break;
				default :
					break;
				}
			}
			super.handleMessage(msg);
		}
	}

	// ----------
	// MediaPlayer Listeners
	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if ((MusicUtils.RepeatState.ONE == mRepeat) || (1 == mList.getTotalCount())) {
				playIndex(mList.getPlayingIndex(false), true);
			} else if (MusicUtils.RepeatState.OFF == mRepeat
					&& getPlayingIndex() == mList.getTotalCount() - 1) {
				playNext();
				pause();
				onPlayTimeMS(0);
			} else {
				playNext();
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.w("Music MediaPlayer onError : what = " + what + ", extra = " + extra);
			mService.getCallback().onError(mService.getString(R.string.error_skip_play));
			if (PlayDirection.NEXT == mPlayDirection) {
				playNext();
			} else if (PlayDirection.PREV == mPlayDirection) {
				prevPlay();
			}
			return true;
		}
	};

	// ----------
	// FilePlayer APIs
	public boolean activePlayer() {
		boolean success = false;
		mErrorFiles.clear();
		if (setTrack(mLastTrack)) {
			if (1 == mList.getTotalCount()) {
				setShuffle(MusicUtils.ShuffleState.OFF);
				setRepeat(MusicUtils.RepeatState.ALL);
				setScan(MusicUtils.ScanState.OFF);
			}
			int position = (Integer)mService.loadPreference(MusicUtils.Preference.PLAY_TIME);
			if ((0 > position) || (mPlayer.getDuration() <= position)) {
				position = 0;
			}
			mPlayer.seekTo(position);
			updateMusicInfo(true);
			if (mStart) {
				startPlay();
			} else {
				onPlayState(getPlayState());
			}
			onPlayTimeMS(position);
			success = true;
			Log.d("Player active : Track = " + mLastTrack + ", PlayTime = " + position);
		} else {
			mService.clearPreference();
			if (null == mPlayer) {
				Log.e("mPlayer = null");
			} else {
				int index = 0;
				while (!setTrack(mList.getTrackFromIndex(index, true))) {
					index++;
					if (mErrorFiles.size() >= mList.getTotalCount()) {
						break;
					}
				}
				if (mList.getTotalCount() > index) {
					mPlayer.seekTo(0);
					updateMusicInfo(true);
					if (mStart) {
						startPlay();
					} else {
						onPlayState(getPlayState());
					}
					onPlayTimeMS(0);
					success = true;
					Log.d("Player active : Track = " + mLastTrack + ", PlayTime = 0");
				}
			}
		}
		return success;
	}

	public String getLastTrack() {
		return mLastTrack;
	}

	public void changeStorage() {
		if ((null != mPlayer) && (null != mLastTrack) && (!mLastTrack.isEmpty())) {
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
				mStart = mPlayer.isPlaying();
				stopPlay();
				if (MusicUtils.Category.ALL != (Integer)mService.loadPreference(MusicUtils.Preference.CATEGORY)) {
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
	private int mPlayState = MusicUtils.PlayState.STOP;
	private int mRepeat = MusicUtils.RepeatState.ALL;
	private int mScan = MusicUtils.ScanState.OFF;
	private ArrayList<String> mErrorFiles = new ArrayList<>();
	private int mPlayDirection = PlayDirection.NEXT;

	private boolean setTrack(String track) {
		boolean success = false;
		if (null == mPlayer) {
			mPlayer = new MediaPlayer();
			mPlayer.setLooping(true);
			mPlayer.setOnCompletionListener(mCompletionListener);
			mPlayer.setOnErrorListener(mErrorListener);
			Log.d("mPlayer != null");
		}
		if ((null != track) && (!track.isEmpty())) {
			mPlayer.reset();
			try {
				FileInputStream fis = new FileInputStream(track);
				mPlayer.setDataSource(fis.getFD());
				fis.close();
				mPlayer.prepare();
				mLastTrack = track;
				mService.savePreference(MusicUtils.Preference.PLAY_FILE, mLastTrack);
				success = true;
			} catch (IOException | IllegalStateException | SecurityException | IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.e("track = " + track + ", mPlayer = " + mPlayer);
				e.printStackTrace();
			}
			if (!success) {
				Log.w("setTrack Error : " + track);
				mErrorFiles.add(track);
				if (mErrorFiles.size() >= mList.getTotalCount()) {
					Log.e("setTrack Error : all track error");
				}
			}
		}
		return success;
	}

	private void changePlay() {
		mPlayer.seekTo(0);
		startPlay();
		updateMusicInfo(false);
		Log.d("Start Play : Track = " + mLastTrack + ", Index = " + mList.getPlayingIndex(false));
	}

	private void stopPlay() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_TIME_PROGRESS);
		mPlayer.stop();
		mPlayState = MusicUtils.PlayState.STOP;
	}

	private void startPlay() {
		mPlayer.start();
		mTimerHandler.sendEmptyMessage(mTimerHandler.MESSAGE_TIME_PROGRESS);
		mPlayState = MusicUtils.PlayState.PLAY;
		onPlayState(mPlayState);
	}

	private void pausePlay() {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_TIME_PROGRESS);
		mPlayer.pause();
	}

	private void prevPlay() {
		mErrorFiles.clear();
		mPlayDirection = PlayDirection.PREV;
		while (!setTrack(mList.getPrevTrack())) {
			if (mErrorFiles.size() >= mList.getTotalCount()) {
				break;
			}
		}
		changePlay();
	}

	private void updateMusicInfo(boolean refresh) {
		mTimerHandler.removeMessages(mTimerHandler.MESSAGE_REQUEST_BITMAP);
		Message message = new Message();
		message.what = mTimerHandler.MESSAGE_REQUEST_BITMAP;
		message.arg1 = (refresh) ? 1 : 0;
		mTimerHandler.sendMessage(message);
	}

	private void setShuffle(int shuffle) {
		mList.setShuffle(shuffle);
		onShuffleState(mList.getShuffle());
		mService.savePreference(MusicUtils.Preference.SHUFFLE, mList.getShuffle());
	}

	private void setRepeat(int repeat) {
		mRepeat = repeat;
		onRepeatState(mRepeat);
		mService.savePreference(MusicUtils.Preference.REPEAT, mRepeat);
	}

	private void setScan(int scan) {
		mScan = scan;
		onScanState(mScan);
		mService.savePreference(MusicUtils.Preference.SCAN, mScan);
	}
}
