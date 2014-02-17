package voodoo.tvdb.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.FavoriteBundle;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.XMLHandlers.XmlHandlerFetchAllSeriesInfo;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

@SuppressLint("SimpleDateFormat")
public class SeriesInfoActivity extends BaseActivity {
	private static final String TAG = "SeriesInfo";

    public static final String ID = "id";

	private static final int COLLAPSED = 0;
	private static final int EXPANDED = 1;

    String title = "";
	
	private Series series;
	private String seriesID;
	private ArrayList<Integer> seasons;
	private ArrayList<Episode> episodeList;
	
	private ImageLoader imageLoader;
	private ViewHolder holder;
	
	LinearLayout seasonsList;
	TextView empty;
	LayoutInflater inflater;
	
	private DatabaseAdapter dbAdapter;
	
	MenuItem favorite;
	Boolean isFavorited = false;

	public static class ViewHolder{
		public ImageView seriesPoster;
		public LinearLayout seriesPosterContainer;
		public TextView seriesInfoBarTitle;
		public TextView seriesTime;
		public TextView seriesDay;
		public TextView seriesStatus;
		public TextView seriesNetwork;
		public TextView seriesRuntime;
		public TextView seriesContentRating;
		public RatingBar seriesRatingBar;
		public TextView seriesRatingText;
		public TextView seriesGenre;
		//public ImageView seriesIMDB;
		public TextView seriesActor;
		public TextView seriesOverview;
		public TextView seriesFirstAired;
		public TextView seriesNextEpisodeAirs;
		
		public TextView seriesOverviewGroupTitle;
		public TextView seriesActorGroupTitle;
		public TextView seriesGenreGroupTitle;
		public TextView seriesFirstAiredOnGroupTitle;
		public TextView seriesImdbGroupTitle;
		public TextView seriesSeasonsGroupTitle;
		
		//Drop Down Image and Container
		public ImageView overviewDropDown;
		public ImageView actorsDropDown;
		public LinearLayout overviewDropDownContainer;
		public LinearLayout actorsDropDownContainer;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.series_info);
		
		imageLoader = ImageLoader.getInstance();
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		dbAdapter = new DatabaseAdapter(this);
		findView();
		
		Bundle extras = getIntent().getExtras();
		seriesID = extras != null ? extras.getString(ID) : null;
		
		//ActionBar
        setActionBarTitle(title);

		// Ads
    	AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
		
	}
	@Override
	public void onResume(){
		super.onResume();
		
		dbAdapter.open();
		
		if(series == null || episodeList == null){
			if(seriesID != null){
				new fetchSeriesInfoAsync(SeriesInfoActivity.this).execute(seriesID);
			}
		}
		
		if(series != null){
			//Update the Favorite Star by clearing all the Actions and creating a new Action bar
			isFavorited = isSeriesFavorited(series.ID);
			invalidateOptionsMenu();
		}
	}
	@Override
	public void onDestroy(){
        //Log.d(TAG, "onDestroy");
        dbAdapter.close();
		super.onDestroy();
	}
	@Override
	public void onPause(){
		super.onPause();
		dbAdapter.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

		String text = series == null ? "http://voodootvdb.com" : "Watching " + series.TITLE + "! http://voodootvdb.com";
		
		//ActionBar
		getMenuInflater().inflate(R.menu.share_menu_item, menu);

		MenuItem share = menu.findItem(R.id.menu_icon_share);
		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
		//provider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, text);
		provider.setShareIntent(i);
		
		favorite = menu.add(getResources().getString(R.string.favorites)).setIcon( isFavorited ? R.drawable.rate_star_med_on_holo_light : R.drawable.rate_star_med_off_holo_light);
        MenuItemCompat.setShowAsAction(favorite, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		favorite.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
                FavoriteHelper faveHelper = new FavoriteHelper(SeriesInfoActivity.this);
                faveHelper.createFavoriteAlert(series, faveHelper.isSeriesFavorited(seriesID), new FavoriteSavingListener(){
                    @Override
                    public void onSavingCompleted(String series_id) {
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
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return false;
	}

	private class fetchSeriesInfoAsync extends AsyncTask<String, Void, FavoriteBundle>{

		private Context context;
    	private ProgressDialog dialog;
    	private FavoriteBundle fb;
    	
    	private AsyncTask<String, Void, FavoriteBundle> myFetSerInfoAsync;
    	
    	//SAXParsers
    	private SAXParserFactory mySAXParserFactory;
    	private SAXParser mySAXParser;
    	private XMLReader mXMLReader;
    	private XmlHandlerFetchAllSeriesInfo xmlHandler;
    	
    	private URL url;
    	
    	//Constructor
    	public fetchSeriesInfoAsync(Activity activity){
    		context = activity;
    		dialog = new ProgressDialog(context);
    		fb = new FavoriteBundle();
    	}
    	
    	@Override
    	protected void onPreExecute(){
    		dialog.setMessage("Loading. Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					myFetSerInfoAsync.cancel(true);
				}
			});
			dialog.show();
			myFetSerInfoAsync = this;
    	}
		
		@Override
		protected FavoriteBundle doInBackground(String... params) {
			for(String id : params){
				
				//Check Series is on Database Already
				Series seriesCheck = dbAdapter.fetchSeries(id);
				
				if(seriesCheck != null){
					//Series is in the database
					fb.SERIES = seriesCheck;
					fb.EPISODES = dbAdapter.fetchAllEpisodes(id);

					//Get list of Season Numbers
					if(fb.EPISODES != null){
						fb.SEASONS = new ArrayList<Integer>();
						int seasonNumber = -100;
						for(int i = 0; i < fb.EPISODES.size(); i++){
							if(seasonNumber != fb.EPISODES.get(i).SEASON_NUMBER){
								seasonNumber = fb.EPISODES.get(i).SEASON_NUMBER;
								fb.SEASONS.add(seasonNumber);
							}
						}
					}else{
						fb.SEASONS = null;
					}
					return fb;
				}else{
					//Series is NOT in Database
					try{
						
						//URL
	        			url = new URL(ServerUrls.getAllSeriesUrl(context, id));
						
						mySAXParserFactory = SAXParserFactory.newInstance();
						mySAXParser = mySAXParserFactory.newSAXParser();
						mXMLReader = mySAXParser.getXMLReader();
						xmlHandler = new XmlHandlerFetchAllSeriesInfo(SeriesInfoActivity.this);
						mXMLReader.setContentHandler(xmlHandler);
				        
			        	mXMLReader.parse(new InputSource(url.openStream()));

						//Get the series info and episodes info 
			        	fb.SERIES = xmlHandler.getSeries();
			        	fb.EPISODES = xmlHandler.getEpisodesList();
			        	fb.SEASONS = xmlHandler.getSeasons();
			        	
			        	return fb;
						
					}catch (MalformedURLException e) {
						//Log.d(TAG, "MalformedURLException");
						e.printStackTrace();
						return null;
					} catch (ParserConfigurationException e) {
						//Log.d(TAG, "ParserConfigurationException");
						e.printStackTrace();
						return null;
					} catch (SAXException e) {
						//Log.d(TAG, "SAXException");
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						//Log.d(TAG, "IOException");
						e.printStackTrace();
						return null;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(FavoriteBundle fb){
			dialog.dismiss();
			
			if(fb != null){
				series = fb.SERIES;
				seasons = fb.SEASONS;
				episodeList = fb.EPISODES;
			}else{
				series = null;
				seasons = null;
				episodeList = null;
			}
			
			if(series != null && seasons != null){
				setContent();
			}
			
			invalidateOptionsMenu();
		}
	}
	
	private void findView() {
		//Find Views
		holder = new ViewHolder();
		holder.seriesInfoBarTitle = (TextView) findViewById(R.id.series_info_bar_title);
		holder.seriesPoster = (ImageView) findViewById(R.id.series_poster);
		//holder.seriesPosterContainer = (LinearLayout) findViewById(R.id.series_poster_container);
		holder.seriesTime = (TextView) findViewById(R.id.series_time);
		holder.seriesDay = (TextView) findViewById(R.id.series_day);
		holder.seriesStatus = (TextView) findViewById(R.id.series_status);
		holder.seriesNetwork = (TextView) findViewById(R.id.series_network);
		holder.seriesRuntime = (TextView) findViewById(R.id.series_runtime);
		holder.seriesContentRating = (TextView) findViewById(R.id.series_content_rating);
		holder.seriesRatingBar = (RatingBar) findViewById(R.id.series_ratingBar);
		holder.seriesRatingText = (TextView) findViewById(R.id.series_ratingText);
		holder.seriesGenre = (TextView) findViewById(R.id.series_genre);
		//holder.seriesIMDB = (ImageView) findViewById(R.id.series_imdb);
		holder.seriesActor = (TextView) findViewById(R.id.series_actors);
		holder.seriesOverview = (TextView) findViewById(R.id.series_overview);
		holder.seriesFirstAired = (TextView) findViewById(R.id.series_first_aired);
		holder.seriesNextEpisodeAirs = (TextView) findViewById(R.id.series_next_episode_airs);
		
		seasonsList = (LinearLayout) findViewById(R.id.seasons_list);
		
		//Find Group Titles
		holder.seriesOverviewGroupTitle = (TextView) findViewById(R.id.series_overview_group_title);
		holder.seriesActorGroupTitle = (TextView) findViewById(R.id.series_actors_group_title);
		holder.seriesGenreGroupTitle = (TextView) findViewById(R.id.series_genre_group_title);
		holder.seriesFirstAiredOnGroupTitle = (TextView) findViewById(R.id.series_first_aired_on_group_title);
		//holder.seriesImdbGroupTitle = (TextView) findViewById(R.id.series_imdb_group_title);
		holder.seriesSeasonsGroupTitle = (TextView) findViewById(R.id.series_seasons_group_title);
		
		//Find Drop Down Image Views
		holder.overviewDropDown = (ImageView) findViewById(R.id.series_overview_img_button);
		holder.actorsDropDown = (ImageView) findViewById(R.id.series_actors_img_button);
		
		//Find Drop Down Images Containers
		holder.overviewDropDownContainer = (LinearLayout) findViewById(R.id.series_overview_img_button_layout);
		holder.actorsDropDownContainer = (LinearLayout) findViewById(R.id.series_actors_img_button_layout);
		
	}
	private void setContent() {

        /** Set Title */
		title = series.TITLE;
        setActionBarTitle(title);
		
		//Set the star status
		isFavorited = isSeriesFavorited(series.ID);
		supportInvalidateOptionsMenu();
		
		/** Image Stuff */
		DisplayImageOptions optionsWithDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub)
			.showImageForEmptyUri(R.drawable.stub_not_found)
			.showImageOnFail(R.drawable.stub_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(new FadeInBitmapDisplayer(1000))
			.build();
			
		String imgUri = ServerUrls.getImageUrl(this, ServerUrls.fixURL(series.POSTER_URL));
		imageLoader.displayImage(imgUri, holder.seriesPoster, optionsWithDelay);
		
		/**
		holder.seriesPosterContainer.setTag(series.POSTER_URL != null ? series.POSTER_URL : null);
		holder.seriesPosterContainer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				String url = ServerUrls.getImageUrlOriginal(getApplicationContext(), (String) v.getTag().toString());
				Toast.makeText(SeriesInfo.this, "clicked on the image: " + url, Toast.LENGTH_SHORT).show();
				
				Intent i = new Intent(SeriesInfo.this, ImageViewDisplay.class);
				i.putExtra("URL",  url);
				startActivity(i);
				
			}
			
		});	*/
		
		holder.seriesTime.setText(series.AIRS_TIME);
		holder.seriesDay.setText(series.AIRS_DAYOFWEEK);
		holder.seriesStatus.setText(series.STATUS != null ? series.STATUS : "No Status Available");
		holder.seriesNetwork.setText(series.NETWORK != null ? series.NETWORK + " -" : "");
		holder.seriesRuntime.setText(series.RUNTIME + " min.");
		holder.seriesContentRating.setText(series.CONTENT_RATING);
		holder.seriesRatingBar.setRating(series.RATING);
		holder.seriesRatingText.setText("(" + series.RATING + "/10)");
		
		//Set Actors & Collapse Tag
		holder.seriesActor.setText(getActors(series.ACTORS));
		holder.seriesActor.setTag(COLLAPSED);
		
		//Set Overview & Collapse Tag
		holder.seriesOverview.setText(series.OVERVIEW != null ? series.OVERVIEW.replace("\n\n", "\n").replace("  ", " ") : "No Overview Available");
		holder.seriesOverview.setTag(COLLAPSED);
		
		holder.seriesFirstAired.setText(dateFormat(series.FIRST_AIRED));
		
		holder.seriesGenre.setText(series.GENRE != null ? 
				(!series.GENRE.equals("||") ? series.GENRE.substring(1, series.GENRE.length()-1).replace("|", ", ") : "No Genre Available") :
				"No Genre Available");
		
		/** 
		 * Removing IMDB Link 
		if(series.IMDB_ID != null){
			holder.seriesIMDB.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.imdb));
			holder.seriesIMDB.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("http://www.imdb.com/title/" + series.IMDB_ID + "/"));
					startActivity(i);
				}
			});
		}
		*/
		
		String nextEp = getNextEpisodeToAir();
		holder.seriesNextEpisodeAirs.setText(nextEp != null ? "Next: " + dateFormat(nextEp) : "No Upcoming");
		
		//Set Drop Down Image View Click Listeners
		holder.seriesOverview.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				
				if( (Integer)holder.seriesOverview.getTag() == COLLAPSED){
					holder.seriesOverview.setMaxLines(Integer.MAX_VALUE);
					holder.seriesOverview.setEllipsize(null);
					holder.seriesOverview.setTag(EXPANDED);
					holder.overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					holder.seriesOverview.setMaxLines(3);
					holder.seriesOverview.setEllipsize(TruncateAt.END);
					holder.seriesOverview.setTag(COLLAPSED);
					holder.overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
				}
			}
			
		});
		holder.seriesActor.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if( (Integer)holder.seriesActor.getTag() == COLLAPSED){
					holder.seriesActor.setMaxLines(Integer.MAX_VALUE);
					holder.seriesActor.setEllipsize(null);
					holder.seriesActor.setTag(EXPANDED);
					holder.actorsDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					holder.seriesActor.setMaxLines(3);
					holder.seriesActor.setEllipsize(TruncateAt.END);
					holder.seriesActor.setTag(COLLAPSED);
					holder.actorsDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
				}
			}
			
		});
		
		//For loop to add the seasons ListView...
		setSeasonsListViewItems();
		
	}
	private CharSequence getActors(String actors) {

		if(actors == null){
			return "No Actors Available";
		}		
		
		return ((!actors.equals("||")) ? actors.substring(1, actors.length()-1).replace("|", ", ") : "No Actors Available");
		
	}
	private void setSeasonsListViewItems() {
		if(seasons != null && seasons.size() > 0){
			for(int i = seasons.size() - 1; i >= 0; i--){
				View vi = inflater.inflate(R.layout.season_item, null);
				TextView seasonNumber = (TextView) vi.findViewById(R.id.seasonNumber);

				if(seasons.get(i) == 0){
					seasonNumber.setText("Movies, Pilots, and Extras");
				}else{
					seasonNumber.setText("Season " + seasons.get(i));
				}
				
				seasonNumber.setTag(seasons.get(i));
				vi.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						String seasonNumber = v.findViewById(R.id.seasonNumber).getTag().toString();
						//Toast.makeText(SeriesInfo.this, season , Toast.LENGTH_SHORT).show();
						
						Intent i = new Intent(SeriesInfoActivity.this, SeasonActivity.class);
						
						i.putExtra(SeasonActivity.SERIES, series);
						i.putExtra(SeasonActivity.SEASON_NUMBER, seasonNumber);
						i.putParcelableArrayListExtra(SeasonActivity.EPISODE_LIST, episodeList);
						
						startActivity(i);
					}
				});
				seasonsList.addView(vi);
			}
		}else{
			View vi = inflater.inflate(R.layout.season_item, null);
			TextView seasonNumber = (TextView) vi.findViewById(R.id.seasonNumber);
			
			seasonNumber.setText("No Seasons Available");
			seasonsList.addView(vi);
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getNextEpisodeToAir(){
		Calendar currentTime = Calendar.getInstance();
		//Log.d(TAG, "Current Time = " + currentTime.get(Calendar.MONTH) + "-" + currentTime.get(Calendar.DAY_OF_MONTH) + "-" + currentTime.get(Calendar.YEAR));
		String nextEpisodeLatest = null;
		String nextEpisodeZero = null;
		
		if(episodeList != null){
			//Search the latest Episodes first
			for(int i = episodeList.size()-1; i >= 0; i--){
				if(episodeList.get(i).FIRST_AIRED != null && episodeList.get(i).SEASON_NUMBER != 0){
					
					SimpleDateFormat sdf;
					if(series.AIRS_TIME != null && series.AIRS_TIME.length() == 8){
						sdf = new SimpleDateFormat("KK:mmayyyy-MM-dd");
					}else{
						sdf = new SimpleDateFormat("yyyy-MM-dd");
					}
					
					Calendar nextEpisodeTime = Calendar.getInstance();
					try {
						String d = (series.AIRS_TIME != null && series.AIRS_TIME.length() == 8)? series.AIRS_TIME + episodeList.get(i).FIRST_AIRED : episodeList.get(i).FIRST_AIRED;
						nextEpisodeTime.setTime(sdf.parse(d.replace(" ", "")));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					//Log.d(TAG, "Compared to NextEpisodeTime in Latest Season:" + nextEpisodeTime.get(Calendar.MONTH) + "-" + nextEpisodeTime.get(Calendar.DAY_OF_MONTH) + "-" + nextEpisodeTime.get(Calendar.YEAR));
					
					if(currentTime.before(nextEpisodeTime)){
						nextEpisodeLatest = episodeList.get(i).FIRST_AIRED;
					}else if(currentTime.after(nextEpisodeTime)){
						break;
					}
				}
			}
			//Log.d(TAG, "NextEpisodeLatest = " + nextEpisodeLatest);
			
			//Then Search for the Movies and extras in Season 0
			//Count how many episodes are in Season 0
			int s0 = 0;
			for(int i = 0; i < episodeList.size(); i++){
				if(episodeList.get(i).SEASON_NUMBER == 0){
					s0++;
				}else if(episodeList.get(i).SEASON_NUMBER > 0){
					break;
				}
			}
			for(int i = s0 - 1; i >= 0; i--){		
				SimpleDateFormat sdf;
				if(series.AIRS_TIME != null && series.AIRS_TIME.length() == 8){
					sdf = new SimpleDateFormat("KK:mmaayyyy-MM-dd");
				}else{
					sdf = new SimpleDateFormat("yyyy-MM-dd");
				}
				
				Calendar nextEpisodeTime = Calendar.getInstance();
				try {
					String d = (series.AIRS_TIME != null && series.AIRS_TIME.length() == 8)? series.AIRS_TIME + episodeList.get(i).FIRST_AIRED : episodeList.get(i).FIRST_AIRED;
					if(d != null){
						nextEpisodeTime.setTime(sdf.parse(d.replace(" ", "")));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				//Log.d(TAG, "Compared to NextEpisodeTime in Season 0:" + nextEpisodeTime.get(Calendar.MONTH) + "-" + nextEpisodeTime.get(Calendar.DAY_OF_MONTH) + "-" + nextEpisodeTime.get(Calendar.YEAR));
				
				if(currentTime.before(nextEpisodeTime)){
					nextEpisodeZero = episodeList.get(i).FIRST_AIRED;
				}else if(currentTime.after(nextEpisodeTime)){
					break;
				}
			}
		}
		//Log.d(TAG, "NextEpisodeZero = " + nextEpisodeZero);
		
		//Select the earliest show between the latest and zero season episodes IF not null
		if(nextEpisodeZero != null && nextEpisodeLatest == null){
			return nextEpisodeZero;
		}else if(nextEpisodeZero == null && nextEpisodeLatest != null){
			return nextEpisodeLatest;
		}else if(nextEpisodeZero != null && nextEpisodeLatest != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar nez = Calendar.getInstance();
			Calendar nel = Calendar.getInstance();
			try {
				nez.setTime(sdf.parse(nextEpisodeZero));
				nel.setTime(sdf.parse(nextEpisodeLatest));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if(nez.before(nel)){
				return nextEpisodeZero;
			}else{
				return nextEpisodeLatest;
			}
		}
		return null;
	}
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
	private boolean isSeriesFavorited(String iD) {
		Series series = dbAdapter.fetchSeries(iD);
		if(series != null){
			return true;
		}else{
			return false;
		}
	}
}

















