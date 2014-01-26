package voodoo.tvdb.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import javax.inject.Inject;

import voodoo.tvdb.Activity.TutorialActivity;
import voodoo.tvdb.Objects.ListObject;
import voodoo.tvdb.R;
import voodoo.tvdb.SharedPreferences.DataStore;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by PUTITO-TV on 10/28/13.
 */
public class DashboardFragment extends RoboSherlockFragment implements HotMainFragment.HotListener,
        QueueMainFragment.QueueListener, UpcomingMainFragment.UpcomingListener{

    private static final String TAG = "DashboardFragment";

    HorizontalScrollView hotView;
    HorizontalScrollView upcomingView;
    HorizontalScrollView queueView;

    LinearLayout hotViewContainer;
    LinearLayout upcomingViewContainer;
    LinearLayout queueViewContainer;

    @Inject
    public DataStore dataStore;

    ImageLoader imageLoader = ImageLoader.getInstance();
    LayoutInflater layoutInflater;

    // Fragments
    HotMainFragment hotFragment;
    QueueMainFragment queueFragment;
    UpcomingMainFragment upcomingFragment;

    DashboardListner listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        layoutInflater = inflater;
        View view = inflater.from(getActivity()).inflate(R.layout.main, container, false);

        hotView = (HorizontalScrollView) view.findViewById(R.id.main_hot_h_scroll_view);
        upcomingView = (HorizontalScrollView) view.findViewById(R.id.main_upcoming_h_scrollview);
        queueView = (HorizontalScrollView) view.findViewById(R.id.main_queue_h_scrollview);

        setupActionBarHomeTitle();
        firstStart();
        initializeFrags();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        listener.onHome(true);

        if(hotFragment.series != null)
            hotFragment.checkForChanges();
        if(upcomingFragment.reminders != null)
            upcomingFragment.checkForChanges();
        if(queueFragment.queue != null)
            queueFragment.checkForChanges();
    }

    @Override
    public void onPause(){
        super.onPause();
        listener.onHome(false);
    }

    private void initializeFrags() {
//        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();

        hotFragment = new HotMainFragment();
        hotFragment.setListener(this);
        hotViewContainer = hotFragment.createView(getSherlockActivity(),layoutInflater,imageLoader);
        hotView.addView(hotViewContainer);
        hotFragment.initialize();

        upcomingFragment = new UpcomingMainFragment();
        upcomingFragment.setListener(this);
        upcomingViewContainer = upcomingFragment.createView(getSherlockActivity(), layoutInflater, imageLoader);
        upcomingView.addView(upcomingViewContainer);
        upcomingFragment.initialize();

        queueFragment = new QueueMainFragment();
        queueFragment.setListener(this);
        queueViewContainer = queueFragment.createView(getSherlockActivity(), layoutInflater, imageLoader);
        queueView.addView(queueViewContainer);
        queueFragment.initialize();
    }

    private void firstStart() {
        if(dataStore.getFirstRun()){
            Log.e(TAG, "First Run");
            // Create Default Lists
            DatabaseAdapter dbAdapter = new DatabaseAdapter(getSherlockActivity());
            dbAdapter.open();
            dbAdapter.insertList(new ListObject(getResources().getString(R.string.list_favorite_name),
                    getResources().getString(R.string.list_favorite_description)));
            dbAdapter.insertList(new ListObject(getResources().getString(R.string.list_to_watch_name),
                    getResources().getString(R.string.list_to_watch_description)));
            dbAdapter.insertList(new ListObject(getResources().getString(R.string.list_watching_name),
                    getResources().getString(R.string.list_watching_description)));
            dbAdapter.close();

            // Launch TutorialActivity
            Intent i = new Intent(getSherlockActivity(),TutorialActivity.class);
            startActivity(i);

            // First Run completed
            dataStore.persistFirstRun();
        }
    }

    @Override
    public void onSavedCompleted() {
        upcomingFragment.checkForChanges();
        hotFragment.checkForChanges();
    }

    @Override
    public void onDeleteCompleted() {
        upcomingFragment.checkForChanges();
        hotFragment.checkForChanges();
        queueFragment.checkForChanges();
    }

    @Override
    public void onWatched() {
        queueFragment.checkForChanges();
    }

    @Override
    public void onLoadQueueFragment() {
        listener.onLoadQueue();
    }

    @Override
    public void onLoadTimelineFragment() {
        listener.onLoadTimeline();
    }

    public void setupActionBarHomeTitle() {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setIcon(R.drawable.logo);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof DashboardListner){
            listener = (DashboardListner) activity;
        }else{
            throw new ClassCastException(activity.toString()
                    + " must implement DashboardFragment.DashboardListener");
        }
    }

    public interface DashboardListner{
        public void onHome(boolean isHome);
        public void onLoadQueue();
        public void onLoadTimeline();
    }
}
