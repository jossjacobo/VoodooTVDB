package voodoo.tvdb.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import voodoo.tvdb.R;

public class TutorialPagerAdapter extends PagerAdapter {

	private final int COUNT = 6;
	
	private Context context;
	private Typeface thinFont;
	
	TextView text1;
	TextView text2;
	TextView text3;
	TextView text4;
	
	public TutorialPagerAdapter(Context c) {
		this.context = c;
		this.thinFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
	}

	@Override
	public int getCount() {
		return COUNT;
	}

	@Override
	public Object instantiateItem(View collection, int position){
		
		LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		int resId = 0;
		switch(position){
		case 0:
			resId = R.layout.page_1;
			break;
		case 1:
			resId = R.layout.page_3;
			break;
		case 2:
			resId = R.layout.page_4;
			break;
		case 3:
			resId = R.layout.page_5;
			break;
		case 4:
			resId = R.layout.page_6;
			break;
		case 5:
			resId = R.layout.page_7;
			break;
		}
		
		//Inflate View
		View view = inflater.inflate(resId, null);
		
		//Add Text Appearance
		switch(position){
		case 0:
			//Page 1
			text1 = (TextView) view.findViewById(R.id.tutorial_page1_text1);
			text2 = (TextView) view.findViewById(R.id.tutorial_page1_text2);
			text3 = (TextView) view.findViewById(R.id.tutorial_page1_text3);
			text4 = (TextView) view.findViewById(R.id.tutorial_page1_text4);

			text1.setTypeface(thinFont);
			text2.setTypeface(thinFont);
			text3.setTypeface(thinFont);
			text4.setTypeface(thinFont);

			break;
		case 1:
			//Page 3
			text1 = (TextView) view.findViewById(R.id.tutorial_page3_text1);
			setColor(text1, new String[]{"Star Icon"}, context.getResources().getColor(R.color.blue));
			text1.setTypeface(thinFont);
			break;
		case 2:
			//Page 4
			text1 = (TextView) view.findViewById(R.id.tutorial_page4_text1);
			setColor(text1, new String[]{"Favorites", "lists!", "lists"}, context.getResources().getColor(R.color.blue));
			text1.setTypeface(thinFont);
			
			break;
		case 3:
			//Page 5
			text1 = (TextView) view.findViewById(R.id.tutorial_page5_text1);
			setColor(text1, new String[]{"Watched Icon","Queue List"}, context.getResources().getColor(R.color.blue));
			text1.setTypeface(thinFont);
			
			break;
		case 4:
			//Page 6
			text1 = (TextView) view.findViewById(R.id.tutorial_page6_text1);
			setColor(text1, new String[]{"Long-Press", "watched"}, context.getResources().getColor(R.color.blue));
			text1.setTypeface(thinFont);
			break;
		case 5:
			//Page 7
			text1 = (TextView) view.findViewById(R.id.tutorial_page7_text1);
			text1.setTypeface(thinFont);
			
			//VoodooTVDB.com
			text2 = (TextView) view.findViewById(R.id.tutorial_page7_text2);
			text2.setTypeface(thinFont);
			text2.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(context.getString(R.string.website)));
					context.startActivity(i);
				}});
			
			
			text1 = (TextView) view.findViewById(R.id.tutorial_page7_text3);
			text1.setTypeface(thinFont);
			
			//JossJacobo@gmail.com
			text3 = (TextView) view.findViewById(R.id.tutorial_page7_text4);
			text3.setTypeface(thinFont);
			text3.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//Intent i = new Intent(Intent.ACTION_SEND);
					//i.setType("plain/text");
					//i.setType("message/rfc822");
					//i.putExtra(Intent.EXTRA_EMAIL, context.getString(R.string.email));
					//i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject));
					//i.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.body));
					
					//context.startActivity(Intent.createChooser(i, "Send mail..."));
					
					//This is a hack to only send the intent to EMAIL applications
					Intent intent = new Intent(Intent.ACTION_SENDTO, 
							Uri.parse( "mailto:" + context.getString(R.string.email) +
						    "?subject=" + Uri.encode(context.getString(R.string.subject)) +
						    "&body=" + Uri.encode("")));
					context.startActivity(Intent.createChooser(intent, "Email"));
				}});
			
			
			text1 = (TextView) view.findViewById(R.id.tutorial_page7_text5);
			text1.setTypeface(thinFont);
			
			//@VoodooXTC
			text4 = (TextView) view.findViewById(R.id.tutorial_page7_text6);
			setColor(text4,new String[]{"@VoodooXTC"},context.getResources().getColor(R.color.blue));
			text4.setTypeface(thinFont);
			text4.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("https://twitter.com/intent/user?screen_name=@" + context.getString(R.string.twitter_username)));
					context.startActivity(i);
				}});
			
			
			text1 = (TextView) view.findViewById(R.id.tutorial_page7_text7);
			text1.setTypeface(thinFont);
			break;
		}
		
		((ViewPager) collection).addView(view, 0);
		
		return view;
	}
	
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2){
		((ViewPager) arg0).removeView((View) arg2);
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);
	}
	
	@Override
	public Parcelable saveState(){
		return null;
	}
	
	private void setColor(TextView view, String[] subtext, int color) {
	      view.setText(view.getText().toString(), TextView.BufferType.SPANNABLE);
		
	      Spannable str = (Spannable) view.getText();
	      for(int i = 0; i < subtext.length; i++){
	    	  
	    	  int index = view.getText().toString().indexOf(subtext[i]);
		      str.setSpan(new ForegroundColorSpan(color), index, index + subtext[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	      
	      }
	}

}
