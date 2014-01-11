package voodoo.tvdb.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import voodoo.tvdb.Objects.Reminder;


public class ViewPagerAdapter extends FragmentPagerAdapter {

	private Context context;
	private ArrayList<Reminder> upcoming;
	private ArrayList<Reminder> older;
	
	public ViewPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context;
	}
	
	@Override
	public Fragment getItem(int position) {
		Fragment f = new Fragment();
//		switch(position){
////		case 0:
////			f = TimelineUpcomingFragment.newInstance(context, upcoming);
////			break;
////		case 1:
////			f = TimelineListFragment.newInstance(context, older);
////			break;
////		}
		return f;

	}

	@Override
	public int getCount() {
		return 2;
	}
	
	public void setItems(ArrayList<Reminder> o, ArrayList<Reminder> u){
		this.older = o;
		this.upcoming = u;
		this.notifyDataSetChanged();
	}
}
