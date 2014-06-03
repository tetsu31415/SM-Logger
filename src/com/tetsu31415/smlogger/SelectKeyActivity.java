package com.tetsu31415.smlogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

public class SelectKeyActivity extends ListActivity{

	private SmDBHelper helper;
	
	private final String TAG = "smlogger";
	
	List<SmResultKey> resultKeys;
	SmResultKeyAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		helper = new SmDBHelper(this);
		resultKeys = helper.getResultKeyList();
		List<SmResultKey> resultKeys = helper.getResultKeyList();
		adapter = new SmResultKeyAdapter(
				this, android.R.layout.simple_list_item_multiple_choice, resultKeys);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemsCanFocus(false);
		setListAdapter(adapter);
		for (int i=0;i<resultKeys.size();i++) {
			getListView().setItemChecked(i, resultKeys.get(i).isEnable());
		}

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		SmResultKey resultKey = resultKeys.get(position);
		resultKey.toggleEnable();
		resultKeys.set(position, resultKey);
		helper.setEnableResultKey(resultKey.getKey(), resultKey.isEnable());
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
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode()==KeyEvent.KEYCODE_MENU) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					copyFile(new File("/data/data/com.tetsu31415.smlogger/databases/sm_log.db"),
							new File("/sdcard/sm_log.db"));
					
				}
			}).start();
		}
		return super.dispatchKeyEvent(event);
	}
}
