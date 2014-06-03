package com.tetsu31415.smlogger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.PrivateCredentialPermission;

import android.R.color;
import android.R.integer;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.StringSplitter;
import android.util.Log;

public class SmDBHelper extends SQLiteOpenHelper{
	
	private final String TAG = "smlogger";
	
	private final String TABLE_LOG= "log_table";
	private final String TABLE_RESULT = "result_table";
	private final String TABLE_MEMO = "memo_table";
	private final String TABLE_KEY = "key_table";
	
	private final String COLUMN_TIME = "time";
	private final String COLUMN_LATITUDE = "lat";
	private final String COLUMN_LONGITUDE ="lon";
	private final String COLUMN_LAC = "lac";
	private final String COLUMN_CID = "cid";
	private final String COLUMN_KEY = "key";
	private final String COLUMN_VALUE = "value";
	private final String COLUMN_ENABLE = "enable";
	private final String COLUME_MEMO = "memo";
	
	public SmDBHelper(Context context) {
		super(context,"sm_log.db", null , 2);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create item_table
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(TABLE_LOG);
		sb.append(" (");
		sb.append("_id integer primary key autoincrement not null,");
		sb.append(COLUMN_TIME).append(",");
		sb.append(COLUMN_LATITUDE).append(",");
		sb.append(COLUMN_LONGITUDE).append(",");
		sb.append(COLUMN_LAC).append(" integer,");
		sb.append(COLUMN_CID).append(" integer");
		sb.append(") ;");
		db.execSQL(sb.toString());
		
		//Create log_table
		sb = new StringBuilder();
		sb.append("create table ");
		sb.append(TABLE_RESULT);
		sb.append(" (");
		sb.append("_id integer primary key autoincrement not null,");
		sb.append(COLUMN_TIME).append(" not null,");
		sb.append(COLUMN_KEY).append(" text not null,");
		sb.append(COLUMN_VALUE);
		sb.append(") ;");
		db.execSQL(sb.toString());
		
		//Create memo_table
		sb = new StringBuilder();
		sb.append("create table ");
		sb.append(TABLE_MEMO);
		sb.append(" (");
		sb.append("_id integer primary key autoincrement not null,");
		sb.append(COLUMN_TIME).append(" not null,");
		sb.append(COLUME_MEMO).append(" not null");
		sb.append(") ;");
		db.execSQL(sb.toString());
		
		//Create key_table
		sb = new StringBuilder();
		sb.append("create table ");
		sb.append(TABLE_KEY);
		sb.append(" (");
		sb.append("_id integer primary key autoincrement not null,");
		sb.append(COLUMN_KEY).append(" text not null,");
		sb.append(COLUMN_ENABLE).append(" integer default 0");
		sb.append(") ;");
		db.execSQL(sb.toString());
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion==1 && newVersion==2) { // Ver.1は黒歴史
			StringBuilder sb = new StringBuilder();
			sb.append("drop table ");
			sb.append(TABLE_LOG);
			sb.append(";");
			db.execSQL(sb.toString());
			onCreate(db);
		}	
	}	
		
	private void insertResultKey(List<SmResult> resultList){
		List<ContentValues> valuesList = new ArrayList<ContentValues>();
		HashSet<String> resultKeySet = getResultKeySet();
		for(SmResult result : resultList){
			String key = result.getKey();
			if (!resultKeySet.contains(key)) {
				resultKeySet.add(key);
				ContentValues values = new ContentValues();
				values.put(COLUMN_KEY, key);
				valuesList.add(values);
			}
		}
		
		SQLiteDatabase db = getWritableDatabase();
		
		try{
			for(ContentValues values : valuesList){
				db.insert(TABLE_KEY, null, values);
			}
		}finally{
			db.close();
		}
	
		
	}	
	
	public HashSet<String> getEnabledResultKeySet() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		HashSet<String> keyHashSet = new HashSet<String>();
		try {
			cursor = db.query(TABLE_KEY, new String[]{COLUMN_KEY}, COLUMN_ENABLE+"=?", new String[]{"1"}, null, null, null);
			int keyIndex = cursor.getColumnIndex(COLUMN_KEY);
			while (cursor.moveToNext()) {
				String key = cursor.getString(keyIndex);
				keyHashSet.add(key);
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return keyHashSet;
	}
	
	public HashSet<String> getResultKeySet() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		HashSet<String> keyHashSet = new HashSet<String>();
		try {
			cursor = db.query(TABLE_KEY, new String[]{COLUMN_KEY}, null, null, null, null, null);
			int keyIndex = cursor.getColumnIndex(COLUMN_KEY);
			while (cursor.moveToNext()) {
				String key = cursor.getString(keyIndex);
				keyHashSet.add(key);
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return keyHashSet;
	}
	
	public List<SmResultKey> getResultKeyList() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		List<SmResultKey> keyList = new ArrayList<SmResultKey>();
		try {
			cursor = db.query(TABLE_KEY, new String[]{COLUMN_KEY,COLUMN_ENABLE}, null, null, null, null, COLUMN_KEY+" asc");
			int keyIndex = cursor.getColumnIndex(COLUMN_KEY);
			int enableIndex = cursor.getColumnIndex(COLUMN_ENABLE);
			while (cursor.moveToNext()) {
				String key = cursor.getString(keyIndex);
				boolean enable = (cursor.getInt(enableIndex) == 1);
				keyList.add(new SmResultKey(key, enable));
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return keyList;
	}

	public void setEnableResultKey(String key, boolean enable) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_ENABLE, enable?1:0);
		try{
			db.update(TABLE_KEY, values, COLUMN_KEY+"=?", new String[]{key});
		}finally{
			db.close();
		}
	}
	
	/*public List<SmLog> getSmLogs(long fromTime, long toTime, Handler handler){
		String selection = null;
		if (fromTime!=0l) {
			selection = COLUMN_TIME+">"+fromTime+" AND "+ COLUMN_TIME+"<"+toTime; 
		}
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		List<SmLog> smLogs = new ArrayList<SmLog>();
		try{
			cursor = db.query(TABLE_LOG, 
					new String[]{COLUMN_TIME,COLUMN_LATITUDE,COLUMN_LONGITUDE,COLUMN_LAC,COLUMN_CID}, 
					selection, null, null, null, null);

			if (handler!=null) {
				Message msg = new Message();
				msg.what = 0;
				msg.arg1 = cursor.getCount();
				handler.sendMessage(msg);
			}
			
			int timeIndex = cursor.getColumnIndex(COLUMN_TIME);
			int latitudeIndex = cursor.getColumnIndex(COLUMN_LATITUDE);
			int longitudeIndex = cursor.getColumnIndex(COLUMN_LONGITUDE);
			int lacIndex = cursor.getColumnIndex(COLUMN_LAC);
			int cidIndex = cursor.getColumnIndex(COLUMN_CID);
		
			while (cursor.moveToNext()) {
				long time = cursor.getLong(timeIndex);
				double latitude = cursor.getDouble(latitudeIndex);
				double longitude = cursor.getDouble(longitudeIndex);
				int lac = cursor.getInt(lacIndex);
				int cid = cursor.getInt(cidIndex);
				SmLog smLog = new SmLog(time, latitude, longitude, lac, cid);
				smLog.setMemoList(getMemoList(time));
				smLog.setResultList(getResultList(time));
				if (smLog.getResultList().size()!=0) {
					smLogs.add(smLog);
					if (handler!=null) {
						handler.sendEmptyMessage(2);
					}
				}else {
					if (handler!=null) {
						handler.sendEmptyMessage(1);
					}
				}			
			}
			
		}finally{
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return smLogs;
	}
	 */
	public List<SmLog> getSmLogs(long fromTime, long toTime){
		return getSmLogs(fromTime,toTime,null,null);
	}
	
	public List<SmLog> getSmLogs(long fromTime, long toTime, String selection, Handler handler){
		if (selection==null) {
			selection = new String();
		}else {
			selection += " AND ";
		}
		selection += COLUMN_TIME+">"+fromTime+" AND "+ COLUMN_TIME+"<"+toTime;
		
		Log.v(TAG, selection);
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		List<SmLog> smLogs = new ArrayList<SmLog>();
		try{
			cursor = db.query(TABLE_RESULT, 
					new String[]{COLUMN_TIME}, 
					selection, null, COLUMN_TIME, null, null);

			if (handler!=null) {
				Message msg = new Message();
				msg.what = 0;
				msg.arg1 = cursor.getCount();
				handler.sendMessage(msg);
			}
			
			int timeIndex = cursor.getColumnIndex(COLUMN_TIME);
		
			while (cursor.moveToNext()) {
				long time = cursor.getLong(timeIndex);
				SmLog smLog = getSmLog(time);
				smLogs.add(smLog);
				if (handler!=null) {
					handler.sendEmptyMessage(1);
				}	
			}
			
		}catch(Exception e){
			if (handler!=null) {
				handler.sendEmptyMessage(2);
				return smLogs;
			}	
		}finally{
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return smLogs;
	}

	public SmLog getSmLog(long time) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		SmLog smLog = null;
		try {
			cursor = db.query(TABLE_LOG, 
					new String[]{COLUMN_LATITUDE,COLUMN_LONGITUDE,COLUMN_LAC,COLUMN_CID}, COLUMN_TIME+"="+time, 
					null, null, null, null);
			if (cursor.getCount()==1) {
				cursor.moveToNext();
				double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
				double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
				int lac = cursor.getInt(cursor.getColumnIndex(COLUMN_LAC));
				int cid = cursor.getInt(cursor.getColumnIndex(COLUMN_CID));
				smLog = new SmLog(time, latitude, longitude, lac, cid);
				smLog.setMemoList(getMemoList(time));
				smLog.setResultList(getResultList(time));
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}		
		return smLog;
	}
	
	public void insertSmLog(SmLog smLog) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		long time = smLog.getTime();
		values.put(COLUMN_TIME, time);
		values.put(COLUMN_LATITUDE, smLog.getLatitude());
		values.put(COLUMN_LONGITUDE, smLog.getLongitude());
		values.put(COLUMN_LAC, smLog.getLac());
		values.put(COLUMN_CID, smLog.getCid());
		try {
			db.insert(TABLE_LOG, null, values);
			insertMemoList(time, smLog.getMemoList());
			insertResultList(time, smLog.getResultList());
			insertResultKey(smLog.getResultList());
		} finally {
			db.close();
		}
	}
	
	private List<String> getMemoList(long time){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		List<String> memoList = new ArrayList<String>();
		try {
			cursor = db.query(TABLE_MEMO, new String[]{COLUME_MEMO}, COLUMN_TIME+"="+time,
					null, null, null, null);
			int memoIndex = cursor.getColumnIndex(COLUME_MEMO);
			while (cursor.moveToNext()) {
				String memo = cursor.getString(memoIndex);
				memoList.add(memo);
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return memoList;
	}	
	
	public void insertMemoList(long time, List<String> memoList){
		List<ContentValues> valuesList = new ArrayList<ContentValues>();
		for(String memo : memoList){
			ContentValues values = new ContentValues();
			values.put(COLUMN_TIME, time);
			values.put(COLUME_MEMO, memo);
			valuesList.add(values);
		}
		SQLiteDatabase db = getWritableDatabase();
		try{
			for (ContentValues values : valuesList) {
				db.insert(TABLE_MEMO, null, values);
			}			
		}finally{
			db.close();
		}		
	}
		
	public List<SmResult> getResultList(long time) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		List<SmResult> resultList = new ArrayList<SmResult>();
		try {
			cursor = db.query(TABLE_RESULT, new String[]{COLUMN_KEY,COLUMN_VALUE},
					COLUMN_TIME+"="+time,null, null, null, null);
			int keyIndex = cursor.getColumnIndex(COLUMN_KEY);
			int valueIndex = cursor.getColumnIndex(COLUMN_VALUE);
			while (cursor.moveToNext()) {
				String key = cursor.getString(keyIndex);
				String value = cursor.getString(valueIndex);
				resultList.add(new SmResult(key, value));
			}
		} finally {
			if (cursor!=null) {
				cursor.close();
			}
			db.close();
		}
		return resultList;
	}

	public void insertResultList(long time, List<SmResult> resultList) {
		List<ContentValues> valuesList = new ArrayList<ContentValues>();
		for(SmResult result : resultList){
			ContentValues values = new ContentValues();
			values.put(COLUMN_TIME, time);
			values.put(COLUMN_KEY, result.getKey());
			values.put(COLUMN_VALUE, result.getValue());
			valuesList.add(values);
		}
		SQLiteDatabase db = getWritableDatabase();
		try{
			for(ContentValues values : valuesList){
				db.insert(TABLE_RESULT, null, values);
			}
		}finally{
			db.close();
		}	
	}
	
}
