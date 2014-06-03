package com.tetsu31415.smlogger;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class LoggingService extends Service implements LocationListener{

	private final String TAG = "smlogger";
	
	private LocationManager locationManager;
	private NotificationManager mNotificationManager;
	private Logcat mLogcat;
	
	private SmDBHelper mDBHelper;
	
	private double latitude = -1.0;
	private double longitude = -1.0;
	
	private int lac = -1;
	private int cid = -1;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			SmLog smItem = (SmLog)msg.obj;
			onUpdateItem(smItem);
		}	
	};
	
	public PhoneStateListener phoneStateListener = new PhoneStateListener(){
		public void onServiceStateChanged(android.telephony.ServiceState serviceState) {
			if (serviceState.getState()!=ServiceState.STATE_IN_SERVICE) {
				lac = cid = -1;
			}
		};
		
		public void onCellLocationChanged(android.telephony.CellLocation location) {
			if (location instanceof GsmCellLocation) {
				GsmCellLocation loc = (GsmCellLocation)location;
				lac = loc.getLac();
				cid = loc.getCid();
			}else if (location instanceof CdmaCellLocation) {
				CdmaCellLocation loc = (CdmaCellLocation)location;
				lac = loc.getNetworkId();
				cid = loc.getBaseStationId();
			}
		};
		
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate");
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mDBHelper = new SmDBHelper(this);
		createNotification();
		
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		resetLocationManager();
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, 
				PhoneStateListener.LISTEN_CELL_LOCATION|PhoneStateListener.LISTEN_SERVICE_STATE);
	}
	
	private void resetLocationManager(){
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		String provider = locationManager.getBestProvider(criteria, true);
		if (provider!=null) {
			locationManager.requestLocationUpdates(provider, 1000, 0, this);
		}else {
			latitude = longitude = -1.0;
		}
	}
	
	private void startLogcat() {
		Log.v(TAG, "STARTLOGCAT");
		new Thread(new Runnable() {

			@Override
			public void run() {
				mLogcat = new Logcat(mHandler);
				mLogcat.start();		
			}
		}).start();
	}	


	private void onUpdateItem(SmLog smLog){
		smLog.setLocation(latitude, longitude);
		smLog.setCellInfo(lac, cid);
		List<SmResult> resultList = smLog.getResultList();
		
		if (resultList.size()==0) {
			return;
		}
		
		//StringBuilder sb = new StringBuilder(new Date(smLog.getTime()).toString()).append("\n");
		
		//List<String> memoList = smLog.getMemoList();
		/*for(SmResult result : resultList){
			sb.append(result.getKey()).append(":").append(result.getValue()).append(",\n");			
		}
		for (String memo : memoList) {
			sb.append(memo).append(",");
		}
		Log.v(TAG, sb.toString());*/
		
		//データベース書き込み処理
		mDBHelper.insertSmLog(smLog);
	}
	
	private void createNotification(){
		Notification.Builder builder = new Notification.Builder(this);
		builder.setTicker(getString(R.string.start_logging));
		builder.setContentTitle(getString(R.string.app_name));
		builder.setContentText(getString(R.string.logging));
		builder.setContentIntent(PendingIntent.getActivity(
				this, 0, new Intent(this,MainActivity.class), 0));
		builder.setOngoing(true);
		builder.setSmallIcon(android.R.drawable.ic_menu_info_details);
		builder.setWhen(System.currentTimeMillis());
		Notification notification;
		if (Build.VERSION.SDK_INT>=16) {
			notification = builder.build();
		}else {
			notification = builder.getNotification();
		}
		mNotificationManager.notify(0,notification);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStart");
		if (mLogcat==null) {
			startLogcat();
		}		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
		if (mLogcat!=null) {
			mLogcat.stop();
		}
		mNotificationManager.cancel(0);
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		resetLocationManager();		
	}

	@Override
	public void onProviderEnabled(String provider) {
		resetLocationManager();
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
}
