package com.litbig.app.photo.receiver;

import java.util.ArrayList;

import com.litbig.app.photo.util.PhotoUtils;
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
					if (!MediaStorage.isPhotoEnable(context)) {
						clearPreference(context);
					}
				}
			}
		}
	}

	private void clearPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences("PhotoPlayer", Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEdit = pref.edit();
		prefEdit.putString("play_file", "");
		prefEdit.putInt("shuffle", PhotoUtils.ShuffleState.OFF);
		prefEdit.putInt("category", PhotoUtils.Category.ALL);
		prefEdit.putString("sub_category", "");
		prefEdit.apply();
	}
}
