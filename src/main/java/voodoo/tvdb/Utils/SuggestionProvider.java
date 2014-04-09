package voodoo.tvdb.utils;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

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
	JSONArray results;
	
	// Columns for custom suggestions
	private static final String[] COLUMNS = {
		"_id", //must include this column
		SearchManager.SUGGEST_COLUMN_TEXT_1,
		SearchManager.SUGGEST_COLUMN_TEXT_2,
		SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
		SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
		SearchManager.SUGGEST_COLUMN_SHORTCUT_ID};

	public SuggestionProvider(){
		setupSuggestions(AUTHORITY, MODE);
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder){
		//Query the user entered into the search bar
		String query = selectionArgs[0];
		
		// MatrixCursor to return results
		MatrixCursor cursor = new MatrixCursor(COLUMNS);
		
		if(query == null || query.length() < 4){
			//User hasn't entered anything
			//thus return a default cursor
			return null;
		}else{
			// Getting JSON String from url
			JSONParser jParser = new JSONParser();
			JSONObject json = jParser.getJSONFromURL(ServerUrls.getSuggestionsUrl(getContext(), query.replace(" ", "%20")));
			
			try{
				// Getting Array of Results
				results = json.getJSONArray(TAG_RESULTS);
				
				// looping through All results
				for(int i = 0; i < results.length(); i++){

                    JSONObject r = results.getJSONObject(i);
					String id = r.getString(TAG_ID);
                    String title = r.getString(TAG_TITLE);

					// Add a row to the cursor
					cursor.addRow(createRow(Integer.valueOf(id), title, query, id));
				}
				
			}catch(Exception e){
//				Log.e(TAG, "Failed to lookup " + query, e);
			}
		}
		
		return cursor;
		
	}
	
	private Object[] createRow(Integer id, String text1, String text2, String name){
		return new Object[]{
                id,     // id
				text1,	// Title of suggestion
				null,	// Sub-Title of suggestion
				text1,  // data to be sent when select from list as query
                id,     // data to be sent as extra string when select from list as result
                "android.intent.action.VIEW", // action to be created
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}
}





















