package voodoo.tvdb.Utils;

import android.content.Context;

import java.util.ArrayList;

import voodoo.tvdb.Objects.ListItem;
import voodoo.tvdb.Objects.ListObject;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class ListHelper {

	//private static final String TAG = "ListHelper";
	private DatabaseAdapter dbAdapter;
	//private Context context;
	
	public ListHelper(Context c){
		
		dbAdapter = new DatabaseAdapter(c);
		//this.context = c;
		
	}
	
	public boolean insertList(String name, String description){
		
		// Remove all the non-alpha numerics characters from the name
		String actualName = name.replaceAll("[^A-Za-z0-9 ]+", "");
		
		boolean result = false;
		ListObject list = getListDetails(actualName);
		
		dbAdapter.open();
		
		if(description == null){
			description = ListObject.KEY_EMPTY;
		}
		
		if(list != null){
			
			list.DESCRIPTION = description;
			list.DELETED = ListObject.KEY_FALSE;
			list.SYNCHED = ListObject.KEY_TRUE;
			
			result = dbAdapter.updateList(list);
			
		}else{
			
			ListObject l = new ListObject(actualName, description);
			
			result = dbAdapter.insertList(l);
		
		}
		
		dbAdapter.close();
		
		return result;
	}
	
	public boolean insertListWithoutSyncFlag(ListObject item){
		
		boolean result = false;
		ListObject list = getListDetails(item.NAME);
		
		dbAdapter.open();
		
		if(item.DESCRIPTION == null){
			item.DESCRIPTION = ListObject.KEY_EMPTY;
		}
		
		result = list != null ? dbAdapter.updateList(item) : dbAdapter.insertList(item);
		
		dbAdapter.close();
		
		return result;
	}
	
	public boolean insertSeriesToLists(String seriesId, ArrayList<String> listNames, String seriesTitle){
		
		dbAdapter.open();
		
		for(int i = 0; i < listNames.size(); i++){
			
			// Check is series is already on the list
			ListItem item = dbAdapter.fetchListItem(listNames.get(i), seriesId);
			
			if(item != null){
				
				// Series is already in that list but probably flagged as DELETED
				// Update to KEY_DELETED equal FALSE
				item.DELETED = ListItem.KEY_FALSE;
				item.SYNCHED = ListItem.KEY_FALSE;
				
				dbAdapter.updateListItem(item);
				
			}else{
				
				// Series is not in a list already, create a list item and add it
				item = new ListItem(seriesId, listNames.get(i), seriesTitle);
				
				dbAdapter.insertListItem(item);
				
			}
			
		}
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean insertSeriesToListWithoutSyncFlag(ListItem item){
		
		dbAdapter.open();
		
		// Check is series is already on the list
		ListItem i = dbAdapter.fetchListItem(item.LIST_NAME, item.SERIES_ID);

		
		if(i != null){
			
			dbAdapter.updateListItem(item);
			
		}else{
			
			dbAdapter.insertListItem(item);
			
		}
		
		dbAdapter.close();
		
		return true;
	}
	
	/**
	 * getListDetails
	 * @param name
	 * @return
	 * 
	 * ListObject if the list is on the database
	 * null otherwise
	 */
	public ListObject getListDetails(String name){
		
		dbAdapter.open();
		
		ListObject list = dbAdapter.fetchListDetails(name);
		
		dbAdapter.close();
		
		return list;
	}
	
	public ArrayList<CharSequence> getAllListNames(){
		
		dbAdapter.open();
		
		ArrayList<CharSequence> list = dbAdapter.fetchNonDeletedListNames();
		
		dbAdapter.close();
		
		return list;
	}
	
	public ArrayList<ListObject> getDeletedLists(){
		
		
		dbAdapter.open();
		
		ArrayList<ListObject> list = dbAdapter.fetchDeletedLists();
		
		dbAdapter.close();
		
		return list;	
	}
	
	public ArrayList<ListItem> getDeletedListItems(){
		
		dbAdapter.open();
		
		ArrayList<ListItem> items = dbAdapter.fetchDeletedListItems();
		
		dbAdapter.close();
		
		return items;
		
	}
	
	public ArrayList<String> getListNamesSeriesIsOn(String seriesId){
		
		dbAdapter.open();
		
		ArrayList<String> list = dbAdapter.fetchListsWithSeriesId(seriesId);
		
		dbAdapter.close();

		return list;
	}
	
	public ArrayList<ListObject> getUncreatedLists(){
		
		dbAdapter.open();
		
		ArrayList<ListObject> lists = dbAdapter.fetchUncreatedLists();
		
		dbAdapter.close();

		return lists;
		
	}
	
	public ArrayList<ListItem> getUnsynchedListItems(){
		
		dbAdapter.open();
		
		ArrayList<ListItem> items = dbAdapter.fetchUnsynchedListItems();
		
		dbAdapter.close();

		return items;
		
	}
	
	public boolean flagSeriesAsDeleted(String seriesId, ArrayList<String> listNames){
		
		dbAdapter.open();
		
		for(int i = 0; i < listNames.size(); i++){
			
			ListItem item = dbAdapter.fetchListItem(listNames.get(i), seriesId);
			
			item.DELETED = ListItem.KEY_TRUE;
			item.SYNCHED = ListItem.KEY_FALSE;
			
			dbAdapter.updateListItem(item);
			
			item = dbAdapter.fetchListItem(listNames.get(i), seriesId);
			
		}
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean flagListsAsDeleted(ArrayList<String> listNames){

		dbAdapter.open();

		for(int i = 0; i < listNames.size(); i++){
			
			// Fetch list details
			ListObject list = dbAdapter.fetchListDetails(listNames.get(i));
			
			// Set deleted flag
			list.DELETED = ListItem.KEY_TRUE;
			list.SYNCHED = ListItem.KEY_FALSE;
			
			// Update List
			dbAdapter.updateList(list);
			
			list = dbAdapter.fetchListDetails(listNames.get(i));
			
			// Fetch all the list items in that list
			ArrayList<ListItem> items = dbAdapter.fetchListItemsOfList(listNames.get(i));
			
			if(items != null){
				
				for(int j = 0; j < items.size(); j++){
					
					// Get list item 
					ListItem item = items.get(j);
					
					// Set Deleted flag as true
					item.DELETED = ListItem.KEY_TRUE;
					item.SYNCHED = ListItem.KEY_FALSE;
					
					// Update
					dbAdapter.updateListItem(item);
					
				}
				
			}
			
		}
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean deleteList(String ListName){
		
		dbAdapter.open();
		
		
		dbAdapter.deleteList(ListName);
		
		// Fetch all the list items in that list
		ArrayList<ListItem> items = dbAdapter.fetchListItemsOfList(ListName);
		
		if(items != null){
			
			for(int j = 0; j < items.size(); j++){
				
				// Get list item 
				ListItem item = items.get(j);
				
				// Set Deleted flag as true
				item.DELETED = ListItem.KEY_TRUE;
				item.SYNCHED = ListItem.KEY_FALSE;
				
				// Update
				dbAdapter.updateListItem(item);
				
			}
			
		}
		
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean deleteListLogOff(String ListName){
		
		dbAdapter.open();
		
		dbAdapter.deleteList(ListName);
		
		// Fetch all the list items in that list
		ArrayList<ListItem> items = dbAdapter.fetchListItemsOfList(ListName);
		
		if(items != null){
			
			for(int j = 0; j < items.size(); j++){
				
				// Get list item 
				ListItem item = items.get(j);
				
				dbAdapter.deleteListItem(ListName, item.SERIES_ID);
				
			}
			
		}
		
		dbAdapter.close();
		
		return true;
	}
	
	public boolean deleteListItem(ListItem item){
		
		dbAdapter.open();
		
		dbAdapter.deleteListItem(item.LIST_NAME, item.SERIES_ID);
		
		dbAdapter.close();
		
		return true;
		
	}
	
	public boolean deleteListItem(String series_id, ArrayList<String> list){
		
		dbAdapter.open();
		
		for(int i = 0; i < list.size(); i++){
			
			dbAdapter.deleteListItem(list.get(i), series_id);
			
		}
		
		
		dbAdapter.close();
		
		return true;
		
	}
	
	public boolean isSeriesInDB(String series_id){
		
		dbAdapter.open();
		
		Series s = dbAdapter.fetchSeries(series_id);
		
		boolean result = s != null ? true : false;
		
		dbAdapter.close();
		
		return result;
		
	}
	
}






















