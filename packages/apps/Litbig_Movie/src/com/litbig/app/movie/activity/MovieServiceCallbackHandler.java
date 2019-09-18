package com.litbig.app.movie.activity;

import com.litbig.app.movie.activity.view.MovieView;
import com.litbig.app.movie.aidl.ListInfo;
import com.litbig.app.movie.aidl.MovieInfo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class MovieServiceCallbackHandler extends Handler {
	private MovieView mView;

	public MovieServiceCallbackHandler(MovieActivity activity) {
		mView = activity.getView();
	}

	public final int MESSAGE_TOTAL_COUNT = 1001;
	public final int MESSAGE_PLAY_STATE = 1002;
	public final int MESSAGE_PLAY_TIME = 1003;
	public final int MESSAGE_MOVIE_INFO = 1004;
	public final int MESSAGE_VIDEO_THUMBNAIL = 1005;
	public final int MESSAGE_SHUFFLE_STATE = 1006;
	public final int MESSAGE_REPEAT_STATE = 1007;
	public final int MESSAGE_SUBTITLE = 1008;
	public final int MESSAGE_LIST_INFO = 1009;
	public final int MESSAGE_ERROR = 1010;

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_TOTAL_COUNT :
			mView.onTotalCount(msg.arg1);
			break;
		case MESSAGE_PLAY_STATE :
			mView.onPlayState(msg.arg1);
			break;
		case MESSAGE_PLAY_TIME :
			mView.onPlayTimeMS(msg.arg1);
			break;
		case MESSAGE_MOVIE_INFO :
			if (msg.obj instanceof MovieInfo) {
				mView.onMovieInfo(msg.arg1, (MovieInfo)msg.obj);
			}
			break;
		case MESSAGE_VIDEO_THUMBNAIL :
			if (msg.obj instanceof Bitmap) {
				mView.onVideoThumbnail(msg.arg1, (Bitmap)msg.obj);
			}
			break;
		case MESSAGE_SHUFFLE_STATE :
			mView.onShuffleState(msg.arg1);
			break;
		case MESSAGE_REPEAT_STATE :
			mView.onRepeatState(msg.arg1);
			break;
		case MESSAGE_SUBTITLE :
			mView.onSubtitle((String)msg.obj);
			break;
		case MESSAGE_LIST_INFO :
			if (msg.obj instanceof ListInfo) {
				mView.onListInfo((ListInfo)msg.obj);
			}
			break;
		case MESSAGE_ERROR :
			mView.onError((String)msg.obj);
			break;
		default :
			break;
		}
		super.handleMessage(msg);
	}
}
