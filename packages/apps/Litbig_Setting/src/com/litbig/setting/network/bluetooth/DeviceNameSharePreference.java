package com.litbig.setting.network.bluetooth;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DeviceNameSharePreference {

	public static final String preference_name = "DeviceNameSharePreference";
	public static final String dev_name = "change_dev_name";

	public static boolean commintChangeDevName(Context context, HashMap<String, String> devName) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(preference_name, 0);
		Editor editor = sharedPreferences.edit();
		JSONObject jsonObj = new JSONObject(devName);
		editor.putString(dev_name, jsonObj.toString());
		editor.apply();
		return true;
	}

	public static HashMap<String, String> getChangeDevName(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(preference_name, 0);
		String jsonString = sharedPreferences.getString(dev_name, "");
		HashMap<String, String> outputMap = new HashMap<>();
		if (jsonString.equals(""))
			return new HashMap<>();
		else {
			try {
				JSONObject jsonObject = new JSONObject(jsonString);
				Iterator<String> keysItr = jsonObject.keys();
				while (keysItr.hasNext()) {
					String key = keysItr.next();
					String value = (String) jsonObject.get(key);
					outputMap.put(key, value);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return outputMap;
		}
	}
}
