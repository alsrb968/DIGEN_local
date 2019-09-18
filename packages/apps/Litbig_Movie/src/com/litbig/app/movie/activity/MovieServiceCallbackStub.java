package com.litbig.app.movie.activity;

import com.litbig.app.movie.aidl.IMovieServiceCallback;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;

import android.graphics.Bitmap;
import android.os.Message;
import android.os.RemoteException;

public class MovieServiceCallbackStub extends IMovieServiceCallback.Stub {
	private MovieServiceCallbackHandler mHandler;

	public MovieServiceCallbackStub(MovieActivity activity) {
		mHandler = activity.getCallbackHandler();
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
	public void onMovieInfo(int index, MovieInfo info) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_MOVIE_INFO;
		message.arg1 = index;
		message.obj = info;
		mHandler.sendMessage(message);
	}

	@Override
	public void onVideoThumbnail(int index, Bitmap videoThumbnail) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_VIDEO_THUMBNAIL;
		message.arg1 = index;
		message.obj = videoThumbnail;
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
	public void onSubtitle(String text) throws RemoteException {
		Message message = new Message();
		message.what = mHandler.MESSAGE_SUBTITLE;
		message.obj = text;
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
}
