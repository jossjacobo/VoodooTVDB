package voodoo.tvdb.alarmServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import voodoo.tvdb.Objects.Reminder;


public class OnAlarmReceiver extends BroadcastReceiver {
	//private static final String TAG = "OnAlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Reminder reminder = new Reminder();
		Integer sync = null;
		Integer now = null;
		
		// Get Extras
		reminder = (Reminder) intent.getExtras().get("REMINDER");
		sync = (Integer) intent.getExtras().get("SYNC");
		now = (Integer) intent.getExtras().get("NOW");
	
		WakeReminderIntentService.acquireStaticLock(context);
		
		Intent i;
		if(reminder != null){
			
			i = new Intent(context, ReminderService.class);
			i.putExtra("REMINDER", reminder);
			i.setAction("foo");
		
		}else if(sync != null){
			
			i = new Intent(context, SyncService.class);
			i.putExtra("SOMETHING", "SOMETHING");
			i.setAction("SYNC");
			
		}else if(now != null){
			
			i = new Intent(context, FavoriteUpdateService.class);
			i.setAction("foo");
			
		}else{
			
			i = new Intent(context, FavoriteUpdateService.class);
			i.setAction("foo");
		
		}
		
		context.startService(i);
	}
}














