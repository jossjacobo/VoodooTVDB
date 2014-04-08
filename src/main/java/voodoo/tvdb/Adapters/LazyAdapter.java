package voodoo.tvdb.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;

public class LazyAdapter extends BaseAdapter implements OnClickListener, OnCreateContextMenuListener{
	private static final String TAG = "LazyAdapter";
    
    private static Activity activity;
    private static LayoutInflater inflater;
    
    private ArrayList<Series> items;
    
    // Universal Image Loader
 	private ImageLoader imageLoader;
 	private DisplayImageOptions optionsWithDelay;
 	private DisplayImageOptions optionsWithoutDelay;
    
    public LazyAdapter(Context context, ArrayList<Series> i) {
        activity = (Activity) context;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = i;

        imageLoader= ImageLoader.getInstance();
        
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

    @Override
	public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(ArrayList<Series> i){
    	this.items = i;
        this.notifyDataSetChanged();
    }
    
	public static class ViewHolder{
		public LinearLayout itemRow;
        public TextView text;
        public ImageView image;
        public RatingBar ratingBar;
        public TextView statusText;
        public TextView genre;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        Series series = items.get(position);
           
        if(convertView==null){
            
        	vi = inflater.inflate(R.layout.item, null);
            holder= new ViewHolder();
            
            holder.itemRow = (LinearLayout) vi.findViewById(R.id.itemRow);
            holder.text = (TextView) vi.findViewById(R.id.text);
            holder.image = (ImageView) vi.findViewById(R.id.image);
            holder.ratingBar = (RatingBar) vi.findViewById(R.id.ratingBar);
            holder.statusText = (TextView) vi.findViewById(R.id.status);
            holder.genre = (TextView) vi.findViewById(R.id.genre);
            vi.setOnClickListener(this);
            vi.setOnCreateContextMenuListener(this);
            vi.setTag(holder);
            
        }else{
            holder=(ViewHolder) vi.getTag();
        }

        
        
        holder.text.setText(series.TITLE.toUpperCase(Locale.ENGLISH));
        holder.ratingBar.setRating(series.RATING);
        holder.statusText.setText(series.STATUS);
        holder.text.setTag(series.ID);
        holder.genre.setText(series.GENRE != null ? 
				(!series.GENRE.equals("||") ? series.GENRE.substring(1, series.GENRE.length()-1).replace("|", ", ") : "No Genre Available") :
				"No Genre Available");
        
        String url = ServerUrls.getImageUrl(activity, ServerUrls.fixURL(series.POSTER_URL));
        Log.d(TAG, url);
        if(!MemoryCacheUtil.findCachedBitmapsForImageUri(url, imageLoader.getMemoryCache()).isEmpty()){
        	
        	imageLoader.displayImage(url, holder.image, this.optionsWithoutDelay);
        	
        }else{
        	
        	imageLoader.displayImage(url, holder.image, this.optionsWithDelay);
        	
        }
        
        return vi;
    }

	@Override
	public void onClick(View v) {
		
		Intent i = new Intent(activity.getApplicationContext(), SeriesInfoActivity.class);
		i.putExtra(SeriesInfoActivity.ID, v.findViewById(R.id.text).getTag().toString());
		activity.startActivity(i);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	}
}














