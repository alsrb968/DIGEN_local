package com.litbig.app.music.receiver;

import java.util.ArrayList;

import com.litbig.app.music.util.MusicUtils;
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
					if (!MediaStorage.isMusicEnable(context)) {
						clearPreference(context);
					}
				}
			}
		}
	}

	private void clearPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences("MusicPlayer", Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEdit = pref.edit();
		prefEdit.putString("play_file", "");
		prefEdit.putInt("play_time", 0);
		prefEdit.putInt("shuffle", MusicUtils.ShuffleState.OFF);
		prefEdit.putInt("repeat", MusicUtils.RepeatState.ALL);
		prefEdit.putInt("scan", MusicUtils.ScanState.OFF);
		prefEdit.putInt("category", MusicUtils.Category.ALL);
		prefEdit.putString("sub_category", "");
		prefEdit.apply();
	}
}
