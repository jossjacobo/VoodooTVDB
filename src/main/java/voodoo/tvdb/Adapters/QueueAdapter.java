package voodoo.tvdb.Adapters;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.TreeSet;

import voodoo.tvdb.Activity.SeasonEpisodeActivity;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.Utils.ServerUrls;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class QueueAdapter extends BaseAdapter implements OnClickListener, OnCreateContextMenuListener{
	//private static final String TAG = "QueueAdapter";
	
	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    
    private static Activity activity;
    private static LayoutInflater inflater=null;
    
    @SuppressWarnings("rawtypes")
	private TreeSet separatorSet;
    private ArrayList<Episode> items;
    
    DatabaseAdapter dbAdapter;
    
    // Universal Image Loader
 	private ImageLoader imageLoader;
 	private DisplayImageOptions optionsWithDelay;
 	private DisplayImageOptions optionsWithoutDelay;

    @SuppressWarnings("rawtypes")
	public QueueAdapter(Context context) {
        activity = (Activity) context;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = new ArrayList<Episode>();
        separatorSet = new TreeSet();
        
		dbAdapter = new DatabaseAdapter(context);
		
		imageLoader = ImageLoader.getInstance();
		
		optionsWithDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub)
			.showImageForEmptyUri(R.drawable.stub_not_found)
			.showImageOnFail(R.drawable.stub_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(new FadeInBitmapDisplayer(1000))
			.build();
	
		optionsWithoutDelay = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.stub)
			.showImageForEmptyUri(R.drawable.stub_not_found)
			.showImageOnFail(R.drawable.stub_not_found)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.build();
    }
    
    public void addItem(final Episode item){
    	items.add(item);
    	notifyDataSetChanged();
    }
    
    @SuppressWarnings("unchecked")
	public void addSeparatorItem(final Episode item){
    	items.add(item);
    	//save the separator position
    	separatorSet.add(items.size() - 1);
    	notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position){
    	return separatorSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }
    
    @Override
    public int getViewTypeCount(){
    	return TYPE_MAX_COUNT;
    }

	@Override
	public int getCount() {
		return items != null ? items.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setItems(ArrayList<Episode> i){
    	if(i != null){
    		items = new ArrayList<Episode>();
    		
    		//Fetch the next episodes to watch from the Queue
    		i = fetchNextEpisode(i);
    		
	    	//Add Separators according to Series Name
		    i = addSeparators(i); 
		    
		    //Log.d(TAG, "i.size() = " + i.size());
		    //FOR loop to add item or separator
		    for(int k = 0; k < i.size(); k++){
		    	//Log.d(TAG, "k = " + k);
		    	if(i.get(k).TYPE == TYPE_ITEM){
		    		//Log.d(TAG, "Add Item");
		    		addItem(i.get(k));
		    	}else if(i.get(k).TYPE == TYPE_SEPARATOR){
		    		//Log.d(TAG, "Add Separator");
		    		addSeparatorItem(i.get(k));
		    	}
		    }
		    //Log.d(TAG, "Out of For Loop to add items...");
    	}else{
    		items = null;
    	}
    	notifyDataSetChanged();
    }
	private ArrayList<Episode> fetchNextEpisode(ArrayList<Episode> i) {
		
		ArrayList<Episode> list = new ArrayList<Episode>();
		Episode e;
		
		dbAdapter.open();
		
		//Go through each episode and fetch the next episode if available
		for(int j = 0; j < i.size(); j++){
			//Check if there is an Next episode available for the same season
			e = dbAdapter.fetchEpisodeBySeasonAndEpisodeNumber(i.get(j).SERIES_ID, i.get(j).SEASON_NUMBER, i.get(j).EPISODE_NUMBER + 1);
			if(e != null){
				
				//Found an next episode on the same season
				e.PREVIOUS_EPISODE_ID = i.get(j).ID;
				list.add(e);
			
			}else{
				
				e = dbAdapter.fetchEpisodeBySeasonAndEpisodeNumber(i.get(j).SERIES_ID, i.get(j).SEASON_NUMBER + 1, 1);
				if(e != null){
					
					//Found the next episode to be the first episode of the next season
					e.PREVIOUS_EPISODE_ID = i.get(j).ID;
					list.add(e);
				}
			}
			
		}
		
		dbAdapter.close();
		
		return list;
	}

	private ArrayList<Episode> addSeparators(ArrayList<Episode> s){
		
		dbAdapter.open();
    	String previousDivider = "";
    	
    	for(int i = 0; i < s.size(); i++){
    		
    		//Fetch the Series for each episode
    		Series series = dbAdapter.fetchSeries(s.get(i).SERIES_ID);
    		
    		//Extract the title of the Series
    		String seriesName = series.TITLE;
    		
    		//Replace the IMAGE_URL of the episode with the Series one
    		s.get(i).IMAGE_URL = series.POSTER_URL;
    		
    		if(seriesName != null && !seriesName.equals(previousDivider)){
    			//Log.d(TAG, "Separator added = " + seriesName);
    			Episode ep = new Episode();
    			ep.TYPE = Series.TYPE_SEPARATOR;
    			ep.DIVIDER_TEXT = seriesName;
    			
    			s.add(i, ep);
    			i++;
    			
    			//Set previousDivider as the Status
    			previousDivider = seriesName;
    		}
    	}
    	dbAdapter.close();
    	return s;
    }

	public static class ViewHolder{
		public TextView queueEpisodeName;
		public TextView queueSeasonAndEpisode;
		public ImageView queueImage;
		public TextView queueSeparator;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
        ViewHolder holder;
        
        //Log.d(TAG, "items.size() = " + this.items.size());
        //Log.d(TAG, "Episode e = items.get(" + position + ")");
        
        Episode e = this.items.get(position);
        
        int type = getItemViewType(position);
        
        if(convertView == null){
        	holder = new ViewHolder();
        	switch(type){
        	case TYPE_ITEM:
        		vi = inflater.inflate(R.layout.reminder_item, null);
            	
            	holder.queueSeasonAndEpisode = (TextView) vi.findViewById(R.id.reminder_date_and_time);
            	holder.queueEpisodeName = (TextView) vi.findViewById(R.id.reminder_episode_name);
            	holder.queueImage = (ImageView) vi.findViewById(R.id.reminder_image);
            	
            	vi.setOnClickListener(this);
            	break;
        	case TYPE_SEPARATOR:
        		vi = inflater.inflate(R.layout.divider, null);
        		
        		vi.setBackgroundColor(activity.getResources().getColor(R.color.white));
        		
        		holder.queueSeparator = (TextView) vi.findViewById(R.id.divider_text);
        		holder.queueSeparator.setTextColor(activity.getResources().getColor(R.color.blue));
        		holder.queueSeparator.setTextSize(20);
        		holder.queueSeparator.setPadding(8, 4, 8, 4);
        		
        		break;
        	}
        	
        	vi.setTag(holder);
        }else{
        	holder = (ViewHolder) vi.getTag();
        }
        
        switch(type){
        case TYPE_ITEM:
        	
        	
        	
        	holder.queueSeasonAndEpisode.setTag(e);
        	holder.queueEpisodeName.setTag(position);
            
        	int sn = e.SEASON_NUMBER == -1 ? 0 : e.SEASON_NUMBER;
    		String s = sn < 10 ? "0"+sn : sn + "";
    		
    		String ep = e.EPISODE_NUMBER < 10 ? "0" + e.EPISODE_NUMBER : e.EPISODE_NUMBER + "";
            
    		holder.queueSeasonAndEpisode.setText("S" + s + "E" + ep);
            holder.queueEpisodeName.setText(e.TITLE);
            
            String url = ServerUrls.getImageUrl(activity, ServerUrls.fixURL(e.IMAGE_URL));
            if(!MemoryCacheUtil.findCachedBitmapsForImageUri(url, ImageLoader.getInstance().getMemoryCache()).isEmpty()){
            	
            	imageLoader.displayImage(url, holder.queueImage, this.optionsWithoutDelay);
            	
            }else{
            	
            	imageLoader.displayImage(url, holder.queueImage, this.optionsWithDelay);
            	
            }
            break;
        case TYPE_SEPARATOR:
        	holder.queueSeparator.setText(e.DIVIDER_TEXT != null ?
        			e.DIVIDER_TEXT :
        			"Series");
        	break;
        }
        
		return vi;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.findViewById(R.id.reminder_episode_name).getTag();
		Episode e = items.get(position);
		
		Reminder r = new Reminder();
		r.EPISODE_ID = e.ID;
		r.SERIES_ID = e.SERIES_ID;
		
		Intent i = new Intent(activity, SeasonEpisodeActivity.class);
		i.putExtra(SeasonEpisodeActivity.REMINDER, r);
		activity.startActivity(i);
	}
}
