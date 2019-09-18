package com.litbig.app.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaButtonReceiver extends BroadcastReceiver {
	public static final String ACTION_MEDIA_BUTTON = "com.litbig.app.music.intent.action.MEDIA_BUTTON";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (null != intent) {
			String intentAction = intent.getAction();
			if (null != intentAction) {
				if (intentAction.equals(Intent.ACTION_MEDIA_BUTTON)) {
					Intent sendIntent = new Intent(ACTION_MEDIA_BUTTON);
					sendIntent.putExtras(intent.getExtras());
					context.sendBroadcast(sendIntent);
				}
			}
		}
	}
}
