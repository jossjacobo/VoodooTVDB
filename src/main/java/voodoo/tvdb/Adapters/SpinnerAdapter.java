package voodoo.tvdb.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import voodoo.tvdb.R;

/**
 *  Custom Spinner Adapter to add font and dynamic list
 */
public class SpinnerAdapter extends ArrayAdapter<CharSequence>{

	private List<CharSequence> itemList;
	private LayoutInflater inflater;
	private Typeface thinFont;
	
	public SpinnerAdapter(Context context, int textViewResourceId, List<CharSequence> objects) {
		super(context, textViewResourceId, objects);
		
		this.itemList = objects;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.thinFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		
		View row = inflater.inflate(R.layout.spinner_item, parent, false);
		TextView v = (TextView) row.findViewById(R.id.spinner_text);
		
		v.setTypeface(thinFont);
		
		v.setText(itemList.get(position));
		
		return row;
		
	}
	
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		
		View row = inflater.inflate(R.layout.spinner_item, parent, false);
		TextView v = (TextView) row.findViewById(R.id.spinner_text);
		
		v.setTypeface(thinFont);
		
		v.setText(itemList.get(position));
		
		return row;
	}
}