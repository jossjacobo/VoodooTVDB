package voodoo.tvdb.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;


public class Reminders {
	//private final static String TAG = "RemindersDatabase";
	
	ArrayList<Reminder> reminders;
	DatabaseAdapter dbAdapter;
	String seriesId;
	
	public Reminders(Context context, ArrayList<Reminder> rs){
		this.dbAdapter = new DatabaseAdapter(context);
		this.reminders = rs;
		this.seriesId = (rs != null && rs.size() > 0) ? rs.get(0).SERIES_ID : null;
	}
	public void open(){
		this.dbAdapter.open();
	}
	public void close(){
		this.dbAdapter.close();
	}
	@SuppressLint("SimpleDateFormat")
	public void addReminders() {
		
		// Create a "CurrentDate" to use it as a cut off point up until 
		// how further back I want to add "Reminders" to the Reminders database
		// currently set to 1 month back
		Calendar currentDate = Calendar.getInstance();
		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.add(Calendar.MONTH, -1);
		
		//Add reminder from Season 0 first
		for(int i = 0; i < reminders.size(); i++){
			Reminder r = reminders.get(i);
			if(r.SEASON_NUMBER == 0){
				if(r.DATE != null){
					//episode is in season 0, now check if date is after today
					SimpleDateFormat sdf;
					sdf = new SimpleDateFormat("yyyy-MM-dd");
					
					Calendar episodeDate = Calendar.getInstance();
					try {
						String d = r.DATE;
						//Log.d(TAG, d);
						episodeDate.setTime(sdf.parse(d));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(episodeDate.after(currentDate) || episodeDate.equals(currentDate)){
						//Log.d(TAG, r.DATE + " " + r.SERIES_NAME + "-" + r.EPISODE_NAME + " added to reminder database");
						dbAdapter.deleteReminder(r.EPISODE_ID);
						dbAdapter.insertReminder(r);
					}
				}
			}else{
				break;
			}
		}
		
		//Add reminder for latest Episodes until you reach Today's Date
		for(int i = reminders.size()-1; i >= 0; i--){
			Reminder r = reminders.get(i);
			if(r.DATE != null){
				
				SimpleDateFormat sdf;
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				Calendar episodeDate = Calendar.getInstance();
				try {
					String d = reminders.get(i).DATE;
					episodeDate.setTime(sdf.parse(d));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if(episodeDate.after(currentDate) || episodeDate.equals(currentDate)){
					dbAdapter.deleteReminder(r.EPISODE_ID);
					dbAdapter.insertReminder(r);
				}else if(episodeDate.before(currentDate)){
					break;
				}
			}
		}
	}
	
	public void deleteAllReminders(){
		
		if(this.seriesId != null){
		
			dbAdapter.deleteAllReminders(seriesId);
		
		}
	}
}






























