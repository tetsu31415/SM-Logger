package com.tetsu31415.smlogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
public class MainActivity extends Activity implements OnClickListener{
	
	private final String TAG = "smlogger";
	
	
	Handler handler = new Handler();
	Button button1,button2,button3,button4,button5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		findViews();
		setListener();
		
	}

	private void findViews() {
		button1 = (Button)findViewById(R.id.button1);
		button2 = (Button)findViewById(R.id.button2);
		button3 = (Button)findViewById(R.id.button3);
		button4 = (Button)findViewById(R.id.button4);
		button5 = (Button)findViewById(R.id.button5);
	}

	private void setListener() {
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		button5.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.button1:
			intent.setClass(this, EditConditionActivity.class);
			startActivity(intent);
			break;
		case R.id.button2:
			intent.setClass(this, GetShellAccessActivity.class);
			startActivity(intent);
			break;
		case R.id.button3:
			intent.setClass(this, LoggingService.class);
			if (hasShellAccess()) {
				startService(intent);
			}else {
				intent = new Intent(this, GetShellAccessActivity.class);
				startActivity(intent);
			}
			break;
		case R.id.button4:
			intent.setClass(this, LoggingService.class);
			stopService(intent);
			break;
		case R.id.button5:
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					boolean ret = copyFile(new File("/data/data/com.tetsu31415.smlogger/databases/sm_log.db"),
							new File("/sdcard/sm_log.db"));
					final String result = "/sdcard/sm_log.db "+( ret ? "success!" : "failed");
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, result, 1).show();
						}
					});
				}
			}).start();
			break;
		default:
			
			break;
		}
	}
		
	public boolean copyFile(File inputFile , File outputFile){
		try {
			Log.v(TAG, inputFile.getPath()+" -> "+outputFile.getPath());
            FileInputStream fis = new FileInputStream(inputFile.getPath());
            FileOutputStream fos = new FileOutputStream(outputFile.getPath());

            byte buffer[] = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
            Log.v(TAG, "Copy Success!");
			return true;
		
		} catch (Exception e) {
			e.printStackTrace();
            Log.v(TAG, "Copy Failed");
			return false;
		}		
	}
	
	private boolean hasShellAccess(){
		String cmd = "adb -s 127.0.0.1:5555 shell id";
		String output = Util.execCmd(cmd);
		return output.contains("uid=2000");
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction()==KeyEvent.ACTION_DOWN&&event.getKeyCode()==KeyEvent.KEYCODE_MENU) {
			//Intent intent = new Intent(this, EditCoditionActivity.class);
			//startActivity(intent);
		}
		return super.dispatchKeyEvent(event);
	}
}
