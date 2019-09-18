package com.litbig.app.movie.util;

public class MovieUtils {
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

	public static class Category {
		public static final int TOP = -1;
		public static final int NOW_PLAYING = 0;
		public static final int ALL = 1;
		public static final int FOLDER = 2;
	}

	public static class ListType extends Category {
		public static final int TRACK = 10;
		public static final int FOLDER_TRACK = 12;
	}

	public static class Preference {
		public static final int PLAY_FILE = 1;
		public static final int PLAY_TIME = 2;
		public static final int SHUFFLE = 3;
		public static final int REPEAT = 4;
		public static final int CATEGORY = 5;
		public static final int FOLDER_PATH = 6;
	}

	public static class ScreenScaleType {
		public static final int CENTER_INSIDE = 0;
		public static final int FIT_XY = 1;
	}
}
