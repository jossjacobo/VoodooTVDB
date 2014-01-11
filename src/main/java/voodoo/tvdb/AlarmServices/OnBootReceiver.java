package voodoo.tvdb.AlarmServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;

import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;


public class OnBootReceiver extends BroadcastReceiver {
	private static final String TAG = "OnBootReceiver";

	DatabaseAdapter dbAdapter;
	ArrayList<Reminder> reminders;
	ArrayList<String> favoriteIdList;
	
	ReminderManager reminderManager;
	
	/**
	 * All the reminder have to re-added to the AlarmManager every time OnBoot...
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//Log.d(TAG, "OnBootReceiver: android.intent.action.MEDIA_MOUNTED");
		Log.d(TAG, "OnBootReceiver: android.intent.action.ACTION_EXTERNAL_APPLICATIONS_AVAIlABLE");
		//Log.d(TAG, "OnBootReceiver: android.intent.action.BOOT_COMPLETED");
		
		//Initialize
		reminderManager = new ReminderManager(context);
		dbAdapter = new DatabaseAdapter(context);
		reminders = new ArrayList<Reminder>();
		favoriteIdList = new ArrayList<String>();
		
		//Open DatabaseAdapter
		dbAdapter.open();
		
		//Fetch all of the reminders from Database
		reminders = dbAdapter.fetchAllReminders();
		
		if(reminders != null){
			
			Log.d(TAG, "OnBootReceiver: Setting " + reminders.size() + " reminders");
			
			for(int i = 0; i < reminders.size(); i++){
				try {
			
					reminderManager.setReminder(reminders.get(i));
				
				} catch (ParseException e) {
				
					Log.d(TAG, "Could not add reminder ID " + reminders.get(i).EPISODE_ID);
					e.printStackTrace();
				
				}
			}
			
		}else{
			Log.d(TAG, "No reminders were found");
		}
		
		//Fetch Favorite ID list to see if an Update Service for 
		//the Favorite'D series need to be set.
		favoriteIdList = dbAdapter.fetchSeriesIdList();
		if(favoriteIdList != null){
			
			Log.d(TAG, "OnBootReceiver: Added a UpdateService alert");
			
			reminderManager.setFavoriteUpdateService();
			
		}else{
			Log.d(TAG, "No favorites found to set UpdateService");
		}
		
		//Finally Close DatabaseAdapter
		dbAdapter.close();
	}
}
