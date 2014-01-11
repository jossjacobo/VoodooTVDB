package voodoo.tvdb.Utils;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListSyncFunctions {

	/** JSON Parser */
	private JSONParser jsonParser;
	
	/** URLs */
	//private static String URL = "http://voodootvdb.com/users/lists.php";
	
	/** Context */
	private Context context;
	
	/** POST Parameter Names */
	//private static String TAG = "tag";
	//private static String ACTION = "action";
	private static String NAME = "name";
	private static String DESCRIPTION = "description";
	private static String EMAIL = "email";
	private static String SERIES_ID = "series_id";
	
	/** POST Parameter Values */
	//private static String TAG_LIST = "list";
	//private static String TAG_LIST_ITEM = "list_item";
	private static String ACTION_ADD = "add";
	private static String ACTION_DELETE = "delete";
	private static String ACTION_GET = "get";
	private static String ACTION_GET_ALL = "get_all";
	
	/** JSON Response node names */
	public static String NODE_TAG = "tag";
	public static String NODE_SUCCESS = "success";
	public static String NODE_ERROR = "error";
	public static String NODE_MESSAGE = "message";
	public static String NODE_NAME = "name";
	public static String NODE_SLUG = "slug";
	public static String NODE_USERNAME = "username";
	public static String NODE_ERROR_MESSAGE = "error_msg";
	public static String NODE_SERIES_ID = "series_id";
	public static String NODE_ITEMS = "items";
	public static String NODE_PRIVACY = "privacy";
	public static String NODE_DESCRIPTION = "description";
	public static String NODE_SHOW_NUMBERS = "show_numbers";
	public static String NODE_ALLOW_COMMENTS = "allow_comments";
	public static String NODE_TYPE = "type";
	public static String NODE_TITLE = "title";
	public static String NODE_EMPTY = "empty";
	
	/** Constructor */
	public ListSyncFunctions(Context c){
		
		jsonParser = new JSONParser();
		context = c;
		
	}
	
	/**
	 * Add a list to VoodooTVDB Server
	 * 
	 * @param name
	 * @param description
	 * @param email
	 * @return
	 */
	public JSONObject addList(String name, String description, String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST));
		//params.add(new BasicNameValuePair(ACTION, ACTION_ADD));
		params.add(new BasicNameValuePair(NAME, name));
		params.add(new BasicNameValuePair(DESCRIPTION, description));
		params.add(new BasicNameValuePair(EMAIL, email));
		
		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListsUrl(context, ACTION_ADD), params);
		
		return json;
	}
	
	/**
	 * Delete list from VoodooTVDB Server
	 * 
	 * @param name
	 * @param email
	 * @return
	 */
	public JSONObject deleteList(String name, String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST));
		//params.add(new BasicNameValuePair(ACTION, ACTION_DELETE));
		params.add(new BasicNameValuePair(NAME, name));
		params.add(new BasicNameValuePair(EMAIL, email));

		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListsUrl(context, ACTION_DELETE), params);
		
		return json;
		
	}
	
	/**
	 * Get list from VoodooTVDB Server
	 * 
	 * @param name
	 * @param email
	 * @return
	 */
	public JSONObject getList(String name, String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST));
		//params.add(new BasicNameValuePair(ACTION, ACTION_GET));
		params.add(new BasicNameValuePair(NAME, name));
		params.add(new BasicNameValuePair(EMAIL, email));

		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListsUrl(context, ACTION_GET), params);
		
		return json;
	}
	
	/**
	 * Get all the User Lists from VoodooTVDB Server
	 * 
	 * @param email
	 * @return
	 */
	public JSONObject getAllLists(String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST));
		//params.add(new BasicNameValuePair(ACTION, ACTION_GET_ALL));
		params.add(new BasicNameValuePair(EMAIL, email));

		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListsUrl(context, ACTION_GET_ALL), params);
		
		return json;
		
	}
	
	/**
	 * Add a list item to a list
	 * 
	 * @param name
	 * @param series_id
	 * @param email
	 * @return
	 */
	public JSONObject addListItem(String name, String series_id, String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST_ITEM));
		//params.add(new BasicNameValuePair(ACTION, ACTION_ADD));
		params.add(new BasicNameValuePair(EMAIL, email));
		params.add(new BasicNameValuePair(SERIES_ID, series_id));
		params.add(new BasicNameValuePair(NAME, name));
	
		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListItemsUrl(context, ACTION_ADD), params);
		
		return json;
		
	}
	
	/**
	 * Delete list item from list
	 * 
	 * @param name
	 * @param series_id
	 * @param email
	 * @return
	 */
	public JSONObject deleteListItem(String name, String series_id, String email){
		
		// Build Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//params.add(new BasicNameValuePair(TAG, TAG_LIST_ITEM));
		//params.add(new BasicNameValuePair(ACTION, ACTION_DELETE));
		params.add(new BasicNameValuePair(EMAIL, email));
		params.add(new BasicNameValuePair(SERIES_ID, series_id));
		params.add(new BasicNameValuePair(NAME, name));
	
		// Get JSON Object
		JSONObject json = jsonParser.getJSONFromUrl(ServerUrls.getListItemsUrl(context, ACTION_DELETE), params);
		
		return json;
		
	}
}






































