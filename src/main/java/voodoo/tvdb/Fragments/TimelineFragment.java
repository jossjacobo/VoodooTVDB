package voodoo.tvdb.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import voodoo.tvdb.R;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class TimelineFragment extends BaseFragment {

    private ViewPagerAdapter adapter;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.timeline_main, container, false);

        adapter = new ViewPagerAdapter(null, getChildFragmentManager());

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                context.getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

        setupActionBar();
        return view;
    }

    private void setupActionBar() {
        ActionBar actionBar = context.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setIcon(R.drawable.icon);

        // Setup Tabs
        ActionBar.Tab upcomingTab = actionBar.newTab().setText(getString(R.string.tab_upcoming)).setTabListener(TimeLineTabListener);
        actionBar.addTab(upcomingTab);

        ActionBar.Tab olderTab = actionBar.newTab().setText(getString(R.string.tab_older)).setTabListener(TimeLineTabListener);
        actionBar.addTab(olderTab);

        setActionBarTitle(getResources().getString(R.string.timeline));
    }

    private ActionBar.TabListener TimeLineTabListener = new ActionBar.TabListener(){

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }
    };

    @Override
    public void onResume(){
        super.onResume();

        if(adapter != null){
            ArrayList<Fragment> frags = getFragments();
            adapter.setFrags(frags);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        context.getSupportActionBar().removeAllTabs();
    }

    private ArrayList<Fragment> getFragments() {
        TimelineListFragment upcomingFragment = getUpcomingFragment();
        TimelineListFragment olderFragment = getOlderFragment();

        ArrayList<Fragment> f = new ArrayList<Fragment>();
        f.add(upcomingFragment);
        f.add(olderFragment);

        return f;
    }

    private TimelineListFragment getUpcomingFragment() {
        Bundle b = new Bundle();
        b.putInt(TimelineListFragment.TYPE, TimelineListFragment.TYPE_UPCOMING);

        TimelineListFragment uFrag = new TimelineListFragment();
        uFrag.setArguments(b);
        return uFrag;
    }

    private TimelineListFragment getOlderFragment() {
        Bundle b = new Bundle();
        b.putInt(TimelineListFragment.TYPE, TimelineListFragment.TYPE_OLDER);

        TimelineListFragment oFrag = new TimelineListFragment();
        oFrag.setArguments(b);
        return oFrag;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

       private ArrayList<Fragment> fragments;
       public ViewPagerAdapter(ArrayList<Fragment> f, FragmentManager fm) {
            super(fm);
            this.fragments = f;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments == null ? 0 : fragments.size();
        }

        public void setFrags(ArrayList<Fragment> f){
            this.fragments = f;
            notifyDataSetChanged();
        }
    }
}
