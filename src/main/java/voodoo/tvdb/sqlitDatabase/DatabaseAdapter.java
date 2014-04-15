package voodoo.tvdb.sqlitDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import voodoo.tvdb.activity.BaseSlidingActivity;
import voodoo.tvdb.objects.Episode;
import voodoo.tvdb.objects.ListItem;
import voodoo.tvdb.objects.ListObject;
import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.objects.Series;



/**
 * Simple database access helper class.
 * Defines the basic CRUD operations (Create, Read Update, Delete)
 * gives the ability to retrieve or modify specific reminders.
 */
public class DatabaseAdapter {
	private static final String TAG = "DatabaseAdapter";
	
	/** Key Fields & Database Related Constants */
	private static final String DATABASE_NAME = "database";
	private static final String DATABASE_SERIES_TABLE = "series";
	private static final String DATABASE_EPISODES_TABLE = "episodes";
	private static final String DATABASE_REMINDERS_TABLE = "reminders";
	private static final String DATABASE_WATCHED_TABLE = "watched";
	private static final String DATABASE_QUEUE_TABLE = "queue";
	private static final String DATABASE_FLAGS_TABLE = "flags";
	private static final String DATABASE_HOT_TABLE = "hot";
	private static final String DATABASE_LOGIN_TABLE = "login";
	private static final String DATABASE_LIST_TABLE = "list";
	private static final String DATABASE_LIST_ITEM_TABLE = "list_item";
	//private static final String DATABASE_REVIEWS_TABLE = "review";
	private static final String DATABASE_WATCHED_PENDING_TABLE = "watched_pending";
	
	/** Database Version */
	private static final int DATABASE_VERSION = 7;
	
	/** Series Keys */
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SERIES_ID = "series_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_OVERVIEW = "overview";
	public static final String KEY_POSTER_URL = "poster_url";
	public static final String KEY_ACTORS = "actors";
	public static final String KEY_AIRS_DAYOFWEEK = "airs_dayofweek";
	public static final String KEY_AIRS_TIME = "airs_time";
	public static final String KEY_GENRE = "genre";
	public static final String KEY_IMDB_ID = "imdb_id";
	public static final String KEY_NETWORK = "network";
	public static final String KEY_RATING = "rating";
	public static final String KEY_RUNTIME = "runtime";
	public static final String KEY_STATUS = "status";
	public static final String KEY_LAST_UPDATED = "last_updated";
	public static final String KEY_RATING_COUNT = "rating_count";
	public static final String KEY_FIRST_AIRED = "first_aired";
	public static final String KEY_CONTENT_RATING = "content_rating";
	
	/** Episodes Keys */
	/** The rest are Shared Keys that can be reused */
	public static final String KEY_IMAGE_URL = "image_url";
	public static final String KEY_GUEST_STARS = "guest_stars";
	public static final String KEY_EPISODE_NUMBER = "episode_number";
	public static final String KEY_SEASON_NUMBER = "season_number";
	public static final String KEY_REMINDER = "reminder";
	
	/** Reminder Keys */
	/** The rest are Shared Keys that can be reused */
	public static final String KEY_EPISODE_ID = "episode_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_TIME = "time";
	public static final String KEY_SERIES_NAME = "series_name";
	public static final String KEY_EPISODE_NAME = "episode_name";
	
	/** Buzz Keys */
	/** Deprecated */
	public static final String KEY_PUB_DATE = "pub_date";
	public static final String KEY_LINK = "link";
	
	/** Flag Keys */
	public static final String KEY_FLAG_NAME = "flag_name";
	public static final String KEY_VALUE = "value";
	
	/** LoginActivity Keys */
	public static final String KEY_USERNAME = "username";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_UID = "uid";
	public static final String KEY_CREATED_AT = "created_at";
	
	/** List Keys */
	public static final String KEY_LIST_NAME = "list_name";
	public static final String KEY_SLUG = "slug";
	public static final String KEY_PRIVACY = "privacy";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_SHOW_NUMBERS = "show_numbers";
	public static final String KEY_ALLOW_COMMENTS = "allow_comments";
	public static final String KEY_SYNCHED = "synched";
	public static final String KEY_CREATED = "created";
	public static final String KEY_DELETED = "deleted";
	public static final String KEY_MISC_1 = "misc1";
	public static final String KEY_MISC_2 = "misc2";
	
	/** List Item Keys */
	public static final String KEY_TYPE = "type";
	/**
	 * All other can be created with other keys
	 * 
	 * KEY_SLUG
	 * KEY_TYPE
	 * KEY_SERIES_ID
	 * KEY_TITLE
	 */
	
	/** Watched Pending List */
	/**
	 * All Keys can be created with other keys
	 * 
	 * KEY_ROWID
	 * KEY_SERIES_ID
	 * KEY_EPISODE_ID
	 */

	
	private DatabaseHelper DbHelper;
	private SQLiteDatabase Db;
	
	/**
	 * Database creation SQL statement
	 */
	private static final String SERIES_DATABASE_CREATE = 
		"create table " + DATABASE_SERIES_TABLE + " (" 
		+ KEY_ROWID + " integer primary key, " 
		+ KEY_TITLE + " text, "
		+ KEY_OVERVIEW + " text, "
		+ KEY_POSTER_URL + " text, "
		+ KEY_ACTORS + " text, "
		+ KEY_AIRS_DAYOFWEEK + " text, "
		+ KEY_AIRS_TIME + " text, "
		+ KEY_GENRE + " text, "
		+ KEY_IMDB_ID + " text, "
		+ KEY_NETWORK + " text, "
		+ KEY_RATING + " float, "
		+ KEY_RUNTIME + " text, "
		+ KEY_STATUS + " text, "
		+ KEY_LAST_UPDATED + " text, "
		+ KEY_RATING_COUNT + " integer, "
		+ KEY_CONTENT_RATING + " text, "
		+ KEY_FIRST_AIRED + " integer);";
	
	private static String EPISODE_DATABASE_CREATE =
		"create table " + DATABASE_EPISODES_TABLE + " (" 
		+ KEY_ROWID + " integer primary key autoincrement, "
		+ KEY_EPISODE_ID + " text, "
		+ KEY_SERIES_ID + " text, "
		+ KEY_TITLE + " text, "
		+ KEY_OVERVIEW + " text, "
		+ KEY_IMAGE_URL + " text, " 
		+ KEY_GUEST_STARS + " text, "
		+ KEY_RATING + " float, "
		+ KEY_RATING_COUNT + " integer, "
		+ KEY_EPISODE_NUMBER + " text, "
		+ KEY_SEASON_NUMBER + " text, "
		+ KEY_LAST_UPDATED + " text, "
		+ KEY_FIRST_AIRED + " text, "
		+ KEY_REMINDER + " BOOLEAN DEFAULT 'FALSE');";
	
	private static String REMINDER_DATABASE_CREATE = 
		"create table " + DATABASE_REMINDERS_TABLE + " ("
		+ KEY_ROWID + " integer primary key autoincrement, "
		+ KEY_EPISODE_ID + " text, "
		+ KEY_SERIES_ID + " text, "
		+ KEY_DATE + " text, "
		+ KEY_TIME + " text, "
		+ KEY_SERIES_NAME + " text, "
		+ KEY_EPISODE_NAME + " text, "
		+ KEY_SEASON_NUMBER + " text, " 
		+ KEY_EPISODE_NUMBER + " text, "
		+ KEY_RATING + " float, "
		+ KEY_OVERVIEW + " text, "
		+ KEY_GUEST_STARS + " text, "
		+ KEY_IMAGE_URL + " text);";
	
	private static String WATCHED_DATABASE_CREATE =
			"create table " + DATABASE_WATCHED_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_SERIES_ID + " text, "
			+ KEY_EPISODE_ID + " text);";
	
	private static String QUEUE_DATABASE_CREATE = 
			"create table " + DATABASE_QUEUE_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_SERIES_ID + " text, "
			+ KEY_EPISODE_ID + " text);";
	
	private static String FLAGS_DATABASE_CREATE = 
			"create table " + DATABASE_FLAGS_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_FLAG_NAME + " text, "
			+ KEY_VALUE + " text);";
	
	private static String HOT_DATABASE_CREATE = 
			"create table " + DATABASE_HOT_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_SERIES_ID + " text, "
			+ KEY_EPISODE_NAME + " text, " //I fucked up on this one it should have been KEY_SERIES_NAME
			+ KEY_POSTER_URL + " text);";
	
	private static String LOGIN_DATABASE_CREATE =
			"create table " + DATABASE_LOGIN_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_USERNAME + " text, "
			+ KEY_EMAIL + " text unique, "
			+ KEY_UID + " text, "
			+ KEY_CREATED_AT + " text);";
	
	private static String LIST_DATABASE_CREATE =
			"create table " + DATABASE_LIST_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement,"
			+ KEY_LIST_NAME + " text unique, "
			+ KEY_SLUG + " text, "
			+ KEY_DESCRIPTION + " text, "
			+ KEY_SHOW_NUMBERS + " text, "
			+ KEY_ALLOW_COMMENTS + " text, "
			+ KEY_SYNCHED + " text, "
			+ KEY_CREATED + " text, "
			+ KEY_DELETED + " text, "
			+ KEY_PRIVACY + " text, "
			+ KEY_MISC_1 + " text, "
			+ KEY_MISC_2 + " text);";
	
	private static String LIST_ITEM_DATABASE_CREATE =
			"create table " + DATABASE_LIST_ITEM_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_LIST_NAME + " text, "
			+ KEY_TYPE + " text, "
			+ KEY_SERIES_ID + " text, "
			+ KEY_TITLE + " text, "
			+ KEY_SYNCHED + " text, "
			+ KEY_DELETED + " text, "
			+ KEY_MISC_1 + " text, "
			+ KEY_MISC_2 + " text);";
	
	private static String WATCHED_PENDING_DATABASE_CREATE =
			"create table " + DATABASE_WATCHED_PENDING_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_SERIES_ID + " text, "
			+ KEY_EPISODE_ID + " text, "
			+ KEY_MISC_1 + " text, "
			+ KEY_MISC_2 + " text);";
	
	private final Context context;
	private static class DatabaseHelper extends SQLiteOpenHelper{
		/**
		 * Implementation of the ADAPTER software (i.e. SQLiteOpenHelper)
		 * by creating an adapter to handle the database communication,
		 * you can communicate with this class via the programming language
		 * of Java while this adapter class does the translation and 
		 * adapts certain Java requests into SQLite-specific commands.
		 */
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SERIES_DATABASE_CREATE);
			db.execSQL(EPISODE_DATABASE_CREATE);
			db.execSQL(REMINDER_DATABASE_CREATE);
			db.execSQL(WATCHED_DATABASE_CREATE);
			db.execSQL(QUEUE_DATABASE_CREATE);
			db.execSQL(FLAGS_DATABASE_CREATE);
			db.execSQL(HOT_DATABASE_CREATE);
			db.execSQL(LOGIN_DATABASE_CREATE);
			db.execSQL(LIST_DATABASE_CREATE);
			db.execSQL(LIST_ITEM_DATABASE_CREATE);
			db.execSQL(WATCHED_PENDING_DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			if(oldVersion <= 1){
				Log.d(TAG, "onUpgrade called, upgrading from Version " + oldVersion + " to " + newVersion);
				db.execSQL(WATCHED_DATABASE_CREATE);
			}
			
			if(oldVersion <= 2){
				Log.d(TAG, "onUpgrade called, upgrading from Version " + oldVersion + " to " + newVersion);
				db.execSQL(QUEUE_DATABASE_CREATE);
			}
			
			if(oldVersion <= 3){
				Log.d(TAG, "onUpgrade called, upgrading from " + oldVersion + " to " + newVersion);
				db.execSQL(FLAGS_DATABASE_CREATE);
			}
			
			if(oldVersion <= 4){
				Log.d(TAG, "onUpgrade called, upgrading from " + oldVersion + " to " + newVersion);
				db.execSQL(HOT_DATABASE_CREATE);
			}
			
			if(oldVersion <= 5){
				Log.d(TAG, "onUpgrade called, upgrading from " + oldVersion + " to " + newVersion);
				db.execSQL(LOGIN_DATABASE_CREATE);
			}
			
			if(oldVersion <= 6){
				db.execSQL(LIST_DATABASE_CREATE);
				db.execSQL(LIST_ITEM_DATABASE_CREATE);
				db.execSQL(WATCHED_PENDING_DATABASE_CREATE);
				Log.d(TAG, "onUpgrade called, upgrading from " + oldVersion + " to " + newVersion);
			}
		}
		
	}
	
	/**
	 * Constructor
	 */
	public DatabaseAdapter(Context c){
		this.context = c;
	}
	
	/**
	 * The open() methods opens(and creates if necessary) the database
	 * using the DatabaseHelper() class that was just created.
	 * 
	 * @return - This class returns itself through the @this java
	 * keyword. The reason that the class is returning itself is 
	 * because the caller (ReminderEditActivity or ReminderListActivity)
	 * needs to access data from this class and this method return an 
	 * instance of the @DbAdapter
	 */
	public DatabaseAdapter open() throws SQLException {
		DbHelper = new DatabaseHelper(context);
		Db = DbHelper.getWritableDatabase();
		return this;
	}
	public void close(){
		DbHelper.close();
	}
	
	/**
	 * FLAGS METHODS
	 */
	public long insertFlag(String flag_name, String value){
		ContentValues iv = new ContentValues();
		iv.put(KEY_FLAG_NAME, flag_name);
		iv.put(KEY_VALUE, value);
		return Db.insert(DATABASE_FLAGS_TABLE, null, iv);
	}
	public boolean updateFlag(String flag_name, String value){
		ContentValues iv = new ContentValues();
		iv.put(KEY_FLAG_NAME, flag_name);
		iv.put(KEY_VALUE, value);
		
		return Db.update(DATABASE_FLAGS_TABLE, iv, KEY_FLAG_NAME + "='" + flag_name + "'", null) > 0;
	}
	public String fetchFlag(String flag_name) throws SQLException{
		Cursor c = Db.query(DATABASE_FLAGS_TABLE, 
				new String[] {KEY_VALUE}, 
				KEY_FLAG_NAME + "='" + flag_name + "'", null, null, null, null);
		if(c.moveToFirst()){
			String value = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALUE));
			c.close();
			return value;
		}else{
			c.close();
			return null;
		}
	}
	public boolean deleteFlag(String flag_name){
		
		return Db.delete(DATABASE_FLAGS_TABLE, KEY_FLAG_NAME + "='" + flag_name + "'", null) > 0;
	}
	
	public boolean isEpisodeFavorites(String episodeID){
		Cursor c = Db.query(DATABASE_EPISODES_TABLE, 
				new String[]{KEY_EPISODE_ID}, 
				KEY_EPISODE_ID + "=" + episodeID, 
				null, null, null, null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}else{
			c.close();
			return false;
		}
	}
	
	public boolean isSeriesFavorites(String ID){
		Cursor c = Db.query(DATABASE_SERIES_TABLE, 
				new String[]{KEY_ROWID}, 
				KEY_ROWID + "=" + ID, 
				null, null, null, null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}else{
			c.close();
			return false;
		}
	}
	
	/**
	 * SERIES METHODS
	 */
	public long insertSeries(Series series){
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ROWID, series.ID);
		initialValues.put(KEY_TITLE, series.TITLE);
		initialValues.put(KEY_OVERVIEW, series.OVERVIEW);
		initialValues.put(KEY_POSTER_URL, series.POSTER_URL);
		initialValues.put(KEY_ACTORS, series.ACTORS);
		initialValues.put(KEY_AIRS_DAYOFWEEK, series.AIRS_DAYOFWEEK);
		initialValues.put(KEY_AIRS_TIME, series.AIRS_TIME);
		initialValues.put(KEY_GENRE, series.GENRE);
		initialValues.put(KEY_IMDB_ID, series.IMDB_ID);
		initialValues.put(KEY_NETWORK, series.NETWORK);
		initialValues.put(KEY_RATING, series.RATING);
		initialValues.put(KEY_RUNTIME, series.RUNTIME);
		initialValues.put(KEY_STATUS, series.STATUS);
		initialValues.put(KEY_LAST_UPDATED, series.LAST_UPDATED);
		initialValues.put(KEY_RATING_COUNT, series.RATING_COUNT);
		initialValues.put(KEY_CONTENT_RATING, series.CONTENT_RATING);
		initialValues.put(KEY_FIRST_AIRED, series.FIRST_AIRED);
		
		return Db.insert(DATABASE_SERIES_TABLE, null, initialValues);
	}
	public boolean deleteSeries(String rowId){
		/** return true if delete, false otherwise */
		return Db.delete(DATABASE_SERIES_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	public ArrayList<Series> fetchAllSeries(){
		Cursor c = Db.query(DATABASE_SERIES_TABLE, 
				new String[] {KEY_ROWID,KEY_TITLE,KEY_OVERVIEW,KEY_POSTER_URL,KEY_ACTORS,KEY_AIRS_DAYOFWEEK,KEY_AIRS_TIME,KEY_GENRE,KEY_IMDB_ID,KEY_NETWORK,KEY_RATING,KEY_RUNTIME,KEY_STATUS,KEY_LAST_UPDATED,KEY_RATING_COUNT,KEY_CONTENT_RATING,KEY_FIRST_AIRED}, 
				null, null, null, null, null);
		
		if(c.moveToFirst()){
			ArrayList<Series> series = new ArrayList<Series>();
			while(!c.isAfterLast()){
				Series s = new Series();
    			s.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
    			s.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
    			s.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
    			s.POSTER_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_POSTER_URL));
    			s.ACTORS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ACTORS));
    			s.AIRS_DAYOFWEEK = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_AIRS_DAYOFWEEK));
    			s.AIRS_TIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_AIRS_TIME));
    			s.GENRE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GENRE));
    			s.IMDB_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMDB_ID));
    			s.NETWORK = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_NETWORK));
    			s.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
    			s.RUNTIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RUNTIME));
    			s.STATUS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_STATUS));
    			s.LAST_UPDATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST_UPDATED));
    			s.RATING_COUNT = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING_COUNT));
    			s.CONTENT_RATING = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CONTENT_RATING));
    			s.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
    			series.add(s);
    			c.moveToNext();
			}
			c.close();
			return series;
		}else{
			c.close();
			return null;
		}
		
	}
	public Series fetchSeries(String rowId) throws SQLException {
		Cursor c = Db.query(DATABASE_SERIES_TABLE, 
				new String[] {KEY_ROWID,KEY_TITLE,KEY_OVERVIEW,KEY_POSTER_URL,KEY_ACTORS,KEY_AIRS_DAYOFWEEK,KEY_AIRS_TIME,KEY_GENRE,KEY_IMDB_ID,KEY_NETWORK,KEY_RATING,KEY_RUNTIME,KEY_STATUS,KEY_LAST_UPDATED,KEY_RATING_COUNT,KEY_CONTENT_RATING,KEY_FIRST_AIRED}, 
				KEY_ROWID + "='" + rowId + "'",
				null, null, null, null, null);
		if(c.moveToFirst()){
			Series s = new Series();
			s.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
			s.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
			s.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
			s.POSTER_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_POSTER_URL));
			s.ACTORS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ACTORS));
			s.AIRS_DAYOFWEEK = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_AIRS_DAYOFWEEK));
			s.AIRS_TIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_AIRS_TIME));
			s.GENRE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GENRE));
			s.IMDB_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMDB_ID));
			s.NETWORK = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_NETWORK));
			s.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
			s.RUNTIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RUNTIME));
			s.STATUS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_STATUS));
			s.LAST_UPDATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST_UPDATED));
			s.RATING_COUNT = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING_COUNT));
			s.CONTENT_RATING = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CONTENT_RATING));
			s.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
			
			c.close();
			return s;
		}
		c.close();
		return null;
	}
	public ArrayList<String> fetchSeriesIdList() throws SQLException	{
		Cursor c = Db.query(DATABASE_SERIES_TABLE, 
				new String[] {KEY_ROWID}, 
				null, null, null, null, null, null);
		if(c.moveToFirst()){
			ArrayList<String> list = new ArrayList<String>();
			while(!c.isAfterLast()){
				list.add(c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID)));
    			c.moveToNext();
			}
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
		
	}
	public boolean updateSeries(Series series){
		ContentValues values = new ContentValues();
		values.put(KEY_ROWID, series.ID);
		values.put(KEY_TITLE, series.TITLE);
		values.put(KEY_OVERVIEW, series.OVERVIEW);
		values.put(KEY_POSTER_URL, series.POSTER_URL);
		values.put(KEY_ACTORS, series.ACTORS);
		values.put(KEY_AIRS_DAYOFWEEK, series.AIRS_DAYOFWEEK);
		values.put(KEY_AIRS_TIME, series.AIRS_TIME);
		values.put(KEY_GENRE, series.GENRE);
		values.put(KEY_IMDB_ID, series.IMDB_ID);
		values.put(KEY_NETWORK, series.NETWORK);
		values.put(KEY_RATING, series.RATING);
		values.put(KEY_RUNTIME, series.RUNTIME);
		values.put(KEY_STATUS, series.STATUS);
		values.put(KEY_LAST_UPDATED, series.LAST_UPDATED);
		values.put(KEY_RATING_COUNT, series.RATING_COUNT);
		values.put(KEY_CONTENT_RATING, series.CONTENT_RATING);
		values.put(KEY_FIRST_AIRED, series.FIRST_AIRED);
		
		return Db.update(DATABASE_SERIES_TABLE, values, KEY_ROWID + "=" + series.ID, null) > 0;
	}
	
	public boolean isSeriesFavorited(String series_id){
		Cursor c = Db.query(DATABASE_SERIES_TABLE, 
				new String[]{KEY_ROWID}, 
				KEY_ROWID + "=" + series_id, 
				null, null, null, null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}else{
			c.close();
			return false;
		}
	}
	
	/**
	 * EPISODES METHODS
	 */
	public long insertEpisode(Episode episode){
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EPISODE_ID, episode.ID);
		initialValues.put(KEY_SERIES_ID, episode.SERIES_ID);
		initialValues.put(KEY_TITLE, episode.TITLE);
		initialValues.put(KEY_OVERVIEW, episode.OVERVIEW);
		initialValues.put(KEY_IMAGE_URL, episode.IMAGE_URL);
		initialValues.put(KEY_GUEST_STARS, episode.GUEST_STARS);
		initialValues.put(KEY_RATING, episode.RATING);
		initialValues.put(KEY_RATING_COUNT, episode.RATING_COUNT);
		initialValues.put(KEY_EPISODE_NUMBER, episode.EPISODE_NUMBER);
		initialValues.put(KEY_SEASON_NUMBER, episode.SEASON_NUMBER);
		initialValues.put(KEY_LAST_UPDATED, episode.LAST_UPDATED);
		initialValues.put(KEY_FIRST_AIRED, episode.FIRST_AIRED);
		initialValues.put(KEY_REMINDER, episode.REMINDER);
		
		return Db.insert(DATABASE_EPISODES_TABLE, null, initialValues);
	}
	public boolean deleteAllEpisode(String seriesId){
		return Db.delete(DATABASE_EPISODES_TABLE, KEY_SERIES_ID + "==" + seriesId, null) > 0;
	}
	/** 
	 * Not being used at the moment
	 * public Cursor fetchEpisodeBySeason(int seasonNumber){
	 *	return Db.query(DATABASE_EPISODES_TABLE,
	 *			new String[] {KEY_ROWID, KEY_SERIES_ID, KEY_TITLE, KEY_OVERVIEW, KEY_IMAGE_URL, KEY_GUEST_STARS, KEY_RATING, KEY_RATING_COUNT, KEY_EPISODE_NUMBER, KEY_SEASON_NUMBER, KEY_LAST_UPDATED, KEY_FIRST_AIRED, KEY_REMINDER}, 
	 *			null, null, null, null, null);
	 * }
	 * 
	 */
	public boolean deleteEpisode(String episodeId){
		return Db.delete(DATABASE_EPISODES_TABLE, KEY_EPISODE_ID + "=" + episodeId, null) > 0;
	}
		
	public Episode fetchEpisode(String episodeId){
		Cursor c = Db.query(DATABASE_EPISODES_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, KEY_TITLE, KEY_OVERVIEW, KEY_IMAGE_URL, KEY_GUEST_STARS, KEY_RATING, KEY_RATING_COUNT, KEY_EPISODE_NUMBER, KEY_SEASON_NUMBER, KEY_LAST_UPDATED, KEY_FIRST_AIRED, KEY_REMINDER}, 
				KEY_EPISODE_ID + "=" + episodeId, 
				null, null, null, null);
		if(c.moveToFirst()){
			Episode e = new Episode();
			e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
			e.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
			e.GUEST_STARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
			e.RATING_COUNT = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING_COUNT));
			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
			e.LAST_UPDATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST_UPDATED));
			e.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
			e.REMINDER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_REMINDER));
			
			c.close();
			return e;
		}else{
			c.close();
			return null;
		}
	}
	public ArrayList<Episode> fetchAllEpisodes(String seriesId){
		Cursor c =  Db.query(DATABASE_EPISODES_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, KEY_TITLE, KEY_OVERVIEW, KEY_IMAGE_URL, KEY_GUEST_STARS, KEY_RATING, KEY_RATING_COUNT, KEY_EPISODE_NUMBER, KEY_SEASON_NUMBER, KEY_LAST_UPDATED, KEY_FIRST_AIRED, KEY_REMINDER}, 
				KEY_SERIES_ID + "=" + seriesId, 
				null, null, null, null);
		
		if(c.moveToFirst()){
			ArrayList<Episode> allEpisodes = new ArrayList<Episode>();
			while(!c.isAfterLast()){
				Episode e = new Episode();
    			e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
    			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
    			e.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
    			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
    			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
    			e.GUEST_STARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
    			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
    			e.RATING_COUNT = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING_COUNT));
    			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
    			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
    			e.LAST_UPDATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST_UPDATED));
    			e.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
    			e.REMINDER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_REMINDER));
    			allEpisodes.add(e);
    			c.moveToNext();
			}
			c.close();
			return allEpisodes;
		}else{
			c.close();
			return null;
		}
	}

    public Episode[] fetchEpisodes(String seriesId){
        ArrayList<Episode> episodes = fetchAllEpisodes(seriesId);
        return  episodes.toArray(new Episode[episodes.size()]);
    }

	public ArrayList<Episode> fetchAllEpisodesBySeason(String series_id, int season_number){
		Cursor c = Db.query(DATABASE_EPISODES_TABLE, new String[] {KEY_EPISODE_ID,KEY_SERIES_ID, KEY_FIRST_AIRED}, 
				KEY_SEASON_NUMBER + "=" + season_number + " AND " + KEY_SERIES_ID + "=" + series_id,
				null, null, null, null);
		if(c.moveToFirst()){
			ArrayList<Episode> allEpisodes = new ArrayList<Episode>();
			while(!c.isAfterLast()){
				Episode e = new Episode();
    			e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
    			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
    			e.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
    			allEpisodes.add(e);
    			c.moveToNext();
			}
			c.close();
			return allEpisodes;
		}else{
			c.close();
			return null;
		}
	}
	public Episode fetchEpisodeBySeasonAndEpisodeNumber(String seriesID, int season, int episode){
		Cursor c = Db.query(DATABASE_EPISODES_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, 
					KEY_TITLE, KEY_OVERVIEW, KEY_IMAGE_URL, 
					KEY_GUEST_STARS, KEY_RATING, KEY_RATING_COUNT, 
					KEY_EPISODE_NUMBER, KEY_SEASON_NUMBER, KEY_LAST_UPDATED, 
					KEY_FIRST_AIRED, KEY_REMINDER}, 
				KEY_SERIES_ID + "=" + seriesID + " AND " + KEY_SEASON_NUMBER + "=" + season + " AND " + KEY_EPISODE_NUMBER + "=" + episode,
				null, null, null, null);
		if(c.moveToFirst()){
			Episode e = new Episode();
			
			e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
			e.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
			e.GUEST_STARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
			e.RATING_COUNT = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING_COUNT));
			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
			e.LAST_UPDATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST_UPDATED));
			e.FIRST_AIRED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_FIRST_AIRED));
			e.REMINDER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_REMINDER));
			
			c.close();
			
			return e;
		}else{
			return null;
		}
	}
	public boolean updateEpisode(Episode episode){
		ContentValues values = new ContentValues();
		values.put(KEY_EPISODE_ID, episode.ID);
		values.put(KEY_SERIES_ID, episode.SERIES_ID);
		values.put(KEY_TITLE, episode.TITLE);
		values.put(KEY_OVERVIEW, episode.OVERVIEW);
		values.put(KEY_IMAGE_URL, episode.IMAGE_URL);
		values.put(KEY_GUEST_STARS, episode.GUEST_STARS);
		values.put(KEY_RATING, episode.RATING);
		values.put(KEY_RATING_COUNT, episode.RATING_COUNT);
		values.put(KEY_EPISODE_NUMBER, episode.EPISODE_NUMBER);
		values.put(KEY_SEASON_NUMBER, episode.SEASON_NUMBER);
		values.put(KEY_LAST_UPDATED, episode.LAST_UPDATED);
		values.put(KEY_FIRST_AIRED, episode.FIRST_AIRED);
		values.put(KEY_REMINDER, episode.REMINDER);
		
		return Db.update(DATABASE_EPISODES_TABLE, values, KEY_EPISODE_ID + "=" + episode.ID, null) > 0;
	}
	/**
	 * REMINDERS METHODS
	 */
	public long insertReminder(Reminder reminder){
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_EPISODE_ID, reminder.EPISODE_ID);
		initialValues.put(KEY_SERIES_ID, reminder.SERIES_ID);
		initialValues.put(KEY_DATE, reminder.DATE);
		initialValues.put(KEY_TIME, reminder.TIME);
		initialValues.put(KEY_SERIES_NAME, reminder.SERIES_NAME);
		initialValues.put(KEY_EPISODE_NAME, reminder.EPISODE_NAME);
		initialValues.put(KEY_SEASON_NUMBER, reminder.SEASON_NUMBER);
		initialValues.put(KEY_EPISODE_NUMBER, reminder.EPISODE_NUMBER);
		initialValues.put(KEY_RATING, reminder.RATING);
		initialValues.put(KEY_OVERVIEW, reminder.OVERVIEW);
		initialValues.put(KEY_GUEST_STARS, reminder.GUESTSTARS);
		initialValues.put(KEY_IMAGE_URL, reminder.IMAGE_URL);
		
		return Db.insert(DATABASE_REMINDERS_TABLE, null, initialValues);
	}
	public boolean deleteReminder(String episodeId){
		return Db.delete(DATABASE_REMINDERS_TABLE, KEY_EPISODE_ID + "=" + episodeId, null) > 0;
	}
	public boolean deleteAllReminders(String seriesId){
		return Db.delete(DATABASE_REMINDERS_TABLE, KEY_SERIES_ID + "=" + seriesId, null) > 0;
	}
	public Reminder fetchReminderByEpisode(String episodeId){
		Cursor c = Db.query(DATABASE_REMINDERS_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, KEY_DATE, KEY_TIME,KEY_SERIES_NAME, KEY_EPISODE_NAME,KEY_SEASON_NUMBER,KEY_EPISODE_NUMBER,KEY_RATING,KEY_OVERVIEW,KEY_GUEST_STARS, KEY_IMAGE_URL}, 
				KEY_EPISODE_ID + "=" + episodeId, 
				null, null, null, null);
		if(c.moveToFirst()){
			Reminder e = new Reminder();
		    e.EPISODE_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
			e.DATE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DATE));
			e.TIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TIME));
			e.SERIES_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_NAME));
			e.EPISODE_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NAME));
			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
			e.GUESTSTARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
			
			c.close();
			return e;
		}else{
			c.close();
			return null;
		}
	}
	public ArrayList<Reminder> fetchAllReminders(){
		Cursor c = Db.query(DATABASE_REMINDERS_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, KEY_DATE, KEY_TIME,KEY_SERIES_NAME, KEY_EPISODE_NAME,KEY_SEASON_NUMBER,KEY_EPISODE_NUMBER,KEY_RATING,KEY_OVERVIEW,KEY_GUEST_STARS, KEY_IMAGE_URL}, 
				null, null, null, null, null);
		if(c.moveToFirst()){
			ArrayList<Reminder> seriesReminders = new ArrayList<Reminder>();
			while(!c.isAfterLast()){
			    Reminder e = new Reminder();
			    e.EPISODE_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
    			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
    			e.DATE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DATE));
    			e.TIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TIME));
    			e.SERIES_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_NAME));
    			e.EPISODE_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NAME));
    			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
    			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
    			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
    			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
    			e.GUESTSTARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
    			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
    			
    			seriesReminders.add(e);
    			c.moveToNext();
			}
			c.close();
			return seriesReminders;
		}else{
			c.close();
			return null;
		}
	}
	public ArrayList<Reminder> fetchAllRemindersBySeries(String seriesId){
		Cursor c = Db.query(DATABASE_REMINDERS_TABLE,
				new String[] {KEY_EPISODE_ID, KEY_SERIES_ID, KEY_DATE, KEY_TIME,KEY_SERIES_NAME, KEY_EPISODE_NAME,KEY_SEASON_NUMBER,KEY_EPISODE_NUMBER,KEY_RATING,KEY_OVERVIEW,KEY_GUEST_STARS, KEY_IMAGE_URL}, 
				KEY_SERIES_ID + "=" + seriesId, 
				null, null, null, null);
		if(c.moveToFirst()){
			ArrayList<Reminder> seriesReminders = new ArrayList<Reminder>();
			while(!c.isAfterLast()){
			    Reminder e = new Reminder();
			    e.EPISODE_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
    			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
    			e.DATE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DATE));
    			e.TIME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TIME));
    			e.SERIES_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_NAME));
    			e.EPISODE_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NAME));
    			e.SEASON_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SEASON_NUMBER));
    			e.EPISODE_NUMBER = c.getShort(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NUMBER));
    			e.RATING = c.getFloat(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_RATING));
    			e.OVERVIEW = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_OVERVIEW));
    			e.GUESTSTARS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_GUEST_STARS));
    			e.IMAGE_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_IMAGE_URL));
    			
    			seriesReminders.add(e);
    			c.moveToNext();
			}
			c.close();
			return seriesReminders;
		}else{
			c.close();
			return null;
		}
	}
	public boolean updateReminder(Reminder reminder){
		ContentValues values = new ContentValues();
		values.put(KEY_EPISODE_ID, reminder.EPISODE_ID);
		values.put(KEY_SERIES_ID, reminder.SERIES_ID);
		values.put(KEY_DATE, reminder.DATE);
		values.put(KEY_TIME, reminder.TIME);
		values.put(KEY_SERIES_NAME, reminder.SERIES_NAME);
		values.put(KEY_EPISODE_NAME, reminder.EPISODE_NAME);
		values.put(KEY_SEASON_NUMBER, reminder.SEASON_NUMBER);
		values.put(KEY_EPISODE_NUMBER, reminder.EPISODE_NUMBER);
		values.put(KEY_RATING, reminder.RATING);
		values.put(KEY_OVERVIEW, reminder.OVERVIEW);
		values.put(KEY_GUEST_STARS, reminder.GUESTSTARS);
		values.put(KEY_IMAGE_URL, reminder.IMAGE_URL);
		
		return Db.update(DATABASE_REMINDERS_TABLE, values, KEY_EPISODE_ID + "=" + reminder.EPISODE_ID, null) > 0;
	}
	/**
	 * WATCHED Methods
	 */
	public long insertWatched(Episode episode){
		ContentValues iv = new ContentValues();
		iv.put(KEY_SERIES_ID, episode.SERIES_ID);
		iv.put(KEY_EPISODE_ID, episode.ID);
		
		//insert episode into QUEUE database if its the latest one or w/e 
		if(isEpisodeWatchedNewest(episode.ID, episode.SERIES_ID)){
			deleteQueueSeries(episode.SERIES_ID);
			insertQueue(episode);
		}
		
		return Db.insert(DATABASE_WATCHED_TABLE, null, iv);
	}
	public long insertWatched(String seriesID, String episodeID){
		ContentValues iv = new ContentValues();
		iv.put(KEY_SERIES_ID, seriesID);
		iv.put(KEY_EPISODE_ID, episodeID);
		
		//insert episode into QUEUE database if its the latest one
		if(isEpisodeWatchedNewest(episodeID, seriesID)){
			deleteQueueSeries(seriesID);
			insertQueue(seriesID, episodeID);
		}
		
		return Db.insert(DATABASE_WATCHED_TABLE, null, iv);
	}
	public boolean deleteWatchedEpisode(String episodeID){
		return Db.delete(DATABASE_WATCHED_TABLE, KEY_EPISODE_ID + "=" + episodeID, null) > 0;
	}

	public  boolean deleteWatchedSeries(String seriesID){
		return Db.delete(DATABASE_WATCHED_TABLE, KEY_SERIES_ID + "=" + seriesID, null) > 0;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeSet fetchWatchedBySeriesId(String seriesID){
		Cursor c = Db.query(DATABASE_WATCHED_TABLE, 
				new String[] {KEY_EPISODE_ID}, 
				KEY_SERIES_ID + "=" + seriesID, 
				null, null, null, null);
		if(c.moveToFirst()){
			TreeSet IDs = new TreeSet();
			while(!c.isAfterLast()){
				String id = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				IDs.add(id);
				c.moveToNext();
			}
			c.close();
			return IDs;
		}else{
			c.close();
			return new TreeSet();
		}
	}
	public ArrayList<String> fetchWatchedBySeries(String seriesID){
		Cursor c = Db.query(DATABASE_WATCHED_TABLE, 
				new String[] {KEY_EPISODE_ID}, 
				KEY_SERIES_ID + "=" + seriesID, 
				null, null, null, null);
		if(c.moveToFirst()){
			ArrayList<String> IDs = new ArrayList<String>();
			while(!c.isAfterLast()){
				String id = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				IDs.add(id);
				c.moveToNext();
			}
			c.close();
			return IDs;
		}else{
			c.close();
			return new ArrayList<String>();
		}
	}
	public boolean isEpisodeWatched(String episodeID){
		Cursor c = Db.query(DATABASE_WATCHED_TABLE, 
				new String[]{KEY_EPISODE_ID}, 
				KEY_EPISODE_ID + "=" + episodeID, 
				null, null, null, null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}else{
			c.close();
			return false;
		}
	}
	
	/**
	 * 
	 * Watched Pending Table
	 * 
	 * private static String WATCHED_PENDING_DATABASE_CREATE =
			"create table " + DATABASE_WATCHED_PENDING_TABLE + " ("
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_SERIES_ID + " text, "
			+ KEY_EPISODE_ID + " text, "
			+ KEY_MISC_1 + " text, "
			+ KEY_MISC_2 + " text);";
	 * 
	 */
	public long insertWatchedPending(Episode episode){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_SERIES_ID, episode.SERIES_ID);
		iv.put(KEY_EPISODE_ID, episode.ID);
		iv.put(KEY_MISC_1, episode.SYNCHED);
		iv.put(KEY_MISC_2, episode.DELETED);
		
		return Db.insert(DATABASE_WATCHED_PENDING_TABLE, null, iv);
	}
	
	public boolean deleteWatchedPending(String episodeId){
		return Db.delete(DATABASE_WATCHED_PENDING_TABLE, KEY_EPISODE_ID + "='" + episodeId + "'", null) > 0;
	}
	
	public boolean deleteWatchedPendingSeries(String seriesId){
		return Db.delete(DATABASE_WATCHED_PENDING_TABLE, 
				KEY_SERIES_ID + "='" + seriesId + "'", 
				null) > 0;
	}
	
	public boolean deleteDeletedWatchedPendingSeries(String seriesId){
		return Db.delete(DATABASE_WATCHED_PENDING_TABLE, 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_MISC_2 + " ='" + Episode.KEY_TRUE + "'", 
				null) > 0;
	}
	
	public boolean deleteUnsynchedWatchedPendingSeries(String seriesId){
		return Db.delete(DATABASE_WATCHED_PENDING_TABLE, 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_MISC_1 + " ='" + Episode.KEY_FALSE + "'", 
				null) > 0;
	}
	
	public boolean isEpisodeWatchedPending(String episodeId){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, 
				KEY_EPISODE_ID + "='" + episodeId + "'", 
				null, null, null, null);
		
		if(c.moveToFirst()){
			
			c.close();
			return true;
			
		}else{
			
			c.close();
			return false;
		}
	}
	
	public Episode fetchWatchedPending(String episodeId){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, 
				KEY_EPISODE_ID + "='" + episodeId + "'", 
				null, null, null, null);
		
		if(c.moveToFirst()){
			
			Episode e = new Episode();
			
			e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
			e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
			e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
			
			c.close();

			return e;
			
		}else{
			
			c.close();
			return null;
		}
	}
	
	public boolean updateWatchedPending(Episode e){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_SERIES_ID, e.SERIES_ID);
		iv.put(KEY_EPISODE_ID, e.ID);
		iv.put(KEY_MISC_1, e.SYNCHED);
		iv.put(KEY_MISC_2, e.DELETED);
		
		return Db.update(DATABASE_WATCHED_PENDING_TABLE, iv, KEY_EPISODE_ID + "='" + e.ID + "'", null) > 0;
		
	}
	
	public ArrayList<Episode> fetchAllDeletedWatchedPending(){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_MISC_2 + "='" + Episode.KEY_TRUE + "'", // Where KEY_DELETED is true
				null, null, null, null); 
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchWatchedPendingBySeries(String seriesId){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_SERIES_ID + "='" + seriesId + "'", // Where KEY_SYNCHED is false
				null, null, null, null); 
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchDeletedWatchedPendingBySeries(String seriesId){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_MISC_2 + "='" + Episode.KEY_TRUE + "'", // Where series_id = id and deleted = true
				null, null, null, null);
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchUnsynchedWatchedPendingBySeries(String seriesId){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_MISC_1 + "='" + Episode.KEY_FALSE + "'", // Where series_id = id and deleted = true
				null, null, null, null);
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchUnsynchedWatchedPending(){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_MISC_1 + "='" + Episode.KEY_FALSE + "'", // Where KEY_SYNCHED is false
				null, null, null, null); 
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchUndeletedWatchedPending(){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				KEY_MISC_2 + "='" + Episode.KEY_TRUE + "'", // Where KEY_DELETED is true
				null, null, null, null); 
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	public ArrayList<Episode> fetchAllWatchedPending(){
		
		Cursor c = Db.query(DATABASE_WATCHED_PENDING_TABLE, 
				null, // Return all 
				null, // All of them
				null, null, null, null); 
		
		if(c.moveToFirst()){
			
			ArrayList<Episode> list = new ArrayList<Episode>();
			
			while(!c.isAfterLast()){
				
				Episode e = new Episode();
				
				e.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				e.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				e.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				e.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(e);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
			
		}else{
			
			c.close();
			return null;
			
		}
	}
	
	/**
	 * 
	 * QUEUE Methods
	 * 
	 * There Should never by more than one episode per series on this database...ever.
	 * 
	 */
	public long insertQueue(Episode episode){
		ContentValues iv = new ContentValues();
		iv.put(KEY_SERIES_ID, episode.SERIES_ID);
		iv.put(KEY_EPISODE_ID, episode.ID);
		
		return Db.insert(DATABASE_QUEUE_TABLE, null, iv);
	}
	public long insertQueue(String seriesID, String episodeID){
		ContentValues iv = new ContentValues();
		iv.put(KEY_SERIES_ID, seriesID);
		iv.put(KEY_EPISODE_ID, episodeID);
		
		return Db.insert(DATABASE_QUEUE_TABLE, null, iv);
	}
	public boolean deleteQueueEpisode(String episodeID){
		return Db.delete(DATABASE_QUEUE_TABLE, KEY_EPISODE_ID + "=" + episodeID, null) > 0;
	}
	public boolean deleteQueueSeries(String seriesID){
		return Db.delete(DATABASE_QUEUE_TABLE, KEY_SERIES_ID + "=" + seriesID, null) > 0;
	}
	public String fetchQueueWithSeriesId(String seriesID){
		Cursor c = Db.query(DATABASE_QUEUE_TABLE, 
				new String[] {KEY_EPISODE_ID}, 
				KEY_SERIES_ID + "=" + seriesID, 
				null, null, null, null);
		if(c.moveToFirst()){
			String id = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			c.close();
			return id;
		}else{
			c.close();
			return "";
		}
	}
	public String fetchQueueWithEpisodeId(String episodeID){
		Cursor c = Db.query(DATABASE_QUEUE_TABLE, 
				new String[] {KEY_EPISODE_ID}, 
				KEY_SERIES_ID + "=" + episodeID, 
				null, null, null, null);
		if(c.moveToFirst()){
			String id = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
			c.close();
			return id;
		}else{
			c.close();
			return "";
		}
	}
	public ArrayList<String> fetchQueueAll(){
		Cursor c = Db.query(DATABASE_QUEUE_TABLE, 
				null, 
				null, 
				null, null, null,
				KEY_ROWID + " DESC"); // Sort by the id  in descending order
		if(c.moveToFirst()){
			ArrayList<String> list = new ArrayList<String>();
			while(!c.isAfterLast()){
				String id = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_ID));
				list.add(id);
				c.moveToNext();
			}
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	public boolean isInQueue(String episodeID){
		Cursor c = Db.query(DATABASE_QUEUE_TABLE, 
				new String[] {KEY_EPISODE_ID}, 
				KEY_EPISODE_ID + "=" + episodeID, 
				null, null, null, null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}else{
			c.close();
			return false;
		}
	}
	/**
	 * 
	 * QUEUE Functions to check which is the next episode in your queue
	 * 
	 */
	public boolean isEpisodeWatchedNewest(String episodeID, String seriesID){
		
		//Fetch episode from DB
		Episode episode = fetchEpisode(episodeID);
		
		//Fetch the episode from Queue DB
		String id = fetchQueueWithSeriesId(seriesID);
		Episode queue = id.equals("") ? null : fetchEpisode(id);
			
			//Compare both of them
			if((queue == null) && (episode != null)){
				//insert episode to queue
				return true;
			}else{
				//Now compare both
				//Compare the Season Number, not taking into consideration season 0
				if((queue.SEASON_NUMBER == episode.SEASON_NUMBER) && (episode.SEASON_NUMBER != 0)){
					//Both are on the same season
					//Compare the Episode Number
					if(queue.EPISODE_NUMBER < episode.EPISODE_NUMBER){
						//delete old queue episode and insert the newer episode
						return true;
					}
				}else if((queue.SEASON_NUMBER < episode.SEASON_NUMBER) && (episode.SEASON_NUMBER != 0)){
					//queue is on an older season
					return true;
				}
				
			}
		
		return false;
	}
	
	/**
	 * 
	 * HOT Methods
	 * 
	 */
	public long insertHot(Series series){
		ContentValues iv = new ContentValues();
		iv.put(KEY_SERIES_ID, series.ID);
		iv.put(KEY_POSTER_URL, series.POSTER_URL);
		iv.put(KEY_EPISODE_NAME, series.TITLE);
		
		return Db.insert(DATABASE_HOT_TABLE, null, iv);
	}
	public boolean deleteHotAll(){
		return Db.delete(DATABASE_HOT_TABLE, KEY_ROWID + ">" + 0 , null) > 0;
	}
	public ArrayList<Series> fetchHot(){
		Cursor c = Db.query(DATABASE_HOT_TABLE, 
				new String[] {KEY_SERIES_ID,KEY_POSTER_URL,KEY_EPISODE_NAME}, 
				null,null, null, null, null);
		if(c.moveToFirst()){
			
			ArrayList<Series> list = new ArrayList<Series>();
			
			while(!c.isAfterLast()){
				
				Series s = new Series();
				
				s.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				s.POSTER_URL = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_POSTER_URL));
				s.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_EPISODE_NAME));
				
				list.add(s);
				c.moveToNext();
			}
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	/**
	 * 
	 * LOGIN Methods
	 * 
	 */
	public void addUser(String username, String email, String uid, String created_at){
		
		ContentValues v = new ContentValues();
		v.put(KEY_USERNAME, username);
		v.put(KEY_EMAIL, email);
		v.put(KEY_UID, uid);
		//v.put(KEY_CREATED_AT, created_at);
		
		Db.insert(DATABASE_LOGIN_TABLE, null, v);
		
	}
	public HashMap<String, String> getUserDetails(){
		
		HashMap<String,String> user = new HashMap<String,String>();
		String query = "SELECT * FROM " + DATABASE_LOGIN_TABLE;
		
		Cursor c = Db.rawQuery(query, null);
		if(c.moveToFirst()){
			
			user.put("username", c.getString(1));
			user.put("email", c.getString(2));
			user.put("uid", c.getString(3));
			user.put("created_at", c.getString(4));
			
			c.close();
			return user;
			
		}else{
			
			c.close();
			return null;
		
		}
		
	}
	public int getUserCount(){
		
		String query = "SELECT * FROM " + DATABASE_LOGIN_TABLE;
		Cursor c = Db.rawQuery(query, null);
		int userCount = c.getCount();
		c.close();
		
		return userCount;
	}
	public boolean deleteAllUsers(){
		return Db.delete(DATABASE_LOGIN_TABLE, null , null) > 0;
	}
	
	/**
	 * List Methods
	 */
	public boolean insertList(ListObject list){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_LIST_NAME, list.NAME);
		iv.put(KEY_DESCRIPTION, list.DESCRIPTION);
		iv.put(KEY_SHOW_NUMBERS, list.SHOW_NUMBERS);
		iv.put(KEY_ALLOW_COMMENTS, list.ALLOW_COMMENTS);
		iv.put(KEY_SYNCHED, list.SYNCHED);
		iv.put(KEY_CREATED, list.CREATED);
		iv.put(KEY_DELETED, list.DELETED);
		iv.put(KEY_MISC_1, list.MISC1);
		iv.put(KEY_MISC_2, list.MISC2);
		
		// Try to create the slug, if not input empty string
		try {
			iv.put(KEY_SLUG, BaseSlidingActivity.slugify(list.NAME));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			iv.put(KEY_SLUG, "");
		}
		
		return Db.insert(DATABASE_LIST_TABLE, null, iv) > 0;
	}
	
	public boolean deleteList(String name){
		
		return Db.delete(DATABASE_LIST_TABLE, KEY_LIST_NAME + "='" + name + "'", null) > 0;

	}
	
	public boolean updateList(ListObject list){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_LIST_NAME, list.NAME);
		iv.put(KEY_DESCRIPTION, list.DESCRIPTION);
		iv.put(KEY_SHOW_NUMBERS, list.SHOW_NUMBERS);
		iv.put(KEY_ALLOW_COMMENTS, list.ALLOW_COMMENTS);
		iv.put(KEY_SYNCHED, list.SYNCHED);
		iv.put(KEY_CREATED, list.CREATED);
		iv.put(KEY_DELETED, list.DELETED);
		iv.put(KEY_PRIVACY, list.PRIVACY);
		iv.put(KEY_MISC_1, list.MISC1);
		iv.put(KEY_MISC_2, list.MISC2);
		iv.put(KEY_SLUG, list.SLUG);
		
		return Db.update(DATABASE_LIST_TABLE, iv, KEY_LIST_NAME + "='" + list.NAME + "'", null) > 0;
		
	}
	
	public ListObject fetchListDetails(String name){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				null, // Return all 
				KEY_LIST_NAME + "='" + name + "'", // Where list_name = name 
				null, null, null, null);
		
		if(c.moveToFirst()){
			ListObject list = new ListObject();
			
			list.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
			list.NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
			list.SLUG = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SLUG));
			list.PRIVACY = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_PRIVACY));
			list.DESCRIPTION = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DESCRIPTION));
			list.SHOW_NUMBERS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SHOW_NUMBERS));
			list.ALLOW_COMMENTS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ALLOW_COMMENTS));
			list.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
			list.CREATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CREATED));
			list.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
			list.PRIVACY = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_PRIVACY));
			list.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
			list.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
		
	}
	
	public ArrayList<CharSequence> fetchNonDeletedListNames(){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				new String[]{KEY_LIST_NAME}, // return just the list name
				KEY_DELETED + "!='" + ListObject.KEY_TRUE + "'", // Where is not flagged as deleted
				null, null, null, 
				KEY_LIST_NAME + " ASC");
		
		if(c.moveToFirst()){
			
			ArrayList<CharSequence> listNames = new ArrayList<CharSequence>();
			
			while(!c.isAfterLast()){
				
				String name = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				listNames.add(name);
				c.moveToNext();
			
			}
			
			c.close();
			return listNames;
		
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<CharSequence> fetchAllListNames(){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				new String[]{KEY_LIST_NAME}, // return just the list name
				null, // all
				null, null, null, 
				KEY_LIST_NAME + " ASC");
		
		if(c.moveToFirst()){
			
			ArrayList<CharSequence> listNames = new ArrayList<CharSequence>();
			
			while(!c.isAfterLast()){
				
				String name = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				listNames.add(name);
				c.moveToNext();
			
			}
			
			c.close();
			return listNames;
		
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<ListObject> fetchUncreatedLists(){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				null, // Return all 
				KEY_CREATED + "='" + ListObject.KEY_FALSE + "'", // Where KEY_CREATED is false 
				null, null, null, null); 
		
		if(c.moveToFirst()){
			ArrayList<ListObject> list = new ArrayList<ListObject>();
			
			while(!c.isAfterLast()){
				
				ListObject item = new ListObject();
				
				item.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
				item.NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.SLUG = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SLUG));
				item.PRIVACY = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_PRIVACY));
				item.DESCRIPTION = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DESCRIPTION));
				item.SHOW_NUMBERS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SHOW_NUMBERS));
				item.ALLOW_COMMENTS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ALLOW_COMMENTS));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.CREATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CREATED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<ListObject> fetchUnsynchedLists(){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				null, // Return all 
				KEY_SYNCHED + "='" + ListObject.KEY_FALSE +"'", // Where KEY_SYNCHED is false 
				null, null, null, null); 
		
		if(c.moveToFirst()){
			ArrayList<ListObject> list = new ArrayList<ListObject>();
			
			while(!c.isAfterLast()){
				
				ListObject item = new ListObject();
				
				item.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
				item.NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.SLUG = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SLUG));
				item.PRIVACY = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_PRIVACY));
				item.DESCRIPTION = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DESCRIPTION));
				item.SHOW_NUMBERS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SHOW_NUMBERS));
				item.ALLOW_COMMENTS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ALLOW_COMMENTS));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.CREATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CREATED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<ListObject> fetchDeletedLists(){
		
		Cursor c = Db.query(DATABASE_LIST_TABLE, 
				null, // Return all 
				KEY_DELETED + "='" + ListObject.KEY_TRUE+"'", // Where KEY_DELETED is false 
				null, null, null, null); 
		
		if(c.moveToFirst()){
			ArrayList<ListObject> list = new ArrayList<ListObject>();
			
			while(!c.isAfterLast()){
				
				ListObject item = new ListObject();
				
				item.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
				item.NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.SLUG = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SLUG));
				item.PRIVACY = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_PRIVACY));
				item.DESCRIPTION = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DESCRIPTION));
				item.SHOW_NUMBERS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SHOW_NUMBERS));
				item.ALLOW_COMMENTS = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ALLOW_COMMENTS));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.CREATED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_CREATED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	
	
	/**
	 * List Item Methods
	 */
	
	public long insertListItem(ListItem item){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_LIST_NAME, item.LIST_NAME);
		iv.put(KEY_TYPE, item.TYPE);
		iv.put(KEY_SERIES_ID, item.SERIES_ID);
		iv.put(KEY_TITLE, item.TITLE);
		iv.put(KEY_SYNCHED, item.SYNCHED);
		iv.put(KEY_DELETED, item.DELETED);
		iv.put(KEY_MISC_1, item.MISC1);
		iv.put(KEY_MISC_2, item.MISC2);
		
		return Db.insert(DATABASE_LIST_ITEM_TABLE, null, iv);
	}
	
	public boolean deleteListItem(String listName, String series_id){

		return Db.delete(DATABASE_LIST_ITEM_TABLE, KEY_LIST_NAME + "='" + listName + "' AND " + KEY_SERIES_ID + "='" + series_id + "'", null) > 0;
	
	}
	
	public boolean updateListItem(ListItem item){
		
		ContentValues iv = new ContentValues();
		
		iv.put(KEY_LIST_NAME, item.LIST_NAME);
		iv.put(KEY_TYPE, item.TYPE);
		iv.put(KEY_SERIES_ID, item.SERIES_ID);
		iv.put(KEY_TITLE, item.TITLE);
		iv.put(KEY_SYNCHED, item.SYNCHED);
		iv.put(KEY_DELETED, item.DELETED);
		iv.put(KEY_MISC_1, item.MISC1);
		iv.put(KEY_MISC_2, item.MISC2);
		
		return Db.update(DATABASE_LIST_ITEM_TABLE, iv, KEY_LIST_NAME + "='" + item.LIST_NAME + "' AND " + KEY_SERIES_ID + "='" + item.SERIES_ID +"'", null) > 0;
		
	}
	
	public ListItem fetchListItem(String listName, String seriesId){
		
		Cursor c = Db.query(DATABASE_LIST_ITEM_TABLE, 
				null, // Return all 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_LIST_NAME + "='" + listName + "'",// Where list_name = name 
				null, null, null, null);
		
		if(c.moveToFirst()){
			
			ListItem item = new ListItem();
			
			item.ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_ROWID));
			item.LIST_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
			item.TYPE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE));
			item.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
			item.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
			item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
			item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
			item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
			item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
			
			return item;
			
		}else{
			return null;
		}
	}
	
	public ArrayList<ListItem> fetchListItemsOfList(String listName){
		
		Cursor c = Db.query(DATABASE_LIST_ITEM_TABLE, 
				null, // Return all 
				KEY_LIST_NAME + "='" + listName + "' AND " + KEY_DELETED + "!='" + ListItem.KEY_TRUE+"'", // Where list_name = name 
				null, null, null, 
				KEY_TITLE + " ASC"); // Sort by the list name ascending
		
		if(c.moveToFirst()){
			ArrayList<ListItem> list = new ArrayList<ListItem>();
			
			while(!c.isAfterLast()){
				
				ListItem item = new ListItem();
				
				item.LIST_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.TYPE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE));
				item.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				item.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
		
	}
	
	public ArrayList<String> fetchListsWithSeriesId(String seriesId){
		
		Cursor c = Db.query(DATABASE_LIST_ITEM_TABLE, 
				null, // Return all 
				KEY_SERIES_ID + "='" + seriesId + "' AND " + KEY_DELETED + "!='" + ListItem.KEY_TRUE + "'", // Where series_id = seriesId 
				null, null, null, 
				KEY_TITLE + " ASC"); // Sort by the list name ascending
		
		if(c.moveToFirst()){
			ArrayList<String> list = new ArrayList<String>();
			
			while(!c.isAfterLast()){
				
				list.add(c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME)));
				
				c.moveToNext();
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<ListItem> fetchUnsynchedListItems(){
		
		Cursor c = Db.query(DATABASE_LIST_ITEM_TABLE, 
				null, // Return all 
				KEY_SYNCHED + "='" + ListItem.KEY_FALSE+"'", // Where KEY_SYNCHED is false 
				null, null, null, null); 
		
		if(c.moveToFirst()){
			ArrayList<ListItem> list = new ArrayList<ListItem>();
			
			while(!c.isAfterLast()){
				
				ListItem item = new ListItem();
				
				item.LIST_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.TYPE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE));
				item.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				item.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
	}
	
	public ArrayList<ListItem> fetchDeletedListItems(){
		
		Cursor c = Db.query(DATABASE_LIST_ITEM_TABLE, 
				null, // Return all 
				KEY_DELETED + "='" + ListItem.KEY_TRUE+"'", // Where KEY_DELETED is true 
				null, null, null, null); 
		
		if(c.moveToFirst()){
			ArrayList<ListItem> list = new ArrayList<ListItem>();
			
			while(!c.isAfterLast()){
				
				ListItem item = new ListItem();
				
				item.LIST_NAME = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_LIST_NAME));
				item.TYPE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE));
				item.SERIES_ID = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SERIES_ID));
				item.TITLE = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_TITLE));
				item.SYNCHED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_SYNCHED));
				item.DELETED = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELETED));
				item.MISC1 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_1));
				item.MISC2 = c.getString(c.getColumnIndexOrThrow(DatabaseAdapter.KEY_MISC_2));
				
				list.add(item);
				c.moveToNext();
			
			}
			
			c.close();
			return list;
		}else{
			c.close();
			return null;
		}
		
	}
	
}














