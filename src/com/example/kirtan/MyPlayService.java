package com.example.kirtan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyPlayService extends Service implements OnCompletionListener,
		OnPreparedListener, OnErrorListener, OnSeekCompleteListener,
		OnBufferingUpdateListener, OnInfoListener {
	private MediaPlayer mp = new MediaPlayer();
	private String songToPlay;
	private int NOTIFICATION_ID = 1111;
	private boolean isPausedInCall;
	private PhoneStateListener phoneStateListner;
	private TelephonyManager telephonyMgr;
	public static final String BROADCAST_BUFFER= "package com.example.kirtan.broadcastbuffer";
	public Intent bufferIntent;
	private int headsetSwitch = 1;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		bufferIntent = new Intent(BROADCAST_BUFFER);
		mp.setOnCompletionListener(this);
		mp.setOnPreparedListener(this);
		mp.setOnErrorListener(this);
		mp.setOnSeekCompleteListener(this);
		mp.setOnBufferingUpdateListener(this);
		mp.setOnInfoListener(this);
		mp.reset();
		registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG
				));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Test1", "This is from service");
		startNotification();
		telephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateListner = new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					if (mp != null) {
						if (isPausedInCall) {
							startMedia();
							isPausedInCall = false;
						}
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
				case TelephonyManager.CALL_STATE_RINGING:
					if (mp != null) {
						pauseMedia();
						isPausedInCall = true;
					}
					break;
				}
			}

			private void pauseMedia() {
				if (mp != null) {
					mp.pause();
				}

			};
		};
		// register for listening events on telephone calls
		telephonyMgr.listen(phoneStateListner,
				PhoneStateListener.LISTEN_CALL_STATE);
		songToPlay = intent.getExtras().getString("songToPlay");
		if (!mp.isPlaying()) {
			try {
				mp.setDataSource(songToPlay);
				//sendBroadcast message
				sendBufferingBroadcast();
				mp.prepareAsync();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Toast.makeText(this, "The service is started",
		// Toast.LENGTH_SHORT).show();
		return START_STICKY;
	}


	@SuppressWarnings("deprecation")
	private void startNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notiMgr = (NotificationManager) getSystemService(ns);
		Notification notification = new Notification(R.drawable.ic_launcher,
				"Playing Kirtan...",
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), "Waheguru",
				"You are listening shabad kirtan.",
				contentIntent);
		notiMgr.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
			}
			mp.release();
			mp = null;
			resetPlayPauseBtn();
			unregisterReceiver(headsetReceiver);
		}
		stopNotification();
	}

	private void stopNotification() {

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notiMgr = (NotificationManager) getSystemService(ns);
		notiMgr.cancel(NOTIFICATION_ID);

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		sendBufferCompletionBroadcast();
		MyUtil.makeToast(this, "Connection Timed Out.\nChannel is not responding at this time.");
		onDestroy();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		sendBufferCompletionBroadcast();
		startMedia();
	}

	private void startMedia() {
		if (!mp.isPlaying()) {
			mp.start();
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		stopMedia();
		stopSelf();

	}

	private void stopMedia() {
		if (mp.isPlaying()) {
			mp.stop();
		}
	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub

	}
	
	private void sendBufferingBroadcast() {
		bufferIntent.putExtra("buffering", 1);
		sendBroadcast(bufferIntent);
	}
	
	private void sendBufferCompletionBroadcast() {
		bufferIntent.putExtra("buffering", 0);
		sendBroadcast(bufferIntent);
	}
	
	private void resetPlayPauseBtn() {
		bufferIntent.putExtra("buffering", 2);
		sendBroadcast(bufferIntent);
	}
	
	//create headset receiver
	
	private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		private boolean headsetConnected = false;
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.hasExtra("state")){
				if(headsetConnected && intent.getIntExtra("state", 0) == 0){
					headsetConnected = false;
					headsetSwitch = 0;
				}else if(!headsetConnected 
						&& intent.getIntExtra("state", 0) == 1){
					headsetConnected = true;
					headsetSwitch = 1;
				}
				
			}
			switch(headsetSwitch){
			case 0:
				headsetDisconnected();
				break;
			case 1:
				break;
			}
		}
	};
	
	private void headsetDisconnected(){
		stopMedia();
		stopSelf();
	}
}
