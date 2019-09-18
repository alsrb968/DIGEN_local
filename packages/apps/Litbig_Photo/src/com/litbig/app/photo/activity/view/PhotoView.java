package com.litbig.app.photo.activity.view;

import java.io.UnsupportedEncodingException;

import com.litbig.app.photo.activity.PhotoActivity;
import com.litbig.app.photo.aidl.PhotoPlayerCallbackInterface;
import com.litbig.app.photo.aidl.PhotoPlayerInterface;
import com.litbig.app.photo.util.PhotoUtils;

import android.graphics.Bitmap;
import android.os.RemoteException;

public abstract class PhotoView implements PhotoPlayerInterface, PhotoPlayerCallbackInterface {
	protected PhotoActivity mActivity;

	public PhotoView(PhotoActivity activity) {
		mActivity = activity;
	}

	public abstract void onCreate();
	public abstract void onResume();
	public abstract void onPause();
	public abstract void onDestroy();
	public abstract void onBackPressed();
	public abstract void onServiceConnected();
	public abstract void onServiceDisconnected();
	public abstract void onMediaScan(boolean scanning);

	// ----------
	// PhotoService APIs
	@Override
	public int getPlayingIndex() {
		int index = -1;
		if (null != mActivity.getService()) {
			try {
				index = mActivity.getService().getPlayingIndex();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	@Override
	public String getFileFullPath(int index, boolean isNowPlaying) {
		String file = null;
		if (null != mActivity.getService()) {
			try {
				file = mActivity.getService().getFileFullPath(index, isNowPlaying);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	@Override
	public int getPlayState() {
		int playState = PhotoUtils.PlayState.PAUSE;
		if (null != mActivity.getService()) {
			try {
				playState = mActivity.getService().getPlayState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return playState;
	}

	@Override
	public int getPlayIntervalMS() {
		int playInterval = 0;
		if (null != mActivity.getService()) {
			try {
				playInterval = mActivity.getService().getPlayIntervalMS();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return playInterval;
	}

	@Override
	public void setPlayIntervalMS(int intervalMS) {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().setPlayIntervalMS(intervalMS);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void play() {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().play();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void pause() {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().pause();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean playIndex(int index, boolean isNowPlaying) {
		boolean success = false;
		if (null != mActivity.getService()) {
			try {
				success = mActivity.getService().playIndex(index, isNowPlaying);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	@Override
	public void playPrev() {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().playPrev();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void playNext() {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().playNext();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getShuffle() {
		int shuffle = PhotoUtils.ShuffleState.OFF;
		if (null != mActivity.getService()) {
			try {
				shuffle = mActivity.getService().getShuffle();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return shuffle;
	}

	@Override
	public void setShuffle() {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().setShuffle();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void requestList(int listType, String subCategory) {
		if (null != mActivity.getService()) {
			try {
				mActivity.getService().requestList(listType, subCategory);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Bitmap getImageBitmap(int index, boolean isNowPlaying, boolean isScale) {
		Bitmap imageBitmap = null;
		if (null != mActivity.getService()) {
			try {
				imageBitmap = mActivity.getService().getImageBitmap(index, isNowPlaying, isScale);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return imageBitmap;
	}

	@Override
	public int getNowPlayingCategory() {
		int category = PhotoUtils.Category.ALL;
		if (null != mActivity.getService()) {
			try {
				category = mActivity.getService().getNowPlayingCategory();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return category;
	}

	@Override
	public String getNowPlayingSubCategory() {
		String subCategory = null;
		if (null != mActivity.getService()) {
			try {
				subCategory = mActivity.getService().getNowPlayingSubCategory();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return subCategory;
	}

	@Override
	public boolean isNowPlayingCategory(int category, String subCategory) {
		boolean nowPlaying = false;
		if (null != mActivity.getService()) {
			try {
				nowPlaying = mActivity.getService().isNowPlayingCategory(category, subCategory);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return nowPlaying;
	}

	// ----------
	// PhotoView internal functions
	private String getCharset(String s) {
		byte[] BOM = s.getBytes();
		if (4 <= BOM.length) {
			if (((BOM[0] & 0xFF) == 0xEF) && ((BOM[1] & 0xFF) == 0xBB) && ((BOM[2] & 0xFF) == 0xBF)) {
				return "UTF-8";
			} else if (((BOM[0] & 0xFF) == 0xFE) && ((BOM[1] & 0xFF) == 0xFF)) {
				return "UTF-16BE";
			} else if (((BOM[0] & 0xFF) == 0xFF) && ((BOM[1] & 0xFF) == 0xFE)) {
				return "UTF-16LE";
			} else if (((BOM[0] & 0xFF) == 0x00) && ((BOM[1] & 0xFF) == 0x00) && ((BOM[0] & 0xFF) == 0xFE) && ((BOM[1] & 0xFF) == 0xFF)) {
				return "UTF-32BE";
			} else if (((BOM[0] & 0xFF) == 0xFF) && ((BOM[1] & 0xFF) == 0xFE) && ((BOM[0] & 0xFF) == 0x00) && ((BOM[1] & 0xFF) == 0x00)) {
				return "UTF-32LE";
			} else {
				String convertStr = null;
				try {
					convertStr = new String(s.getBytes("ISO-8859-1"), "ISO-8859-1");
					if (s.equals(convertStr)) {
						return "ISO-8859-1";
					}
					convertStr = new String(s.getBytes("EUC-KR"), "EUC-KR");
					if (s.equals(convertStr)) {
						return "EUC-KR";
					}
					convertStr = new String(s.getBytes("KSC5601"), "KSC5601");
					if (s.equals(convertStr)) {
						return "KSC5601";
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return "NONE";
	}

	private String setCharset(String value, String charset) {
		if (charset.equals("UTF-8")) {
			try {
				value = new String(value.getBytes(charset), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			try {
				value = new String(value.getBytes(charset), "EUC-KR");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	protected String setCharset(String value) {
		if ((null != value) && (!value.isEmpty())) {
			String charset = getCharset(value);
			if (!charset.equals("NONE")) {
				value = setCharset(value, charset);
			}
		}
		return value;
	}
}
