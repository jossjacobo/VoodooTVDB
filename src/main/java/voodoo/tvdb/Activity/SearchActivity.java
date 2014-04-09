package voodoo.tvdb.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.volley.RequestQueue;
import android.volley.Response;
import android.volley.VolleyError;
import android.volley.toolbox.StringRequest;
import android.volley.toolbox.Volley;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ads.AdView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import voodoo.tvdb.R;
import voodoo.tvdb.adapters.LazyAdapter;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.ServerUrls;

public class SearchActivity extends BaseActivity implements OnScrollListener{

	private static final String TAG = "Search";

	private String query;
	
	private ListView list;
    private LazyAdapter adapter;
    private View loadingView;
    private TextView empty;
    private LinearLayout loadingContainer;
    private LinearLayout contentContainer;

    private ArrayList<Series> items;
    
    private DatabaseAdapter dbAdapter;
    private RequestQueue volley;
    private Gson gson;

    private final int limit = 8;
    private boolean fetching = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        gson = new Gson();
        volley = Volley.newRequestQueue(this);
        dbAdapter = new DatabaseAdapter(this);
        items = new ArrayList<Series>();

        empty = (TextView) findViewById(R.id.empty);
        loadingContainer = (LinearLayout) findViewById(R.id.search_loading);
        contentContainer = (LinearLayout) findViewById(R.id.search_content);
        loadingView = getLayoutInflater().inflate(R.layout.item_loading, null);

        // Ads
    	AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);

        query = "";

        if(getIntent().getAction().equals(Intent.ACTION_VIEW)){
            handleSearchView(getIntent(), true);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        volley.cancelAll(this);
    }

    @Override
    public void onNewIntent(final Intent queryIntent){
    	super.onNewIntent(queryIntent);

        String queryAction = queryIntent.getAction();
    	if(Intent.ACTION_SEARCH.equals(queryAction)){
            handleSearch(queryIntent);
        }else if(Intent.ACTION_VIEW.equals(queryAction)){
            handleSearchView(queryIntent, false);
        }
    }

    private void handleSearchView(Intent searchViewIntent, boolean finishActivity) {
        // Get ID passed from suggestions
        String id = searchViewIntent.getExtras().getSerializable(SearchManager.EXTRA_DATA_KEY).toString();
        Intent i = new Intent(this, SeriesInfoActivity.class);
        i.putExtra(SeriesInfoActivity.ID, id);
        startActivity(i);

        if(finishActivity)
            finish();
    }

    private void handleSearch(Intent searchIntent){
        String oldQuery = query;
        query = searchIntent.getStringExtra(SearchManager.QUERY);

        // If it's a new search query, clear fetching flag
        if(!oldQuery.equals(query)){
            fetching = false;
        }

        setActionBarTitle(query == null ? "" : query);
        search(query);
    }

    private void setupListView() {
        items.clear();
        adapter = new LazyAdapter(this, items);
        list = (ListView) findViewById(R.id.list);
        list.setOnScrollListener(null);
        list.removeFooterView(loadingView);
        list.addFooterView(loadingView);
        list.setEmptyView(empty);
        list.setAdapter(adapter);
    }

    private void search(String query) {

        if(!fetching){
            // Performing a new search, initialize ListView
            setupListView();
            fetching = true;

            showLoading();
            String url = ServerUrls.getSearchUrlv2(this, query, limit, 0);
            final SearchActivity activity = this;
            volley.add(new StringRequest(
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            showContent();
                            if(response != null && !response.equals("[]")){
                                Collections.addAll(items, gson.fromJson(response, Series[].class));
                                adapter.setItems(items);
                                fetching = false;
                                list.setOnScrollListener(activity);
                            }else{
                                list.removeFooterView(loadingView);
                                list.setOnScrollListener(null);
                                fetching = false;
                            }
                        }
                    },new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showContent();
                            list.removeFooterView(loadingView);
                            list.setOnScrollListener(null);
                            fetching = true;
                        }
             })).setTag(this);
        }
    }

    private void fetchMore() {

        if(!fetching){
            fetching = true;
            final SearchActivity activity = this;
            String url = ServerUrls.getSearchUrlv2(this, query, limit, items.size());
            volley.add(new StringRequest(
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if(response!= null && !response.equals("[]")){
                                Collections.addAll(items, gson.fromJson(response, Series[].class));
                                adapter.setItems(items);
                                fetching = false;
                                list.setOnScrollListener(activity);
                            }else{
                                list.removeFooterView(loadingView);
                                fetching = true;
                                list.setOnScrollListener(null);
                            }
                        }
                    },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    list.removeFooterView(loadingView);
                    list.setOnScrollListener(null);
                    fetching = true;
                }
            })).setTag(this);
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case android.R.id.home:
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		return true;
	}
	
	/**
	 * Scroll Listener
	 * 
	 * When the user reaches the end of the list it launches an AsyncTask to fetch for more items.
	 * If all the items on the list have been fetched it will stop.
	 */
	public void onScroll(AbsListView view, int firstVisibleItem,
			final int visibleItemCount, int totalItemCount) {
		
		int lastItemOnScreen = firstVisibleItem + visibleItemCount;
		boolean loadMore = lastItemOnScreen == totalItemCount;
		
		if(loadMore && !fetching){
            fetchMore();
		}
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState){
	}

    /**
     * On Item LongPress Context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    		MenuInflater mi = getMenuInflater();
        	mi.inflate(R.menu.list_menu_item_longpress, menu);
    }
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item){

    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    	Series s = items.get(info.position);
    	switch(item.getItemId()){
    	case R.id.menu_open:
    		Intent i = new Intent(this, SeriesInfoActivity.class);
    		i.putExtra(SeriesInfoActivity.ID, s.ID);
    		startActivity(i);
    		return true;
    	case R.id.menu_favorite:
            FavoriteHelper faveHelper = new FavoriteHelper(this);
            faveHelper.createFavoriteAlert(s, faveHelper.isSeriesFavorited(s.ID), new FavoriteSavingListener() {
                @Override
                public void onSavingCompleted(String series_id) {
                }
                @Override
                public void onDeleteCompleted(String series_id) {
                }
            });

    		return true;
    	}

    	return super.onContextItemSelected(item);
    }

    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
    }

    private void showContent(){
        loadingContainer.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
    }
}
