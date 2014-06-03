package com.tetsu31415.smlogger;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SmLogArrayAdapter extends ArrayAdapter<SmLog>{

	private final String KEY_SHOW_DATE = "show_date";
	private final String KEY_SHOW_LOCATION = "show_location";
	private final String KEY_SHOW_MEMO = "show_memo";
	private final String KEY_SHOW_CELLINFO = "show_cellinfo";
	
	private LayoutInflater inflater;
	
	private int resource;
	
	private List<SmLog> smLogs;
	
	private HashSet<String> keySet;
	
	private boolean showDate;
	private boolean showMemo;
	private boolean showLocation;
	private boolean showCellInfo;
	
	public SmLogArrayAdapter(Context context, int resource,
			List<SmLog> smLogs,HashSet<String> keySet) {
		super(context, resource, smLogs);
		this.resource = resource;
		this.smLogs = smLogs;	
		this.keySet = keySet;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		showDate = sp.getBoolean(KEY_SHOW_DATE, true);
		showMemo = sp.getBoolean(KEY_SHOW_MEMO, false);
		showLocation = sp.getBoolean(KEY_SHOW_LOCATION, false);
		showCellInfo = sp.getBoolean(KEY_SHOW_CELLINFO, false);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView!=null) {
			view = convertView;
		}else {
			view = inflater.inflate(resource, null);
		}
		
		SmLog smLog = smLogs.get(position);
				
		StringBuilder sb = new StringBuilder();
		if (showDate) {
			sb.append(new Date(smLog.getTime()).toString()).append("\n");
		}
		if (showLocation) {
			sb.append(smLog.getLatitude()).append(",").append(smLog.getLongitude()).append("\n");
		}
		if (showCellInfo) {
			sb.append("LAC/NID").append(":").append(smLog.getLac()).append(", ")
			.append("CID/BID").append(":").append(smLog.getCid()).append("\n\n");
		}
		if (showMemo) {
			for (String memo : smLog.getMemoList()) {
			sb.append(memo).append("\n");
			}
		}
	
		for(SmResult result : smLog.getResultList()){
			if (keySet.contains(result.getKey())) {
				sb.append(result.getKey()).append(":").append(result.getValue()).append("\n");
			}
		}
		TextView textView = (TextView)view.findViewById(android.R.id.text1);
		textView.setText(sb.toString());

		return view;
	}

}
