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
	}
}
