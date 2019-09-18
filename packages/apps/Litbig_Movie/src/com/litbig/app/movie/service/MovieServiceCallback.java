package com.litbig.app.movie.service;

import com.litbig.app.movie.aidl.IMovieServiceCallback;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;
import com.litbig.app.movie.aidl.MoviePlayerCallbackInterface;

import android.graphics.Bitmap;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

public class MovieServiceCallback<E extends IMovieServiceCallback> extends RemoteCallbackList<E> implements MoviePlayerCallbackInterface {
	private boolean mCallbackProcessing = false;

	private void checkCallbackProcessing() {
		while (mCallbackProcessing) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mCallbackProcessing = true;
	}

	@Override
	public void onTotalCount(int totalCount) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onTotalCount(totalCount);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onPlayState(int playState) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onPlayState(playState);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onPlayTimeMS(int playTimeMS) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onPlayTimeMS(playTimeMS);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onMovieInfo(int index, MovieInfo info) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
			 	IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onMovieInfo(index, info);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onVideoThumbnail(int index, Bitmap videoThumbnail) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onVideoThumbnail(index, videoThumbnail);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onShuffleState(int shuffle) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onShuffleState(shuffle);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onRepeatState(int repeat) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onRepeatState(repeat);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onSubtitle(String text) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onSubtitle(text);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onListInfo(ListInfo info) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
				IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onListInfo(info);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}

	@Override
	public void onError(String error) {
		checkCallbackProcessing();
		try {
			int count = beginBroadcast();
			for (int item = 0; item < count; item++) {
			 	IMovieServiceCallback callback = getBroadcastItem(item);
				if (null != callback) {
					try {
						callback.onError(error);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			finishBroadcast();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mCallbackProcessing = false;
		}
	}
}
