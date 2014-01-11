package voodoo.tvdb.AlarmServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Preferences.Prefs;


public class ReminderManager {
	private static final String TAG = "ReminderManager";
	
	private Context context;
	private AlarmManager alarmManager;
	
	private SharedPreferences prefs;
	
	public ReminderManager(Context context){
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		prefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public void setReminder(Reminder reminder) throws ParseException{
		/**
		 * This intent is responsible for specifying what should happen
		 * when the alarm goes off. OnAlarmReceiver would be called.
		 */
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.putExtra("REMINDER", reminder);
		/**
		 * Setting Dummy Action "foo" so that the extras get 
		 * attached. This is a bug, if no Action is set
		 * extras won't get Attached.
		 * 
		 * SETTING ACTION VERY IMPORTANT!! 
		 * This will make the Alarm different from the others with 
		 * the use of an ID, otherwise the if you try adding 3, 4
		 * or multiple alarms the previous one will be replaced with 
		 * the newer one because it appears to be the same one...
		 * the ID makes them different
		 */
		i.setAction("foo:" + reminder.EPISODE_ID);
		
		
		/**
		 * Since the AlarmManager operates in a SEPARATE PROCESS a
		 * PendingIntent MUST be created for it to notify an
		 * application an action needs to take place.
		 */
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		//Get the date, Parse it, and input it into the alarmManager;
		Calendar date = getDate(reminder);
		Calendar calendar = Calendar.getInstance();

		if(date != null){
			if(date.after(calendar)){
				
				Log.d(TAG, "Reminder.Episode_id = " + reminder.EPISODE_ID);
				
				//Set reminder X minutes before according to settings
				String minutes = prefs.getString("reminder_time", "60");
				date.set(Calendar.MINUTE, date.get(Calendar.MINUTE) - Integer.parseInt(minutes));
				alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pi);
			}
		}else{
			Log.d(TAG, "Reminder for " + reminder.EPISODE_ID + " could not be added");
		}
		
	}

	public void removeReminder(Reminder reminder) throws ParseException{
		/**
		 * Create the EXACT SAME PendingIntent so it can be removed
		 */
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.putExtra("REMINDER", reminder);
		i.setAction("foo:" + reminder.EPISODE_ID);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		alarmManager.cancel(pi);
	}
	
	public void setFavoriteUpdateService(){
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.setAction("foo");
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		Calendar calendar = Calendar.getInstance();
		
		String[] Time = Prefs.getUpdateTime(context).split(":");
		
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Time[0]));
		calendar.set(Calendar.MINUTE, Integer.parseInt(Time[1]));
		calendar.clear(Calendar.SECOND);
		
		//If calendar time has passed (i.e. current 5:00PM and service to run at 11:00AM) set the date for next day
		if(calendar.before(Calendar.getInstance())){
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
		}
		
		//Log.d(TAG, Time[0] + ":" + Time[1]);
		//Log.d(TAG, "Update Service set to " + calendar.getTime().toGMTString() + " -- " + calendar.getTimeInMillis());
		
		// Set how often to run the update service
		SharedPreferences settings = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(context);
		int freq = Integer.valueOf(settings.getString("update_frequency", "1"));
		
		switch(freq){
		
		case 0:
			
			Log.d(TAG, "Manual");
			
			// Manual
			alarmManager.cancel(pi);
			break;
			
		case 1:
			
			Log.d(TAG, "Daily");
			
			// Daily
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
			break;
			
		case 2:
			
			Log.d(TAG, "Every Other Day");
			
			// Every Other Day
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 2, pi);
			break;
			
		case 3:
			
			Log.d(TAG, "Weekly");
			
			// Weekly
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
			break;
			
		}
		
	}
	
	public void setFavoriteUpdateServiceWithFreq(int freq){
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.setAction("foo");
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		Calendar calendar = Calendar.getInstance();
		
		String[] Time = Prefs.getUpdateTime(context).split(":");
		
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Time[0]));
		calendar.set(Calendar.MINUTE, Integer.parseInt(Time[1]));
		calendar.clear(Calendar.SECOND);
		
		//If calendar time has passed (i.e. current 5:00PM and service to run at 11:00AM) set the date for next day
		if(calendar.before(Calendar.getInstance())){
			calendar.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY + 24);
		}
		
		//Log.d(TAG, Time[0] + ":" + Time[1]);
		//Log.d(TAG, "Update Service set to " + calendar.getTime().toGMTString() + " -- " + calendar.getTimeInMillis());
		
		switch(freq){
		
		case 0:
			
			Log.d(TAG, "Manual");
			
			// Manual
			alarmManager.cancel(pi);
			break;
			
		case 1:
			
			Log.d(TAG, "Daily");
			
			// Daily
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
			break;
			
		case 2:
			
			Log.d(TAG, "Every Other Day");
			
			// Every Other Day
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 2, pi);
			break;
			
		case 3:
			
			Log.d(TAG, "Weekly");
			
			// Weekly
			alarmManager.cancel(pi);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
			break;
			
		}
		
	}
	
	public void setSyncUpdateService(){
	
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.setAction("sync");
		
		i.putExtra("SYNC", 1);
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 1);
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	
	}
	
	public void setNowService(){
		
		Intent i = new Intent(context, OnAlarmReceiver.class);
		i.setAction("foo");
		
		i.putExtra("NOW", 1);
		
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 1);
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	
	}
	
	private Calendar getDate(Reminder reminder) throws ParseException {
		String dateString;
		Calendar d = Calendar.getInstance();
		if(reminder.TIME != null && reminder.DATE != null){
			
			dateString = reminder.TIME + reminder.DATE;
			dateString.replace(" ", "");
			//Log.d(TAG, dateString + " length: " + dateString.length());
			
			SimpleDateFormat sdf = new SimpleDateFormat("KK:mmaayyyy-MM-dd");
			
			d.setTime(sdf.parse(dateString.replace(" ", "")));
		}else if(reminder.DATE != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dateString = reminder.DATE;
			d.setTime(sdf.parse(dateString));
		}else{
			d = null;
		}
		return d;
	}
}













