package voodoo.tvdb.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.taig.pmc.PopupMenuCompat;

import java.util.ArrayList;

import voodoo.tvdb.Activity.SeasonEpisodeActivity;
import voodoo.tvdb.Activity.SeriesInfoActivity;
import voodoo.tvdb.Activity.BaseSlidingActivity;
import voodoo.tvdb.Objects.Episode;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.Objects.Series;
import voodoo.tvdb.R;
import voodoo.tvdb.Utils.ServerUrls;
import voodoo.tvdb.Utils.WatchedHelper;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by PUTITO-TV on 10/11/13.
 */
public class QueueMainFragment{

    private static final String TAG = "QueueAsync";

    /** Item Types */
    public static final int ITEM = 0;
    public static final int EMPTY = 1;
    public static final int MORE = 2;

    public ArrayList<Episode> queue;
    private DatabaseAdapter dbAdapter;

    public LinearLayout view;
    private ImageLoader imageLoader;

    private QueueListener listener;
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
                null, false);
        card.findViewById(R.id.card_menu).setVisibility(View.INVISIBLE);
        card.findViewById(R.id.card_star).setVisibility(View.INVISIBLE);
        view.addView(card);

        return view;
    }

    public void initialize(){
        new initializeAsync().execute(true);
    }

    class initializeAsync extends AsyncTask<Boolean, Void, ArrayList<Episode>> {

        @Override
        protected ArrayList<Episode> doInBackground(Boolean... booleans) {
            return fetchNextEpisode(fetchQueue());
        }

        @Override
        protected void onPostExecute(ArrayList<Episode> r){
            queue = r;

            if(queue != null){
                // Queue from database is not empty add, MORE Item
                queue.add(cardMore());
                updateView(queue);
            }else{
                // If Queue are not on the DB
                queue = new ArrayList<Episode>();
                queue.add(cardEmpty());
                updateView(queue);
            }
        }
    }

    private Episode cardEmpty() {
        Episode e = new Episode();
        e.TYPE = EMPTY;
        return e;
    }

    private Episode cardMore() {
        Episode e = new Episode();
        e.TYPE = MORE;
        return e;
    }
    
    public void checkForChanges(){
        new checkForChangesAsync().execute(true);
    }

    class checkForChangesAsync extends AsyncTask<Boolean,Void,ArrayList<Episode>>{

        @Override
        protected ArrayList<Episode> doInBackground(Boolean... booleans) {
            return fetchNextEpisode(fetchQueue());
        }

        @Override
        protected void onPostExecute(ArrayList<Episode> query){
            //Remove the "More" or "Empty" item before comparing with the query
            if(queue.get(queue.size()-1).TYPE == MORE || queue.get(queue.size()-1).TYPE == EMPTY)
                queue.remove(queue.size()-1);

            if(!isEqualEpisodes(queue, query)){
                Log.d(TAG, "Episodes Not Equal, update View");
                queue = query;
                if(queue == null){
                    //If episodes are still NULL add the empty episode
                    queue = new ArrayList<Episode>();
                    queue.add(cardEmpty());
                }else{
                    //Else just add the More episode at the end
                    queue.add(cardMore());
                }
                updateView(queue);
            }

            queue.add(queue.size() == 0 ? cardEmpty() : cardMore());
        }
    }

    public void updateView(ArrayList<Episode> e){
        //Add the items to the Horizontal Scroll View
        int size = e.size() < 8 ? e.size() : 8;
        view.removeAllViews();

        for(int i = 0; i < size; i++){

            Episode episode;
            if(i == size-1){
                episode = e.get(e.size()-1);
            }else{
                episode = e.get(i);
            }

            LinearLayout item = (LinearLayout) View.inflate(context,
                    R.layout.card_horizontal_item, null);

            LinearLayout imgWrapper = (LinearLayout) item.findViewById(R.id.card_image_container);
            ImageView img = (ImageView) item.findViewById(R.id.card_img);

            RelativeLayout bottomContainer = (RelativeLayout) item.findViewById(R.id.card_bottom_content_container);
            TextView name = (TextView) item.findViewById(R.id.card_title_2);
            TextView date = (TextView) item.findViewById(R.id.card_title_1);
            ImageView menu = (ImageView) item.findViewById(R.id.card_menu);

            switch(episode.TYPE){
                case EMPTY:
                    imgWrapper.setTag(i);
                    img.setImageResource(R.drawable.show_empty);
                    name.setText("EMPTY");
                    date.setText("");
                    menu.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setClickable(false);
                    break;
                case MORE:
                    imgWrapper.setTag(i == size -1 ? e.size()-1 : i);
                    img.setImageResource(R.drawable.show_more);
                    name.setText("MORE");
                    date.setText("");
                    menu.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View arg0) {
//                            Intent j = new Intent(context, QueueActivity.class);
//                            startActivity(j);
                            listener.onLoadQueueFragment();
                        }
                    });
                    break;
                case ITEM:
                    imgWrapper.setTag(i);
                    name.setTag(i);

                    int sn = episode.SEASON_NUMBER == -1 ? 0 : episode.SEASON_NUMBER;
                    String s = sn < 10 ? "0"+sn : sn + "";

                    String ep = episode.EPISODE_NUMBER < 10 ? "0" + episode.EPISODE_NUMBER : episode.EPISODE_NUMBER + "";

                    date.setText("S" + s + "E" + ep);
                    name.setText(episode.TITLE);

                    /** Image */
                    String imgUri = ServerUrls.getImageUrl(context,
                            ServerUrls.fixURL(episode.IMAGE_URL));
                    imageLoader.displayImage(imgUri, img, BaseSlidingActivity.optionsWithFadeIn);

                    /** Menu Item Set On Click */
                    bottomContainer.setTag(episode);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(final View v) {

                            /** Initialize PopupMenu Class */
                            PopupMenuCompat popup = PopupMenuCompat.newInstance(context, v);
                            popup.inflate(R.menu.main_queue_menu);
                            popup.setOnMenuItemClickListener(new PopupMenuCompat.OnMenuItemClickListener(){

                                @Override
                                public boolean onMenuItemClick(
                                        android.view.MenuItem item) {

                                    Intent i;
                                    Reminder re;

                                    Episode e = (Episode) v.getTag();
                                    int id = item.getItemId();

                                    switch(id){
                                        case R.id.main_queue_menu_watched:
                                            /** Mark the Episode as watched */
                                            WatchedHelper wHelper = new WatchedHelper(context);
                                            wHelper.markWatched(e.ID);
                                            listener.onWatched();
                                            break;

                                        case R.id.main_queue_menu_episode:
                                            /** Episode Info Activity */
                                            re = new Reminder();
                                            re.EPISODE_ID = e.ID;
                                            re.SERIES_ID = e.SERIES_ID;

                                            i = new Intent(context, SeasonEpisodeActivity.class);
                                            i.putExtra(SeasonEpisodeActivity.REMINDER, re);
                                            context.startActivity(i);
                                            break;
                                        case R.id.main_queue_menu_show:
                                            /** Series Info Activity */
                                            i = new Intent(context, SeriesInfoActivity.class);
                                            i.putExtra(SeriesInfoActivity.ID, e.SERIES_ID);
                                            context.startActivity(i);
                                            break;
                                        case R.id.main_queue_menu_previous:
                                            /** Previous Episode Info Activity */
                                            re = new Reminder();
                                            re.EPISODE_ID = e.PREVIOUS_EPISODE_ID;
                                            re.SERIES_ID = e.SERIES_ID;

                                            i = new Intent(context, SeasonEpisodeActivity.class);
                                            i.putExtra(SeasonEpisodeActivity.REMINDER, re);
                                            context.startActivity(i);
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

            imgWrapper.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    Episode e = queue.get(position);
                    switch(e.TYPE){
                        case ITEM:

                            Reminder re = new Reminder();
                            re.EPISODE_ID = e.ID;
                            re.SERIES_ID = e.SERIES_ID;

                            Intent i = new Intent(context,
                                    SeasonEpisodeActivity.class);
                            i.putExtra(SeasonEpisodeActivity.REMINDER, re);
                            context.startActivity(i);
                            break;
                        case EMPTY:
                            break;
                        case MORE:
//                            Intent j = new Intent(context, QueueActivity.class);
//                            startActivity(j);
                            listener.onLoadQueueFragment();
                            break;
                    }
                }
            });
            view.addView(item);
        }
    }

    /**
     * Database Queries and Utility Functions
     * For Queue ListView
     */
    private ArrayList<Episode> fetchQueue(){

        dbAdapter.open();
        ArrayList<String> idList = dbAdapter.fetchQueueAll();
        if(idList != null){

            ArrayList<Episode> items = new ArrayList<Episode>();

            for(int i = 0; i < idList.size(); i++){
                Episode e = (Episode) dbAdapter.fetchEpisode(idList.get(i));
                Series s = (Series) dbAdapter.fetchSeries(e.SERIES_ID);


                if( e != null && s != null){

                    // Replace Episode Image with Series Image
                    e.IMAGE_URL = s.POSTER_URL;

                    // Add to items arraylist
                    items.add(e);

                }
            }

            // just return items in the order they were sent... not sorted by title
            // (hopefully) they will be in LIFO order
            //return sortByTitle(items);
            dbAdapter.close();
            return items;
        }else{
            dbAdapter.close();
            return null;
        }
    }

    private ArrayList<Episode> fetchNextEpisode(ArrayList<Episode> i) {

        ArrayList<Episode> list = new ArrayList<Episode>();
        Episode e;
        Series s;

        if(i != null){
            //Go through each episode and fetch the next episode if available
            for(int j = 0; j < i.size(); j++){
                //Check if there is an Next episode available for the same season
                dbAdapter.open();
                e = dbAdapter.fetchEpisodeBySeasonAndEpisodeNumber(i.get(j).SERIES_ID, i.get(j).SEASON_NUMBER, i.get(j).EPISODE_NUMBER + 1);


                if(e != null){
                    //Found an next episode on the same season, fix the Poster Url
                    s = dbAdapter.fetchSeries(e.SERIES_ID);
                    e.IMAGE_URL = s.POSTER_URL;
                    e.PREVIOUS_EPISODE_ID = i.get(j).ID;
                    list.add(e);
                }else{

                    e = dbAdapter.fetchEpisodeBySeasonAndEpisodeNumber(i.get(j).SERIES_ID, i.get(j).SEASON_NUMBER + 1, 1);

                    if(e != null){
                        //Found the next episode to be the first episode of the next season, fix the Poster url
                        s = dbAdapter.fetchSeries(e.SERIES_ID);
                        e.IMAGE_URL = s.POSTER_URL;
                        e.PREVIOUS_EPISODE_ID = i.get(j).ID;
                        list.add(e);
                    }
                }
                dbAdapter.close();
            }
            return list;
        }else{
            return null;
        }

    }

    private boolean isEqualEpisodes(ArrayList<Episode> a, ArrayList<Episode> b){

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

    public void setImageLoader(ImageLoader loader){
        this.imageLoader = loader;
    }

//    @Override
//    public void onAttach(Activity activity){
//        super.onAttach(activity);
//
////        if(activity instanceof QueueListener){
////            listener = (QueueListener) activity;
////        }else{
////            throw new ClassCastException(activity.toString()
////                    + " must implement QueueMainFragment.QueueListener");
////        }
//
//    }

    public void setListener(QueueListener l){
        this.listener = l;
    }

    public interface QueueListener{
        public void onWatched();
        public void onLoadQueueFragment();
    }
}

















