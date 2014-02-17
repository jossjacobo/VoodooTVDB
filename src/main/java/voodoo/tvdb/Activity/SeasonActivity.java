package voodoo.tvdb.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import voodoo.tvdb.Adapters.SeasonAdapter;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.WatchedHelper;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * TODO Add Reminder for this particular episode
 * TODONE Display a little "Clock" for episodes that have reminder...use SeriesID
 * TODO Share Episode info...
 * TODO Resolve the possible Favorite Star being wrong with onResume() returning.
 */
@SuppressLint("SimpleDateFormat")
public class SeasonActivity extends BaseActivity {
	private static final String TAG = "Season";

    public static final String SERIES = "series";
    public static final String SEASON_NUMBER = "season_number";
    public static final String EPISODE_LIST = "episode_list";

    String title;
	
	/** Action Bar */
	MenuItem favorite;
	boolean isFavorite = false;
	
	Series series;
	String season_number;
	ArrayList<Episode> episodes;
	ArrayList<Episode> fullEpisodeList;
	String[] reminderList;

	ListView list;
	SeasonAdapter adapter;
	DatabaseAdapter dbAdapter;
	TextView seasonBarTitle;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.season);
		dbAdapter = new DatabaseAdapter(this);
		
		list = (ListView) findViewById(R.id.seasons_list);
		
		//Get Resources Needed
		Intent i = getIntent();
		series = i.getParcelableExtra(SERIES);
		season_number = i.getStringExtra(SEASON_NUMBER);
		fullEpisodeList = i.getParcelableArrayListExtra(EPISODE_LIST);

		/** Action Bar */
		title = "Season " + season_number;
        setActionBarTitle(title);

		seasonBarTitle = (TextView) findViewById(R.id.season_bar_title);
		seasonBarTitle.setText(series.TITLE);
		
		registerForContextMenu(list);
		
		// Ads
    	AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
		
	}
	@Override
	public void onResume(){
		super.onResume();
		dbAdapter.open();
		
		if(adapter == null){
			adapter = new SeasonAdapter(this);
			
			episodes = sortOutSeasonEpisodes(fullEpisodeList, season_number);
			
			lookForRemindersInSD();
			setRemindersInEpisode();
			
			adapter.setItems(episodes);
			adapter.setFullEpisodeList(fullEpisodeList);
			adapter.setSeries(series);
			
			list.setAdapter(adapter);
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}else{
			adapter.getWatched();
			adapter.notifyDataSetChanged();
		}
		
		//update the star status
		isFavorite = dbAdapter.isSeriesFavorited(series.ID);
		supportInvalidateOptionsMenu();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		dbAdapter.close();
	}
	@Override
	public void onDestroy(){
		if(adapter != null){
			list.setAdapter(null);
		}
        //Log.d(TAG, "onDestroy");
        super.onDestroy();
        dbAdapter.close();
	}
	/**
	 * Create the menu Settings...
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		//ActionBar
		favorite = (MenuItem) menu.add("favorite").setIcon( isFavorite ? R.drawable.rate_star_med_on_holo_light : R.drawable.rate_star_med_off_holo_light);
		favorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		favorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {

                final FavoriteHelper faveHelper = new FavoriteHelper(SeasonActivity.this);
                faveHelper.createFavoriteAlert(series, faveHelper.isSeriesFavorited(series.ID),
                        new FavoriteSavingListener() {
                            @Override
                            public void onSavingCompleted(
                                    String series_id) {
                                favorite.setIcon(R.drawable.rate_star_med_on_holo_light);
                            }

                            @Override
                            public void onDeleteCompleted(String series_id) {
                                favorite.setIcon(R.drawable.rate_star_med_off_holo_light);
                            }
                        });
				return true;
			}
			
		});
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case android.R.id.home:
			Intent i = new Intent(this, SeriesInfoActivity.class);
			i.putExtra(SeriesInfoActivity.ID, series.ID);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.episode_menu_item_longpress, menu);
	}
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item){
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Episode e = (Episode) episodes.get(info.position);
		
		switch(item.getItemId()){
		case R.id.episode_menu_watched_all_previous:
			//mark all of the previous as watched...
			new allPrevAsync(SeasonActivity.this).execute(e);
			break;
		//Add more cases for each menu option
		}
		
		return super.onContextItemSelected(item);
	}
	
    
	/**
	 * Reminder Functions
	 */
    ArrayList<Reminder> reminders;
    private void lookForRemindersInSD() {
    	reminders = dbAdapter.fetchAllRemindersBySeries(series.ID);
    	sortByCalendar();
    	removeOlderReminders();
    	if(reminders != null && reminders.size() > 0){
    		reminderList = new String[reminders.size()];
    		for(int i = 0; i < reminderList.length; i++){
    			reminderList[i] = reminders.get(i).EPISODE_ID;
    		}
    	}else{
    		reminderList = null;
    	}
	}
    private void setRemindersInEpisode() {
		if(reminderList != null){
			for(int k = 0; k < episodes.size(); k++){
				Episode e = episodes.get(k);
				for(int i = 0; i < reminderList.length; i++){
					if(e.ID.equals(reminderList[i])){
						episodes.get(k).REMINDER = 1;
						break;
					}
				}
			}
		}
	}
    private void removeOlderReminders() {
    	if(reminders != null && reminders.size() > 1){
    		Calendar currentTime = Calendar.getInstance();
    		for(int i = 0; i < reminders.size(); i++){
    			Calendar date = getDate(reminders.get(i));
    			if(date.before(currentTime)){
    				reminders.remove(i);
    			}else{
    				break;
    			}
    		}
    	}
	}
    private void sortByCalendar(){
		if(reminders != null){
			Comparator<Reminder> comparator = new Comparator<Reminder>(){
				@Override
				public int compare(Reminder object1, Reminder object2) {
					Calendar object1Calendar = getDate(object1);
					Calendar object2Calendar = getDate(object2);
					return object1Calendar.compareTo(object2Calendar);
				}				
			};
			if(reminders.size() > 0){
				Collections.sort(reminders, comparator);
				//Log.d(TAG, "Series List Sorted by FIRST_AIRED");
			}
		}
	}
    @SuppressLint("SimpleDateFormat")
	private Calendar getDate(Reminder reminder){
		String dateString;
		Calendar d = Calendar.getInstance();
		if(reminder.TIME != null && reminder.DATE != null){
			
			dateString = reminder.TIME + reminder.DATE;
			
			SimpleDateFormat sdf = new SimpleDateFormat(dateString.length() == 17 ? "KK:mmaayyyy-MM-dd" : "KK:mmyyyy-MM-dd" );
			
			try {
				d.setTime(sdf.parse(dateString.replace(" ", "")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else if(reminder.DATE != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dateString = reminder.DATE;
			try {
				d.setTime(sdf.parse(dateString));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else{
			d = null;
		}
		return d;
	}
    
    /**
     * getDate Function from Episode Object
     */
    private Calendar getDateE(Episode e){
    	String dateString;
    	Calendar d = Calendar.getInstance();
    	if(e.FIRST_AIRED != null){
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dateString = e.FIRST_AIRED;
			try {
				d.setTime(sdf.parse(dateString));
			} catch (ParseException er) {
				er.printStackTrace();
			}
    	}else{
    		d = null;
    	}
    	return d;
    }
    
    /**
     * Season Function 
     * 	to sort out episodes from an individual season
     */
    private ArrayList<Episode> sortOutSeasonEpisodes(
			ArrayList<Episode> episodeList, String sNumber) {
		
		ArrayList<Episode> newList = new ArrayList<Episode>();
		int s = Integer.parseInt(sNumber);
		
		for(int i = 0; i < episodeList.size(); i++){
			if(s == episodeList.get(i).SEASON_NUMBER){
				newList.add(episodeList.get(i));
			}
		}
		
		return newList;
	}
    
    /**
     * AsyncTask to Mark as all previous episodes as seen
     */
    private class allPrevAsync extends AsyncTask<Episode, Void, String>{
    	
    	private Context context;
    	private ProgressDialog dialog;
    	private WatchedHelper wHelper;
		
		private AsyncTask<Episode, Void, String> mySaveToDbAsync = null;
		
		//Constructor
    	public allPrevAsync(Activity activity){
    		context = activity;
    		dialog = new ProgressDialog(context);
    		wHelper = new WatchedHelper(context);
    	}
    	
    	@Override
		protected void onPreExecute(){
			dialog.setMessage("Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mySaveToDbAsync.cancel(true);
				}
			});
			dialog.show();
			mySaveToDbAsync = this;
		}
		
		@Override
		protected String doInBackground(Episode... params) {
			String message;
			for(Episode e : params){
				
				ArrayList<Episode> E;
				switch(e.SEASON_NUMBER){
				
				case 0:
					//fetch all episodes from season 0
					E = dbAdapter.fetchAllEpisodesBySeason(e.SERIES_ID, e.SEASON_NUMBER);
					if(E != null){
						for(int i = 0; i < E.size(); i++){
							Calendar c = getDateE(E.get(i));
							
							if(e.FIRST_AIRED != null && E.get(i).FIRST_AIRED != null && c.before(getDateE(e))){
							
								//Add from the Watched database
								wHelper.markWatched(E.get(i).ID);
							
							}else{
								
								break;
							
							}
						}
					}
					break;
				
				default:
					for(int i = e.SEASON_NUMBER; i >= 1; i--){
						E = dbAdapter.fetchAllEpisodesBySeason(e.SERIES_ID, i);
						if(E != null){
							for(int j = 0; j < E.size(); j++){
								Calendar c = getDateE(E.get(j));
								if(e.FIRST_AIRED != null && E.get(j).FIRST_AIRED != null && c.before(getDateE(e))){

									//Add from the Watched database
									wHelper.markWatched(E.get(j).ID);
									
								}else{
									break;
								}
							}
						}
					}
					break;
				}
			}
			message = "All Previous set as seen";
			return message;
		}
		
		@Override
    	protected void onPostExecute(String m){
    		dialog.dismiss();
    		Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
    		adapter.getWatched();
			adapter.notifyDataSetChanged();
    	}
    	
    }
}