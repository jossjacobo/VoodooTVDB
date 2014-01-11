package voodoo.tvdb.Utils;

import android.content.Context;

import java.util.ArrayList;

import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class WatchedHelper {
	
	//private static final String TAG = "WatchedHelper";
	private DatabaseAdapter dbAdapter;
	
	public WatchedHelper(Context c){
		
		dbAdapter = new DatabaseAdapter(c);
	}
	
	public boolean markWatched(String episodeId){
		
		// Check if series already in the Watched Pending Table
		boolean isPending = isPending(episodeId);
		
		// Open Database
		dbAdapter.open();
		
		// Fetch Episode
		Episode episode = dbAdapter.fetchEpisode(episodeId);
		
		// Insert to Watched Table
		dbAdapter.insertWatched(episode);
		
		// Now handle the Watched Pending Table
		if(isPending){
			
			Episode e = dbAdapter.fetchWatchedPending(episodeId);
			
			if(e.DELETED.equals(Episode.KEY_TRUE)){
				
				// It was watched once, flagged as deleted but has not Sychronize'd
				// so just take it out of pending
				dbAdapter.deleteWatchedPending(e.ID);
				
				//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " marked as watched, was also on the Pending table to be deleted, removed.");
				
				dbAdapter.close();
				return true;
				
			}else{
				
				// Just update
				// Update to deleted = false
				e.SYNCHED = Episode.KEY_FALSE;
				e.DELETED = Episode.KEY_FALSE;
				
				if(dbAdapter.updateWatchedPending(e)){
					
					//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " was on the Pending table, updated to sync.");
					
					dbAdapter.close();
					return true;
					
				}else{
					
					//Log.d(TAG, "ERROR: Episode " + e.ID + "-" + e.TITLE + " in Pending Table could not be updated.");
					dbAdapter.close();
					return false;
					
				}
				
			}
			
		}else{
			
			Episode e = new Episode();
			
			e.SERIES_ID = episode.SERIES_ID;
			e.ID = episode.ID;
			e.SYNCHED = Episode.KEY_FALSE;
			e.DELETED = Episode.KEY_FALSE;
			
			dbAdapter.insertWatchedPending(e);
			
			//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " inserted to Pending Table to be synched.");
			
			// Close Database
			dbAdapter.close();
			
			return true;
			
		}
		
	}
	
	public boolean markWatchedWithSyncFlag(String episodeId){
		
		// Check if series already in the Watched Pending Table
		boolean isPending = isPending(episodeId);
		
		// Check if series is in the Watched Table
		boolean isWatched = isWatched(episodeId);
		
		// Open Database
		dbAdapter.open();
		
		// Fetch Episode
		Episode episode = dbAdapter.fetchEpisode(episodeId);
		
		if(episode != null && !isWatched){
			
			// Insert to Watched Table
			dbAdapter.insertWatched(episode);
			
			// Now handle the Watched Pending Table
			if(isPending){
				
				dbAdapter.deleteWatchedPending(episode.ID);
				
			}
			
		}
		
		dbAdapter.close();
		
		return true;
		
	}
	
	public boolean removeWatched(Episode episode){

		// Check if series already in the Watched Pending Table
		boolean isPending = isPending(episode.ID);
		
		// Open Database
		dbAdapter.open();
		
		//Remove from the Watched database
		dbAdapter.deleteWatchedEpisode(episode.ID);
		
		// Take care of Watched Pending Table
		if(isPending){
			
			Episode e = dbAdapter.fetchWatchedPending(episode.ID);
			
			if(e.SYNCHED.equals(Episode.KEY_FALSE)){
				
				dbAdapter.deleteWatchedPending(e.ID);
				
				//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " marked not-watched, in Pending Table to be synched as watched, removed.");
				
				dbAdapter.close();
				return true;
			
			}else{
				
				// Update to deleted = true
				e.SYNCHED = Episode.KEY_FALSE;
				e.DELETED = Episode.KEY_TRUE;
				
				if(dbAdapter.updateWatchedPending(e)){
					
					//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " marked not-watched, in Pending Table updated to be removed.");
					
					dbAdapter.close();
					return true;
					
				}else{
					
					//Log.d(TAG, "ERROR: Episode " + e.ID + "-" + e.TITLE + " marked not-watched, could not be updated.");
					
					dbAdapter.close();
					return false;
					
				}
			}
			
		}else{
			
			Episode e = new Episode();
			
			e.SERIES_ID = episode.SERIES_ID;
			e.ID = episode.ID;
			e.SYNCHED = Episode.KEY_FALSE;
			e.DELETED = Episode.KEY_TRUE;
			
			dbAdapter.insertWatchedPending(e);
			
			//Log.d(TAG, "Episode " + e.ID + "-" + e.TITLE + " marked not-watched, added to Pending table to be synched.");
			
			// Close Database
			dbAdapter.close();
			
			return true;
			
		}
		
	}
	
	public boolean removedWatchedBySeries(String seriesId){
		
		// Open Database
		dbAdapter.open();
		
		// Create list of watched episodes that are on the Pending Table
		ArrayList<Episode> pendingWatchedList = new ArrayList<Episode>();
		
		// Fetch the episodes from the pending table that match the series
		pendingWatchedList = dbAdapter.fetchWatchedPendingBySeries(seriesId);
		
		// Close the database
		dbAdapter.close();
		
		// Loop through the list items (if any) and removed them from watched and handle the pending table
		if(pendingWatchedList != null){
			
			for(int i = 0; i < pendingWatchedList.size(); i++){
				
				removeWatched(pendingWatchedList.get(i));
				
			}
			
		}
		
		return true;
	}
	
	public ArrayList<Episode> getAllDeletedWatchedPending(){
		
		dbAdapter.open();
		
    	ArrayList<Episode> episodes = dbAdapter.fetchAllDeletedWatchedPending();
    	
    	dbAdapter.close();
    	
    	return episodes;	
		
	}
	
	public ArrayList<Episode> getAllUnsyncWatchedPendingBySeriesId(String series_id){
		
		dbAdapter.open();
		
		ArrayList<Episode> episodes = dbAdapter.fetchUnsynchedWatchedPendingBySeries(series_id);
		
		dbAdapter.close();
		
		return episodes;
	}
	
	public ArrayList<Episode> getAllDeletedWatchedPendingBySeriesId(String series_id){
		
		dbAdapter.open();
		
    	ArrayList<Episode> episodes = dbAdapter.fetchDeletedWatchedPendingBySeries(series_id);
    	
    	dbAdapter.close();
    	
    	return episodes;	
		
	}
	
	public boolean deleteDeletedWatchedPendingBySeries(String series_id){
		
		dbAdapter.open();
		
    	dbAdapter.deleteDeletedWatchedPendingSeries(series_id);
    	
    	dbAdapter.close();
    	
    	return true;
	
	}
	
	public void deleteDeletedWatchedPendingByBatch(String syncItems) {
		
		String[] items = syncItems.split(",");
		
		dbAdapter.open();
		
		for(int i = 0; i < items.length; i++){
			
			dbAdapter.deleteWatchedPending(items[i]);
			
		}
		
		dbAdapter.close();
	}
	
	public boolean deleteWatchedPendingByEpisode(String episode_id){
		
		dbAdapter.open();
		
		dbAdapter.deleteWatchedPending(episode_id);
    	
    	dbAdapter.close();
    	
    	return true;
	
	}
	
	public boolean deleteUnsynchedWatchedPendingBySeries(String series_id){
		
		dbAdapter.open();
		
		dbAdapter.deleteUnsynchedWatchedPendingSeries(series_id);
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean isPending(String episodeId){
		
		dbAdapter.open();
		
    	boolean isPending = dbAdapter.isEpisodeWatchedPending(episodeId);
    	
    	dbAdapter.close();
    	
    	return isPending;
	}
	
	public boolean isWatched(String episodeId){
		
		dbAdapter.open();
		
		boolean isWatched = dbAdapter.isEpisodeWatched(episodeId);
		
		dbAdapter.close();
		
		return isWatched;
	}

}
