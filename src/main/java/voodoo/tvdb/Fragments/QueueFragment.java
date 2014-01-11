package voodoo.tvdb.Fragments;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

import java.util.ArrayList;

import voodoo.tvdb.Adapters.QueueAdapter;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.R;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class QueueFragment extends BaseListFragment {

    ArrayList<Episode> items;
    QueueAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        /** ActionBar */
        setupActionBar();

        registerForContextMenu(getListView());

        items = null;
        adapter = new QueueAdapter(context);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.icon);

        setActionBarTitle(getResources().getString(R.string.queue));
    }

    @Override
    public void onResume(){
        super.onResume();

        //Check if the ArrayList<Episode> is null to fetch data
        //If it contains data check if needs to be updated
        if(items == null){
            items = fetchQueue();
            adapter.setItems(items);
            getListView().setAdapter(adapter);
        }else{
            ArrayList<Episode> query;
            query = fetchQueue();
            if(!items.equals(query)){
                items = query;
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * Database Queries and Utility Functions
     */
    private ArrayList<Episode> fetchQueue() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(context);

        dbAdapter.open();
        ArrayList<String> idList = dbAdapter.fetchQueueAll();

        if(idList != null){

            ArrayList<Episode> items = new ArrayList<Episode>();

            for(int i = 0; i < idList.size(); i++){
                items.add(dbAdapter.fetchEpisode(idList.get(i)));
            }
            dbAdapter.close();
            return items;
        }else{
            dbAdapter.close();
            return null;
        }
    }
}
