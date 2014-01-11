package voodoo.tvdb.Utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

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

import voodoo.tvdb.AlarmServices.MyAlarmManager;
import voodoo.tvdb.Objects.FavoriteBundle;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.XMLHandlers.XmlHandlerFetchAllSeriesInfo;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class FavoriteHelper {
	
	private static final String CHECKED_ITEMS = "checked_items";
	private static final String SELECTED_ITEMS = "selected_items";

    private static final int DELETED = 0;
    private static final int SAVED = 1;

	
	//private static final String TAG = "FavoriteHeleper";
	private DatabaseAdapter dbAdapter;
	private Context context;
	
	/** Favorite Saving Listener */
	FavoriteSavingListener listener;
	FavoriteSavingListener emptyListener = new SimpleFavoriteSavingListener();
	
	public FavoriteHelper(Context c){
		
		dbAdapter = new DatabaseAdapter(c);
		context = c;
		
	}
	
	public FavoriteHelper(Context c, DatabaseAdapter d){
		
		dbAdapter = d;
		context = c;
		
	}
	
	public boolean isSeriesFavorited(String series_id){
		
		dbAdapter.open();
		
		boolean isSaved = (dbAdapter.fetchSeries(series_id) == null) ? false : true;
		
		dbAdapter.close();
		
		return isSaved;
		
	}
	
	public void createFavoriteAlert(final Series series, final boolean isFave, FavoriteSavingListener faveListener){
		
		if(faveListener == null){
			listener = emptyListener;
		}else{
			listener = faveListener;
		}
		
		/** Create List Helper */
		final ListHelper listHelper = new ListHelper(context);
		
		/** Build the Alert Dialog Box */
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		/** Set Builder Defaults */
		builder.setCancelable(true)
			.setTitle("Add to your lists")
			.setNegativeButton("Manage Lists", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					dialog.dismiss();
					
					/** Manage List Creation and Modification */
					ListManager listManager = new ListManager(context);
					
					/** Alert Dialog Box for List Management*/
					AlertDialog.Builder b = listManager.getAlertBuilder();
					AlertDialog a = b.create();
					a.show();
					
				}
			});
		
		/** If Series is in the DB prompt ability to delete it */
		if(isFave){
			
			builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
					
					/** Check if Series is on any lists */
					ArrayList<String> listNamesSeriesIsOn = listHelper.getListNamesSeriesIsOn(series.ID);
					
					if(listNamesSeriesIsOn != null){
						
						/** Flag them as deleted */
						listHelper.flagSeriesAsDeleted(series.ID, listNamesSeriesIsOn);
						
					}
					
					/** Run the Save Async task so it deletes the saved series */
					new saveQuery(context).execute(series.ID);
					
				}
			});
			
		}
		
		
		/** Get List Item Names */
		final ArrayList<CharSequence> listNames = listHelper.getAllListNames();
		
		/** Display the Alert with the Available lists */
		if(listNames != null){
			
			Bundle bundle = getSelectedListItems(listNames, series.ID, isFave, listHelper);
			
			/** Pre-Select Lists Series is already on */
			final ArrayList<Integer> mSelectedItems = bundle.getIntegerArrayList(SELECTED_ITEMS);
			
			/**
			 * Create an boolean array for the pre-selected
			 * list items.
			 */
			boolean[] checkedItems = bundle.getBooleanArray(CHECKED_ITEMS);
			
			/** Build Multiple Choice List */
			builder.setMultiChoiceItems(listNames.toArray(new CharSequence[listNames.size()]), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if (isChecked) {
                       // If the user checked the item, add it to the selected items
                       mSelectedItems.add(which);
                   } else if (mSelectedItems.contains(which)) {
                       // Else, if the item is already in the array, remove it 
                       mSelectedItems.remove(Integer.valueOf(which));
                   }
				}
			})
			.setPositiveButton("I'm Done!", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//Log.d(TAG, "I'm Done");
					
					if(isFave){
						
						//Log.d(TAG, "Series is already saved");
						
						// Check is series is already on any lists
						ArrayList<String> listNamesSeriesIsOn = listHelper.getListNamesSeriesIsOn(series.ID); 
						if(listNamesSeriesIsOn != null){
							
							//Log.d(TAG, "Series " + series.TITLE + " is already in one or more lists");
							//Log.d(TAG, "Flag them as deleted");
							
							// Series is saved and on one or more lists
							// Flag them as deleted
							listHelper.flagSeriesAsDeleted(series.ID, listNamesSeriesIsOn);
							
						}
						
						// Insert series to List Selected (If any)
						if(mSelectedItems.size() > 0){
							
							//Log.d(TAG, "User selected " + mSelectedItems.size() + " lists from the options");
							//Log.d(TAG, "Insert/Update series into list (update deleted flag to false");
							
							ArrayList<String> list_names = new ArrayList<String>();
							
							for(int i = 0; i < mSelectedItems.size(); i++){
								list_names.add(listNames.get(mSelectedItems.get(i)).toString());
								//Log.d(TAG, "List " + listNames.get(mSelectedItems.get(i)).toString() + " was selected");
							}
							
							listHelper.insertSeriesToLists(series.ID, list_names, series.TITLE);
						}
					}else{

						// Series is not yet saved
						//Log.d(TAG, "Series is NOT already saved");
						
						// Insert series to List Selected (If any)
						if(mSelectedItems.size() > 0){
							
							//Log.d(TAG, "User selected lists from the options");
							//Log.d(TAG, "Insert series into list");
							
							ArrayList<String> list_names = new ArrayList<String>();
							
							for(int i = 0; i < mSelectedItems.size(); i++){
								list_names.add(listNames.get(mSelectedItems.get(i)).toString());
								//Log.d(TAG, "List " + listNames.get(mSelectedItems.get(i)).toString() + " was selected");
							}
							
							listHelper.insertSeriesToLists(series.ID, list_names, series.TITLE);
						}else{
							//Log.d(TAG, "User did not select any lists from options");
						}
						
						// Run the Favorite ASync Task to save it
						new saveQuery(context).execute(series.ID);
					}
				}
			});
			
		}else{
			
			// Prompt the Alert Box to say they don't currently 
			// have lists created...to create some
			builder
			.setMessage("No lists found. Create some lists to get started!");
			
		}
		
		// Create the Alert and display it
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	
	private Bundle getSelectedListItems(ArrayList<CharSequence> listNames, String series_id, boolean isFav, ListHelper listHelper){
		
		/** Pre-Select Lists Series is already on */
		final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
		
		/**
		 * Create an boolean array for the pre-selected
		 * list items.
		 */
		boolean[] checkedItems = null;
		
		/**
		 * Populate the boolean array to match the
		 * lists the series is already on.
		 */
		if(isFav){
			
			ArrayList<String> listsAlreadyIn = listHelper.getListNamesSeriesIsOn(series_id);
			
			if(listsAlreadyIn != null){
				checkedItems = new boolean[listNames.size()];
				
				for(int j = 0; j < listNames.size(); j++){
					
					if(listsAlreadyIn.contains(listNames.get(j))){
						// set this as cell as true
						checkedItems[j] = true;
						
						// also add the integer position to the mSelectedItems
						mSelectedItems.add(j);
						
					}else{
						checkedItems[j] = false;
					}
					
				}
				
			}
			
		}
		
		Bundle b = new Bundle();
		
		b.putBooleanArray(CHECKED_ITEMS, checkedItems);
		b.putIntegerArrayList(SELECTED_ITEMS, mSelectedItems);
		
		return b;
	}
	
	/**
     * AsyncTask to add series to favorites
     * If series is already in the DataBase delete it, otherwise download it and save it.
     */
    private class saveQuery extends AsyncTask<String, Void, FavoriteBundle> {
    	
    	private Context context;
    	private ProgressDialog dialog;
    	
    	private AsyncTask<String, Void, FavoriteBundle> myQuery;
    	
    	public saveQuery(Context c){
    		context = c;
    		dialog = new ProgressDialog(c);
    	}
    	
		@Override
    	protected void onPreExecute(){
    		dialog.setMessage("Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					myQuery.cancel(true);
				}
			});
			dialog.show();
			myQuery = this;
			
    	}
    	
		@Override
		protected FavoriteBundle doInBackground(String... params) {
			for(String id : params){
				
				dbAdapter.open();
				Series seriesCheck = dbAdapter.fetchSeries(id);
				dbAdapter.close();
				
				FavoriteBundle fb = new FavoriteBundle();
				
				fb.isOnDB = seriesCheck == null ? false : true;
								
				if(!fb.isOnDB){
					try{
	        			
						//Series not in Database, DownLoad and save
	        			//String onlineString = "http://voodootvdb.com/getAllSeries.php?ID=" + id;
	        			
	        			//URL
	        			URL url = new URL(ServerUrls.getAllSeriesUrl(context, id));
	        			
	        			SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
						SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
						XMLReader mXMLReader = mySAXParser.getXMLReader();
						XmlHandlerFetchAllSeriesInfo xmlHandler = new XmlHandlerFetchAllSeriesInfo(context);
						mXMLReader.setContentHandler(xmlHandler);
			        	mXMLReader.parse(new InputSource(url.openStream()));
			        	
			        	//Get the series info and episodes info 
			        	fb.SERIES = xmlHandler.getSeries();
						fb.EPISODES  = xmlHandler.getEpisodesList();
        			
        			}catch (MalformedURLException e) {
    					//Log.d(TAG, "MalformedURLException");
    					e.printStackTrace();
    				} catch (ParserConfigurationException e) {
    					//Log.d(TAG, "ParserConfigurationException");
    					e.printStackTrace();
    				} catch (SAXException e) {
    					//Log.d(TAG, "SAXException");
    					e.printStackTrace();
    				} catch (IOException e) {
    					//Log.d(TAG, "IOException");
    					e.printStackTrace();
    				}
					return fb;
				}else{
					fb.SERIES = seriesCheck;
					return fb;
				}
				
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(FavoriteBundle fb){
			dialog.dismiss();
			new saveDB(context).execute(fb);
		}
    	
    }

	/**
     * AsyncTask to save the download data into the SQLiteDB
     */
    private class saveDB extends AsyncTask<FavoriteBundle, Void, Integer> {

    	private Context context;
    	private ProgressDialog dialog;
    	private String seriesId = null;
    	
    	private AsyncTask<FavoriteBundle, Void, Integer> mySave;
    	
    	public saveDB(Context c){
    		context = c;
    		dialog = new ProgressDialog(c);
    	}
    	
    	@Override
    	protected void onPreExecute(){
    		dialog.setMessage("Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					mySave.cancel(true);
				}
			});
			dialog.show();
			mySave = this;
    	}
    	
		@Override
		protected Integer doInBackground(FavoriteBundle... params) {

			for(FavoriteBundle fb : params){
				
				seriesId = fb.SERIES.ID;
				
				dbAdapter.open();
				
				if(!fb.isOnDB){
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
					
					Reminders reminderManager = new Reminders(context, reminders);
	    			reminderManager.open();
	    			reminderManager.addReminders();
	    			reminderManager.close();
	    			
	    			//Create Alarms from Reminder Files
	    			MyAlarmManager myAlarmManager = new MyAlarmManager(context, fb.SERIES.ID);
	    			myAlarmManager.addAlarms();
                    dbAdapter.close();

                    return SAVED;
				}else{
					
					//fb.isOnDB returned true, so delete it.
					
					//Delete Series, all Episodes and Watched Episodes
					dbAdapter.deleteSeries(fb.SERIES.ID);
	    			dbAdapter.deleteAllEpisode(fb.SERIES.ID);
	    			dbAdapter.deleteWatchedSeries(fb.SERIES.ID);
					
	    			//Remove Alarms before deleting Reminder
	    			MyAlarmManager myAlarmManager = new MyAlarmManager(context, fb.SERIES.ID);
	    			myAlarmManager.removeAlarms();
	    			
	    			//Finally delete all the reminders
	    			dbAdapter.deleteAllReminders(fb.SERIES.ID);
	    			
	    			//Delete all the watched
	    			WatchedHelper wHelper = new WatchedHelper(context);
	    			wHelper.removedWatchedBySeries(fb.SERIES.ID);
	    			
	    			//Delete all of the Queue
	    			dbAdapter.deleteQueueSeries(fb.SERIES.ID);
                    dbAdapter.close();

	    			return DELETED;
				}
			}
            return -1;
		}
		
		@Override
		protected void onPostExecute(Integer status){
			dialog.dismiss();

            switch (status){
                case DELETED:
                    Toast.makeText(context, "Series Removed", Toast.LENGTH_SHORT).show();
                    listener.onDeleteCompleted(seriesId);
                    break;
                case SAVED:
                    Toast.makeText(context, "Series Saved!", Toast.LENGTH_SHORT).show();
                    listener.onSavingCompleted(seriesId);
                    break;
            }
		}
    	
    }
	
}
































