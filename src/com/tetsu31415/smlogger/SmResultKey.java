package com.tetsu31415.smlogger;

public class SmResultKey {

	private String key;
	private boolean enable;
	
	public SmResultKey(String key, boolean enable) {
		this.key = key;
		this.enable = enable;
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean isEnable() {
		return enable;
	}
	
	public void toggleEnable() {
		this.enable = !enable;
	}
	
}
