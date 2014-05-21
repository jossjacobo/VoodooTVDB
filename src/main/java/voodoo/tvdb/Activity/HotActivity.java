package voodoo.tvdb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;

import voodoo.tvdb.adapters.HotAdapter;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class HotActivity extends BaseActivity {

    private static final String TAG = "Hot";
	
	//Database
	DatabaseAdapter dbAdapter;

	//Hot Series List
	ArrayList<Series> hotList;
	HotAdapter adapter;
	
	//List Views
	ListView list;
	TextView empty;
	
	//Title
	String title = "Hot Shows";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hot);

        list = (ListView) findViewById(R.id.hot_list);
        empty = (TextView) findViewById(R.id.empty_hot_list);

        list.setEmptyView(empty);

		//Initialize
		hotList = null;
		dbAdapter = new DatabaseAdapter(this);
		adapter = new HotAdapter(this);
		
        //Action Bar title
        setActionBarTitle(title);

        // Ads
        adView = (PublisherAdView) findViewById(R.id.adView);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        adView.loadAd(adRequest);
        viewAds(adView);
	}
	
	@Override
    public void onResume(){
    	super.onResume();
    	dbAdapter.open();
    	
    	//Check if the ArrayList<Series> is null to fetch data
    	//If it contains data check if it needs to be updated
    	if(hotList == null){
    		
    		hotList = dbAdapter.fetchHot();
            Log.e(TAG, "hotList size: " + (hotList == null ? "null" : hotList.size()));
    		adapter.setItems(hotList);
    		list.setAdapter(adapter);
    		
    	}else{

            ArrayList<Series> query = dbAdapter.fetchHot();
        	if(!hotList.equals(query)){
        		//Log.d(TAG, "Series is not equal to query");
        		hotList = query;
        		adapter.setItems(hotList);
        		adapter.notifyDataSetChanged();
        	}
    	}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		dbAdapter.close();
	}
	@Override
    public void onDestroy()
    {
		if(adapter != null){
			list.setAdapter(null);
		}
        super.onDestroy();
        dbAdapter.close();
    }
	@Override
	public void onStop(){
		super.onStop();
		dbAdapter.close();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case android.R.id.home:
            Intent i = new Intent(this, voodoo.tvdb.activity.MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return false;
	}
}





























