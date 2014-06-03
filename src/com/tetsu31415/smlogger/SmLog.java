package com.tetsu31415.smlogger;

import java.util.Date;
import java.util.List;

public class SmLog {

	private long time;
	private double latitude = -1.0;
	private double longitude = -1.0;
	private int lac = -1;
	private int cid = -1;

	private List<SmResult> resultList;
	private List<String> memoList;
	
	public SmLog() {
		this.time = new Date().getTime();
	}
	
	public SmLog(long time, double latitude, double longitude, int lac, int cid){
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.lac = lac;
		this.cid = cid;
	}
	
	public long getTime() {
		return time;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public int getLac() {
		return lac;
	}
	
	public int getCid() {
		return cid;
	}
	
	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public void setCellInfo(int lac, int cid) {
		this.lac = lac;
		this.cid = cid;
	}
	
	public void setMemoList(List<String> memoSet) {
		this.memoList = memoSet;
	}
	
	public List<String> getMemoList() {
		return memoList;
	}
	
	public void setResultList(List<SmResult> resultList) {
		this.resultList = resultList;
	}
	
	public List<SmResult> getResultList() {
		return resultList;
	}
}
