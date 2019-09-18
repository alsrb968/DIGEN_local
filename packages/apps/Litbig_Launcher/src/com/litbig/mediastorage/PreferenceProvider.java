package com.litbig.mediastorage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PreferenceProvider extends ContentProvider {
    public static final String TAG = "PreferenceProvider";

    public static final String AUTHORITY = "com.litbig.mediastorage";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Preference {
        public static final String MOUNTEDSTORAGE = "MOUNTED_STORAGE";
        public static final String MUSICENABLE = "MUSIC_ENABLE";
        public static final String MOVIEENABLE = "MOVIE_ENABLE";
        public static final String PHOTOENABLE = "PHOTO_ENABLE";
        public static final String ENABLESTORAGE = "ENABLE_STORAGE";
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @NonNull
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.d(TAG, "insert(uri: " + uri.toString() + ")");
        List<String> reqValue = uri.getPathSegments();

        if(reqValue.size() > 0) {
            String preference = reqValue.get(0);
            Log.d(TAG, "preference: " + preference);

            if (preference.equals(Preference.MOUNTEDSTORAGE)) {
                ArrayList<String> al = MediaStorage.getMountedStorage(getContext());
                if (!(al.size() > 0)) return uri;
                String mountedStorage = "";
                for(String s : al) mountedStorage += ("/" + s);
                Log.d(TAG, "mountedStorage: " + mountedStorage);
                return Uri.parse(CONTENT_URI + "/" + preference + mountedStorage);
            } else if (preference.equals(Preference.ENABLESTORAGE)) {
                ArrayList<String> al = MediaStorage.getEnableStorage(getContext());
                if (!(al.size() > 0)) return uri;
                String enableStorage = "";
                for(String s : al) enableStorage += ("/" + s);
                Log.d(TAG, "enableStorage: " + enableStorage);
                return Uri.parse(CONTENT_URI + "/" + preference + enableStorage);
            } else if (preference.equals(Preference.MUSICENABLE)) {
                boolean musicEnable = MediaStorage.isMusicEnable(getContext());
                Log.d(TAG, "musicEnable: " + musicEnable);
                return Uri.parse(CONTENT_URI + "/" + preference + "/" + musicEnable);
            } else if (preference.equals(Preference.MOVIEENABLE)) {
                boolean movieEnable = MediaStorage.isMovieEnable(getContext());
                Log.d(TAG, "movieEnable: " + movieEnable);
                return Uri.parse(CONTENT_URI + "/" + preference + "/" + movieEnable);
            } else if (preference.equals(Preference.PHOTOENABLE)) {
                boolean photoEnable = MediaStorage.isPhotoEnable(getContext());
                Log.d(TAG, "photoEnable: " + photoEnable);
                return Uri.parse(CONTENT_URI + "/" + preference + "/" + photoEnable);
            }
        }

        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,String[] selectionArgs) {
        return 0;
    }
}
