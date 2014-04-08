package voodoo.tvdb.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ServerUrls {
	
	private static SharedPreferences prefs;
	
	public static final String NETWORK_MODE = "network_mode";
	
	/**
	 * Online Strings
	 */
	public static final String onlineHost = "http://ci.voodootvdb.com/";

	
	/**
	 * Offline string (Local Server)
	 */
	public static final String localHost = "http://10.0.2.2/";
	
	/**
	 * Get Functions
	 */
	private static boolean getMode(Context context){
		
		// Preferences
		prefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(context);
		
		//Check the Preferences to see if we are online or local network
		boolean mode = prefs.getBoolean(NETWORK_MODE, true);
		
		return mode;
	}
	
	/**
	 * Host URL
	 */
	public static String getHost(Context context){
		
		return getMode(context) ? onlineHost : localHost;

	}
	
	/**
	 * Search Url
	 */
	public static String getSearchUrl(Context context, String query){
		
		//URL String
		String url = getHost(context);
		
		return url += getMode(context) ? "search/show/xml/" + query : "VoodooTVDB/search/show_offline/xml/" + query;
		
	}

    /**
     * Search Url v2
     */
    public static String getSearchUrlv2(Context context, String query, int limit, int start){
        return getHost(context) + "search/shows/json/" + query + "/" + limit + "/" + start;
    }
	
	/**
	 * Get Series URL
	 */
	public static String getSeriesUrl(Context context, String series_id){
		
		//URL String
		String url = getHost(context);
		
		return url += getMode(context) ? "shows/getSeries/xml/" + series_id : "VoodooTVDB/shows/getSeries/xml/" + series_id;
		
	}
	
	/**
	 * Get All Series URL
	 */
	public static String getAllSeriesUrl(Context context, String series_id){
		
		//URL String
		String url = getHost(context);
		
		return url += getMode(context) ? "shows/getAllSeries/xml/" + series_id : "VoodooTVDB/shows/getAllSeries/xml/" + series_id;
		
	}
	
	/**
	 * Get Image URL
	 */
	public static String getImageUrl(Context context, String path){
		
		//URL String
		String url = getHost(context);
		
		url += getMode(context) ? "img/" + path : "VoodooTVDB/img/" + path;
		
		return url;
		
	}
	
	/**
	 * Get Original Size Image Url
	 */
	public static String getImageUrlOriginal(Context context, String path){
		
		//URL String
		String url = getHost(context);
		
		url += getMode(context) ? "img/" + path + "/original/": "VoodooTVDB/img/" + path + "/original/";
		
		return url;
		
	}
	
	/**
	 * Get Suggestion Provider URL
	 */
	public static String getSuggestionsUrl(Context context, String query){
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "search/suggestions/json/" + query : "VoodooTVDB/search/suggestions_offline/json/" + query;
		
	}
	
	/**
	 * Get Server Time URL
	 */
	public static String getServerTimeUrl(Context context){
		
		//String urlString = "http://voodootvdb.com/getServertime.php";
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "servers/getTime/" : "VoodooTVDB/servers/getTime/";
	}
	
	/**
	 * Get Hot Url
	 */
	public static String getHotUrl(Context context){
		
		//"http://voodootvdb.com/getHot.php"
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "hotshows/get/xml/" : "VoodooTVDB/hotshows/get/xml/";
	}
	
	/**
	 * Get LoginActivity URLs
	 */
	public static String getLoginUrl(Context context){
		
		//"http://voodootvdb.com/getHot.php"
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "users/login/encrypted/" : "VoodooTVDB/users/login/encrypted/";
	}
	
	
	
	/**
	 * Get RegisterActivity URLs
	 */
	public static String getRegisterUrl(Context context){
		
		//"http://voodootvdb.com/getHot.php"
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "users/register/encrypted/" : "VoodooTVDB/users/register/encrypted/";
	}
	
	/**
	 * Get Watched URLs
	 */
	public static String getWatchedUrl(Context context, String action){
		
		// http://voodootvdb.com/users/watched.php
		
		// Url String
		String url = getHost(context);
		
		return url += getMode(context) ? "watched/" + action + "/" : "VoodooTVDB/watched/" + action + "/";
		
	}
	
	/**
	 * Get Lists URLs
	 */
	public static String getListsUrl(Context context, String action){
		
		// URL String 
		String url = getHost(context);
		
		url += getMode(context) ? "lists/" + action + "/" : "VoodooTVDB/lists/" + action + "/";
		
		return url;
	}
	
	/**
	 * Get List Items URLs
	 */
	public static String getListItemsUrl(Context context, String action){
		
		// URL String 
		String url = getHost(context);
		
		url += getMode(context) ? "list_items/" + action + "/" : "VoodooTVDB/lists_items/" + action + "/";
		
		return url;
	}
	
	
	public static String getUpdateUrl(Context context, String timestamp){
		
		// URL String 
		String url = getHost(context);
		url += getMode(context) ? "shows/update/xml/"  + timestamp : "VoodooTVDB/shows/update/xml/"  + timestamp;
		
		return url;
	}
	
	public static String getHotServerTime(Context context){
		
		// URL String 
		String url = getHost(context);
		url += getMode(context) ? "hotshows/servertime/xml/": "VoodooTVDB/hotshows/servertime/xml/";
		
		return url;
		
	}

    /** Fix Old image urls */
    public static String fixURL(String url){

        if(url == null)
            return null;

        // Old URLS
        String oldOnlineImgUrl = "http://voodootvdb.com/getImage.php?URL=";
        String oldOfflineImgUrl = "http://10.0.2.2/VoodooTVDB/getImage.php?URL=";

        // New URLS
        String newOnlineImgUrl = "";
        String newOfflineImgUrl = "";

        url = url.replace(oldOnlineImgUrl, newOnlineImgUrl);
        url = url.replace(oldOfflineImgUrl, newOfflineImgUrl);

        return url;
    }
}





















