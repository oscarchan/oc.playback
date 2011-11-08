package oc.playback.rsync;

import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * scheduler for next rsync service call 
 */
public class RsyncScheduler extends BroadcastReceiver {
	private static final String TAG = "RsyncScheduler";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "received: action=" + intent.getAction() + "; data=" + intent.getDataString());

		// start and run service NOW
//		Intent rsyncIntent = new Intent(context.getApplicationContext(), RsyncService.class);
//		context.startService(rsyncIntent);
		
		// schedule future runs
		startRecurringTasks(context);
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
}
