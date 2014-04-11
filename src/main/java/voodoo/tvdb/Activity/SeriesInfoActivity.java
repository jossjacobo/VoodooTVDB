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
import android.volley.RequestQueue;
import android.volley.Response;
import android.volley.VolleyError;
import android.volley.toolbox.StringRequest;
import android.volley.toolbox.Volley;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.ads.AdView;
import com.google.gson.Gson;
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
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import voodoo.tvdb.objects.Episode;
import voodoo.tvdb.objects.FavoriteBundle;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.xmlHandlers.XmlHandlerFetchAllSeriesInfo;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

@SuppressLint("SimpleDateFormat")
public class SeriesInfoActivity extends BaseActivity {

    public static final String ID = "id";

	private static final int COLLAPSED = 0;
	private static final int EXPANDED = 1;

    private String title = "";
	
	private Series series;
	private String seriesID;
	private ArrayList<Integer> seasons;
	private ArrayList<Episode> episodeList;
	
	private ImageLoader imageLoader;
	

	private DatabaseAdapter dbAdapter;

	private MenuItem favorite;
	private Boolean isFavorited = false;

	private LayoutInflater inflater;
    private ImageView seriesPoster;
    private TextView seriesTime;
    private TextView seriesDay;
    private TextView seriesStatus;
    private TextView seriesNetwork;
    private TextView seriesRuntime;
    private TextView seriesContentRating;
    private RatingBar seriesRatingBar;
    private TextView seriesRatingText;
    private TextView seriesGenre;
    private TextView seriesActor;
    private TextView seriesOverview;
    private TextView seriesFirstAired;
    private TextView seriesNextEpisodeAirs;
	private LinearLayout seasonsList;
    private LinearLayout loading;

    //Drop Down Image and Container
    private ImageView overviewDropDown;
    private ImageView actorsDropDown;

    private Gson gson;
    private RequestQueue volley;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.series_info);

        gson = new Gson();
        volley = Volley.newRequestQueue(this);
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
		if(series == null || episodeList == null){
			if(seriesID != null){
                handleId(seriesID);
			}
		}
		
		if(series != null){
			//Update the Favorite Star by clearing all the Actions and creating a new Action bar
			isFavorited = isSeriesFavorited(series.ID);
            supportInvalidateOptionsMenu();
		}
	}

    private void handleId(String seriesID) {
        dbAdapter.open();

        //Check Series is on Database Already
        series = dbAdapter.fetchSeries(seriesID);

        if(series != null){

            //Series is in the database

            episodeList = dbAdapter.fetchAllEpisodes(seriesID);

            //Get list of Season Numbers
            if(episodeList != null){
                seasons = getSeasons(episodeList);
            }else{
                seasons = null;
            }
            handleContent();
        }else {
            //Series is NOT in Database
            String url = ServerUrls.getAllSeriesUrl(this, seriesID);
            volley.add(new StringRequest(
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response != null && !response.equals("[]")){
                                series = gson.fromJson(response, Series.class);

                            }else{

                            }
                        }
                    },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showContent();
                    list.removeFooterView(loadingView);
                    list.setOnScrollListener(null);
                    fetching = true;
                }
            })).setTag(this);
        }

        dbAdapter.close();
    }

    private ArrayList<Integer> getSeasons(ArrayList<Episode> episodeList) {
        ArrayList<Integer> s = new ArrayList<Integer>();
        int seasonNumber = -100;
        for(Episode episode : episodeList){
            if(seasonNumber != episode.SEASON_NUMBER){
                seasonNumber = episode.SEASON_NUMBER;
                s.add(seasonNumber);
            }
        }
        return s;
    }

    private void handleContent(){
        loading.setVisibility(View.GONE);
        if(series != null && seasons != null){
            setContent();
        }
        supportInvalidateOptionsMenu();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

		String text = series == null ? "http://voodootvdb.com" : "Watching " + series.TITLE + "! http://voodootvdb.com";

		getMenuInflater().inflate(R.menu.menu_item_share, menu);
		MenuItem share = menu.findItem(R.id.menu_icon_share);
		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, text);
        i.putExtra(Intent.EXTRA_TEXT, "voodoo");
		provider.setShareIntent(i);

        getMenuInflater().inflate(R.menu.menu_item_favorite, menu);
		favorite = menu.findItem(R.id.menu_item_favorite);
        favorite.setIcon( isFavorited ? R.drawable.rate_star_med_on_holo_light : R.drawable.rate_star_med_off_holo_light);
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
			
            supportInvalidateOptionsMenu();
		}
	}
	
	private void findView() {
		//Find Views
		seriesPoster = (ImageView) findViewById(R.id.series_poster);
		seriesTime = (TextView) findViewById(R.id.series_time);
		seriesDay = (TextView) findViewById(R.id.series_day);
		seriesStatus = (TextView) findViewById(R.id.series_status);
		seriesNetwork = (TextView) findViewById(R.id.series_network);
		seriesRuntime = (TextView) findViewById(R.id.series_runtime);
		seriesContentRating = (TextView) findViewById(R.id.series_content_rating);
		seriesRatingBar = (RatingBar) findViewById(R.id.series_ratingBar);
		seriesRatingText = (TextView) findViewById(R.id.series_ratingText);
		seriesGenre = (TextView) findViewById(R.id.series_genre);
		seriesActor = (TextView) findViewById(R.id.series_actors);
		seriesOverview = (TextView) findViewById(R.id.series_overview);
		seriesFirstAired = (TextView) findViewById(R.id.series_first_aired);
		seriesNextEpisodeAirs = (TextView) findViewById(R.id.series_next_episode_airs);
		
		seasonsList = (LinearLayout) findViewById(R.id.seasons_list);
		
		//Find Drop Down Image Views
		overviewDropDown = (ImageView) findViewById(R.id.series_overview_img_button);
		actorsDropDown = (ImageView) findViewById(R.id.series_actors_img_button);

        loading = (LinearLayout) findViewById(R.id.series_loading);
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
		imageLoader.displayImage(imgUri, seriesPoster, optionsWithDelay);
		
		/**
		seriesPosterContainer.setTag(series.POSTER_URL != null ? series.POSTER_URL : null);
		seriesPosterContainer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				String url = ServerUrls.getImageUrlOriginal(getApplicationContext(), (String) v.getTag().toString());
				Toast.makeText(SeriesInfo.this, "clicked on the image: " + url, Toast.LENGTH_SHORT).show();
				
				Intent i = new Intent(SeriesInfo.this, ImageViewDisplay.class);
				i.putExtra("URL",  url);
				startActivity(i);
				
			}
			
		});	*/
		
		seriesTime.setText(series.AIRS_TIME);
		seriesDay.setText(series.AIRS_DAYOFWEEK);
		seriesStatus.setText(series.STATUS != null ? series.STATUS : "No Status Available");
		seriesNetwork.setText(series.NETWORK != null ? series.NETWORK + " -" : "");
		seriesRuntime.setText(series.RUNTIME + " min.");
		seriesContentRating.setText(series.CONTENT_RATING);
		seriesRatingBar.setRating(series.RATING);
		seriesRatingText.setText("(" + series.RATING + "/10)");
		
		//Set Actors & Collapse Tag
		seriesActor.setText(getActors(series.ACTORS));
		seriesActor.setTag(COLLAPSED);
		
		//Set Overview & Collapse Tag
		seriesOverview.setText(series.OVERVIEW != null ? series.OVERVIEW.replace("\n\n", "\n").replace("  ", " ") : "No Overview Available");
		seriesOverview.setTag(COLLAPSED);
		
		seriesFirstAired.setText(dateFormat(series.FIRST_AIRED));
		
		seriesGenre.setText(series.GENRE != null ? 
				(!series.GENRE.equals("||") ? series.GENRE.substring(1, series.GENRE.length()-1).replace("|", ", ") : "No Genre Available") :
				"No Genre Available");
		
		String nextEp = getNextEpisodeToAir();
		seriesNextEpisodeAirs.setText(nextEp != null ? "Next: " + dateFormat(nextEp) : "No Upcoming");
		
		//Set Drop Down Image View Click Listeners
		seriesOverview.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				
				if( (Integer)seriesOverview.getTag() == COLLAPSED){
					seriesOverview.setMaxLines(Integer.MAX_VALUE);
					seriesOverview.setEllipsize(null);
					seriesOverview.setTag(EXPANDED);
					overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					seriesOverview.setMaxLines(3);
					seriesOverview.setEllipsize(TruncateAt.END);
					seriesOverview.setTag(COLLAPSED);
					overviewDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
				}
			}
			
		});
		seriesActor.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if( (Integer)seriesActor.getTag() == COLLAPSED){
					seriesActor.setMaxLines(Integer.MAX_VALUE);
					seriesActor.setEllipsize(null);
					seriesActor.setTag(EXPANDED);
					actorsDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light));
				}else{
					seriesActor.setMaxLines(3);
					seriesActor.setEllipsize(TruncateAt.END);
					seriesActor.setTag(COLLAPSED);
					actorsDropDown.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light));
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

















