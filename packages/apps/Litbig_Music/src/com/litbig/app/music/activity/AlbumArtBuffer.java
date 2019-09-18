package com.litbig.app.music.activity;

//import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.util.SparseArray;

public class AlbumArtBuffer {
	private final int MAX_SIZE = 20;
//	private HashMap<Integer, Bitmap> mCacheMap;
	private SparseArray<Bitmap> mCacheMap;
	private Queue<Integer> mQueue;

	public AlbumArtBuffer() {
//		mCacheMap = new HashMap<Integer, Bitmap>();
		mCacheMap = new SparseArray<>();
		mQueue = new LinkedList<>();
	}

	public void release() {
		clear();
		mCacheMap = null;
		mQueue = null;
	}

	public void clear() {
		if ((null != mQueue) && (0 < mQueue.size())) {
			mQueue.clear();
		}
		if (null != mCacheMap) {
			for (int index = (mCacheMap.size() - 1); index >= 0; index--) {
				Bitmap bitmap = mCacheMap.get(index);
				if (null != bitmap) {
					if (!bitmap.isRecycled()) {
						bitmap.recycle();
					}
					bitmap = null;
				}
			}
			mCacheMap.clear();
		}
	}

	public synchronized void add(int index, Bitmap bitmap) {
//		if ((null != mCacheMap) && (mCacheMap.containsKey(index))) {
		if ((null != mCacheMap) && (0 <= mCacheMap.indexOfKey(index))) {
			if ((null != bitmap) && (null == mCacheMap.get(index))) {
				updateQueue(index, bitmap);
			} else {
				updateQueue(index);
			}
		} else {
			addCache(index, bitmap);
		}
	}

	public synchronized Bitmap get(int index) {
		Bitmap bitmap = null;
		if (null != mCacheMap) {
			bitmap = mCacheMap.get(index);
		}
		if ((null != bitmap) && (bitmap.isRecycled())) {
			mQueue.remove(index);
			mCacheMap.remove(index);
			bitmap = null;
		}
		return bitmap;
	}

	private void addCache(int index, Bitmap bitmap) {
		if (MAX_SIZE <= mCacheMap.size()) {
			int topIndex = mQueue.remove();
			mCacheMap.remove(topIndex);
		}
		mCacheMap.put(index, bitmap);
		mQueue.add(index);
	}

	private void updateQueue(int index) {
		mQueue.remove(index);
		mQueue.add(index);
	}

	private void updateQueue(int index, Bitmap bitmap) {
		mQueue.remove(index);
		mQueue.add(index);
		mCacheMap.remove(index);
		mCacheMap.put(index, bitmap);
	}
}
