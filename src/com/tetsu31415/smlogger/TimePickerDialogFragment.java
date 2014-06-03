package com.tetsu31415.smlogger;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.widget.NumberPicker.OnValueChangeListener;

public class TimePickerDialogFragment extends DialogFragment 
	implements OnClickListener,OnValueChangeListener{
	
	private final int MAX_YEAR = 2099;
	private final int MIN_YEAR = 2000;
	private static final String KEY_TIME_FROM = "time_from";
	private static final String KEY_TIME_TO = "time_to";
	private static final String KEY_FROM = "from";
	
	private NumberPicker yearPicker,monthPicker,dayPicker,hourPicker,minutePicker,secondPicker;
	
	private long fromTime,toTime;
	private boolean from;
	
	public interface TimePickerListener {
		public void onUpdateTime(long time,boolean starting);
	}
	
	private TimePickerListener listener;
	
	public void setOnUpdateTimeListener(TimePickerListener listener) {
		this.listener = listener;
	}
		
	public static final TimePickerDialogFragment
		newInstance(long fromTime, long toTime, boolean from) {
	    Bundle args = new Bundle();
    	args.putLong(KEY_TIME_FROM, fromTime);
    	args.putLong(KEY_TIME_TO, toTime);
    	args.putBoolean(KEY_FROM, from);
	    
	    TimePickerDialogFragment fragment = new TimePickerDialogFragment();
	    fragment.setArguments(args);
	    return fragment;
	}
	
	private long getTime() {
		return from ? fromTime : toTime;
	}
	
	private long getAnotherTime() {
		return ! from ? fromTime : toTime;
	}
	
	private void setTime(long time) {
		if (from){
			fromTime = time;
		}else {
			toTime = time;
		}
	}
	
	private boolean isOkTime(long time){
		return from ? time < getAnotherTime() : time > getAnotherTime();
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle bundle;
		
		if (savedInstanceState!=null) {
			bundle = savedInstanceState;
		}else{
			bundle = getArguments();
		}
		
		fromTime = bundle.getLong(KEY_TIME_FROM, new Date().getTime());
		toTime = bundle.getLong(KEY_TIME_TO, new Date().getTime());
		from = bundle.getBoolean(KEY_FROM,true);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(from ? R.string.from_time : R.string.to_time);
		builder.setView(createPickersView());
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}
	
	private View createPickersView(){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.timepicker, null,false);
		yearPicker = (NumberPicker)view.findViewById(R.id.numberPicker1);
		monthPicker = (NumberPicker)view.findViewById(R.id.numberPicker2);
		dayPicker = (NumberPicker)view.findViewById(R.id.numberPicker3);
		hourPicker = (NumberPicker)view.findViewById(R.id.numberPicker4);
		minutePicker= (NumberPicker)view.findViewById(R.id.numberPicker5);
		secondPicker = (NumberPicker)view.findViewById(R.id.numberPicker6);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(getTime());		
		
		yearPicker.setMaxValue(MAX_YEAR);
		yearPicker.setMinValue(MIN_YEAR);
		monthPicker.setMaxValue(12);
		monthPicker.setMinValue(1);
		dayPicker.setMaxValue(31);
		dayPicker.setMinValue(1);
		hourPicker.setMaxValue(23);
		hourPicker.setMinValue(0);
		minutePicker.setMaxValue(59);
		minutePicker.setMinValue(0);
		secondPicker.setMaxValue(59);
		secondPicker.setMinValue(0);
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		onChangedMonth(year, month);
		
		yearPicker.setValue(year);
		monthPicker.setValue(month);
		dayPicker.setValue(calendar.get(Calendar.DAY_OF_MONTH));
		hourPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));
		minutePicker.setValue(calendar.get(Calendar.MINUTE));
		secondPicker.setValue(calendar.get(Calendar.SECOND));
		
		yearPicker.setOnValueChangedListener(this);
		monthPicker.setOnValueChangedListener(this);
		dayPicker.setOnValueChangedListener(this);
		hourPicker.setOnValueChangedListener(this);
		minutePicker.setOnValueChangedListener(this);
		secondPicker.setOnValueChangedListener(this);
			
		return view;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		long time = getTime();
		if (isOkTime(time)) {
			listener.onUpdateTime(time, from);
		}else {
			Toast.makeText(getActivity(), R.string.wrong_time, Toast.LENGTH_SHORT).show();
		}		
	}

	private void onChangedMonth(int year, int month){
		switch (month) {
			case 2:
				if (year%4==0 && year%100!=0 || year%400==0)
					dayPicker.setMaxValue(29);
				else
					dayPicker.setMaxValue(28);
				break;
				
			case 4:
			case 6:
			case 9:
			case 11:				
				dayPicker.setMaxValue(30);
				break;
				
			default:
				dayPicker.setMaxValue(31);
				break;
		}
	}
	
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		long time = getTimeformPickers();
		if (isOkTime(time)) {
			clearPickersColor();
		}else {
			setPickerColor(picker);
		}
		if (picker==yearPicker || picker==monthPicker) {
			onChangedMonth(yearPicker.getValue(), monthPicker.getValue());
		}		
		setTime(time);
	}
	
	private void setPickerColor(NumberPicker picker){
		picker.setBackgroundColor(Color.GRAY);
	}
	
	private void clearPickersColor(){
		int color = Color.TRANSPARENT;
		yearPicker.setBackgroundColor(color);
		monthPicker.setBackgroundColor(color);
		dayPicker.setBackgroundColor(color);
		hourPicker.setBackgroundColor(color);
		minutePicker.setBackgroundColor(color);
		secondPicker.setBackgroundColor(color);
	}
	
	private long getTimeformPickers(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, yearPicker.getValue());
		calendar.set(Calendar.MONTH, monthPicker.getValue()-1);
		calendar.set(Calendar.DAY_OF_MONTH, dayPicker.getValue());
		calendar.set(Calendar.HOUR_OF_DAY, hourPicker.getValue());
		calendar.set(Calendar.MINUTE, minutePicker.getValue());
		calendar.set(Calendar.SECOND, secondPicker.getValue());
		return calendar.getTimeInMillis();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(KEY_TIME_FROM, fromTime);
		outState.putLong(KEY_TIME_TO, toTime);
		outState.putBoolean(KEY_FROM, from);
	}
	
}
