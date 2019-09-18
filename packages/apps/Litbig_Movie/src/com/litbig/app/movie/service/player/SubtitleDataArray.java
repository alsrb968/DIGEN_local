package com.litbig.app.movie.service.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.text.Html;

public class SubtitleDataArray {
	private ArrayList<SubtitleData> mList = null;

	public SubtitleDataArray(String videoPath) {
		File subtitleFile = new File(videoPath.substring(0, videoPath.lastIndexOf(".")) + ".smi");
		if ((subtitleFile.isFile()) && (subtitleFile.canRead())) {
			makeSmiDataList(subtitleFile);
		} else {
			subtitleFile = new File(videoPath.substring(0, videoPath.lastIndexOf(".")) + ".srt");
			if ((subtitleFile.isFile()) && (subtitleFile.canRead())) {
				makeSrtDataList(subtitleFile);
			}
		}
	}

	// ----------
	// SmiData
	private class SubtitleData {
		int mTime;
		String mText;

		public SubtitleData(int time, String text) {
			mTime = time;
			mText = text;
		}

		public int getTime() {
			return mTime;
		}

		public String getText() {
			return Html.fromHtml(mText).toString();
		}
	}

	// ----------
	// SmiDataList APIs
	private boolean mValid = false;
	private int mCurrentIndex = -1;

	public boolean isValid() {
		return mValid;
	}

	public int getCurrentIndex() {
		return mCurrentIndex;
	}

	public void setCurrentIndex(int index) {
		mCurrentIndex = index;
	}

	public int getIndex(long playTime) {
		int lastIndex = mList.size();
		int front = 0;
		int rear = lastIndex;
		int mid;
		int next;
		int index = -1;
		while (front <= rear) {
			mid = (front + rear) / 2;
			next = mid + 1;
			if (lastIndex <= next) {
				if (mList.get(mid).getTime() <= playTime) {
					index = mid;
				}
				break;
			} else {
				if (mList.get(mid).getTime() <= playTime) {
					if (lastIndex <= next || playTime < mList.get(next).getTime()) {
						index = mid;
						break;
					}
				}
				if (playTime > mList.get(next).getTime()) {
					front = mid + 1;
				} else {
					rear = mid - 1;
				}
			}
		}
		return index;
	}

	public int getTime(int index) {
		if (null != mList && 0 <= index && index < mList.size()) {
			return mList.get(index).getTime();
		}
		return 0;
	}

	public String getText(int index) {
		if (null != mList && 0 <= index && index < mList.size()) {
			return mList.get(index).getText();
		}
		return "";
	}

	// ----------
	// SmiDataList internal functions
	private String checkCodeSet(File smiFile) {
		String codeSet = "UTF-8";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(smiFile.toString())), "UTF-8"));
			String s = in.readLine();
			if (null != s) {
				byte[] BOM = s.getBytes();
				if (2 > BOM.length) {
					codeSet = "MS949";
				} else if (((BOM[0] & 0xFF) == 0xEF) && ((BOM[1] & 0xFF) == 0xBB) && ((2 < BOM.length) && ((BOM[2] & 0xFF) == 0xBF))) {
					codeSet = "UTF-8";
				} else if (((BOM[0] & 0xFF) == 0xFE) && ((BOM[1] & 0xFF) == 0xFF)) {
					codeSet = "UTF-16BE";
				} else if (((BOM[0] & 0xFF) == 0xFF) && ((BOM[1] & 0xFF) == 0xFE)) {
					codeSet = "UTF-16LE";
				} else if (((BOM[0] & 0xFF) == 0x00) && ((BOM[1] & 0xFF) == 0x00) && ((BOM[0] & 0xFF) == 0xFE) && ((BOM[1] & 0xFF) == 0xFF)) {
					codeSet = "UTF-32BE";
				} else if (((BOM[0] & 0xFF) == 0xFF) && ((BOM[1] & 0xFF) == 0xFE) && ((BOM[0] & 0xFF) == 0x00) && ((BOM[1] & 0xFF) == 0x00)) {
					codeSet = "UTF-32LE";
				} else {
					codeSet = "MS949";
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return codeSet;
	}

	private void makeSmiDataList(File subtitleFile) {
		mList = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(subtitleFile.toString())), checkCodeSet(subtitleFile)));
			String s;
			boolean smiStart = false;
			int time = -1;
			StringBuilder text = null;
			while (null != (s = in.readLine())) {
				if (s.contains("<SYNC")) {
					smiStart = true;
					if (-1 != time) {
						mList.add(new SubtitleData(time, text.toString()));
					}
					time = Integer.parseInt(s.substring(s.indexOf("=") + 1, s.indexOf(">")));
					text = new StringBuilder(s.substring(s.indexOf(">") + 1, s.length()));
					text = new StringBuilder(text.substring(text.indexOf(">") + 1, text.length()));
				} else {
					if (smiStart) {
						text.append(s);
					}
				}
			}
			in.close();
			if (0 < mList.size()) {
				mValid = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void makeSrtDataList(File subtitleFile) {
		mList = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(subtitleFile.toString())), checkCodeSet(subtitleFile)));
			String s;
			boolean srtStart = false;
			int startTime = -1;
			int endTime = -1;
			String text = "";
			while (null != (s = in.readLine())) {
				if (s.contains("-->")) {
					startTime = (Integer.parseInt(s.substring(1, 2)) * 3600
						+ Integer.parseInt(s.substring(3, 5)) * 60
						+ Integer.parseInt(s.substring(6, 8))) * 1000
						+ Integer.parseInt(s.substring(9, 12));
					endTime = (Integer.parseInt(s.substring(17, 19)) * 3600
						+ Integer.parseInt(s.substring(20, 22)) * 60
						+ Integer.parseInt(s.substring(23, 25))) * 1000
						+ Integer.parseInt(s.substring(26, 29));
					srtStart = true;
				} else {
					if (s.equals("")) {
						text = text.replaceAll("<br><br>", "<br>");
						mList.add(new SubtitleData(startTime, text));
						text = "";
						mList.add(new SubtitleData(endTime, text));
						srtStart = false;
					} else {
						if (srtStart) {
							if (text.equals("")) {
								text += s;
							} else {
								text += ("<br>" + s);
							}
						}
					}
				}
			}
			in.close();
			if (0 < mList.size()) {
				mValid = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
