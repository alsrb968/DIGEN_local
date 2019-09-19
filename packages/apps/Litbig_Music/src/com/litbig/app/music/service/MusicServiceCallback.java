package com.litbig.app.music.service;

import com.litbig.app.music.aidl.IMusicServiceCallback;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;
import com.litbig.app.music.aidl.MusicPlayerCallbackInterface;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class MusicServiceCallback<E extends IMusicServiceCallback> extends RemoteCallbackList<E> implements MusicPlayerCallbackInterface {
	@Override
	public void onTotalCount(int totalCount) {
		mHandler.removeMessages(mHandler.MESSAGE_TOTAL_COUNT);
		Message message = new Message();
		message.what = mHandler.MESSAGE_TOTAL_COUNT;
		message.arg1 = totalCount;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPlayState(int playState) {
		mHandler.removeMessages(mHandler.MESSAGE_PLAY_STATE);
		Message message = new Message();
		message.what = mHandler.MESSAGE_PLAY_STATE;
		message.arg1 = playState;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) {
		mHandler.removeMessages(mHandler.MESSAGE_PLAY_TIME);
		Message message = new Message();
		message.what = mHandler.MESSAGE_PLAY_TIME;
		message.arg1 = playTimeMS;
		mHandler.sendMessage(message);
	}

	@Override
	public void onMusicInfo(int index, MusicInfo info) {
		mHandler.removeMessages(mHandler.MESSAGE_MUSIC_INFO);
		Message message = new Message();
		message.what = mHandler.MESSAGE_MUSIC_INFO;
		message.arg1 = index;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onAlbumArt(int index, Bitmap albumArt) {
		mHandler.removeMessages(mHandler.MESSAGE_ALBUM_ART);
		Message message = new Message();
		message.what = mHandler.MESSAGE_ALBUM_ART;
		message.arg1 = index;
		message.obj = albumArt;
		mHandler.sendMessage(message);
	}

	@Override
	public void onShuffleState(int shuffle) {
		mHandler.removeMessages(mHandler.MESSAGE_SHUFFLE_STATE);
		Message message = new Message();
		message.what = mHandler.MESSAGE_SHUFFLE_STATE;
		message.arg1 = shuffle;
		mHandler.sendMessage(message);
	}

	@Override
	public void onRepeatState(int repeat) {
		mHandler.removeMessages(mHandler.MESSAGE_REPEAT_STATE);
		Message message = new Message();
		message.what = mHandler.MESSAGE_REPEAT_STATE;
		message.arg1 = repeat;
		mHandler.sendMessage(message);
	}

	@Override
	public void onScanState(int scan) {
		mHandler.removeMessages(mHandler.MESSAGE_SCAN_STATE);
		Message message = new Message();
		message.what = mHandler.MESSAGE_SCAN_STATE;
		message.arg1 = scan;
		mHandler.sendMessage(message);
	}

	@Override
	public void onListState(int listState) {
		mHandler.removeMessages(mHandler.MESSAGE_LIST_STATE);
		Message message = new Message();
		message.what = mHandler.MESSAGE_LIST_STATE;
		message.arg1 = listState;
		mHandler.sendMessage(message);
	}

	@Override
	public void onListInfo(ListInfo info) {
		mHandler.removeMessages(mHandler.MESSAGE_LIST_INFO);
		Message message = new Message();
		message.what = mHandler.MESSAGE_LIST_INFO;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onError(String error) {
		mHandler.removeMessages(mHandler.MESSAGE_ERROR);
		Message message = new Message();
		message.what = mHandler.MESSAGE_ERROR;
		message.obj = error;
		mHandler.sendMessage(message);
	}

	private CallbackHandler mHandler = new CallbackHandler();

	private class CallbackHandler extends Handler {
		public final int MESSAGE_TOTAL_COUNT = 1001;
		public final int MESSAGE_PLAY_STATE = 1002;
		public final int MESSAGE_PLAY_TIME = 1003;
		public final int MESSAGE_MUSIC_INFO = 1004;
		public final int MESSAGE_ALBUM_ART = 1005;
		public final int MESSAGE_SHUFFLE_STATE = 1006;
		public final int MESSAGE_REPEAT_STATE = 1007;
		public final int MESSAGE_SCAN_STATE = 1008;
		public final int MESSAGE_LIST_STATE = 1009;
		public final int MESSAGE_LIST_INFO = 1010;
		public final int MESSAGE_ERROR = 1011;

		@Override
		public void handleMessage(Message msg) {
			try {
				int count = beginBroadcast();
				for (int item = 0; item < count; item++) {
				 	IMusicServiceCallback callback = getBroadcastItem(item);
					if (null != callback) {
						try {
							switch (msg.what) {
							case MESSAGE_TOTAL_COUNT :
								callback.onTotalCount(msg.arg1);
								break;
							case MESSAGE_PLAY_STATE :
								callback.onPlayState(msg.arg1);
								break;
							case MESSAGE_PLAY_TIME :
								callback.onPlayTimeMS(msg.arg1);
								break;
							case MESSAGE_MUSIC_INFO :
								if (msg.obj instanceof MusicInfo) {
									callback.onMusicInfo(msg.arg1, (MusicInfo)msg.obj);
								}
								break;
							case MESSAGE_ALBUM_ART :
								if ((msg.obj instanceof Bitmap) || (null == msg.obj)) {
									callback.onAlbumArt(msg.arg1, (Bitmap)msg.obj);
								}
								break;
							case MESSAGE_SHUFFLE_STATE :
								callback.onShuffleState(msg.arg1);
								break;
							case MESSAGE_REPEAT_STATE :
								callback.onRepeatState(msg.arg1);
								break;
							case MESSAGE_SCAN_STATE :
								callback.onScanState(msg.arg1);
								break;
							case MESSAGE_LIST_STATE :
								callback.onListState(msg.arg1);
								break;
							case MESSAGE_LIST_INFO :
								if (msg.obj instanceof ListInfo) {
									callback.onListInfo((ListInfo)msg.obj);
								}
								break;
							case MESSAGE_ERROR :
								callback.onError((String)msg.obj);
								break;
							default :
								break;
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
				finishBroadcast();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			super.handleMessage(msg);
		}
	}
}
