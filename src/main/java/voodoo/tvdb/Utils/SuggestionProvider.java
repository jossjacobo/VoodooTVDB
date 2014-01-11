package voodoo.tvdb.Utils;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {

	public static final String AUTHORITY = SuggestionProvider.class.getName();
	public static final int MODE = DATABASE_MODE_QUERIES;
	
	//JSON Node names
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ID = "series_id";
	private static final String TAG_TITLE = "title";
	
	// Results JSONArray
	JSONArray results = null;
	
	// Columns for custom suggestions
	private static final String[] COLUMNS = {
		"_id", //must include this column
		SearchManager.SUGGEST_COLUMN_TEXT_1,
		SearchManager.SUGGEST_COLUMN_TEXT_2,
		SearchManager.SUGGEST_COLUMN_INTENT_DATA,
		SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
		SearchManager.SUGGEST_COLUMN_SHORTCUT_ID};

	private static final String TAG = "SuggestionProvider";
	
	public SuggestionProvider(){
		setupSuggestions(AUTHORITY, MODE);
	}
	
	@Override
	public boolean onCreate(){
		boolean create = super.onCreate();
		return create;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder){
		/**
		 * The Custom android URI looks like this
		 * 
		 * "content://authority/optionalPath/search_suggest_query/queryText"
		 * 
		 * Where the search_suggest_query is an unique element added by android
		 * and the queryText is what the user actually inputed into the search bar
		 */
		
		//Query the user entered into the search bar
		String query = selectionArgs[0];
		
		// MatrixCursor to return results
		MatrixCursor cursor = new MatrixCursor(COLUMNS);
		
		if(query == null || query.length() < 4){
			//User hasn't entered anything
			//thus return a default cursor
			
			Log.d(TAG, "User hasn't entered anything in the search query: " + query);
			
			return null;
			
		}else{
			//query contains the user search
			//return a cursor with the appropriate data
			
			// Creating JSON Parser instance
			JSONParser jParser = new JSONParser();
					
			// Getting JSON String from url
			JSONObject json = jParser.getJSONFromURL(ServerUrls.getSuggestionsUrl(getContext(), query.replace(" ", "%20")));
			
			try{
				// Getting Array of Results
				results = json.getJSONArray(TAG_RESULTS);
				
				// looping through All results
				for(int i = 0; i < results.length(); i++){
					JSONObject r = results.getJSONObject(i);
					// Storing each JSON item in variable
					String id = r.getString(TAG_ID);
                    // Clean up query, leave only Alpha Numeric Characters
                    String title = r.getString(TAG_TITLE).replace("-"," ").replaceAll("[^a-zA-Z0-9 ]+","");
					// Add a row to the cursor
					cursor.addRow(createRow(Integer.valueOf(id), title, query, id));
				}
				
			}catch(Exception e){
				Log.e(TAG, "Failed to lookup " + query, e);
			}
		}
		
		return cursor;
		
	}
	
	private Object[] createRow(Integer id, String text1, String text2, String name){
		/**
		 * id : the id of the show
		 * text1 : is the title of the suggestion
		 * text2 : is what the user has typed so far
		 * name : is also the id
		 */
		return new Object[]{ id, // id
				text1,	// Title of suggestion
				null,	// Sub-Title of suggestion
				text1, "android.intent.action.VIEW", // action to be created
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}
}





















