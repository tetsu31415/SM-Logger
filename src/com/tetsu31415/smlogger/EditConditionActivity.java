package com.tetsu31415.smlogger;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.tetsu31415.smlogger.TimePickerDialogFragment.TimePickerListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EditConditionActivity extends Activity implements OnClickListener,OnCheckedChangeListener,TimePickerListener{

	private Button fromTimeButton,toTimeButton;
	private CheckBox checkBox1,checkBox2,checkBox3,checkBox4;
	private EditText editText1;
	private long fromTime,toTime;
	
	private final String KEY_SHOW_DATE = "show_date";
	private final String KEY_SHOW_LOCATION = "show_location";
	private final String KEY_SHOW_MEMO = "show_memo";
	private final String KEY_SHOW_CELLINFO = "show_cellinfo";
	
	
	SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.condition);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		findViews();
		setListeners();
		
		fromTime = new Date().getTime()-10*60*1000;
		toTime = new Date().getTime();
		updateButton();		
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
		
	
	private void findViews() {
		fromTimeButton = (Button)findViewById(R.id.button1);
		toTimeButton = (Button)findViewById(R.id.button2);
		checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
		checkBox1.setChecked(sp.getBoolean(KEY_SHOW_DATE, true));
		checkBox2 = (CheckBox)findViewById(R.id.checkBox2);
		checkBox2.setChecked(sp.getBoolean(KEY_SHOW_MEMO, false));
		checkBox3 = (CheckBox)findViewById(R.id.checkBox3);
		checkBox3.setChecked(sp.getBoolean(KEY_SHOW_LOCATION, false));
		checkBox4 = (CheckBox)findViewById(R.id.checkBox4);
		checkBox4.setChecked(sp.getBoolean(KEY_SHOW_CELLINFO, false));
		editText1 = (EditText)findViewById(R.id.editText1);
		editText1.setSingleLine();
	}


	private void setListeners() {
		fromTimeButton.setOnClickListener(this);
		toTimeButton.setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button4).setOnClickListener(this);
		checkBox1.setOnCheckedChangeListener(this);
		checkBox2.setOnCheckedChangeListener(this);
		checkBox3.setOnCheckedChangeListener(this);
		checkBox4.setOnCheckedChangeListener(this);
	}

	private void updateButton(){
		fromTimeButton.setText(getDateString(fromTime));
		toTimeButton.setText(getDateString(toTime));
	}	
	
	private String getDateString(long time){
		java.text.DateFormat df = SimpleDateFormat.getDateTimeInstance();
		return df.format(new Date(time));
	}

	private void showTimePickerDialog(long fromTime,long toTime, boolean from){
		TimePickerDialogFragment dialogFragment = 
				TimePickerDialogFragment.newInstance(fromTime,toTime,from);
		dialogFragment.setOnUpdateTimeListener(this);
		dialogFragment.show(getFragmentManager(), "time_picker");
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
		case R.id.button2:
			showTimePickerDialog(fromTime, toTime, v.getId()==R.id.button1);
			break;
		case R.id.button3:
			startActivity(new Intent(this,SelectKeyActivity.class));
			break;
		case R.id.button4:
			Intent intent = new Intent(this,ShowLogsActivity.class);
			intent.putExtra("from_time", fromTime);
			intent.putExtra("to_time", toTime);
			if (editText1.length()!=0) {
				String query = editText1.getText().toString();
				query = query.replaceAll("\"([^\":]+):\"", 
						"time in (select time from result_table where key=\"$1\")");
				
				query = query.replaceAll("\"([^\":]+):([^\":]+)\"", 
						"time in (select time from result_table where key=\"$1\" AND value=\"$2\")");
				intent.putExtra("query", query);
			}
			
			startActivity(intent);
			break;
		default:
			break;
		}
	}


	@Override
	public void onUpdateTime(long time, boolean from) {
		if (from) {
			fromTime = time;
		}else {
			toTime = time;
		}
		updateButton();
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.checkBox1:
			sp.edit().putBoolean(KEY_SHOW_DATE, isChecked).commit();
			break;
		case R.id.checkBox2:
			sp.edit().putBoolean(KEY_SHOW_MEMO, isChecked).commit();
			break;

		case R.id.checkBox3:
			sp.edit().putBoolean(KEY_SHOW_LOCATION, isChecked).commit();
			break;

		case R.id.checkBox4:
			sp.edit().putBoolean(KEY_SHOW_CELLINFO, isChecked).commit();
			break;

		default:
			break;
		}
		
	}
	
}
