package voodoo.tvdb.utils;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WatchedSyncFunctions {
	// JSON Parser
		private JSONParser jsonParser;
		
		// URLs
		//private static String URL = "http://voodootvdb.com/users/watched.php";
		
		// Context
		private Context context;
		
		// POST Parameter Names
		//private static String ACTION = "action";
		private static String EMAIL = "email";
		private static String SERIES_ID = "series_id";
		private static String ITEMS = "items";
		
		// POST Parameter Values
		private static String ACTION_ADD = "add";
		private static String ACTION_REMOVE = "remove";
		private static String ACTION_GET = "get";
		
		/** JSON Response node names */
		public static String NODE_SUCCESS = "success";
		public static String NODE_ERROR = "error";
		public static String NODE_ERROR_MESSAGE = "error_msg";
		public static String NODE_EPISODES = "episodes";
		public static String NODE_EPISODE_ID = "episode_id";
		public static String NODE_SEASON_NUMBER = "season_num";
		public static String NODE_EPISODE_NUMBER = "episode_num";
		public static String NODE_TITLE = "title";
		public static String NODE_ITEMS = "items";
		
		// Constructor
		public WatchedSyncFunctions(Context c){
			
			jsonParser = new JSONParser();
			context = c;
			
		}
		
		/**
		 * Add Episodes to Watched
		 * 
		 * @param series_id
		 * @param items
		 * @param email
		 * @return
		 */
		public JSONObject addWatched(String series_id, String items, String email){
			
			// Build Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair(ACTION, ACTION_ADD));
			params.add(new BasicNameValuePair(EMAIL, email));
			params.add(new BasicNameValuePair(SERIES_ID, series_id));
			params.add(new BasicNameValuePair(ITEMS, items));
			
			// Get JSON Object
			JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getWatchedUrl(context, ACTION_ADD),params);
			
			return json;
		}
		
		/**
		 * Removed Episodes from Watched
		 * 
		 * @param series_id
		 * @param items
		 * @param email
		 * @return
		 */
		public JSONObject removeWatched(String series_id, String items, String email){
			
			// Build Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair(ACTION, ACTION_REMOVE));
			params.add(new BasicNameValuePair(EMAIL, email));
			params.add(new BasicNameValuePair(SERIES_ID, series_id));
			params.add(new BasicNameValuePair(ITEMS, items));
			
			// Get JSON Object
			JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getWatchedUrl(context, ACTION_REMOVE), params);
			
			return json;
		}
		
		/**
		 * Get Watched Episodes for Series from Watched
		 * 
		 * @param series_id
		 * @param email
		 * @return
		 */
		public JSONObject getWatched(String series_id, String email){
			
			// Build Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair(ACTION, ACTION_GET));
			params.add(new BasicNameValuePair(EMAIL, email));
			params.add(new BasicNameValuePair(SERIES_ID, series_id));
			
			// Get JSON Object
			JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getWatchedUrl(context, ACTION_GET), params);
			
			return json;
			
		}
}




































