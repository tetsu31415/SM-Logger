package com.tetsu31415.smlogger;

import java.util.HashSet;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ShowLogsActivity extends ListActivity{

	private final String TAG = "smlogger";
	
	private SmLogArrayAdapter adapter;
	
	private String query;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		long fromTime = extras.getLong("from_time",0l);
		long toTime = extras.getLong("to_time",0l);
		query = extras.getString("query");
		
		LoadSmLogsTask task = new LoadSmLogsTask();
		task.execute(fromTime,toTime);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		SmLog smLog = adapter.getItem(position);
		Intent intent = new Intent(this,ShowSmLogActivity.class);
		intent.putExtra("time", smLog.getTime());
		startActivity(intent);
	}

	private class LoadSmLogsTask extends AsyncTask<Long, Void, List<SmLog>>{

		private SmDBHelper helper;
		
		private ProgressDialog dialog;
		
		private int count = 0;
		
		public LoadSmLogsTask() {
			helper = new SmDBHelper(ShowLogsActivity.this);
		}
		
		private Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 0:
						dialog.setMax(msg.arg1);
						break;
					case 1:
						dialog.setProgress(++count);
						break;
					case 2:
						Toast.makeText(ShowLogsActivity.this, R.string.wrong_query, 0).show();
						break;
					default:
						break;
				}
				
			};		
		};
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ShowLogsActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setTitle(R.string.loading);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected List<SmLog> doInBackground(Long... params) {
			String selection = null;
			if (query!=null) {
				selection =	query;
								
			}
			List<SmLog> smLogs = helper.getSmLogs(params[0],params[1],
					selection,handler);
			
			return smLogs;
		}
		
		@Override
		protected void onPostExecute(List<SmLog> smLogs) {
			super.onPostExecute(smLogs);
			if (smLogs.size()==0) {
				Toast.makeText(ShowLogsActivity.this, R.string.not_found, Toast.LENGTH_LONG).show();
				dialog.dismiss();
				return;
			}
			
			HashSet<String> keySet = helper.getEnabledResultKeySet();
			
			adapter = new SmLogArrayAdapter(
					ShowLogsActivity.this, android.R.layout.simple_list_item_1, smLogs, keySet);
			setListAdapter(adapter);
			getListView().setFastScrollEnabled(true);
			dialog.dismiss();
		}
		
	}
}
