package voodoo.tvdb.AlarmServices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import voodoo.tvdb.Activity.MainActivity;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.R;

public class ReminderService extends WakeReminderIntentService{
	public ReminderService() {
		super("ReminderService");
	}

	@SuppressWarnings("unused")
	private static final String TAG = "Reminder Service";
	@Override
	void doReminderWork(Intent intent) {
		
		Reminder reminder = new Reminder();
		reminder = (Reminder) intent.getExtras().get("REMINDER");

		//TODO I have to create a way to keep track on UNREAD/UNOPENED notifications...how many...and their content
		//then loop through them to create and update the current notification...
		//TODO try it with a SharedPreference 3 of them: count, seriesTitles, episodeTitles...just += with each new one and delete all of them when opened...
		//ye...that should work...yeee...
		
		//Create a Simple Notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_action_icon)
			.setContentTitle("New " + reminder.SERIES_NAME + " Episode!")
			.setContentText(reminder.EPISODE_NAME)
			.setAutoCancel(true);
		
		//Create an explicit intent for Activity in my Application

		// TODO launch main activity and open up the Timeline Fragment
        //Intent resultIntent = new Intent(this, TimelineActivity.class);
        Intent resultIntent = new Intent(this, MainActivity.class);
		//resultIntent.putExtra("REMINDER", reminder);
		
		//The stack builder object will contain an artifact back stack for the
		//started Activity.
		//This Ensures that navigating backwards from the Activity leads out of
		//your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//Add the back stack for the Intent (but not the Intent itself)
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		//Set an ID for the notification, so it can be updated
		int notifyID = 1;
		
		mBuilder.setNumber(0);
		
		nManager.notify(notifyID, mBuilder.build());
	}

}

















