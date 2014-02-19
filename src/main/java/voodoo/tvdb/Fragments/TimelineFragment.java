package voodoo.tvdb.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

    View view;
    ArrayList<Fragment> frags;
    ViewPagerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        view = inflater.inflate(R.layout.timeline_main, container, false);

        //Action bar
        setupActionBar();

        adapter = new ViewPagerAdapter(null, getChildFragmentManager());

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        view.findViewById(R.id.first_tab).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.second_tab).setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        view.findViewById(R.id.first_tab).setVisibility(View.INVISIBLE);
                        view.findViewById(R.id.second_tab).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        return view;
    }

    private void setupActionBar() {
        ActionBar actionBar = context.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.icon);

        setActionBarTitle(getResources().getString(R.string.timeline));
    }

    @Override
    public void onResume(){
        super.onResume();

        if(adapter != null){
            frags = getFragments();
            adapter.setFrags(frags);
        }
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
