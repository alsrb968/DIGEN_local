package com.litbig.app.music.activity;

import com.litbig.app.music.activity.MusicActivity;
import com.litbig.app.music.aidl.IMusicServiceCallback;
import com.litbig.app.music.aidl.ListInfo;
import com.litbig.app.music.aidl.MusicInfo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

public class MusicServiceCallbackStub extends IMusicServiceCallback.Stub {
	private MusicActivity mActivity;

	public MusicServiceCallbackStub(MusicActivity activity) {
		mActivity = activity.getActivity();
	}

	@Override
	public void onTotalCount(int totalCount) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_TOTAL_COUNT;
		message.arg1 = totalCount;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPlayState(int playState) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_PLAY_STATE;
		message.arg1 = playState;
		mHandler.sendMessage(message);
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_PLAY_TIME;
		message.arg1 = playTimeMS;
		mHandler.sendMessage(message);
	}

	@Override
	public void onMusicInfo(int index, MusicInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_MUSIC_INFO;
		message.arg1 = index;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onAlbumArt(int index, Bitmap albumArt) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_ALBUM_ART;
		message.arg1 = index;
		message.obj = albumArt;
		mHandler.sendMessage(message);
	}

	@Override
	public void onShuffleState(int shuffle) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_SHUFFLE_STATE;
		message.arg1 = shuffle;
		mHandler.sendMessage(message);
	}

	@Override
	public void onRepeatState(int repeat) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_REPEAT_STATE;
		message.arg1 = repeat;
		mHandler.sendMessage(message);
	}

	@Override
	public void onScanState(int scan) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_SCAN_STATE;
		message.arg1 = scan;
		mHandler.sendMessage(message);
	}

	@Override
	public void onListState(int listState) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_LIST_STATE;
		message.arg1 = listState;
		mHandler.sendMessage(message);
	}

	@Override
	public void onListInfo(ListInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_LIST_INFO;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onError(String error) throws RemoteException {
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
			switch (msg.what) {
			case MESSAGE_TOTAL_COUNT :
				mActivity.onTotalCount(msg.arg1);
				break;
			case MESSAGE_PLAY_STATE :
				mActivity.onPlayState(msg.arg1);
				break;
			case MESSAGE_PLAY_TIME :
				mActivity.onPlayTimeMS(msg.arg1);
				break;
			case MESSAGE_MUSIC_INFO :
				if (msg.obj instanceof MusicInfo) {
					mActivity.onMusicInfo(msg.arg1, (MusicInfo)msg.obj);
				}
				break;
			case MESSAGE_ALBUM_ART :
				if ((msg.obj instanceof Bitmap) || (null == msg.obj)) {
					mActivity.onAlbumArt(msg.arg1, (Bitmap)msg.obj);
				}
				break;
			case MESSAGE_SHUFFLE_STATE :
				mActivity.onShuffleState(msg.arg1);
				break;
			case MESSAGE_REPEAT_STATE :
				mActivity.onRepeatState(msg.arg1);
				break;
			case MESSAGE_SCAN_STATE :
				mActivity.onScanState(msg.arg1);
				break;
			case MESSAGE_LIST_STATE :
				mActivity.onListState(msg.arg1);
				break;
			case MESSAGE_LIST_INFO :
				if (msg.obj instanceof ListInfo) {
					mActivity.onListInfo((ListInfo)msg.obj);
				}
				break;
			case MESSAGE_ERROR :
				mActivity.onError((String)msg.obj);
				break;
			default :
				break;
			}
			super.handleMessage(msg);
		}
	};
}
