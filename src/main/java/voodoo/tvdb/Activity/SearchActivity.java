package voodoo.tvdb.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ads.AdView;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import voodoo.tvdb.adapters.LazyAdapter;
import voodoo.tvdb.objects.SearchBundle;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.xmlHandlers.XmlHandlerFetchInfo;
import voodoo.tvdb.xmlHandlers.XmlHandlerSearch;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class SearchActivity extends BaseActivity implements OnScrollListener{

	private static final String TAG = "Search";

	public boolean busy_fetching_more = false;
	private String query;
	
	ListView list;
	TextView empty;
    LazyAdapter adapter;
    View loadingView;
    
    private SearchBundle searchBundle;
    
    DatabaseAdapter dbAdapter;
    TextView searchColon;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        list = (ListView) findViewById(R.id.list);
        empty = (TextView) findViewById(R.id.empty);

        list.setEmptyView(empty);
        
        dbAdapter = new DatabaseAdapter(this);
        loadingView = getLayoutInflater().inflate(R.layout.item_loading, null);
        
        //Get Intent, verify the action and get the query
        Intent intent = getIntent();

        Log.e(TAG, "intent.getAction() = " + intent.getAction());
        
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
        	
        	//Get Query passed from search
        	query = intent.getStringExtra(SearchManager.QUERY);
        	//Log.d("TAG", "Query passed = '" + query + "'");
        	
        	//Perform search
        	new searchQuery(this).execute(query);
        	
        } else if(Intent.ACTION_VIEW.equals(intent.getAction())){
        	
        	// Get Query passed from suggestions
        	Uri detailUri = intent.getData();

            query = detailUri.getLastPathSegment().toLowerCase();
            Log.e(TAG, "Suggestions Query: " + query);
        	
        	//Perform search
        	new searchQuery(this).execute(query);
        	
        }
        //ActionBar
        setActionBarTitle(query == null ? "" : query);
        searchBundle = new SearchBundle();
        searchColon = (TextView) findViewById(R.id.searchColon);

        // Ads
    	AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
    }
    
    @Override
    public void onNewIntent(final Intent queryIntent){
    	super.onNewIntent(queryIntent);
    	
    	// Set Empty Item List
    	adapter = new LazyAdapter(this, new ArrayList<Series>());
    	adapter.setItems(new ArrayList<Series>());
    	adapter.notifyDataSetChanged();

        Log.e(TAG, "intent.getAction() = " + queryIntent.getAction());
    	
    	final String queryAction = queryIntent.getAction();
    	if(Intent.ACTION_SEARCH.equals(queryAction)){
    		
    		//Get Query passed from search
        	query = queryIntent.getStringExtra(SearchManager.QUERY);
        	Log.e("TAG", "Query passed = '" + query + "'");
        	
        	//Perform search
        	new searchQuery(this).execute(query);
        	
    	}else if(Intent.ACTION_VIEW.equals(queryAction)){
    		
    		// Get Query passed from suggestions
        	Uri detailUri = queryIntent.getData();
            query = detailUri.getLastPathSegment().toLowerCase();
        	
        	Log.e(TAG, "ACTION_VIEW Query = " + query);
        	
        	//Perform search
        	new searchQuery(this).execute(query);
        	
    	}
    	//Change the title of the ActionBar
        setActionBarTitle(query == null ? "" : query);
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
		
		if(loadMore && !busy_fetching_more){
			busy_fetching_more = true;
			searchBundle.TOLOAD = visibleItemCount;
			new moreQuery(this).execute(searchBundle);
		}
		
		if(searchBundle.IDLIST.size() == (totalItemCount + searchBundle.EXCEPTIONS)){
			if(list.getFooterViewsCount() != 0)
				list.removeFooterView(loadingView);
			busy_fetching_more = true;
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

    	Series s = searchBundle.ITEMS.get(info.position);
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

    /**
     * AsyncTask to search for the passed query and download the initial items
     */
    private class searchQuery extends AsyncTask<String, Void, SearchBundle>{

    	private Context context;
    	private ProgressDialog dialog;
    	private AsyncTask<String, Void, SearchBundle> myQuery = null;
    	
    	//SAXParsers
    	SAXParserFactory mySAXParserFactory;
    	SAXParser mySAXParser;
    	XMLReader mXMLReader;
    	XmlHandlerSearch mXmlHandler;
    	XmlHandlerFetchInfo mXmlHandlerFetchInfo;
    	
    	URL url;
    	
    	//Constructor
    	public searchQuery(Activity activity){
    		context = activity;
    		dialog = new ProgressDialog(context);
    	}
    	
		@Override
		protected void onPreExecute(){
			dialog.setMessage("Loading. Please wait...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					myQuery.cancel(true);
				}
			});
			dialog.show();
			myQuery = this;
			
			//Log.d(TAG, "searchQuery, onPreExecute()");
			
			// Hide Footer
			if(list != null && list.getFooterViewsCount() > 0){
				list.removeFooterView(loadingView);
			}
			
		}
    	
    	@Override
		protected SearchBundle doInBackground(String... queryString) {
			SearchBundle sb = new SearchBundle();
			
			//Log.d(TAG, "searchQuery, doInBackground()");
    		
    		//Download the ID List
    		for(String query : queryString){
    			
    			//String onlineString = "http://ci.voodootvdb.com/search/show/xml/" + query.replace(" ", "%20");
    			
    			//URL String
    			try{

    				url = new URL(ServerUrls.getSearchUrl(context, query.replace(" ", "%20")));
    				Log.d(TAG, url.toString());
    				
    				mySAXParserFactory = SAXParserFactory.newInstance();
        			mySAXParser = mySAXParserFactory.newSAXParser();
        			mXMLReader = mySAXParser.getXMLReader();
        			mXmlHandler = new XmlHandlerSearch();
        			
        			mXMLReader.setContentHandler(mXmlHandler);
        			
        			//Create an input source and set encoding to "ISO-8857-15"
        			InputSource is = new InputSource(url.openStream());
        			//is.setEncoding("ISO-8859-1");
        			mXMLReader.parse(is);
        			
        			//Log.d(TAG, "searchQuery, create SAXParseFactory, SAXParser...etc and open stream...");
        			
        			sb.IDLIST = mXmlHandler.getIdList();
        			sb.COUNT = sb.IDLIST.size() > 5 ? 5 : sb.IDLIST.size();
        			
    			}catch (MalformedURLException e) {
    				//Log.d(TAG, "MalformedURLException fetching ID list");
    				e.printStackTrace();
    				return null;
    			} catch (ParserConfigurationException e) {
    				//Log.d(TAG, "ParserConfigurationException fetching ID list");
    				e.printStackTrace();
    				return null;
    			} catch (SAXException e) {
    				//Log.d(TAG, "SAXException fetching ID list");
    				e.printStackTrace();
    				return null;
    			} catch (IOException e) {
    				//Log.d(TAG, "IOException fetching ID list");
    				e.printStackTrace();
    				return null;
    			}
    			
    		}
    		
    		//Download the Initial Items
    		sb.ITEMS = new ArrayList<Series>();
    		sb.EXCEPTIONS = 0;
    		
    		//Log.d(TAG, "searchQuery, Download initial items one by one");
    		
    		//Download Initial items one by one...
    		for(int i = 0; i < sb.COUNT; i++){
    			
    			//String onlineString = "http://voodootvdb.com/getAllSeries.php?ID=" + sb.IDLIST.get(i).ID;
    			
    			//URL
    			try {
    				
    				url = new URL( ServerUrls.getSeriesUrl(context, sb.IDLIST.get(i).ID));
    				Log.d(TAG, url.toString());
    				
    				mySAXParserFactory = SAXParserFactory.newInstance();
    	        	mySAXParser = mySAXParserFactory.newSAXParser();
    	        	mXMLReader = mySAXParser.getXMLReader();
    	        	mXmlHandlerFetchInfo = new XmlHandlerFetchInfo(context);
    	        	
    	        	mXMLReader.setContentHandler(mXmlHandlerFetchInfo);
    	        	mXMLReader.parse(new InputSource(url.openStream()));
    	        	
    	        	//Log.d(TAG, "searchQuery, after creating it all the SAXParseFactory and opening stream....");
    	        	
    	        	Series s = mXmlHandlerFetchInfo.getSeries();
    	        	sb.ITEMS.add(s);
    	        	
    			} catch (MalformedURLException e) {
    				sb.EXCEPTIONS++;
    				//Log.e(TAG, "MalformedURLException fetching series info");
    				e.printStackTrace();
    			} catch (ParserConfigurationException e){
    				sb.EXCEPTIONS++;
    				//Log.e(TAG, "ParserConfigurationException no element found fetching series info");
    				e.printStackTrace();
    			} catch (SAXException e) {
    				sb.EXCEPTIONS++;
    				//Log.e(TAG, "SAXException fetching series info");
    				e.printStackTrace();
    			} catch (IOException e) {
    				sb.EXCEPTIONS++;
    				//Log.e(TAG, "IOException fetching series info");
    				e.printStackTrace();
    			}
    		}
    		
			return sb;
		}
    	
    	@Override
    	protected void onPostExecute(SearchBundle sb){
    		
    		//Log.d(TAG, "searchQuery, onPostExecute()");
    		
    		searchBundle = sb;
    		
    		dialog.dismiss();
    		
    		if(sb == null || sb.ITEMS == null || sb.ITEMS.isEmpty()){
    			empty.setText(R.string.empty_text);
    			return;
    		}
    		adapter=new LazyAdapter(SearchActivity.this, searchBundle.ITEMS);
            list.setFastScrollEnabled(true);
            
            adapter.setItems(sb.ITEMS);

            if(sb.IDLIST.size() > 5){
            	list.setOnScrollListener(SearchActivity.this);
            	list.addFooterView(loadingView);
            }
            list.setAdapter(adapter);
            registerForContextMenu(list);
            
            busy_fetching_more = false;
    	}
    	
    }
    
    /**
     * AsyncTask to download more Series when the user scrolls to the bottom of the list
     */
    private class moreQuery extends AsyncTask<SearchBundle, Void, SearchBundle>{

    	private Context context;
    	
    	public moreQuery(Activity activity){
    		context = activity;
    	}
    	
		@Override
		protected SearchBundle doInBackground(SearchBundle... params) {
			for(SearchBundle sb : params){
				
				//Calculate how many shows to load
				int toLoad = (sb.ITEMS.size() + sb.TOLOAD) > sb.IDLIST.size() ? sb.IDLIST.size() : (sb.ITEMS.size() + sb.TOLOAD);
				
				for(int i = sb.ITEMS.size(); i < toLoad; i++){
					
					//Download each item individually
					
					//String onlineString = "http://voodootvdb.com/getAllSeries.php?ID=" + sb.IDLIST.get(i).ID;
					
					//URL 
					try {
						
						URL url = new URL( ServerUrls.getSeriesUrl(context, sb.IDLIST.get(i).ID));
						Log.d(TAG, url.toString());
						
						SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
			        	SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			        	XMLReader mXMLReader = mySAXParser.getXMLReader();
			        	XmlHandlerFetchInfo mXmlHandlerFetchInfo = new XmlHandlerFetchInfo(context);
			        	mXMLReader.setContentHandler(mXmlHandlerFetchInfo);
			        	mXMLReader.parse(new InputSource(url.openStream()));
			        	
			        	Series s = mXmlHandlerFetchInfo.getSeries();
			        	sb.ITEMS.add(s);
			        	//Log.d(TAG, "doInBackground ... sb.ITEMS.size() = " + sb.ITEMS.size());
			        	
					} catch (MalformedURLException e) {
						sb.EXCEPTIONS++;
						//Log.e(TAG, "MalformedURLException fetching series info");
						e.printStackTrace();
					} catch (ParserConfigurationException e){
						sb.EXCEPTIONS++;
						//Log.e(TAG, "ParserConfigurationException no element found fetching series info");
						e.printStackTrace();
					} catch (SAXException e) {
						sb.EXCEPTIONS++;
						//Log.e(TAG, "SAXException fetching series info");
						e.printStackTrace();
					} catch (IOException e) {
						sb.EXCEPTIONS++;
						//Log.e(TAG, "IOException fetching series info");
						e.printStackTrace();
					}
				}
				return sb;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(SearchBundle sb){
			//Log.d(TAG, "searchBundle.ITEMS.size() = " + searchBundle.ITEMS.size());
			//Log.d(TAG, "sb.ITEMS.size() = " + sb.ITEMS.size());
			
			searchBundle = sb;
			adapter.setItems(searchBundle.ITEMS);
			adapter.notifyDataSetChanged();
			busy_fetching_more = false;
			
			// Determine whether to remove footer loading view
			if( (searchBundle.ITEMS.size() + searchBundle.EXCEPTIONS) >= searchBundle.IDLIST.size()){
				list.removeFooterView(loadingView);
			}
			
		}
    	
    }
}
