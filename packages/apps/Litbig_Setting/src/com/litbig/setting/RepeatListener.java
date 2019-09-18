package com.litbig.setting;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class RepeatListener implements OnTouchListener{

	private Handler mHandler = new Handler();
	private View mDownView;

	private int mInitialInterval;

	private final int mNormalInterval;

	private final OnClickListener mClickListener;

	private Runnable handlerRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.postDelayed(this, mNormalInterval);
			mClickListener.onClick(mDownView);
		}
	};

	/**
	 * @param initialInterval The interval after first click event
	 * @param normalInterval The interval after second and subsequent click
	 *       events
	 * @param clickListener The OnClickListener, that will be called
	 *       periodically
	 */
	public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener) {

		if (clickListener == null)

			throw new IllegalArgumentException("null runnable");

		if (initialInterval < 0 || normalInterval < 0)

			throw new IllegalArgumentException("negative interval");

		this.mInitialInterval = initialInterval;
		this.mNormalInterval = normalInterval;
		this.mClickListener = clickListener;

	}



	public boolean onTouch(View view, MotionEvent motionEvent) {

		switch (motionEvent.getAction()) {

		case MotionEvent.ACTION_DOWN:
			mHandler.removeCallbacks(handlerRunnable);
			mHandler.postDelayed(handlerRunnable, mInitialInterval);
			mDownView = view;
			mClickListener.onClick(view);
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mHandler.removeCallbacks(handlerRunnable);
			mDownView = null;
			break;

		}

		return false;
	}
}
