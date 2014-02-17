package voodoo.tvdb.Adapters;

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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;

public class FavoriteAdapter extends BaseAdapter implements OnClickListener, OnCreateContextMenuListener{
		//private static final String TAG = "FavoriteAdapter";
		
		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
	    
	    private static Context c;
	    private static LayoutInflater inflater=null;
	    
	    @SuppressWarnings("rawtypes")
		private TreeSet separatorSet;
	    private ArrayList<Series> items;
	    
	    // Universal Image Loader
	    private ImageLoader imageLoader;
	    private DisplayImageOptions optionsWithDelay;
		private DisplayImageOptions optionsWithoutDelay;
	    
	    @SuppressWarnings("rawtypes")
		public FavoriteAdapter(Context context) {
	       
	    	c = context;
	        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        items = new ArrayList<Series>();
	        separatorSet = new TreeSet();
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
	    
	    public void addItem(final Series item){
	    	items.add(item);
	    	notifyDataSetChanged();
	    }
	    
	    @SuppressWarnings("unchecked")
		public void addSeparatorItem(final Series item){
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
	    
	    public void setItems(ArrayList<Series> i){
	    	if(i != null){
	    		items = new ArrayList<Series>();
		    	//Add Separators according to Status
			    i = addSeparators(i); 
			    
			    //FOR loop to add item or separator
			    for(int k = 0; k < i.size(); k++){
			    	if(i.get(k).TYPE == Series.TYPE_ITEM){
			    		addItem(i.get(k));
			    	}else if(i.get(k).TYPE == Series.TYPE_SEPARATOR){
			    		addSeparatorItem(i.get(k));
			    	}
			    }
	    	}else{
	    		items = null;
	    	}
	    	notifyDataSetChanged();
	    }
	    private ArrayList<Series> addSeparators(ArrayList<Series> s){
	    	String previousDivider = "";
	    	for(int i = 0; i < s.size(); i++){
	    		//Log.d(TAG, "s.get(i).STATUS = " + s.get(i).STATUS);
	    		if(s.get(i).STATUS != null && !s.get(i).STATUS.equals(previousDivider)){
	    			//Log.d(TAG, "Separator added = " + s.get(i).STATUS);
	    			Series series = new Series();
	    			series.TYPE = Series.TYPE_SEPARATOR;
	    			series.DIVIDER_TEXT = s.get(i).STATUS;
	    			
	    			s.add(i, series);
	    			i++;
	    			
	    			//Set previousDivider as the Status
	    			previousDivider = s.get(i).STATUS;
	    		}
	    	}
	    	return s;
	    }
	    
	    public ArrayList<Series> removeSeparators(ArrayList<Series> s){
	    	
	    	for(int i = 0; i < s.size(); i++){
	    		
	    		if(s.get(i).TYPE == Series.TYPE_SEPARATOR){
	    			s.remove(i);
	    		}
	    		
	    	}
	    	
	    	return s;
	    }
	    
		public static class ViewHolder{
			public LinearLayout itemRow;
	        public TextView text;
	        public ImageView image;
	        public RatingBar ratingBar;
	        public TextView statusText;
	        public TextView genre;
	        public TextView separator;
	    }
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View vi = convertView;
	        ViewHolder holder;

	        Series series = items.get(position);
	        int type = getItemViewType(position);
	           
	        if(convertView == null){
	        	holder= new ViewHolder();
	            switch(type){
	            case TYPE_ITEM:
	            	
	            	vi = inflater.inflate(R.layout.item, null);
	            	
	            	holder.itemRow = (LinearLayout) vi.findViewById(R.id.itemRow);
		            holder.text = (TextView) vi.findViewById(R.id.text);
		            holder.image = (ImageView) vi.findViewById(R.id.image);
		            holder.ratingBar = (RatingBar) vi.findViewById(R.id.ratingBar);
		            holder.statusText = (TextView) vi.findViewById(R.id.status);
		            holder.genre = (TextView) vi.findViewById(R.id.genre);
		            
		            //Set Listeners
		            vi.setOnClickListener(this);
		            vi.setOnCreateContextMenuListener(this);
		            
		            break;
	            case TYPE_SEPARATOR:
	            	
	            	vi = inflater.inflate(R.layout.divider, null);
	            	vi.setBackgroundColor(c.getResources().getColor(R.color.white));
	            	
	            	holder.separator = (TextView) vi.findViewById(R.id.divider_text);
	            	holder.separator.setTextColor(c.getResources().getColor(R.color.blue));
	            	holder.separator.setTextSize(20);
	            	holder.separator.setPadding(8, 4, 8, 4);
	            	
	            	break;
	            }
	            
	            vi.setTag(holder);
	        }else{
	            holder = (ViewHolder) vi.getTag();
	        }

	        switch(type){
	        case TYPE_ITEM:
	        	holder.text.setText(series.TITLE.toUpperCase(Locale.ENGLISH));
		        holder.ratingBar.setRating(series.RATING);
		        holder.statusText.setText(series.STATUS);
		        holder.text.setTag(series.ID);
		        holder.genre.setText(series.GENRE != null ? 
						(!series.GENRE.equals("||") ? series.GENRE.substring(1, series.GENRE.length()-1).replace("|", ", ") : "No Genre Available") :
						"No Genre Available");
		        
		        String imgUri = ServerUrls.getImageUrl(c, ServerUrls.fixURL(series.POSTER_URL));
		        if(!MemoryCacheUtil.findCachedBitmapsForImageUri(imgUri, imageLoader.getMemoryCache()).isEmpty()){
		        	
		        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithoutDelay);
		        	
		        }else{
		        	
		        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithDelay);
		        	
		        }
		        
		        break;
	        case TYPE_SEPARATOR:
	        	holder.separator.setText(series.DIVIDER_TEXT != null ? series.DIVIDER_TEXT : "Ended");
	        	break;
	        }
	        return vi;
	    }

		@Override
		public void onClick(View v) {
			Intent i = new Intent(c, SeriesInfoActivity.class);
			i.putExtra(SeriesInfoActivity.ID, v.findViewById(R.id.text).getTag().toString());
			c.startActivity(i);
		}
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			//You setup this DUMMY onCreateContextMenu as a place holder so that the Search Class can implement the ContextMenu... ><
			
		}
	}