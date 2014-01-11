package voodoo.tvdb.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

import voodoo.tvdb.Activity.SeasonEpisodeActivity;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.Utils.ServerUrls;
import voodoo.tvdb.Utils.WatchedHelper;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

@SuppressLint("SimpleDateFormat")
public class SeasonAdapter extends BaseAdapter implements OnClickListener, OnCreateContextMenuListener{
	//private static final String TAG = "SeasonAdapter";
	
	private static final int TYPE_NOT_WATCHED = 0;
	private static final int TYPE_WATCHED = 1;
	
	private static Activity activity;
	private static LayoutInflater inflater = null;
	
	public ImageLoader imageLoader;
	private DisplayImageOptions optionsWithDelay;
	private DisplayImageOptions optionsWithoutDelay;
	
	private DatabaseAdapter dbAdapter;

	@SuppressWarnings("rawtypes")
	private TreeSet watched;
	private ArrayList<Episode> episodes;
	private ArrayList<Episode> full_episode_list;
	private Series series;

	private String SERIES_ID;
	
	@SuppressWarnings("rawtypes")
	public SeasonAdapter(Context context){
		activity = (Activity) context;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = ImageLoader.getInstance();
		dbAdapter = new DatabaseAdapter(context);
		
		episodes = new ArrayList<Episode>();
		watched = new TreeSet();
		
		optionsWithDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub_land)
			.showImageForEmptyUri(R.drawable.stub_land_not_found)
			.showImageOnFail(R.drawable.stub_land_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(new FadeInBitmapDisplayer(1000))
			.build();

		optionsWithoutDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub_land)
			.showImageForEmptyUri(R.drawable.stub_land_not_found)
			.showImageOnFail(R.drawable.stub_land_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.build();

		
    }
	
	public void addItem(final Episode item){
    	episodes.add(item);
    	notifyDataSetChanged();
    }
	
	public void setFullEpisodeList(final ArrayList<Episode> fel){
		this.full_episode_list = fel;
	}
	
	public void setSeries(final Series s){
		this.series = s;
	}

	public void getWatched() {
		//Open the database and fetch all the watched episodes with that Series ID
		dbAdapter.open();
		this.watched = dbAdapter.fetchWatchedBySeriesId(SERIES_ID);
		dbAdapter.close();
	}
	
    public int getItemWatchedType(String ID){
    	return watched.contains(ID) ? TYPE_WATCHED : TYPE_NOT_WATCHED;
    }
	
	@Override
	public int getCount() {
		if(episodes != null){
				return episodes.size();
		}else{
			return 0;
		}
	}
	
	@Override
    public Object getItem(int position) {
        return position;
    }
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@SuppressWarnings("rawtypes")
	public void setItems(ArrayList<Episode> episodes) {
		if(episodes != null){
			//if episodes are not null get the Series Id from the first episode on the list
			this.SERIES_ID = episodes.get(0).SERIES_ID;
			
			//get watched from DB
			getWatched();
			
			//Add every episode to the list
			for(int i = 0; i < episodes.size(); i++){
				this.episodes.add(episodes.get(i));
			}
		}else{
			this.SERIES_ID = null;
			this.watched = new TreeSet();
			this.episodes = null;
		}
	}
	
	public static class ViewHolder{
		public RelativeLayout itemRow;
		public TextView title;
		public ImageView image;
		public TextView episodeSeasonNumber;
		public TextView firstAired;
		public ImageView clock;
		public CheckBox watchedBtn;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		dbAdapter.open();
		View vi = convertView;
		ViewHolder holder;
		
		Episode episode = episodes.get(position);
		int type = getItemWatchedType(episode.ID);
		
		if(convertView == null){
			
			holder = new ViewHolder();
			
			vi = inflater.inflate(R.layout.season_episode_item, null);
			
			holder.itemRow = (RelativeLayout) vi.findViewById(R.id.episode_row);
			holder.title = (TextView) vi.findViewById(R.id.episode_title);
			holder.image = (ImageView) vi.findViewById(R.id.episode_image);
			holder.episodeSeasonNumber = (TextView) vi.findViewById(R.id.episode_season_number);
			holder.firstAired = (TextView) vi.findViewById(R.id.episode_airdate);
			holder.clock = (ImageView) vi.findViewById(R.id.episode_clock);
			
			holder.watchedBtn = (CheckBox) vi.findViewById(R.id.season_episode_checkbox);
			holder.watchedBtn.setOnClickListener(this);
			holder.watchedBtn.setTag(position);
			
			vi.setTag(holder);
			vi.setOnCreateContextMenuListener(this);
			vi.setOnClickListener(this);
			
		}else{
			holder = (ViewHolder) vi.getTag();
			((ViewHolder) vi.getTag()).watchedBtn.setTag(position);
		}
		holder.title.setTag(position);
		holder.title.setText(episode.TITLE);
		
		//Log.d("SeasonAdapter", episode.IMAGE_URL);
		
		
		//Season number and episode number i.e. 'S01E10'
		String s = episode.SEASON_NUMBER < 10 ? "0"+episode.SEASON_NUMBER : episode.SEASON_NUMBER + "";
		String e = episode.EPISODE_NUMBER < 10 ? "0"+episode.EPISODE_NUMBER : episode.EPISODE_NUMBER + "";
		holder.episodeSeasonNumber.setText("S" + s + "E" + e);
		
		//Reminder Clock
		Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.DAY_OF_MONTH, -1);
		Calendar episodeTime = getDate(episode);
		holder.clock.setImageResource( episodeTime == null ? R.drawable.clock_null : (getDate(episode).after(currentTime) ? R.drawable.clock : R.drawable.clock_null));

		//CheckBox
		holder.watchedBtn.setClickable(dbAdapter.isSeriesFavorited(episode.SERIES_ID));
		switch(type){
		case TYPE_WATCHED:
			holder.watchedBtn.setChecked(true);
			break;
		case TYPE_NOT_WATCHED:
			holder.watchedBtn.setChecked(false);
			break;
		}

		holder.firstAired.setText(dateFormat(episode.FIRST_AIRED));
		
		/** Image Stuff */
		String imgUri = ServerUrls.getImageUrl(activity, ServerUrls.fixURL(episode.IMAGE_URL));
		if(!MemoryCacheUtil.findCachedBitmapsForImageUri(imgUri, imageLoader.getMemoryCache()).isEmpty()){
        	
        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithoutDelay);
        	
        }else{
        	
        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithDelay);
        	
        }
		
		dbAdapter.close();
		return vi;
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		
		int position;
		switch(v.getId()){
		case R.id.season_episode_item:
			Intent i = new Intent(activity.getApplicationContext(), SeasonEpisodeActivity.class);
			position = (Integer) v.findViewById(R.id.episode_title).getTag();
			i.putExtra(SeasonEpisodeActivity.EPISODE, episodes.get(position));
			i.putParcelableArrayListExtra(SeasonEpisodeActivity.FULL_EPISODE_LIST, full_episode_list);
			i.putExtra(SeasonEpisodeActivity.SERIES, series);
			activity.startActivity(i);
			break;
		
		case R.id.season_episode_checkbox:
			
			// Get Position from View
			position = (Integer) v.getTag();
			
			// Open Database
			dbAdapter.open();

			// Get the CheckBox View
			CheckBox checkbox = (CheckBox) v;
			
			// Watched Helper
			WatchedHelper wHelper = new WatchedHelper(activity.getApplicationContext());
			
			if(wHelper.isWatched(episodes.get(position).ID)){
				
				//Episode has been watched: TRUE, toggle check to: FALSE
				checkbox.setChecked(false);

				// Remove from Watched table and handler Pending
				wHelper.removeWatched(episodes.get(position));
				
				// Remove from the local ArrayList of things to be displayed
				watched.remove(episodes.get(position).ID);
				
				//Remove the episode from the Queue DB and check if there is an earlier episode and add to Queue DB
				if(dbAdapter.isInQueue(episodes.get(position).ID)){
					//Episode removed was in the Queue DB
					dbAdapter.deleteQueueEpisode(episodes.get(position).ID);
					//Log.d(TAG, "Episode " + episodes.get(position).ID + " removed from the Queue DB");
					
					//Check Previous Watched Episodes and add the latest one, if any
					for(int j = position; j >= 0; j--){
						//Start at POSITION and work back
						if(dbAdapter.isEpisodeWatched(episodes.get(j).ID)){
							dbAdapter.insertQueue(episodes.get(j));
							//Log.d(TAG, "Episode " + episodes.get(j).ID + " added to the Queue DB");
							break;
						}
					}
				}
				
			}else{
				
				//Episode has not been watched, set to TRUE
				checkbox.setChecked(true);
				
				// Mark Episode as Watched and handler Pending
				wHelper.markWatched(episodes.get(position).ID);

				// Add to the local ArrayList of items to be displayed as watched
				watched.add(episodes.get(position).ID);
			
			}
			notifyDataSetChanged();
			dbAdapter.close();
			break;
		}
		
	}
	
	//Get Date
	@SuppressLint("SimpleDateFormat")
	private Calendar getDate(Episode episode){
		String dateString;
		Calendar d = Calendar.getInstance();
		if(episode.FIRST_AIRED != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dateString = episode.FIRST_AIRED;
			try {
				d.setTime(sdf.parse(dateString));
			} catch (ParseException e) {
				e.printStackTrace();
				d.setTime(new Date(1100, 0, 0));
			}
		}else{
			d = null;
		}
		return d;
	}

}




















