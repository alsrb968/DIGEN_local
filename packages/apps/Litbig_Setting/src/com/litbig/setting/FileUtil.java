package com.litbig.setting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import android.util.Log;

public class FileUtil {
	
	public static boolean isFile(File file) {
		
		if (file != null && file.exists() && file.isFile())
			return true;
		
		return false;
	}
	
	public static boolean isDirectory(File dir) {
		
		if (dir != null && dir.isDirectory())
			return true;
		
		return false;
	}
	
	public static boolean deleteFile(File file) {
		
		if (isFile(file)) {
			file.delete();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * writing content to file
	 * @param file
	 * @param content
	 * @return true if succeed in writing content to file, 
	 * 		or false if file is not exist or content is null
	 */
	public static boolean writeFile(File file, String content) {
			
		if (isFile(file) && content != null) {
			
			try {
				BufferedWriter bufferdWriter = new BufferedWriter(new FileWriter(file));
				
				try {
					bufferdWriter.write(content);
					bufferdWriter.flush();
					bufferdWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * reading the file
	 * @param file
	 * @return byte array if succeed in reading the file,
	 * 		or null if file is not exist.
	 */
	public static String readFile(File file) {

		StringBuilder content = new StringBuilder();
		String line;
		
		if (isFile(file)) {
			
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				
				while((line = bufferedReader.readLine()) != null)
					content.append(line);
				
				bufferedReader.close();
				
				return content.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
