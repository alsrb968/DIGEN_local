package com.litbig.app.photo.activity;

import com.litbig.app.photo.activity.view.PhotoView;
import com.litbig.app.photo.aidl.FileInfo;
import com.litbig.app.photo.aidl.ListInfo;
import com.litbig.app.photo.aidl.PhotoInfo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class PhotoServiceCallbackHandler extends Handler {
	private PhotoView mView;

	public PhotoServiceCallbackHandler(PhotoActivity activity) {
		mView = activity.getView();
	}

	public final int MESSAGE_TOTAL_COUNT = 1001;
	public final int MESSAGE_PLAY_STATE = 1002;
	public final int MESSAGE_PHOTO_INFO = 1003;
	public final int MESSAGE_IMAGE_BITMAP = 1004;
	public final int MESSAGE_SHUFFLE_STATE = 1005;
	public final int MESSAGE_LIST_INFO = 1006;
	public final int MESSAGE_FILE_INFO = 1007;

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_TOTAL_COUNT :
			mView.onTotalCount(msg.arg1);
			break;
		case MESSAGE_PLAY_STATE :
			mView.onPlayState(msg.arg1);
			break;
		case MESSAGE_PHOTO_INFO :
			if (msg.obj instanceof PhotoInfo) {
				mView.onPhotoInfo(msg.arg1, (PhotoInfo)msg.obj);
			}
			break;
		case MESSAGE_IMAGE_BITMAP :
			if (msg.obj instanceof Bitmap) {
				mView.onImageBitmap(msg.arg1, (Bitmap)msg.obj);
			}
			break;
		case MESSAGE_SHUFFLE_STATE :
			mView.onShuffleState(msg.arg1);
			break;
		case MESSAGE_LIST_INFO :
			if (msg.obj instanceof ListInfo) {
				mView.onListInfo((ListInfo)msg.obj);
			}
			break;
		case MESSAGE_FILE_INFO :
			if (msg.obj instanceof FileInfo) {
				mView.onFileInfo(msg.arg1, (FileInfo)msg.obj);
			}
			break;
		default :
			break;
		}
		super.handleMessage(msg);
	}
}
