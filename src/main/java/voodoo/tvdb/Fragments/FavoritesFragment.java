package voodoo.tvdb.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import voodoo.tvdb.activity.MainActivity;
import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.adapters.FavoriteAdapter;
import voodoo.tvdb.adapters.SpinnerAdapter;
import voodoo.tvdb.alarmServices.MyAlarmManager;
import voodoo.tvdb.objects.ListItem;
import voodoo.tvdb.objects.ListObject;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ListHelper;
import voodoo.tvdb.utils.WatchedHelper;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by PUTITO-TV on 10/30/13.
 */
public class FavoritesFragment extends BaseListFragment {

    private static final String TAG = "FavoritesFragment";

    ArrayList<Series> series;
    String listName;
    FavoriteAdapter adapter;

    DatabaseAdapter dbAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        //Initialize
        series = null;
        dbAdapter = new DatabaseAdapter(context);
        adapter = new FavoriteAdapter(context);
        getListView().setAdapter(adapter);

        //Action Bar
        setupActionBar();

        registerForContextMenu(getListView());
    }

    private void setupActionBar() {
        ActionBar actionBar = context.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setIcon(R.drawable.icon);
    }

    @Override
    public void onResume(){
        super.onResume();
        dbAdapter.open();


        //Check if the ArrayList<Series> is null to fetch data
        //If it contains data check if it needs to be updated
        if(series == null){

            // Create the DropDown navigation, it will automatically load the "All" list
            createDropDownNav();

        }else{

            // Series is not null
            // Back button pressed
            if(!listName.equals("All")){

                // Check is the list didn't get deleted
                ListHelper listHelper = new ListHelper(context);

                ListObject l = listHelper.getListDetails(listName);

                if( (l == null) || (l.DELETED.equals(ListObject.KEY_TRUE) )){

                    // List was deleted
                    createDropDownNav();

                }else{

                    // List has not been deleted
                    ArrayList<Series> query = fetchFavorites(listName);
                    series = adapter.removeSeparators(series);

                    // Check if items are different
                    if(!Equals(series, query)){

                        // Items are different, load new items
                        series = query;

                        adapter = new FavoriteAdapter(context);
                        adapter.setItems(series);

                        getListView().setAdapter(adapter);
                    }
                    // else do nothing
                }

            }else{

                // Currently displaying All list
                ArrayList<Series> query = fetchFavorites(listName);
                series = adapter.removeSeparators(series);

                // Check if items are different
                if(!Equals(series, query)){

                    // Items are different, load new items
                    series = query;

                    adapter = new FavoriteAdapter(context);
                    adapter.setItems(series);

                    getListView().setAdapter(adapter);
                }
            }

        }
    }

    private void createDropDownNav() {
        // Set Drop Down Items
        final List<CharSequence> items = new ArrayList<CharSequence>();
        // Always will have the ALL item
        items.add("All");
        // Add the lists from the database
        dbAdapter.open();
        List<CharSequence> lists = dbAdapter.fetchNonDeletedListNames();
        dbAdapter.close();
        if(lists != null){
            items.addAll(lists);
        }
        ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener(){
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                listName = items.get(itemPosition).toString();
                series = fetchFavorites(listName);
                adapter = new FavoriteAdapter(context);
                adapter.setItems(series);
                getListView().setAdapter(adapter);
                return true;
            }
        };
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(context,
                android.R.layout.simple_dropdown_item_1line, items);
        context.getSupportActionBar()
                .setListNavigationCallbacks(spinnerAdapter, navListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.settings:
                startActivity(new Intent(context, Prefs.class));
                return true;
            case android.R.id.home:
                startActivity(new Intent(context, MainActivity.class));
                return true;
        }
        return false;
    }

    /**
     * On Item LongPress Context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mi = context.getMenuInflater();
        mi.inflate(R.menu.list_menu_favorite_item_longpress, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.d(TAG, "" + info.position);

        Series s = series.get(info.position);

        switch(item.getItemId()){
            case R.id.menu_open:
                Intent i = new Intent(context, SeriesInfoActivity.class);
                i.putExtra(SeriesInfoActivity.ID, s.ID);
                startActivity(i);
                return true;
            case R.id.menu_remove:
                removeFromFavorites(s.ID);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Database Queries and Utility Functions
     */
    private ArrayList<Series> fetchFavorites(String list_name){
        ArrayList<Series> series_items;
        ArrayList<ListItem> list_items;

        dbAdapter.open();
        if(list_name.equals("All")){
            series_items = dbAdapter.fetchAllSeries();
        }else{
            list_items = dbAdapter.fetchListItemsOfList(list_name);
            if(list_items != null){
                series_items = new ArrayList<Series>();
                for(int i = 0; i < list_items.size(); i++){
                    Series s = dbAdapter.fetchSeries(list_items.get(i).SERIES_ID);
                    series_items.add(s);
                }
            }else{
                series_items = null;
            }
        }
        dbAdapter.close();

        series_items = sortByTitle(series_items);
        series_items = sortByStatus(series_items);

        return series_items;
    }

    private ArrayList<Series> sortByTitle(ArrayList<Series> S) {

        Comparator<Series> comparator = new Comparator<Series>(){
            @Override
            public int compare(Series object1, Series object2) {

                if(object1.TITLE == null && object2.TITLE != null){

                    return 1;

                }

                if(object2.TITLE == null && object1.TITLE != null){

                    return -1;

                }

                if(object2.TITLE == null && object2.TITLE == null){

                    return 0;

                }

                return object1.TITLE.compareToIgnoreCase(object2.TITLE);
            }
        };

        if(S != null){
            if(S.size() > 1){
                Collections.sort(S, comparator);
                Log.d(TAG, "Series List Sorted by Title");
            }
        }
        return S;
    }

    private ArrayList<Series> sortByStatus(ArrayList<Series> S) {

        Comparator<Series> comparator = new Comparator<Series>(){
            @Override
            public int compare(Series object1, Series object2) {
                if(object1.STATUS == null){
                    object1.STATUS = "Ended";
                }
                if(object2.STATUS == null){
                    object2.STATUS = "Ended";
                }
                return object1.STATUS.compareToIgnoreCase(object2.STATUS);
            }
        };

        if(S != null){
            if(S.size() > 1){
                Collections.sort(S, comparator);
                Log.d(TAG, "Series List Sorted by Status");
            }
        }
        return S;
    }

    private void removeFromFavorites(String iD) {

        // Delete from Lists
        ListHelper listHelper = new ListHelper(context);

        // Check is series is already on any lists
        ArrayList<String> listNamesSeriesIsOn = listHelper.getListNamesSeriesIsOn(iD);
        if(listNamesSeriesIsOn != null){

            // Series is saved and on one or more lists
            // Flag them as deleted
            listHelper.flagSeriesAsDeleted(iD, listNamesSeriesIsOn);

        }

        //Delete the Favorite Files
        dbAdapter.open();
        dbAdapter.deleteSeries(iD);
        dbAdapter.deleteAllEpisode(iD);
        dbAdapter.deleteWatchedSeries(iD);


        //Remove Alarms before deleting Reminder Files
        MyAlarmManager myAlarmManager = new MyAlarmManager(context, iD);
        myAlarmManager.removeAlarms();

        //Delete all the reminders
        dbAdapter.deleteAllReminders(iD);

        //Delete all the watched Pending
        WatchedHelper wHelper = new WatchedHelper(context);
        wHelper.removedWatchedBySeries(iD);

        //Delete all of the Queue
        dbAdapter.deleteQueueSeries(iD);
        dbAdapter.deleteSeries(iD);
        dbAdapter.close();

        series = fetchFavorites(listName);
        adapter.setItems(series);
        adapter.notifyDataSetChanged();

    }

    private boolean Equals(ArrayList<Series> series, ArrayList<Series> query){
        if(series == null || query == null){
            return false;
        }
        if(series.size() == query.size()){
            for(int i = 0; i < series.size(); i++){
                if(series.get(i).ID == null && query.get(i).ID != null){
                    return false;
                }
                if(series.get(i).ID != null && query.get(i).ID == null){
                    return false;
                }
                if(!series.get(i).ID.equals(series.get(i).ID)){
                    return false;
                }
            }
        }else{
            return false;
        }
        return true;

    }
}


















