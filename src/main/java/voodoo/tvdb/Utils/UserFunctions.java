package voodoo.tvdb.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import voodoo.tvdb.activity.MainActivity;
import voodoo.tvdb.alarmServices.MyAlarmManager;
import voodoo.tvdb.alarmServices.SyncService;
import voodoo.tvdb.objects.ListObject;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class UserFunctions {

	private JSONParser jsonParser;
	
	//private static String LOGIN_TAG = "login";
	//private static String REGISTER_TAG = "register";
	//private static String ENCRYPTED_TRUE = "true";
	//private static String ENCRYPTED_FALSE = "false";
	
	public static final String KEY_SYNC = "sync";
	public static final String SYNC_TRUE = "t";
	public static final String SYNC_FALSE = "f";
	
	// Context
	private Context context;
	
	// Constructor
	public UserFunctions(Context c){
		
		jsonParser = new JSONParser();
		context = c;
		
	}
	
	/**
	 * Function to make the LoginActivity Request
	 * @param email
	 * @param password
	 * @return
	 */
	public JSONObject loginUser(String email, String password){
		
		// Encode the Password
		password = Base_64.encode(password);
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair("tag", LOGIN_TAG));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		//params.add(new BasicNameValuePair("encrypted", ENCRYPTED_TRUE));
		
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getLoginUrl(context) , params);
		return json;
	}
	
	/**
	 * Function to make RegisterActivity Request
	 * @param name
	 * @param email
	 * @param password
	 * @return
	 */
	public JSONObject registerUser(String name, String email, String password, Boolean trakt){
		
		// Encode the Password
		password = Base_64.encode(password);
				
		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair("tag", REGISTER_TAG));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("trakt_account", trakt ? "true" : "false"));
		//params.add(new BasicNameValuePair("encrypted", ENCRYPTED_TRUE));
		
		// getting JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getRegisterUrl(context), params);
		
		// return JSON
		return json;
	}
	
	/**
	 * Function to get the LoginActivity status
	 * @param context
	 * @return
	 */
	public boolean isUserLoggedIn(){
		DatabaseAdapter db = new DatabaseAdapter(context);
		
		db.open();
		int count = db.getUserCount();
		db.close();
		
		return count > 0;
	}
	
	
	public boolean logoutUser(){
		
		new logoffAsync(context, getSyncStatus()).execute("");
		
		return true;
	}
	
	public void logOffUserStatic(){
		
		boolean syncDevice = getSyncStatus();
		
		DatabaseAdapter db = new DatabaseAdapter(context);
		db.open();
		
		if(syncDevice){
			
			// Delete everything with the Series
			ArrayList<Series> series = db.fetchAllSeries();
			
			if(series != null){
				
				for(int i = 0; i < series.size(); i++){
					
					deleteSeries(series.get(i).ID);
					
				}
			}
			
			// Delete all the Lists
			ListHelper listHelper = new ListHelper(context);
			ArrayList<CharSequence> lists = listHelper.getAllListNames();
			
			if(lists != null){
				
				for(int i = 0; i < lists.size(); i++){
					
					listHelper.deleteListLogOff(lists.get(i).toString());
					
				}
			}
			;
			
			// Insert the 3 default Lists
			db.insertList(new ListObject(context.getResources().getString(R.string.list_favorite_name), context.getResources().getString(R.string.list_favorite_description)));
        	db.insertList(new ListObject(context.getResources().getString(R.string.list_watching_name), context.getResources().getString(R.string.list_watching_description)));
        	db.insertList(new ListObject(context.getResources().getString(R.string.list_to_watch_name), context.getResources().getString(R.string.list_to_watch_description)));
        	
		}
			
		// Delete user info
		db.deleteAllUsers();
		db.deleteFlag(KEY_SYNC);
		db.deleteFlag(SyncService.KEY_FIRST_SYNC);
		
		db.close();
	}
	
	public boolean setSync(boolean sync){
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        
		SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean(KEY_SYNC, sync);
    	
    	editor.commit();
		
		return true;
	}
	
	public boolean getSyncStatus(){
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		
		boolean result = settings.getBoolean(KEY_SYNC, false);
		
		return result;
		
	}
	
	public HashMap<String, String> getUser(){
		
		DatabaseAdapter db = new DatabaseAdapter(context);
		
		db.open();
		
		HashMap<String,String> user = db.getUserDetails();
		
		db.close();
		
		return user;
		
	}
	
	private void deleteSeries(String iD) {
		
		// Database Adapter
		DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
		
		// Open the database
		dbAdapter.open();
    	
    	//Delete the Favorite Files
    	dbAdapter.deleteSeries(iD);
    	dbAdapter.deleteAllEpisode(iD);
    	dbAdapter.deleteWatchedSeries(iD);
    	
   
    	//Remove Alarms before deleting Reminder Files
		MyAlarmManager myAlarmManager = new MyAlarmManager(context, iD);
		myAlarmManager.removeAlarms();
    	
		//Delete all the reminders
		dbAdapter.deleteAllReminders(iD);
		
		//Delete all the watched Pending
		WatchedHelper wHelper = new WatchedHelper(context);
		wHelper.removedWatchedBySeries(iD);
		
		//Delete all of the Queue
		dbAdapter.deleteQueueSeries(iD);

    	// Close Database connection
    	dbAdapter.close();
	
    }
	
	/**
     * AsyncTask to save the download data into the SQLiteDB
     */
    private class logoffAsync extends AsyncTask<String, Void, String> {

    	private Context context;
    	private ProgressDialog dialog;
    	private boolean syncDevice;
    	
    	public logoffAsync(Context c, boolean sync){
    		context = c;
    		dialog = new ProgressDialog(context);
    		syncDevice = sync;
    	}
    	
    	@Override
    	protected void onPreExecute(){
    		dialog.setMessage("Logging off...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			
			DatabaseAdapter db = new DatabaseAdapter(context);
			db.open();
			
			if(syncDevice){
				
				// Delete everything with the Series
				ArrayList<Series> series = db.fetchAllSeries();
				
				if(series != null){
					
					for(int i = 0; i < series.size(); i++){
						
						deleteSeries(series.get(i).ID);
						
					}
				}
				
				// Delete all the Lists
				ListHelper listHelper = new ListHelper(context);
				ArrayList<CharSequence> lists = listHelper.getAllListNames();
				
				if(lists != null){
					
					for(int i = 0; i < lists.size(); i++){
						
						listHelper.deleteListLogOff(lists.get(i).toString());
						
					}
				}
				;
				
				// Insert the 3 default Lists
				db.insertList(new ListObject(context.getResources().getString(R.string.list_favorite_name), context.getResources().getString(R.string.list_favorite_description)));
	        	db.insertList(new ListObject(context.getResources().getString(R.string.list_watching_name), context.getResources().getString(R.string.list_watching_description)));
	        	db.insertList(new ListObject(context.getResources().getString(R.string.list_to_watch_name), context.getResources().getString(R.string.list_to_watch_description)));
	        	
			}
				
			// Delete user info
			db.deleteAllUsers();
			db.deleteFlag(KEY_SYNC);
			db.deleteFlag(SyncService.KEY_FIRST_SYNC);
			
			db.close();
			
			return "User Logged off";
			
		}

		@Override
		protected void onPostExecute(String toast){
			dialog.dismiss();
			Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
			
			Intent i = new Intent(context, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);
		}
		
		private void deleteSeries(String iD) {
			
			// Database Adapter
			DatabaseAdapter dbAdapter = new DatabaseAdapter(context);
			
			// Open the database
			dbAdapter.open();
	    	
	    	//Delete the Favorite Files
	    	dbAdapter.deleteSeries(iD);
	    	dbAdapter.deleteAllEpisode(iD);
	    	dbAdapter.deleteWatchedSeries(iD);
	    	
	   
	    	//Remove Alarms before deleting Reminder Files
			MyAlarmManager myAlarmManager = new MyAlarmManager(context, iD);
			myAlarmManager.removeAlarms();
	    	
			//Delete all the reminders
			dbAdapter.deleteAllReminders(iD);
			
			//Delete all the watched Pending
			WatchedHelper wHelper = new WatchedHelper(context);
			wHelper.removedWatchedBySeries(iD);
			
			//Delete all of the Queue
			dbAdapter.deleteQueueSeries(iD);

	    	// Close Database connection
	    	dbAdapter.close();
		
	    }
    }

}


































