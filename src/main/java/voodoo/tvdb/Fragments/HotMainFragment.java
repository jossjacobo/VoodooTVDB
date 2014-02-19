package voodoo.tvdb.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.taig.pmc.PopupMenuCompat;

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

import voodoo.tvdb.activity.HotActivity;
import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.activity.BaseSlidingActivity;
import voodoo.tvdb.objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.FavoriteHelper;
import voodoo.tvdb.utils.FavoriteSavingListener;
import voodoo.tvdb.utils.Keys;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.xmlHandlers.XmlHandlerHot;
import voodoo.tvdb.xmlHandlers.XmlHandlerServertime;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by PUTITO-TV on 10/11/13.
 */
public class HotMainFragment{

    private static final String TAG = "HotMainFragment";

    // Item Types
    public static final int ITEM = 0;
    public static final int EMPTY = 1;
    public static final int MORE = 2;

    public DatabaseAdapter dbAdapter;
    public ArrayList<Series> series;

    public LinearLayout view;
    public ImageLoader imageLoader;

    private HotListener listener;
    private Context context;
    private LayoutInflater inflater;

    public LinearLayout createView(Context c, LayoutInflater i, ImageLoader il){

        context = c;
        inflater = i;
        imageLoader = il;
        dbAdapter = new DatabaseAdapter(context);

        view = (LinearLayout) inflater.inflate(R.layout.main_card_container, null);

        // Loading Card
        View card = inflater.from(context).inflate(R.layout.card_hot_horizontal_item,
                null,false);
        card.findViewById(R.id.card_menu).setVisibility(View.INVISIBLE);
        card.findViewById(R.id.card_star).setVisibility(View.INVISIBLE);
        view.addView(card);

        return view;
    }

    public void initialize(){
        new initializeAsync().execute(true);
    }

    class initializeAsync extends AsyncTask<Boolean, Void, ArrayList<Series>>{

        @Override
        protected ArrayList<Series> doInBackground(Boolean... booleans) {
            return fetchHot();
        }

        @Override
        protected void onPostExecute(ArrayList<Series> s){
            series = s;

            if(series != null){
                //Hot Show list from database is not empty add, MORE Item
                series.add(cardMore());
                updateView(series);
            }else{
                // If Series is not on the DB, try to download it.
                new DownloadHotShows().execute(true);
            }
        }
    }

    class DownloadHotShows extends AsyncTask<Boolean,Void,ArrayList<Series>>{

        @Override
        protected ArrayList<Series> doInBackground(Boolean... booleans) {
            return downloadHotShows();
        }

        @Override
        protected void onPostExecute(ArrayList<Series> s){
            series = s;
            if(series == null){
                // Download was not successful add the EMPTY item
                series = new ArrayList<Series>();
                series.add(cardEmpty());
            }else{
                series.add(cardMore());
                //Update the servertime_hot flag in the database
                new UpdateHotServertime().execute(true);
            }
            updateView(series);
        }
    }

    class UpdateHotServertime extends AsyncTask<Boolean,Void,Void>{
        @Override
        protected Void doInBackground(Boolean... booleans) {
            updateServertimeHot();
            return null;
        }
    }

    private Series cardEmpty() {
        Series s = new Series();
        s.TYPE = EMPTY;
        return s;
    }

    private Series cardMore() {
        Series s = new Series();
        s.TYPE = MORE;
        return s;
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        if(series != null){
//            checkForChanges();
//        }
//    }

    public void checkForChanges() {
        new checkForChangesAsync().execute(true);
    }

    class checkForChangesAsync extends AsyncTask<Boolean,Void,ArrayList<Series>>{

        @Override
        protected ArrayList<Series> doInBackground(Boolean... booleans) {
            return fetchHot();
        }

        @Override
        protected void onPostExecute(ArrayList<Series> temp){
            if(series.get(series.size()-1).TYPE == MORE || series.get(series.size()-1).TYPE == EMPTY)
                series.remove(series.size() - 1);

            if(!isEqualSeries(series,temp)){
                if(temp == null){
                    // Series for some reason is now empty...I don't even...kno..
                    series = new ArrayList<Series>();
                    series.add(cardEmpty());
                    // If Series is not on the DB, try to download it.
                    new DownloadHotShows().execute(true);
                }else{
                    series = temp;
                    series.add(cardMore());
                }
                //Log.d(TAG, "Something Changed, update the views");
                updateView(series);
            }

            series.add(series.size() == 0 ? cardEmpty() : cardMore());
        }
    }

    private void updateView(ArrayList<Series> s) {

        //Add the items to the Horizontal Scroll View
        int size = s.size() < 8 ? s.size() : 8;
        view.removeAllViews();

        for(int i = 0; i < size; i++){

            /**
             * If we reached the limit of shows to add
             * skip until the last item in the series array
             * (which will be the MORE item)
             */
            Series seriesItem = i == size-1 ? s.get(s.size()-1) : s.get(i);

            /** Find all the Layouts */
            LinearLayout item = (LinearLayout) View.inflate(context,
                    R.layout.card_hot_horizontal_item, null);
            LinearLayout imgWrapper = (LinearLayout) item.findViewById(R.id.card_image_container);
            ImageView img = (ImageView) item.findViewById(R.id.card_img);
            RelativeLayout bottomContainer = (RelativeLayout) item.findViewById(R.id.card_bottom_content_container);
            TextView title = (TextView) item.findViewById(R.id.card_hot_title);
            ImageView menu = (ImageView) item.findViewById(R.id.card_menu);
            ImageView star = (ImageView) item.findViewById(R.id.card_star);

            switch(seriesItem.TYPE){
                /** Empty Card Case */
                case EMPTY:
                    imgWrapper.setTag(i);
                    img.setImageResource(R.drawable.show_empty);
                    title.setText("EMPTY");
                    menu.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setClickable(false);
                    star.setVisibility(ImageView.INVISIBLE);
                    break;
                /** More Card Case */
                case MORE:
                    imgWrapper.setTag(i == size -1 ? series.size()-1 : i);
                    img.setImageResource(R.drawable.show_more);
                    title.setText("MORE");
                    menu.setVisibility(ImageView.INVISIBLE);
                    star.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent j = new Intent(context, HotActivity.class);
                            context.startActivity(j);
                        }
                    });
                    break;
                /** Item Card Case */
                case ITEM:
                    imgWrapper.setTag(i);
                    title.setText(seriesItem.TITLE);
                    /** Image */
                    String imgUri = ServerUrls.getImageUrl(context,
                            ServerUrls.fixURL(seriesItem.POSTER_URL));
                    imageLoader.displayImage(imgUri, img, BaseSlidingActivity.optionsWithFadeIn);

                    /** Star */
                    star.setImageResource( (seriesItem.IS_FAVORITE == 1) ?
                            R.drawable.rate_star_med_on_holo_light :
                            R.drawable.rate_star_med_off_holo_light);
                    star.setTag(seriesItem);
                    star.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            Series s = (Series) v.getTag();
                            FavoriteHelper faveHelper = new FavoriteHelper(context);
                            faveHelper.createFavoriteAlert(s, faveHelper.isSeriesFavorited(s.ID), new FavoriteSavingListener(){
                                @Override
                                public void onSavingCompleted(
                                        String series_id) {
                                    listener.onSavedCompleted();
                               }

                                @Override
                                public void onDeleteCompleted(String series_id) {
                                    listener.onDeleteCompleted();
                                }
                            });
                        }
                    });

                    /** Menu Item Set On Click */
                    bottomContainer.setTag(seriesItem);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(final View v) {
                            /** Initialize PopupMenu Class */
                            PopupMenuCompat popup = PopupMenuCompat.newInstance(context, v);
                            popup.inflate(R.menu.main_hot_menu);
                            popup.setOnMenuItemClickListener(new PopupMenuCompat.OnMenuItemClickListener(){
                                @Override
                                public boolean onMenuItemClick(
                                        android.view.MenuItem item) {
                                    Series s = (Series) v.getTag();
                                    String series_id = s.ID;
                                    int item_id = item.getItemId();
                                    switch(item_id){
                                        case R.id.main_hot_menu_open:
                                            Intent i = new Intent(context,
                                                    SeriesInfoActivity.class);
                                            i.putExtra(SeriesInfoActivity.ID, series_id);
                                            context.startActivity(i);
                                            break;
                                        case R.id.main_hot_menu_favorite:
                                            Toast.makeText(context,"Favorite",Toast.LENGTH_SHORT).show();
                                            FavoriteHelper faveHelper = new FavoriteHelper(context);
                                            faveHelper.createFavoriteAlert(s, faveHelper.isSeriesFavorited(s.ID), new FavoriteSavingListener(){
                                                @Override
                                                public void onSavingCompleted(
                                                        String series_id) {
                                                    listener.onSavedCompleted();
                                                }

                                                @Override
                                                public void onDeleteCompleted(String series_id) {
                                                    listener.onDeleteCompleted();
                                                }
                                            });
                                            break;
                                    }
                                    return true;
                                }
                            });
                            popup.show();
                        }
                    });
                    break;
            }
            /** Image Wrapper On Click Listener */
            imgWrapper.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int index = (Integer) v.getTag();
                    Series s = series.get(index);
                    switch(s.TYPE){
                        case ITEM:
                            Intent i = new Intent(context,
                                    SeriesInfoActivity.class);
                            i.putExtra(SeriesInfoActivity.ID, s.ID);
                            context.startActivity(i);
                            break;
                        case EMPTY:
                            break;
                        case MORE:
                            Intent j = new Intent(context, HotActivity.class);
                            context.startActivity(j);
                            break;
                    }
                }
            });
            view.addView(item);
        }
    }

    public void setImageLoader(ImageLoader loader){
        this.imageLoader = loader;
    }

    private ArrayList<Series> fetchHot() {

        dbAdapter.open();
        ArrayList<Series> series = dbAdapter.fetchHot();
        dbAdapter.close();

        if(series == null){
            return null;
        }else{
            FavoriteHelper fHelper = new FavoriteHelper(context);
            for(int i = 0; i < series.size(); i++){
                series.get(i).IS_FAVORITE = fHelper.isSeriesFavorited(series.get(i).ID) ?
                      Series.SERIES_IS_IN_FAVES : Series.SERIES_IS_NOT_IN_FAVES;
            }
        }
        return series;
    }

    /**
     * Database Queries and Utility Functions
     */
    private ArrayList<Series> downloadHotShows(){

        //SAXParsers
        SAXParserFactory mySAXParserFactory;
        SAXParser mySAXParser;
        XMLReader mXMLReader;
        XmlHandlerHot mXmlHandler;

        //URL
        URL url;

        //List
        ArrayList<Series> list = null;

        try{

            url = new URL(ServerUrls.getHotUrl(context));

            mySAXParserFactory = SAXParserFactory.newInstance();
            mySAXParser = mySAXParserFactory.newSAXParser();
            mXMLReader = mySAXParser.getXMLReader();
            mXmlHandler = new XmlHandlerHot(context);
            mXMLReader.setContentHandler(mXmlHandler);

            mXMLReader.parse(new InputSource(url.openStream()));

            list = mXmlHandler.getSeries();

        }catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        saveHotShows(list);
        return list;
    }

    private void saveHotShows(ArrayList<Series> list){
        dbAdapter.open();
        for(Series s : list){
            dbAdapter.insertHot(s);
        }
        dbAdapter.close();
    }

    private void updateServertimeHot(){

        String urlString = ServerUrls.getServerTimeUrl(context);
        try {
            //Create the XML parser
            SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
            SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
            XMLReader mXMLReader = mySAXParser.getXMLReader();
            XmlHandlerServertime xmlHandler = new XmlHandlerServertime();
            mXMLReader.setContentHandler(xmlHandler);

            //Create URL
            URL url = new URL(urlString);
            mXMLReader.parse(new InputSource(url.openStream()));

            //Get ServerTime and input it to flags table
            String time = xmlHandler.getTime();

            dbAdapter.open();
            if(dbAdapter.fetchFlag(Keys.SERVERTIME_HOT) == null){
                dbAdapter.insertFlag(Keys.SERVERTIME_HOT, time);
            }else{
                dbAdapter.updateFlag(Keys.SERVERTIME_HOT, time);
            }
            dbAdapter.close();

        }catch (MalformedURLException e) {
            //Log.d(TAG, "MalformedURLException");
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            //Log.d(TAG, "ParserConfigurationException");
            e.printStackTrace();
        }catch (SAXException e) {
            //Log.d(TAG, "SAXException");
            e.printStackTrace();
        }catch (IOException e) {
            //Log.d(TAG, "IOException");
            e.printStackTrace();
        }
    }

    private boolean isEqualSeries(ArrayList<Series> a, ArrayList<Series> b){
        /** NOT Equal if either one is null and the other is not */
        if((a == null && b != null) || (a != null && b == null)){
            return false;
        }
        /** EQUAL if both are null */
        if(a == null && b == null){
            return true;
        }
        /** NOT equal if sizes are different */
        if(a.size() != b.size()){
            return false;
        }
        /** NOT equal if items are different */
        for(int i = 0; i < a.size(); i++){
            if(a.get(i).compareTo(b.get(i)) == 0){
                return false;
            }
        }
        /** EQUAL if everything is identical */
        return true;
    }

//    @Override
//    public void onAttach(Activity activity){
//        super.onAttach(activity);
//
//        if(activity instanceof HotListener){
//            listener = (HotListener) activity;
//        }else{
//            throw new ClassCastException(activity.toString()
//                    + " must implement HotMainFragment.HotListener");
//        }
//
//    }

//    public void setActivity(Activity activity){
//        this.a = activity;
//    }

    public void setListener(HotListener l){
        this.listener = l;
    }

    public interface HotListener{
        public void onSavedCompleted();
        public void onDeleteCompleted();
    }
}
