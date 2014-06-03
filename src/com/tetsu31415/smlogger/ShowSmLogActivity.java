package com.tetsu31415.smlogger;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowSmLogActivity extends Activity{

	
	private TextView textView;
	private long time;
	
	private double lat = -1.0, lon = -1.0;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			SmLog smLog = (SmLog)msg.obj;
			lat = smLog.getLatitude();
			lon = smLog.getLongitude();
			StringBuilder sb = new StringBuilder();
			sb.append(new Date(smLog.getTime()).toString()).append("\n\n");
			sb.append(smLog.getLatitude()).append(",")
			.append(smLog.getLongitude()).append("\n");
			
			sb.append("LAC/NID").append(":").append(smLog.getLac()).append(", ")
			.append("CID/BID").append(":").append(smLog.getCid()).append("\n\n");
			
			
			for (String memo : smLog.getMemoList()) {
				sb.append(memo).append("\n");
			}
			sb.append("\n");
			
			for(SmResult result : smLog.getResultList()){
				sb.append(result.getKey()).append(":").append(result.getValue()).append("\n");
				
			}
			textView.append(sb.toString());
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_log);
		
		
		textView = (TextView)findViewById(R.id.textView1);
		time = getIntent().getLongExtra("time", 0l);
		if (time == 0l) {
			finish();
			return;
		}
		
		final SmDBHelper helper = new SmDBHelper(this);
	
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SmLog smLog = helper.getSmLog(time);
				
				Message msg = new Message();
				msg.obj = smLog;
				handler.sendMessage(msg);
			}
		}).start();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_log, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.open_map) {
			if (lat==-1.0) {
				return true;
			}
			Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+lat+","+lon+"?q="+lat+","+lon));
			try {
				startActivity(intent);
			} catch (Exception e) {
				
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
