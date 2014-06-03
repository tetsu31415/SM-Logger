package com.tetsu31415.smlogger;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

public class SmResultKeyAdapter extends ArrayAdapter<SmResultKey>{
	
	private LayoutInflater inflater;
	private int resource;
	private List<SmResultKey> list;
	
	public SmResultKeyAdapter(Context context, int resource,List<SmResultKey> list) {
		super(context, resource, list);
		this.resource = resource;
		this.list = list;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView!=null) {
			view = convertView;
		}else {
			view = inflater.inflate(resource, null);
		}
				
		SmResultKey key = list.get(position);
		Log.v("smlogger", key.getKey()+" : "+key.isEnable());
		CheckedTextView textView;
		textView = (CheckedTextView)view.findViewById(android.R.id.text1);
		textView.setText(key.getKey());
		return view;
	}
	
}
