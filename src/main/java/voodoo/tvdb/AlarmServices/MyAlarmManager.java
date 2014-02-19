package voodoo.tvdb.alarmServices;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;


public class MyAlarmManager {
	private static final String TAG = "Alarms";
	Context context;
	String id;
	
	DatabaseAdapter dbAdapter;
	
	ArrayList<Reminder> reminders;
	
	public MyAlarmManager(Context context, String id){
		this.context = context;
		this.id = id;
		dbAdapter = new DatabaseAdapter(context);
		
		populateReminders(id);
	}
	
	public MyAlarmManager(Context context){
		this.context = context;
		dbAdapter = new DatabaseAdapter(context);
	}
	
	public void setReminders(ArrayList<Reminder> re){
		this.reminders = re;
	}
	
	private void populateReminders(String ID) {
		dbAdapter.open();
		reminders = dbAdapter.fetchAllRemindersBySeries(ID);
		dbAdapter.close();
	}
	
	public void addAlarms() {
		if(reminders != null){
			Log.d(TAG, "alarms to add: " + reminders.size());
			ReminderManager alarmManager = new ReminderManager(context);
			for(int i = 0; i < reminders.size(); i++){
				try {
					alarmManager.setReminder(reminders.get(i));
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	public void removeAlarms() {
		if(reminders != null){
			ReminderManager alarmManager = new ReminderManager(context);
			for(int i = 0; i < reminders.size(); i++){
				try {
					alarmManager.removeReminder(reminders.get(i));
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
