package com.litbig.app.movie.receiver;

import java.util.ArrayList;

import com.litbig.app.movie.util.MovieUtils;
import com.litbig.mediastorage.MediaStorage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StorageEjectReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (null != intent) {
			String intentAction = intent.getAction();
			if (null != intentAction) {
				if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_PREPARED)) {
					ArrayList<String> mountedStorage = MediaStorage.getMountedStorage(context);
					if (0 == mountedStorage.size()) {
						clearPreference(context);
					}
				} else if (intentAction.equals(MediaStorage.INTENT_ACTION_MEDIA_EJECT)) {
					if (!MediaStorage.isMovieEnable(context)) {
						clearPreference(context);
					}
				}
			}
		}
	}

	private void clearPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences("MoviePlayer", Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEdit = pref.edit();
		prefEdit.putString("play_file", "");
		prefEdit.putInt("play_time", 0);
		prefEdit.putInt("shuffle", MovieUtils.ShuffleState.OFF);
		prefEdit.putInt("repeat", MovieUtils.RepeatState.ALL);
		prefEdit.putInt("category", MovieUtils.Category.ALL);
		prefEdit.putString("sub_category", "");
		prefEdit.apply();
	}
}
