package voodoo.tvdb.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;

public class HotAdapter extends BaseAdapter implements OnClickListener {

	private Context context;
	private static LayoutInflater inflater = null;
	
	// Universal Image Loader
	private ImageLoader imageLoader;
	private DisplayImageOptions optionsWithDelay;
	private DisplayImageOptions optionsWithoutDelay;
	private ArrayList<Series> items;
	    
    public HotAdapter(Context context) {
        
    	this.context = context;
    	imageLoader = ImageLoader.getInstance();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		return items != null ? items.size(): 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setItems(ArrayList<Series> items){
		this.items = items;
	}
	
	public static class ViewHolder{
		public RelativeLayout itemRow;
		public TextView text;
		public ImageView image;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
        ViewHolder holder;

        Series series = items.get(position);
        if(convertView==null){
        	vi = inflater.inflate(R.layout.card_hot_listview_item, null);
            holder= new ViewHolder();
            
            holder.itemRow = (RelativeLayout) vi.findViewById(R.id.itemRow);
            holder.text = (TextView) vi.findViewById(R.id.reminder_series_name);
            holder.image = (ImageView) vi.findViewById(R.id.reminder_image);
            vi.setOnClickListener(this);
            vi.setTag(holder);
        }else{
            holder=(ViewHolder) vi.getTag();
        }
        holder.text.setText(series.TITLE != null ? series.TITLE.toUpperCase() : "Title unavailable");
        holder.image.setTag(series.POSTER_URL);
        holder.text.setTag(series.ID);
        
        String imgUri = ServerUrls.getImageUrl(context, ServerUrls.fixURL(series.POSTER_URL));
        if(!MemoryCacheUtil.findCachedBitmapsForImageUri(imgUri, imageLoader.getMemoryCache()).isEmpty()){
        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithoutDelay);
        }else{
        	imageLoader.displayImage(imgUri, holder.image, this.optionsWithDelay);
        }
        return vi;
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(context, SeriesInfoActivity.class);
		i.putExtra(SeriesInfoActivity.ID, v.findViewById(R.id.reminder_series_name).getTag().toString());
		context.startActivity(i);
	}
	
}
