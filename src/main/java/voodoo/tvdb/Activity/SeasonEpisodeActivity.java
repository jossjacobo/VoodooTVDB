package voodoo.tvdb.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import voodoo.tvdb.objects.Episode;
import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.utils.WatchedHelper;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

@SuppressLint("SimpleDateFormat")
public class SeasonEpisodeActivity extends BaseActivity {

	private static final int COLLAPSED = 0;
	private static final int EXPANDED = 1;

    public static final String EPISODE = "episode";
    public static final String FULL_EPISODE_LIST = "full_episode_list";
    public static final String SERIES = "series";
    public static final String REMINDER = "reminder";

    String title;
	
	//Views
	ImageView image;
	TextView seasonNumber;
	TextView airdate;
	RatingBar ratingBar;
	TextView ratingText;
	TextView overview;
	TextView guestStars;
	ImageLoader imageLoader;
	
	TextView seasonEpisodeBarTitle;
	TextView episodeOverviewGroupTitle;
	TextView episodeGuestStarsGroupTitle;
	
	//Resources
	private Episode episode;
	private Reminder reminder;
	private ArrayList<Episode> full_episode_list;
	private Series series;
	
	//Database adapter and Inflater
	DatabaseAdapter dbAdapter;

	//Action bar
	MenuItem watched;
	Boolean isWatched = false;
	
	//Drop Down Image and Container
	public ImageView overviewDropDown;
	public ImageView guestDropDown;
	public LinearLayout overviewDropDownContainer;
	public LinearLayout guestDropDownContainer;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.season_episode);
		
		//Get Resources needed from intent
		Intent i = getIntent();
		episode = i.getParcelableExtra(EPISODE);
		reminder = i.getParcelableExtra(REMINDER);
		full_episode_list = i.getParcelableArrayListExtra(FULL_EPISODE_LIST);
		series = i.getParcelableExtra(SERIES);
		
		//Database Adapter Initialized
		dbAdapter = new DatabaseAdapter(this);
		
		boolean isReminder = reminder != null;
		if(isReminder){
			//Intent came from TimeLine Activity with a Reminder
			//Fetch other resources from database
			dbAdapter.open();
			
			episode = dbAdapter.fetchEpisode(reminder.EPISODE_ID);
			series = dbAdapter.fetchSeries(reminder.SERIES_ID);
			full_episode_list = dbAdapter.fetchAllEpisodes(reminder.SERIES_ID);
			
			dbAdapter.close();
		}
		
		//ActionBar
        title = series.TITLE;
        setActionBarTitle(title);

		findViews();
		setContent();
		
		// Ads
    	AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
	}
	@Override
	public void onResume(){
		super.onResume();
		
		isWatched = isWatched(episode.ID);
		supportInvalidateOptionsMenu();
		
	}
	@Override
	public void onPause(){
		super.onPause();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	/**
	 * Create the menu Settings...
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		//ActionBar
		getMenuInflater().inflate(R.menu.share_menu_item, menu);

		MenuItem share = menu.findItem(R.id.menu_icon_share);
		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
		//provider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, "Watching \"" + episode.TITLE + "\" from " + series.TITLE + "! http://voodootvdb.com");
		provider.setShareIntent(i);
		
		watched = menu.add("watched").setIcon(isWatched ? R.drawable.btn_check_on : R.drawable.btn_check_off);
        MenuItemCompat.setShowAsAction(watched, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		watched.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				dbAdapter.open();
				
				if(dbAdapter.isSeriesFavorited(episode.SERIES_ID)){
				
					WatchedHelper wHelper = new WatchedHelper(getApplicationContext());
					
					if(isWatched){
						
						//Set it to "not-watched"
						watched.setIcon(R.drawable.btn_check_off);
						
						//Remove from the Watched database
						wHelper.removeWatched(episode);
						
						//Remove the episode from the Queue DB and check if there is an earlier episode and add to Queue DB
						if(dbAdapter.isInQueue(episode.ID)){
							//Episode removed was in the Queue DB
							dbAdapter.deleteQueueEpisode(episode.ID);
							
							//Check Previous Watched Episodes and add the latest one, if any
							for(int j = full_episode_list.size()-1; j >= 0; j--){
								//Start at POSITION and work back
								if(dbAdapter.isEpisodeWatched(full_episode_list.get(j).ID)){
									dbAdapter.insertQueue(full_episode_list.get(j));
									break;
								}
							}
						}
						
						//Set Boolean Flag as False
						isWatched = false;
						
						
					}else{
						
						//Set it to "watched"
						watched.setIcon(R.drawable.btn_check_on);
						
						//Add from the Watched database
						wHelper.markWatched(episode.ID);
						
						//Set Boolean Flag as True
						isWatched = true;
					}

					
				}
				
				dbAdapter.close();
				
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
			break;
		
		case android.R.id.home:
			//Start the Season Activity
			Intent i = new Intent(SeasonEpisodeActivity.this, SeasonActivity.class);
			i.putExtra(SeasonActivity.SERIES, series);
			i.putExtra(SeasonActivity.SEASON_NUMBER, episode.SEASON_NUMBER + "");
			i.putParcelableArrayListExtra(SeasonActivity.EPISODE_LIST, full_episode_list);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
			
		case R.id.menu_icon_share:
			
			Log.d("SEASON_EPISODE",  "onOptionsItemSelected");
			break;
		}
		
		return true;
	}

	private void findViews() {
		overview = (TextView) findViewById(R.id.episode_overview);
		image = (ImageView) findViewById(R.id.episode_image);
		guestStars = (TextView) findViewById(R.id.episode_guest_stars);
		ratingBar = (RatingBar) findViewById(R.id.episode_ratingbar);
		ratingText = (TextView) findViewById(R.id.episode_rating_text);
		seasonNumber = (TextView) findViewById(R.id.episode_season_number);
		airdate = (TextView) findViewById(R.id.episode_airdate);
		
		imageLoader = ImageLoader.getInstance();
		
		seasonEpisodeBarTitle = (TextView) findViewById(R.id.season_episode_bar_title);
		episodeOverviewGroupTitle = (TextView) findViewById(R.id.episode_overview_group_title);
		episodeGuestStarsGroupTitle = (TextView) findViewById(R.id.episode_guest_stars_group_title);
		
		//Find Drop Down Image Views
		overviewDropDown = (ImageView) findViewById(R.id.episode_overview_img_button);
		guestDropDown = (ImageView) findViewById(R.id.episode_guest_img_button);
		
		//Find Drop Down Images Containers
		overviewDropDownContainer = (LinearLayout) findViewById(R.id.episode_overview_img_button_layout);
		guestDropDownContainer = (LinearLayout) findViewById(R.id.episode_guest_img_button_layout);
		
	}

	private void setContent() {
		//Fetch info from database
		//episode = dbAdapter.fetchEpisode(episode.ID);
		
		//Set Overview & Collapse Tag
		overview.setText(episode.OVERVIEW != null ? episode.OVERVIEW.replace("\n\n", "\n").replace("  ", " ").replace("\"\"", "\"") : "No Overview Available");
		overview.setTag(COLLAPSED);
		
		
		/** Image Stuff */
		DisplayImageOptions optionsWithDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub_land)
			.showImageForEmptyUri(R.drawable.stub_land_not_found)
			.showImageOnFail(R.drawable.stub_land_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(new FadeInBitmapDisplayer(1000))
			.build();
			
		String imgUri = ServerUrls.getImageUrlOriginal(this, ServerUrls.fixURL(episode.IMAGE_URL));
		imageLoader.displayImage(imgUri, image, optionsWithDelay);
		
		ratingBar.setRating(episode.RATING);
		ratingText.setText("(" + episode.RATING + "/10)");
		
		//Set Guest Stars & Collapse Tag
		guestStars.setText(episode.GUEST_STARS != null ? episode.GUEST_STARS : "No Guest Stars");
		guestStars.setTag(COLLAPSED);
		
		int sn = episode.SEASON_NUMBER == -1 ? 0 : episode.SEASON_NUMBER;
		String s = sn < 10 ? "0"+sn : sn + "";
		
		String e = episode.EPISODE_NUMBER < 10 ? "0" + episode.EPISODE_NUMBER : episode.EPISODE_NUMBER + "";
		seasonNumber.setText("S" + s + "E" + e);
		
		airdate.setText(dateFormat(episode.FIRST_AIRED));
		
		//Bar Titles & StyleFace
		seasonEpisodeBarTitle.setText(episode.TITLE);
		
		//Set DropDown On ClickListeners
		overview.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if( (Integer)overview.getTag() == COLLAPSED){
					overview.setMaxLines(Integer.MAX_VALUE);
					overview.setEllipsize(null);
					overview.setTag(EXPANDED);
					overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					overview.setMaxLines(3);
					overview.setEllipsize(TruncateAt.END);
					overview.setTag(COLLAPSED);
					overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
				}
			}
			
		});
		guestStars.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if( (Integer)guestStars.getTag() == COLLAPSED){
					guestStars.setMaxLines(Integer.MAX_VALUE);
					guestStars.setEllipsize(null);
					guestStars.setTag(EXPANDED);
					guestDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					guestStars.setMaxLines(3);
					guestStars.setEllipsize(TruncateAt.END);
					guestStars.setTag(COLLAPSED);
					guestDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
				}
			}
			
		});
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private String dateFormat(String first_Aired) {
		if(first_Aired != null){
			SimpleDateFormat s = new SimpleDateFormat("MMMM dd, yyyy");
			SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd");

			try {
				return s.format(ss.parse(first_Aired));
			} catch (ParseException e) {
				e.printStackTrace();
				return first_Aired;
			}
		}else{
			return first_Aired;
		}
		
	}
	
    private boolean isWatched(String id){
    	
    	dbAdapter.open();
    	boolean isWatched = dbAdapter.isEpisodeWatched(id);
    	dbAdapter.close();
    	
    	return isWatched;
    }
}
