package voodoo.tvdb.alarmServices;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import voodoo.tvdb.activity.MainActivity;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.FavoriteBundle;
import voodoo.tvdb.Objects.ListItem;
import voodoo.tvdb.Objects.ListObject;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ListHelper;
import voodoo.tvdb.utils.ListSyncFunctions;
import voodoo.tvdb.utils.Reminders;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.utils.UserFunctions;
import voodoo.tvdb.utils.WatchedHelper;
import voodoo.tvdb.utils.WatchedSyncFunctions;
import voodoo.tvdb.XMLHandlers.XmlHandlerFetchAllSeriesInfo;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * 
 * SyncService is used to sync List, List_items, and Watched 
 * to and from the server.
 * 
 * @author VoodooXTC
 *
 */

public class SyncService extends WakeReminderIntentService{
	
	/** Database Keys */
	public static final String KEY_FIRST_SYNC = "first_sync";

	private static final String TAG = "SyncService";
	
	// Notification Builder
	private NotificationCompat.Builder mBuilder;
	
	// Notification Manager
	private NotificationManager nManager;
	
	// Notification ID
	int notifyID = 3;

	// Max Progress
	int MAX_PROGRESS = 100; // Set Progress to 0 percent out of 100
	
	public SyncService() {
		super("SyncService");
	}

	// Get progress given Max and Current and a percentage max of 50%
	private int getProgress(int max, int current){
		
		int percentage_max = 50;
		
		return ( current * percentage_max)/max;
	}
	
	private int getProgress(int max, int current, int total_sections, int section){
		
		double percentage_max = section / total_sections;
		
		int result =  (int) ((int) (current * max)/percentage_max);
		
		return result;
	}
	
	@Override
	void doReminderWork(Intent intent){
		
		//Create a Simple Notification
		mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_action_dl_from_cloud)
			.setContentTitle("VoodooTVDB")
			.setContentText("Synching...")
			.setAutoCancel(true);
		
		//Create an explicit intent for Activity in my Application
		Intent resultIntent = new Intent(this, MainActivity.class);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		
		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Set Progress to Zero
		mBuilder.setProgress(MAX_PROGRESS, 0, false);
		
		nManager.notify(notifyID, mBuilder.build());
		
		/**
		 * Get User Info
		 */
		UserFunctions userFunctions = new UserFunctions(this);
		HashMap<String,String> user = userFunctions.getUser();
		
		DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
		dbAdapter.open();
		
		String firstSync = dbAdapter.fetchFlag(KEY_FIRST_SYNC);
		
		if(firstSync == null){
			
			/**
			 * Check if its the first time Sync is run
			 */
			try {
				
				// Sync Watched Episodes to server first
				firstTimeSyncTest(user);
				
				// Sync from the server everything...
				syncFromServer(user);
				
				// Successful
				// Set the firstSync Flag to "FALSE"
				dbAdapter.insertFlag(KEY_FIRST_SYNC, "FALSE");
				
			} catch (JSONException e) {

				// Something went wrong with the first time sync
				// Remove the FIRST SYNC Flag from the database
				dbAdapter.deleteFlag(KEY_FIRST_SYNC);
				e.printStackTrace();
			}
			
		}
		
		dbAdapter.close();
		
		/**
		 * 
		 * Sync List (with KeyCreated, KeySynched), 
		 * 		ListItems (with KeyCreated, KeySynched), 
		 * 		and Watched Pending 
		 * 
		 * 		TO the server
		 * 
		 */
		syncToServer(user);
		
		Log.d(TAG, "Dismiss notification");
		
		// Dismiss Notification
		nManager.cancel(notifyID);
		
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(i);
		
	}
	
	/**
	 * If its the first time sync, try to synchronize the 
	 * watched episodes to the server
	 *  
	 * @param user
	 * @throws org.json.JSONException
	 */
	@SuppressLint("UseSparseArrays")
	private void firstTimeSyncTest(HashMap<String,String> user) throws JSONException {
		
		if(user != null){
			
			Log.d(TAG, "Check for first time sync");
			
			DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
			dbAdapter.open();
			
			String firstSync = dbAdapter.fetchFlag(KEY_FIRST_SYNC);
			
			if(firstSync == null){
				
				// Update Notification
				mBuilder.setContentTitle("VoodooTVDB - First Time Sync")
				.setSmallIcon(R.drawable.ic_action_ul_to_cloud)
				.setProgress(MAX_PROGRESS, 0, false);
				
				nManager.notify(notifyID, mBuilder.build());
				
				/**
				 * Try to synchronize all the watched episodes
				 * 
				 * if unsuccessful add them to the watch pending for
				 * them to get synchronized at a regular update
				 */
				
				Log.d(TAG, "First time sync");
				
				// User Email
				String email = user.get("email");
				
				// Get list of ALL the Series in the SQLite database
				ArrayList<Series> series = dbAdapter.fetchAllSeries();

				if(series != null){
					
					// Watched Sync Functions
					WatchedSyncFunctions watchedSync = new WatchedSyncFunctions(this);
					
					Log.d(TAG, series.size() + " many series found to sync");
					
					// Loop through Series_id ArrayList (if any)
					for(int i = 0; i < series.size(); i++){
						
						// Update Notification
						mBuilder.setProgress(MAX_PROGRESS, this.getProgress(series.size(), i, 1, 1), false);
						nManager.notify(notifyID, mBuilder.build());
						
						// Fetch all the watched episodes for the series sorted by Season Number and Episode Number
						ArrayList<String> watchedEpisodeIds = dbAdapter.fetchWatchedBySeries(series.get(i).ID);
						
						if(watchedEpisodeIds != null){
							
							// String Items to sync to the server
							String syncItems = null;
							
							//asdfasdf;
							Log.d(TAG, series.get(i).TITLE + " has " + watchedEpisodeIds.size() + " watched to sync");
							
							int counter = 0;
							// Loop through each watched episode
							for(int k = 0; k < watchedEpisodeIds.size(); k++){
								
								Log.d(TAG, "fetch watched episode to add to pending: " + watchedEpisodeIds.get(k));
								
								// Get watched episode and extract Season Number and Episode Number
								Episode e = dbAdapter.fetchEpisode(watchedEpisodeIds.get(k));
								
								// If episode not on Watched Pending table, add it just in case something goes wrong with the sync
								if(!dbAdapter.isEpisodeWatchedPending(e.ID)){
									
									dbAdapter.insertWatchedPending(e);
								
								}
								
								syncItems = syncItems == null ? e.ID : syncItems + "," + e.ID;
								
								// Add one to counter
								counter++;
								
								if(counter == 12){
									
									Log.d(TAG, "items to be created: " + syncItems);
									
									// Sync Deleted Watched Pending to the server
									JSONObject result = watchedSync.addWatched(series.get(i).ID, syncItems, email);
									
									try{
										
										if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
											
											// Delete Watched Pending for Series
											// Just on the initial one is fine we do this...because its the only episodes
											// in the Pending Watched table
											dbAdapter.deleteWatchedPendingSeries(series.get(i).ID);
										
										}
										
									}catch (JSONException er) {
										er.printStackTrace();
									}
									
									// Reset Counter
									counter = 0;
									
									// Reset syncItems
									syncItems = null;
								}
									
								
							}// END of watched episodes 
						
							if( syncItems != null){
								
								Log.d(TAG, "items to be created: " + syncItems);

								// Sync Deleted Watched Pending to the server
								JSONObject result = watchedSync.addWatched(series.get(i).ID, syncItems, email);
								
								try{
									
									if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
										
										// Delete Watched Pending for Series
										dbAdapter.deleteWatchedPendingSeries(series.get(i).ID);
									
									}
									
								}catch (JSONException e) {
									e.printStackTrace();
								}
								
							}
							
						}
					
					}// END of Series_id ArrayList
					
				}
				
			}else{
				
				// Not first time we sync...should we do something...think not
				// Do nothing...yeah that sounds good for now
				
			}
			
			dbAdapter.close();
			
		}
		
	}
	
	private void syncFromServer(HashMap<String,String> user){
		
		if(user != null){
			
			// List Sync
			ListSyncFunctions listSync = new ListSyncFunctions(this);
			
			// Create a SERIES_ID ArrayList
			ArrayList<String> SERIES_IDS = new ArrayList<String>();
			
			// Get a a List for all the list the user has created
			String email = user.get("email");
			JSONObject lists = listSync.getAllLists(email);
			
			try {
				
				// Check for success
				if(lists.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
					
					// Update Notification
					mBuilder.setContentTitle("VoodooTVDB - Sync From Server")
					.setProgress(MAX_PROGRESS, 0, false)
					.setSmallIcon(R.drawable.ic_action_dl_from_cloud);
					
					nManager.notify(notifyID, mBuilder.build());
					
					// Get List names
					JSONArray items = lists.getJSONArray(ListSyncFunctions.NODE_ITEMS);
					
					// Loop through the lists
					for(int i = 0; i < items.length(); i++){
						
						// Update progress bar
						mBuilder.setProgress(MAX_PROGRESS, getProgress(items.length(), i), false);
						nManager.notify(notifyID, mBuilder.build());
						
						// List name
						String listName = items.getJSONObject(i).getString(ListSyncFunctions.NODE_NAME);
						
						Log.i("SyncService", listName);
						
						// Get all the list items per list
						JSONObject list = listSync.getList(listName, email);
						
						// Check for success
						if(list.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
							
							Log.i("SyncService", listName + " download successful");
							
							// List Manager
							ListHelper listHelper = new ListHelper(this);
							
							// List Object
							ListObject item = new ListObject();
							item.NAME = list.getString(ListSyncFunctions.NODE_NAME);
							item.SLUG = list.getString(ListSyncFunctions.NODE_SLUG);
							item.PRIVACY = list.getString(ListSyncFunctions.NODE_PRIVACY);
							item.DESCRIPTION = list.getString(ListSyncFunctions.NODE_DESCRIPTION);
							item.SHOW_NUMBERS = list.getString(ListSyncFunctions.NODE_SHOW_NUMBERS);
							item.ALLOW_COMMENTS = list.getString(ListSyncFunctions.NODE_ALLOW_COMMENTS);
							item.SYNCHED = ListObject.KEY_TRUE;
							item.CREATED = ListObject.KEY_TRUE;
							item.DELETED = ListObject.KEY_FALSE;
							item.MISC1 = ListObject.KEY_EMPTY;
							item.MISC2 = ListObject.KEY_EMPTY;
							
							Log.i("SyncService", "Slug: " + item.SLUG);
							
							// Add List to SQLite
							listHelper.insertListWithoutSyncFlag(item);
							
							// Get List Items
							JSONArray listItems = list.getJSONArray(ListSyncFunctions.NODE_ITEMS);
							
							// Loop through list items
							if(listItems.length() > 0){
								
								JSONObject type = listItems.getJSONObject(0);
								String sType = type.getString(ListSyncFunctions.NODE_TYPE);
								
								if(!sType.equals(ListSyncFunctions.NODE_EMPTY)){
									
									for( int j = 0; j < listItems.length(); j++){
										
										Log.i("SyncService", listName + " has " + listItems.length() + " list items");
										
										JSONObject li = listItems.getJSONObject(j);
										
										// Add List Item (Series) to SQLite
										ListItem listItem = new ListItem();
										listItem.LIST_NAME = listName;
										listItem.TYPE = li.getString(ListSyncFunctions.NODE_TYPE);
										listItem.SERIES_ID = li.getString(ListSyncFunctions.NODE_SERIES_ID);
										listItem.TITLE = li.getString(ListSyncFunctions.NODE_TITLE);
										listItem.SYNCHED = ListItem.KEY_TRUE;
										listItem.DELETED = ListItem.KEY_FALSE;
										listItem.MISC1 = ListItem.KEY_EMPTY;
										listItem.MISC2 = ListItem.KEY_EMPTY;
										
										listHelper.insertSeriesToListWithoutSyncFlag(listItem);
										
										Log.i("SyncService", "insert " + listItem.TITLE + " to list " + listName);
										
										
										// Add series_id to the SERIES_ID ArrayList, if not already in it
										if( !SERIES_IDS.contains(listItem.SERIES_ID)){
											
											SERIES_IDS.add(listItem.SERIES_ID);
											
											Log.i("SyncService", "Add series " + listItem.TITLE + " to the SERIES_ID arraylist");
											
										}
									
									}// END of List Items loop
									
								}else{
									
									Log.d("SyncService", "List is Empty");
									
								}
								
							}
						}
							
					}// END of List Loop
					
					// Reached 50 percent of progress
					// Update progress bar
					mBuilder.setProgress(MAX_PROGRESS, MAX_PROGRESS/2, false);
					nManager.notify(notifyID, mBuilder.build());
					
					/**
					 * Download the Watched Episodes for each Series
					 */
					if(SERIES_IDS.size() > 0){
						
						Log.i("SyncService", "SERIES_IDS arraylist has " + SERIES_IDS.size() + " items");
						
						// Watched Sync 
						WatchedSyncFunctions watchedSync = new WatchedSyncFunctions(this);
						
						// Watched Helper
						WatchedHelper watchHelper = new WatchedHelper(this);
						
						// ListHelper
						ListHelper listHelper = new ListHelper(this);
						
						// Loop through each item in the SERIES_ID ArrayList
						for(int i = 0; i < SERIES_IDS.size(); i++){
							
							// Update progress bar
							mBuilder.setProgress(MAX_PROGRESS, getProgress(SERIES_IDS.size(),i) + 50, false);
							nManager.notify(notifyID, mBuilder.build());
							
							boolean seriesOnDB;
							
							Log.i("SyncService", "Check is series " + SERIES_IDS.get(i) + " is in our database");
							
							// Check if we have the Show in our SQLite
							if( !listHelper.isSeriesInDB(SERIES_IDS.get(i))){
								
								Log.i("SyncService", "Series " + SERIES_IDS.get(i) + " is not on the database");
								
								// If not, download all the series
								seriesOnDB = downloadSeries(SERIES_IDS.get(i));
								
								// Add an error check for series download...
								// for shame for not doing it before ><
								
								Log.i("SyncService", seriesOnDB ? "Series was downloaded successfully" : "Series was not downloaded successfully");
								
							}else{
								
								seriesOnDB = true;
								
								Log.i("SyncService", "Series is already in the database");
							}

							if(seriesOnDB){
								
								Log.i("SyncService", "Series is on database, download watched");
								
								// Download Watched episodes for the List Item (Show)
								JSONObject watchedEpisodes = watchedSync.getWatched(SERIES_IDS.get(i), email);
								
								// Check for Success
								if( watchedEpisodes.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
									
									Log.i("SyncService", "Watched downloaded successfully");
									
									JSONArray episodes = watchedEpisodes.getJSONArray(WatchedSyncFunctions.NODE_EPISODES);
									
									if(episodes.length() > 0){
									
										Log.i("SyncService", "There are " + episodes.length() + " watched episodes to add");
				
										// If any add to the watched SQLite table
										// NOTE: do not add any of these items to the WatchedPending Table
										for(int j = 0; j < episodes.length(); j++){
											
											JSONObject e = episodes.getJSONObject(j);
											
											String episode_id = e.getString(WatchedSyncFunctions.NODE_EPISODE_ID);
											
											watchHelper.markWatchedWithSyncFlag(episode_id);
											
											Log.i("SyncService", "Add episode " + episode_id + " to the Watched Table");
											
										}
										
									}
									
								}
								
							}else{
								
								// Series is NOT on the database and couldn't be downloaded
								
								// Delete everything for that series
								deleteSeries(SERIES_IDS.get(i), this);
							}
							
						}// END of SERIES_ID ArrayList Loop
						
					}
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	@SuppressLint("UseSparseArrays")
	private void syncToServer(HashMap<String,String> user){
		
		if(user != null){
			
			// Update Notification
			mBuilder.setContentTitle("VoodooTVDB - Sync To Server")
			.setProgress(MAX_PROGRESS, 0, false)
			.setSmallIcon(R.drawable.ic_action_ul_to_cloud);
			
			nManager.notify(notifyID, mBuilder.build());
			
			// List Sync
			ListSyncFunctions listSync = new ListSyncFunctions(this);
			
			// List Helper
			ListHelper listHelper = new ListHelper(this);
			
			// Get a a List for all the list the user has created
			String email = user.get("email");
			
			// Get all lists that have been deleted
			ArrayList<ListObject> listDeleted = listHelper.getDeletedLists();
					
			if(listDeleted != null){
				
				Log.d(TAG, "There are " + listDeleted.size() + " list deleted to be synched");

				// Loop through them
				for(int i = 0; i < listDeleted.size(); i++){
					
					Log.d(TAG, "Synching deleted list " + listDeleted.get(i).NAME);
					
					// Update Notification
					mBuilder.setProgress(MAX_PROGRESS, this.getProgress(MAX_PROGRESS, i, 5, 1), false);
					nManager.notify(notifyID, mBuilder.build());
					
					// Delete from server
					JSONObject result = listSync.deleteList(listDeleted.get(i).NAME, email);
					
					try{

						// If successful, delete from SQLite
						if(result.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
							
							Log.d(TAG, "Deleted Synched successful");
							
							listHelper.deleteList(listDeleted.get(i).NAME);
							
						}else{
							Log.d(TAG, "Deleted Sync was not successful");
						}
					}catch (JSONException e) {
						e.printStackTrace();
					}
				
				}// END of DELETED lists
				
			}
			
			// Get all list items deleted
			ArrayList<ListItem> itemsDeleted = listHelper.getDeletedListItems();
			
			if(itemsDeleted != null){
				
				Log.d(TAG, "There are " + itemsDeleted.size() + " list items to be deleted");
				
				// Loop through them
				for(int i = 0; i < itemsDeleted.size(); i++){
					
					Log.d(TAG, "Deleting item " + itemsDeleted.get(i).TITLE + " from list " + itemsDeleted.get(i).LIST_NAME);
					
					// Update Notification
					mBuilder.setProgress(MAX_PROGRESS, this.getProgress(MAX_PROGRESS, i, 5, 2), false);
					nManager.notify(notifyID, mBuilder.build());

					// Delete from server
					JSONObject result = listSync.deleteListItem(itemsDeleted.get(i).LIST_NAME, itemsDeleted.get(i).SERIES_ID, email);
					
					try{
						
						// If successful, delete from SQLite
						if(result.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
							
							listHelper.deleteListItem(itemsDeleted.get(i));
							
							Log.d(TAG, "List item deleted successfully");
							
						}else{
							
							Log.d(TAG, "List item was not deleted successfully");
							
						}
						
					}catch (JSONException e) {
						e.printStackTrace();
					}
					
					
					
				}// END of DELETED LIST ITEMS
				
			}
			
			
			
			// get a all the Lists that have not yet been created
			ArrayList<ListObject> listsUncreated = listHelper.getUncreatedLists();
			
			if(listsUncreated != null){
				
				Log.d(TAG, "There are " + listsUncreated.size() + " uncreated lists");
				
				// Loop through the lists and sync them to the server
				for(int i = 0; i < listsUncreated.size(); i++){
					
					Log.d(TAG, "Creating list " + listsUncreated.get(i).NAME);
					
					// Update Notification
					mBuilder.setProgress(MAX_PROGRESS, this.getProgress(MAX_PROGRESS, i, 5, 3), false);
					nManager.notify(notifyID, mBuilder.build());

					// Sync to the server
					JSONObject result = listSync.addList(listsUncreated.get(i).NAME, listsUncreated.get(i).DESCRIPTION, email);
					
					try{
						
						// if successful update list to CREATED=TRUE and SYNCED=TRUE
						if(result.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
							
							ListObject l = listsUncreated.get(i);
							l.CREATED = ListObject.KEY_TRUE;
							l.SYNCHED = ListObject.KEY_TRUE;
							
							listHelper.insertListWithoutSyncFlag(l);
							
							Log.d(TAG, "List created successfully");
							
						}else{
							
							Log.d(TAG, "List not created successfully");
							
						}
						
					}catch (JSONException e) {
						e.printStackTrace();
					}
					
				
				}// END of list loop
				
			}
			
			// Get all List Items that have not been synch'd
			ArrayList<ListItem> items = listHelper.getUnsynchedListItems();

			if(items != null){
				
				Log.d(TAG, "There are " + items.size() + " list items that have not been synched");
				
				// Loop through them
				for(int i = 0; i < items.size(); i++){
					
					Log.d(TAG, "Synching " + items.get(i).TITLE + " to list " + items.get(i).LIST_NAME);
					
					// Update Notification
					mBuilder.setProgress(MAX_PROGRESS, this.getProgress(MAX_PROGRESS, i, 5, 4), false);
					nManager.notify(notifyID, mBuilder.build());

					// Sync to server
					JSONObject result = listSync.addListItem(items.get(i).LIST_NAME, items.get(i).SERIES_ID, email);
					
					try{
						
						// If successful, update list item to SYNCHED=TRUE
						if(result.getString(ListSyncFunctions.NODE_SUCCESS).equals("1")){
							
							listHelper.insertSeriesToListWithoutSyncFlag(items.get(i));
							
							Log.d(TAG, "List item syched successfully");
							
						}else{
							
							Log.d(TAG, "List item not syched successfully");
							
						}
						
					}catch (JSONException e) {
						e.printStackTrace();
					}
					
					
					
				}// END of List Items
				
			}
			
			// Watched Helper
			WatchedHelper wHelper = new WatchedHelper(this);
			
			// Watched Sync Functions
			WatchedSyncFunctions wSync = new WatchedSyncFunctions(this);
			
			// Database Adapter
			DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
			
			// Open Database
			dbAdapter.open();
			
			// Get list of all Series on SQLite database
			ArrayList<Series> series = dbAdapter.fetchAllSeries();
			
			// Close Database Adapter
			dbAdapter.close();
			
			if(series != null){
			
				Log.d(TAG, "Checking " + series.size() + " series for Pending Watched Items");
				
				// Loop through the series
				for(int k = 0; k < series.size(); k++){
					
					Log.d(TAG, "Checking series " + series.get(k).TITLE + " for deleted watched items");
					
					// Update Notification
					mBuilder.setProgress(MAX_PROGRESS, this.getProgress(MAX_PROGRESS, k, 5, 5), false);
					nManager.notify(notifyID, mBuilder.build());
					
					// Series ID
					String series_id = series.get(k).ID;
					
					// Fetch all the Deleted Watched Pending for that series
					ArrayList<Episode> deletedWatchedPending = wHelper.getAllDeletedWatchedPendingBySeriesId(series_id);
					
					// Create a long string of "Series_id", separated by commas and do the rest of the work on the server..
					if(deletedWatchedPending != null){
						
						Log.d(TAG, series.get(k).TITLE + " has " + deletedWatchedPending.size() + " watched items to be deleted");
						
						// String Items to sync to the server
						String syncItems = null;
						
						int counter = 0;
						// Loop through the deleted watched pending
						for(int i = 0; i < deletedWatchedPending.size(); i++){
							
							syncItems = syncItems == null ? deletedWatchedPending.get(i).ID : syncItems + "," + deletedWatchedPending.get(i).ID;
							
							counter++;
							
							if(counter == 12){
								
								Log.d(TAG, "items to be deleted: " + syncItems);
								// Sync Deleted Watched Pending to the server
								JSONObject result = wSync.removeWatched(series_id, syncItems, email);
								try{
									
									if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
										
										// if successful, delete the deleted Episodes from the Pending Watched table
										wHelper.deleteDeletedWatchedPendingByBatch(syncItems);
										
									}else{
										
										Log.d(TAG, "Deleted watched items did not sync successfully");
										
									}
									
								}catch (JSONException e) {
									e.printStackTrace();
								}
								
								// Reset counter
								counter = 0;
								
								// Reset syncItems
								syncItems = null;
								
							}
							
						}// END of deleted watched pending
					
						if(syncItems != null){
							
							Log.d(TAG, "items to be deleted: " + syncItems);

							// Sync Deleted Watched Pending to the server
							JSONObject result = wSync.removeWatched(series_id, syncItems, email);
							
							try{
								
								if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
									
									// if successful, delete the deleted Episodes from the Pending Watched table
									wHelper.deleteDeletedWatchedPendingBySeries(series_id);
									
								}else{
									
									Log.d(TAG, "Deleted watched items did not sync successfully");
									
								}
								
							}catch (JSONException e) {
								e.printStackTrace();
							}
							
						}
					}
					
					// Fetch all the UnSynched Watched Pending for that series
					ArrayList<Episode> unsyncWatchedPending = wHelper.getAllUnsyncWatchedPendingBySeriesId(series_id);
					
					// Create a long string of "Series_id", separated by commas and do the rest of the work on the server..
					if(unsyncWatchedPending != null){
						
						Log.d(TAG, series.get(k).TITLE + " has " + unsyncWatchedPending.size() + " watched items to be created");
						
						// String Items to sync to the server
						String syncItems = null;
						
						int counter = 0;
						// Loop through the deleted watched pending
						for(int i = 0; i < unsyncWatchedPending.size(); i++){
							
							syncItems = syncItems == null ? unsyncWatchedPending.get(i).ID : syncItems + "," + unsyncWatchedPending.get(i).ID;
							
							counter++;
							
							if(counter == 12){
								
								Log.d(TAG, "items to be created: " + syncItems);
								// Sync Deleted Watched Pending to the server
								JSONObject result = wSync.addWatched(series_id, syncItems, email);
								try{
									
									if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
										
										// if successful, delete the Episodes from the Pending Watched table
										wHelper.deleteDeletedWatchedPendingByBatch(syncItems);
										
									}else{
										
										Log.d(TAG, "Created watched items did not sync successfully");
										
									}
									
								}catch (JSONException e) {
									e.printStackTrace();
								}
								
								// Reset counter
								counter = 0;
								
								// Reset syncItems
								syncItems = null;
								
							}
					
						}// END of deleted watched pending
						
						if( syncItems != null){
							
							Log.d(TAG, "items to be created: " + syncItems);

							// Sync Deleted Watched Pending to the server
							JSONObject result = wSync.addWatched(series_id, syncItems, email);
							
							try{
								
								if(result.getString(WatchedSyncFunctions.NODE_SUCCESS).equals("1")){
									
									// if successful, delete the created Episodes from the Pending Watched table
									wHelper.deleteUnsynchedWatchedPendingBySeries(series_id);
								
								}
								
							}catch (JSONException e) {
								e.printStackTrace();
							}
							
						}
					}
				}
				
			}// END of series loop
			
		}
		
		
	}
	
	private boolean downloadSeries(String series_id){
		
		Log.d("SyncService: DownloadSeries", series_id);
		
		try{
			//Series not in Database, DownLoad and save
			//URL
			//String onlineString = "http://voodootvdb.com/getAllSeries.php?ID=" + series_id;
			
			URL url = new URL(ServerUrls.getAllSeriesUrl(this, series_id));
			
			SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
			SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			XMLReader mXMLReader = mySAXParser.getXMLReader();
			XmlHandlerFetchAllSeriesInfo xmlHandler = new XmlHandlerFetchAllSeriesInfo(this);
			mXMLReader.setContentHandler(xmlHandler);
        	mXMLReader.parse(new InputSource(url.openStream()));
        	
        	//Get the series info and episodes info 
        	FavoriteBundle fb = new FavoriteBundle();
        	fb.SERIES = xmlHandler.getSeries();
			fb.EPISODES  = xmlHandler.getEpisodesList();
			
			if(fb.SERIES.ID == null || fb.EPISODES.size() == 0){
				
				return false;
				
			}
			
			DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
			dbAdapter.open();
			
			dbAdapter.insertSeries(fb.SERIES);
			
			//Insert each episode & create a reminder for each
			ArrayList<Reminder> reminders = new ArrayList<Reminder>();
			for(int i = 0; i < fb.EPISODES.size(); i++){
				Reminder r = new Reminder();
				
				r.EPISODE_ID = fb.EPISODES.get(i).ID;
				r.DATE = fb.EPISODES.get(i).FIRST_AIRED;
				r.TIME = fb.SERIES.AIRS_TIME;
				r.SERIES_NAME = fb.SERIES.TITLE;
				r.EPISODE_NAME = fb.EPISODES.get(i).TITLE;
				r.SEASON_NUMBER = fb.EPISODES.get(i).SEASON_NUMBER;
				r.EPISODE_NUMBER = fb.EPISODES.get(i).EPISODE_NUMBER;
				r.SERIES_ID = fb.SERIES.ID;
				r.RATING = fb.EPISODES.get(i).RATING;
				r.OVERVIEW = fb.EPISODES.get(i).OVERVIEW;
				r.GUESTSTARS = fb.EPISODES.get(i).GUEST_STARS;
				r.IMAGE_URL = fb.SERIES.POSTER_URL;
				
				//Insert the episode and save the Reminder in the ArrayList
				dbAdapter.insertEpisode(fb.EPISODES.get(i));
				reminders.add(r);
			}
			
			dbAdapter.close();
			
			Reminders reminderManager = new Reminders(this, reminders);
			reminderManager.open();
			reminderManager.addReminders();
			reminderManager.close();
			
			//Create Alarms from Reminder Files
			MyAlarmManager myAlarmManager = new MyAlarmManager(this, fb.SERIES.ID);
			myAlarmManager.addAlarms();
		
		}catch (MalformedURLException e) {
			
			e.printStackTrace();
			return false;
			
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
			return false;
			
		} catch (SAXException e) {

			e.printStackTrace();
			return false;
			
		} catch (IOException e) {

			e.printStackTrace();
			return false;
			
		}
		
		return true;
	}

	private void deleteSeries(String iD, Context c) {
		
		// Database Adapter
		DatabaseAdapter dbAdapter = new DatabaseAdapter(c);
		
		// Open the database
		dbAdapter.open();
    	
    	// Delete from Lists
    	ListHelper listHelper = new ListHelper(this);
    	
    	// Check is series is already on any lists
		ArrayList<String> listNamesSeriesIsOn = listHelper.getListNamesSeriesIsOn(iD); 
		if(listNamesSeriesIsOn != null){
			
			// Series is saved and on one or more lists
			// Flag them as deleted
			
			// TODO
			// Aki esta el pedo...la serie esta en listas y no se pudo bajar.
			// 1. Marcar la Serie de alguna manera para ke se trate de bajar otra vez
			// PENDEJO!
			listHelper.deleteListItem(iD, listNamesSeriesIsOn);
			
		}
    	
    	//Delete the Favorite Files
    	dbAdapter.deleteSeries(iD);
    	dbAdapter.deleteAllEpisode(iD);
    	dbAdapter.deleteWatchedSeries(iD);
    	
   
    	//Remove Alarms before deleting Reminder Files
		MyAlarmManager myAlarmManager = new MyAlarmManager(c, iD);
		myAlarmManager.removeAlarms();
    	
		//Delete all the reminders
		dbAdapter.deleteAllReminders(iD);
		
		//Delete all the watched Pending
		WatchedHelper wHelper = new WatchedHelper(this);
		wHelper.removedWatchedBySeries(iD);
		
		//Delete all of the Queue
		dbAdapter.deleteQueueSeries(iD);

    	// Close Database connection
    	dbAdapter.close();
	
    }
}



























