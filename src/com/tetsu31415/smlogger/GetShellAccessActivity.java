package com.tetsu31415.smlogger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GetShellAccessActivity extends Activity implements OnClickListener{
		
	private Button mGetButton, mOpenButton;
	private EditText mEditText;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mEditText.append((String)msg.obj+"\n");
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_shell_access);
		findViews();
		setListener();
	}

	private void findViews() {
		mGetButton = (Button)findViewById(R.id.button1);
		mOpenButton = (Button)findViewById(R.id.button2);
		mEditText = (EditText)findViewById(R.id.editText1);
	}

	private void setListener() {
		mGetButton.setOnClickListener(this);
		mOpenButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v==mGetButton) {
			getShellAccess();
		}else if (v==mOpenButton) {
			openTerminal();
		}
	}
	
	private void openTerminal() {
		String cmd = 
				"adb kill-server;"+
				"adb start-server;"+
				"exit;";
		Intent intent = new Intent("jackpal.androidterm.RUN_SCRIPT");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra("jackpal.androidterm.iInitialCommand", cmd);
		try {
			startActivity(intent);
		}catch(SecurityException e){ 
			Toast.makeText(this, "Permission denied\nRe-install this app.", Toast.LENGTH_LONG).show();
		}catch (Exception e) {
			Toast.makeText(this, R.string.install_terminal, Toast.LENGTH_LONG).show();
			Intent playIntent = new Intent(Intent.ACTION_VIEW, 
					Uri.parse("https://play.google.com/store/apps/details?id=jackpal.androidterm"));
			startActivity(playIntent);
			finish();
		}
		
		
	}

	private void getShellAccess(){
		mEditText.setText("");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				printLog("=== GET SHELL ACCESS ===");
				String output = Util.execCmd("adb connect 127.0.0.1:5555");
				printLog(output);
	
				output = Util.execCmd("adb devices");
				printLog(output);
				output = Util.execCmd("adb -s 127.0.0.1:5555 shell id");
				if (output.contains("uid=2000")) {
					printLog("Success!");
				}else {
					printLog(output);
				}
				
			}
			
			public void printLog(String str) {
				Message message = Message.obtain(mHandler, 0);
				message.obj = str;
				mHandler.sendMessage(message);
			}
			
		}).start();

		
	}
	
}
