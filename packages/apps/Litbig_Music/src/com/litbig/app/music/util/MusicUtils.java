package com.litbig.app.music.util;

public class MusicUtils {
	// ----------
	// Definitions
	public static class PlayState {
		public static final int STOP = 0;
		public static final int PLAY = 1;
		public static final int PAUSE = 2;
		public static final int FAST_FORWARD = 3;
		public static final int FAST_REWIND = 4;
	}

	public static class ShuffleState {
		public static final int OFF = 0;
		public static final int ALL = 1;
	}

	public static class RepeatState {
		public static final int OFF = 0;
		public static final int ALL = 1;
		public static final int ONE = 2;
	}

	public static class ScanState {
		public static final int OFF = 0;
		public static final int ALL = 1;
	}

	public static class Category {
		public static final int TOP = -1;
		public static final int NOW_PLAYING = 0;
		public static final int ALL = 1;
		public static final int ARTIST = 2;
		public static final int ALBUM = 3;
		public static final int GENRE = 4;
		public static final int FOLDER = 5;
		public static final int _RECENT = 6;
	}

	public static class ListType extends Category {
		public static final int TRACK = 10;
		public static final int ARTIST_TRACK = 12;
		public static final int ALBUM_TRACK = 13;
		public static final int GENRE_TRACK = 14;
		public static final int FOLDER_TRACK = 15;
	}

	public static class Preference {
		public static final int PLAY_FILE = 1;
		public static final int PLAY_TIME = 2;
		public static final int SHUFFLE = 3;
		public static final int REPEAT = 4;
		public static final int SCAN = 5;
		public static final int CATEGORY = 6;
		public static final int ARTIST_NAME = 7;
		public static final int ALBUM_NAME = 8;
		public static final int GENRE_ID = 9;
		public static final int FOLDER_PATH = 10;
	}
}
