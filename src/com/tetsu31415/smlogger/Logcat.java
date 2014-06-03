package com.tetsu31415.smlogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Logcat {
	private final String TAG = "smlogger";
	private boolean isRuninng = false;
	private Handler mHandler;
	
	public Logcat(Handler handler) {
		this.mHandler = handler;
	}
	
	public void start(){
		isRuninng = true;		
		try {
			Runtime.getRuntime().exec("adb -s 127.0.0.1:5555 shell logcat -c");
			Process process = Runtime.getRuntime().exec(
					"adb -s 127.0.0.1:5555 shell logcat -v raw ServiceModeApp_RIL:I ServiceMode:I *:S"
					);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			List<SmResult> resultList = new ArrayList<SmResult>();
			
			List<String> memoSet = new ArrayList<String>();
			
			String line;
			while(isRuninng && (line = reader.readLine()) != null){
				if (line.length()==0) {
					continue;
				}				
				if (line.contains("Line")) {
					parseLine(line.substring(line.indexOf(":")+1, line.length()-1),
							resultList,memoSet);
				}
				if (line.contains("Update")) {
					sendSmItem(resultList,memoSet);
				}
			}	
			reader.close();
			process.waitFor();			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void stop(){
		isRuninng = false;
	}
	
	private void parseLine(String line, List<SmResult> resultList, List<String> memoList){
		String regex = ":[ ]*([^, ]+)[ ]+([A-Z]+)[ ]*:";
		Pattern pattern = Pattern.compile(regex);
		while(pattern.matcher(line).find()){
			line = line.replaceFirst(regex, ":$1,$2:");
		}
		
		String[] results = line.split(",");
		String group = new String();
		for(int i=0; i<results.length; i++){
			String[] tmp = results[i].split(":");
			String key = tmp[0].trim();
			if (key.length()==0) {
				continue;
			}
			if (tmp.length ==2) {
				if (i==0) {
					String[] dividedKey = key.split(" ");
					if (dividedKey.length>=2) {
						group = dividedKey[0];
					}
				}else {
					if (group.length()!=0) {
						key = group + " " + key;
					}
				}
				resultList.add(new SmResult(key, tmp[1].trim()));
				Log.v(TAG, key+":"+tmp[1].trim());
			}else if (tmp.length==1) {
				memoList.add(key);
			}else {
				resultList.add(new SmResult(key,
					results[i].substring(results[i].indexOf(":")+1).trim()));
			}
		}
	}
	
	private void sendSmItem(List<SmResult> resultList, List<String> memoList) {
		SmLog smItem = new SmLog();
		smItem.setResultList(new ArrayList<SmResult>(resultList));
		smItem.setMemoList(new ArrayList<String>(memoList));
		Message message = Message.obtain(mHandler, 0);
		message.obj = smItem;
		mHandler.sendMessage(message);
		resultList.clear();
		memoList.clear();
	}	
}
