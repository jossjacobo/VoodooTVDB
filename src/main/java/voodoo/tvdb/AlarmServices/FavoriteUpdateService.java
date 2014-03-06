package voodoo.tvdb.alarmServices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import voodoo.tvdb.activity.MainActivity;
import voodoo.tvdb.objects.Episode;
import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.xmlHandlers.XmlHandlerFetchAllSeriesInfo;
import voodoo.tvdb.xmlHandlers.XmlHandlerHot;
import voodoo.tvdb.xmlHandlers.XmlHandlerServertime;
import voodoo.tvdb.xmlHandlers.XmlHandlerUpdate;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;
import voodoo.tvdb.utils.Reminders;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.utils.UserFunctions;

public class FavoriteUpdateService extends WakeReminderIntentService{
	
	private static final String TAG = "FavoriteUpdateService";
	private static final String KEY_SERVERTIME = "servertime";
	private static final String KEY_SERVERTIME_HOT = "servertime_hot";
	
	public FavoriteUpdateService() {
		super(TAG);
	}
	
	@Override
	void doReminderWork(Intent intent) {
		
		/**
		 * Define Note
		 */
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// Notification Manager
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("VoodooTVDB")
		.setContentText("Update")
		.setSmallIcon(R.drawable.ic_action_dl_from_cloud);
		
		// Create Intent to launch when Notification gets clicked
		Intent notificationIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntent(notificationIntent);
		PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pi);
		
		// Create notification
		manager.notify(1, mBuilder.build());

		//List of ID's
		ArrayList<String> SeriesIds = new ArrayList<String>();
		ArrayList<Series> HotSeriesList;
		
		DatabaseAdapter dbAdapter = new DatabaseAdapter(getApplicationContext());
		dbAdapter.open();
		
		//Fetch LastUpdate server time in DB
		String lastupdatetime = dbAdapter.fetchFlag(KEY_SERVERTIME);
		String lastUpdateTimeHot = dbAdapter.fetchFlag(KEY_SERVERTIME_HOT);
		
		Log.d(TAG, "local lastupdatetime is " + lastupdatetime);
		Log.d(TAG, "local lastupdatetime HOT is " + lastUpdateTimeHot);
		
		if(lastupdatetime == null){
			
			updateServerTime(dbAdapter, KEY_SERVERTIME);
			
			dbAdapter.close();
			
			// Dismiss Notification
		    manager.cancel(1);
			
		}else{
			
			// Grab the list of Episode and Series ID's that have been updated
			String urlString = ServerUrls.getUpdateUrl(getApplicationContext(), lastupdatetime);
			//String urlString = "http://voodootvdb.com/update.php?timestamp=" + lastupdatetime;
			
			Log.d(TAG, urlString);
			
			//Store the full id list in separate ArrayLists
			ArrayList<String> FullSeriesIds = new ArrayList<String>();
			
			//Get two ArrayList one for series id and the other for episode id
			try {
				
				//Create the XML parser
				SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
				SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
				XMLReader mXMLReader = mySAXParser.getXMLReader();
				XmlHandlerUpdate xmlHandler = new XmlHandlerUpdate();
				mXMLReader.setContentHandler(xmlHandler);
				
				//Create URL
				URL url = new URL(urlString);
				mXMLReader.parse(new InputSource(url.openStream()));
				
				//Get the ID's
				FullSeriesIds = xmlHandler.getSeries();
				
			}catch (MalformedURLException e) {
				Log.d(TAG, "MalformedURLException");
				e.printStackTrace();
			}catch (ParserConfigurationException e) {
				Log.d(TAG, "ParserConfigurationException");
				e.printStackTrace();
			}catch (SAXException e) {
				Log.d(TAG, "SAXException");
				e.printStackTrace();
			}catch (IOException e) {
				Log.d(TAG, "IOException");
				e.printStackTrace();
			}
			
			//Match the ID's list to the ones in our database
			//Match SeriesIds first
			for(int i = 0; i < FullSeriesIds.size(); i++){
				String dlID = FullSeriesIds.get(i);
				if(dbAdapter.isSeriesFavorited(dlID)){
					Log.d(TAG, "series " + dlID + " added to list");
					SeriesIds.add(dlID);
				}
			}

			// Max Progress and Progress Status
			final int MAX_PROGRESS;
			int mProgressStatus = 0;
			
			//If we have any matches download them
			MAX_PROGRESS = SeriesIds.size() + 1;
			
			/**
			 * 
			 * Update the Series and All its Episodes
			 * 
			 */
			for(int i = 0; i < SeriesIds.size(); i++){
				
				Log.d(TAG, "Downloading Series " + SeriesIds.get(i));
				
				//Update the progress bar
				mBuilder.setProgress(MAX_PROGRESS, mProgressStatus, false);
				mBuilder.setAutoCancel(true);
				manager.notify(1, mBuilder.build());
				
				// URL
				String urlSeriesString = ServerUrls.getAllSeriesUrl(getApplicationContext(), SeriesIds.get(i));
				//String urlSeriesString = "http://voodootvdb.com/getAllSeries.php?ID=" + SeriesIds.get(i);
				
				// Series Object to insert
				Series s = null;
				
				// Episodes
				ArrayList<Episode> el = null;
				
				// Get two ArrayList one for series id and the other for episode id
				try {
					
					// Create the XML parser
					SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
					SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
					XMLReader mXMLReader = mySAXParser.getXMLReader();
					XmlHandlerFetchAllSeriesInfo xmlHandler = new XmlHandlerFetchAllSeriesInfo(this);
					mXMLReader.setContentHandler(xmlHandler);
					
					// Create URL
					URL url = new URL(urlSeriesString);
					mXMLReader.parse(new InputSource(url.openStream()));
					
					// Get Series
					s = xmlHandler.getSeries();
					
					// Get Episodes
					el = xmlHandler.getEpisodesList();
					
				}catch (MalformedURLException e) {
					Log.d(TAG, "MalformedURLException");
					e.printStackTrace();
				}catch (ParserConfigurationException e) {
					Log.d(TAG, "ParserConfigurationException");
					e.printStackTrace();
				}catch (SAXException e) {
					Log.d(TAG, "SAXException");
					e.printStackTrace();
				}catch (IOException e) {
					Log.d(TAG, "IOException");
					e.printStackTrace();
				}
				
				// Update the Series on the DataBase
				if(s != null){
					dbAdapter.updateSeries(s);
				}
				
				// Update the Episodes
	        	ArrayList<Reminder> remin = new ArrayList<Reminder>();
	    		
	        	if( el != null && el.size() > 0 ){
	    			
	        		// Delete all Episodes
	        		//dbAdapter.deleteAllEpisode(s.ID);
	    			
	        		for(int j = 0; j < el.size(); j++){
	    				
	        			// Episode
	    				Episode e = el.get(j);
	    				
	    				// If Episode is already on database update, if not insert it
	    				if(dbAdapter.isEpisodeFavorites(e.ID)){
							//Episode is on DataBase, update
							dbAdapter.updateEpisode(e);
						}else{
							//Episode is not on DataBase, insert
							dbAdapter.insertEpisode(e);
						}
	    				
	    				// Create an reminder for this episode
	    				Reminder r = new Reminder();
	    				
	    				r.EPISODE_ID = e.ID;
	    				r.DATE = e.FIRST_AIRED;
	    				r.TIME = s.AIRS_TIME;
	    				r.SERIES_NAME = s.TITLE;
	    				r.EPISODE_NAME = e.TITLE;
	    				r.SEASON_NUMBER = e.SEASON_NUMBER;
	    				r.EPISODE_NUMBER = e.EPISODE_NUMBER;
	    				r.SERIES_ID = s.ID;
	    				r.RATING = e.RATING;
	    				r.OVERVIEW = e.OVERVIEW;
	    				r.GUESTSTARS = e.GUEST_STARS;
	    				r.IMAGE_URL = s.POSTER_URL;
	    					
	    				remin.add(r);
	    				
	    			}
	    		}
				
				// Update all the reminders for the Series
	        	// NOTE: reminder serve to power the "Upcoming Section" of the application
				Reminders reminderManager = new Reminders(getApplicationContext(), remin);
				reminderManager.open();
				reminderManager.deleteAllReminders();
				reminderManager.addReminders();
	        	reminderManager.close();
				
	        	// Create Alarms from Reminder Files
	        	// NOTE: These are the actual reminder alerts
				MyAlarmManager myAlarmManager = new MyAlarmManager(getApplicationContext());
				myAlarmManager.setReminders(remin);
				myAlarmManager.addAlarms();
			}
			
			/**
			 * Hot Update Part
			 */
			
			//Update the progress bar
			mBuilder.setProgress(MAX_PROGRESS, mProgressStatus, false);
			mBuilder.setAutoCancel(true);
			manager.notify(1, mBuilder.build());
			
			if(lastUpdateTimeHot == null){
				
				//Download hot shows
				HotSeriesList = downloadHot();
				
				if(HotSeriesList != null){
					//Delete the Previous Hot Shows and insert the new ones
					dbAdapter.deleteHotAll();
					
					//Insert to DataBase
					for(int i = 0; i < HotSeriesList.size(); i++){
						dbAdapter.insertHot(HotSeriesList.get(i));
					}
					
					//update the hot time in database
					updateServerTime(dbAdapter, KEY_SERVERTIME_HOT);
				}
			}else{
				
				//Compare the lastUpdateTimeHot with the one on the server
				String ServerLastUpdatedTimeHot = getServerLastUpdatedTimeHot(dbAdapter);
				
				Log.d(TAG, "LastUpdateTimeHot = " + lastUpdateTimeHot);
				
				//download if its not up to date
				if(ServerLastUpdatedTimeHot != null){
					
					Log.d(TAG, "Server LastUpdatedTimeHot = " + ServerLastUpdatedTimeHot);
					
					int luth = Integer.parseInt(lastUpdateTimeHot);
					int sluth = Integer.parseInt(ServerLastUpdatedTimeHot); 
					
					if( luth < sluth){
						
						Log.d(TAG, "Hot Shows are out of date, download");
						
						//Download hot shows
						HotSeriesList = downloadHot();
						
						if(HotSeriesList != null){
							
							//Delete the Previous Hot Shows and insert the new ones
							dbAdapter.deleteHotAll();
							
							//Insert to DataBase
							for(int i = 0; i < HotSeriesList.size(); i++){
								dbAdapter.insertHot(HotSeriesList.get(i));
								Log.d(TAG, "Inserting " + HotSeriesList.get(i).TITLE);
							}
							
							//update the hot time in database
							updateServerTime(dbAdapter, KEY_SERVERTIME_HOT);
						}
					}
				}
				
			}
			mProgressStatus++;
			
			//Close out the notification
			//Notify VoodooTVDB has been updated
		    mBuilder.setContentText("Update Complete")
		    	.setProgress(0, 0, false).setAutoCancel(true);
		    manager.notify(1, mBuilder.build());
		    
		    //Update the ServerTime
		    updateServerTime(dbAdapter, KEY_SERVERTIME);
		    
		    //Close Database
		    dbAdapter.close();
		    
		    // Check if user is logged in and if they have the "Keep Device Synch'ed" option selected
		    UserFunctions uFunctions = new UserFunctions(this);
		    
		    if( uFunctions.getUser() != null){
		    	
		    	if( uFunctions.getSyncStatus() ){
		    		
		    		// Start Sync Service
					ReminderManager rm = new ReminderManager(this);
					rm.setSyncUpdateService();
		    		
		    	}
		    	
		    }
		    
		    // Dismiss Notification
		    manager.cancel(1);
		}
	}
	
	/**
	 * Utility Methods
	 */
	private void updateServerTime(DatabaseAdapter dbAdapter, String KEY_FLAG_NAME){
		
		Log.d(TAG, "updateServerTime on app database");
		//if it returns null
		//grab the current ServerTime and finish(), try to update tomorrow
		String urlString = ServerUrls.getServerTimeUrl(getApplicationContext());
		//String urlString = "http://voodootvdb.com/getServertime.php";
		
		try {
			
			//Create the XML parser
			SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
			SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			XMLReader mXMLReader = mySAXParser.getXMLReader();
			XmlHandlerServertime xmlHandler = new XmlHandlerServertime();
			mXMLReader.setContentHandler(xmlHandler);
			
			//Create URL
			URL url = new URL(urlString);
			mXMLReader.parse(new InputSource(url.openStream()));
			
			//Get ServerTime and input it to flags table
			String time = xmlHandler.getTime();
			
			Log.d(TAG, "Servertime = " + time);
			
			if(dbAdapter.fetchFlag(KEY_FLAG_NAME) == null){
				
				Log.d(TAG, "local servertime is null, insert " + time + "into database");
				dbAdapter.insertFlag(KEY_FLAG_NAME, time);
				
			}else{
				
				Log.d(TAG, "Old servertime is " + time + ", update time too " + time);
				dbAdapter.updateFlag(KEY_FLAG_NAME, time);
			
			}
			
		}catch (MalformedURLException e) {
			Log.d(TAG, "MalformedURLException");
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			Log.d(TAG, "ParserConfigurationException");
			e.printStackTrace();
		}catch (SAXException e) {
			Log.d(TAG, "SAXException");
			e.printStackTrace();
		}catch (IOException e) {
			Log.d(TAG, "IOException");
			e.printStackTrace();
		}
		
	}
	
	private String getServerLastUpdatedTimeHot(DatabaseAdapter dbAdapter){
		
		String urlString = ServerUrls.getHotServerTime(getApplicationContext());
		//String urlString = "http://voodootvdb.com/getHotServerTime.php";
		
		try {
			
			//Create the XML parser
			SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
			SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			XMLReader mXMLReader = mySAXParser.getXMLReader();
			XmlHandlerServertime xmlHandler = new XmlHandlerServertime();
			mXMLReader.setContentHandler(xmlHandler);
			
			//Create URL
			URL url = new URL(urlString);
			mXMLReader.parse(new InputSource(url.openStream()));
			
			// Return Time
			return xmlHandler.getTime();
			
		}catch (MalformedURLException e) {
			Log.d(TAG, "MalformedURLException");
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			Log.d(TAG, "ParserConfigurationException");
			e.printStackTrace();
		}catch (SAXException e) {
			Log.d(TAG, "SAXException");
			e.printStackTrace();
		}catch (IOException e) {
			Log.d(TAG, "IOException");
			e.printStackTrace();
		}
		
		return null;
	}
	
	private ArrayList<Series> downloadHot(){
		
		Log.d(TAG, "download hot shows");
		//if it returns null
		//grab the current ServerTime and finish(), try to update tomorrow
		String urlString = ServerUrls.getHotUrl(getApplicationContext());
		//String urlString = "http://voodootvdb.com/getHot.php";
		
		try {
			
			//Create the XML parser
			SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
			SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			XMLReader mXMLReader = mySAXParser.getXMLReader();
			XmlHandlerHot xmlHandler = new XmlHandlerHot(this);
			mXMLReader.setContentHandler(xmlHandler);
			
			//Create URL
			URL url = new URL(urlString);
			mXMLReader.parse(new InputSource(url.openStream()));
			
			//Return Series List
			return xmlHandler.getSeries();
			
		}catch (MalformedURLException e) {
			Log.d(TAG, "MalformedURLException");
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			Log.d(TAG, "ParserConfigurationException");
			e.printStackTrace();
		}catch (SAXException e) {
			Log.d(TAG, "SAXException");
			e.printStackTrace();
		}catch (IOException e) {
			Log.d(TAG, "IOException");
			e.printStackTrace();
		}

		return null;
	}
}



















