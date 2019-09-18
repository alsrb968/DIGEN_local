package com.litbig.systemmanager;

public class SystemKey {
	public static final String INTENT_ACTION_SYSTEM_KEY_CLICK = "com.litbig.intent.action.SYSTEM_KEY_CLICK";
	public static final String INTENT_EXTRA_SYSTEM_KEY_CODE = "com.litbig.intent.extra.SYSTEM_KEY_CODE";

	public static class KeyCode {
		public static final byte PLAY = (byte)0x00;
		public static final byte PAUSE = (byte)0x01;
		public static final byte PREV = (byte)0x02;
		public static final byte NEXT = (byte)0x03;
	}
}
