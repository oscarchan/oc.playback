package oc.playback.rsync;

import java.util.Date;
import java.util.List;

import oc.playback.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OcRsyncActivity extends Activity 
{
	private static final String TAG = "OcRsyncActivity";

	private RsyncService rsyncService;
	
	private ServiceConnection rsyncConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			rsyncConn = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			rsyncService = ((RsyncService.RsyncBinder) service).getService();
		}
	};
	
	void doBindService() {
		bindService(new Intent(OcRsyncActivity.this, 
            RsyncService.class), rsyncConn, Context.BIND_AUTO_CREATE);
	}
	
	void doUnbindService() { 
		if(rsyncService!=null)
			unbindService(rsyncConn);
	}

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.rsync_controller);
		startService(new Intent(OcRsyncActivity.this.getApplicationContext(), RsyncService.class));		
		
		View rsync = this.findViewById(R.id.rsync);
		final TextView srcPathView = (TextView) this.findViewById(R.id.src_path);
		final TextView desetPathView = (TextView) this.findViewById(R.id.dest_path);
		
		startRecurringTasks(OcRsyncActivity.this);
		doBindService();
		rsync.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				rsyncService.rsync();
			};
		});
	}
	
	private void startRecurringTasks(Context context) {
		Intent rsyncIntent = new Intent(context, RsyncService.class);

		PendingIntent rsyncPendingIntent = PendingIntent.getService(context, 0, rsyncIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
//		PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
//            0, schedulerIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		List<RsyncConfig> configList = RsyncConfigFactory.getRsyncConfig();
		
		RsyncConfig config = configList.iterator().next();
		Schedule schedule = config.getSchedule();
		Date nextDate = schedule.nextDate(new Date());
		long interval = schedule.getRecurringInterval();
		
		
		Log.i(TAG, "next=" + nextDate + ", interval=" + interval);
		
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextDate.getTime(), interval, rsyncPendingIntent);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		doUnbindService();
	}
}
