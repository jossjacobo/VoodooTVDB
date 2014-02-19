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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import voodoo.tvdb.activity.SeasonEpisodeActivity;
import voodoo.tvdb.activity.SeriesInfoActivity;
import voodoo.tvdb.activity.BaseSlidingActivity;
import voodoo.tvdb.objects.Reminder;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by PUTITO-TV on 10/11/13.
 */
public class UpcomingMainFragment {

    private static final String TAG = "UpcomingAsync";

    // Item Types
    public static final int ITEM = 0;
    public static final int EMPTY = 1;
    public static final int MORE = 2;

    public DatabaseAdapter dbAdapter;
    public ArrayList<Reminder> reminders;

    public LinearLayout view;
    public ImageLoader imageLoader;

    private UpcomingListener listener;
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

    class initializeAsync extends AsyncTask<Boolean, Void, ArrayList<Reminder>> {

        @Override
        protected ArrayList<Reminder> doInBackground(Boolean... booleans) {

            return lookForRemindersInSD();
        }

        @Override
        protected void onPostExecute(ArrayList<Reminder> r){
            reminders = r;

            if(reminders != null){
                //Reminders from database is not empty add, MORE Item
                reminders.add(cardMore());
                updateView(reminders);
            }else{
                // If Reminders are not on the DB
                reminders = new ArrayList<Reminder>();
                reminders.add(cardEmpty());
                updateView(reminders);
            }
        }
    }


    public void checkForChanges(){
        new checkForChangesAsync().execute(true);
    }

    class checkForChangesAsync extends AsyncTask<Boolean,Void,ArrayList<Reminder>>{

        @Override
        protected ArrayList<Reminder> doInBackground(Boolean... booleans) {
            return lookForRemindersInSD();
        }

        @Override
        protected void onPostExecute(ArrayList<Reminder> query){
            //Remove the "More" or "Empty" item before comparing with the query
            if(reminders.get(reminders.size()-1).TYPE == MORE || reminders.get(reminders.size()-1).TYPE == EMPTY)
                reminders.remove(reminders.size()-1);

            if(!isEqualReminders(reminders,query)){
                reminders = query;
                //Check again which to add, if the "More" or "Empty" Item
                if(reminders == null){
                    //Add Empty View
                    reminders = new ArrayList<Reminder>();
                    reminders.add(cardEmpty());
                }else{
                    reminders.add(cardMore());
                }
                updateView(reminders);
            }

            reminders.add(reminders.size() == 0 ? cardEmpty() : cardMore());
        }
    }

    private void updateView(ArrayList<Reminder> r) {

        //Add the items to the Horizontal Scroll View
        int size = r.size() < 8 ? r.size() : 8;
        view.removeAllViews();

        for(int i = 0; i < size; i++){

            Reminder reminder;
            if(i == size-1){
                reminder = r.get(r.size()-1);
            }else{
                reminder = r.get(i);
            }

            LinearLayout item = (LinearLayout) View.inflate(context,
                    R.layout.card_horizontal_item, null);

            LinearLayout imgWrapper = (LinearLayout) item.findViewById(R.id.card_image_container);
            ImageView img = (ImageView) item.findViewById(R.id.card_img);

            RelativeLayout bottomContainer = (RelativeLayout) item.findViewById(R.id.card_bottom_content_container);
            TextView name = (TextView) item.findViewById(R.id.card_title_2);
            TextView date = (TextView) item.findViewById(R.id.card_title_1);
            ImageView menu = (ImageView) item.findViewById(R.id.card_menu);

            switch(reminder.TYPE){
                case EMPTY:
                    imgWrapper.setTag(i);
                    img.setImageResource(R.drawable.show_empty);
                    name.setText("EMPTY");
                    date.setText("");
                    menu.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setClickable(false);

                    break;
                case MORE:
                    imgWrapper.setTag(i == size -1 ? reminders.size()-1 : i);
                    img.setImageResource(R.drawable.show_more);
                    name.setText("MORE");
                    date.setText("");
                    menu.setVisibility(ImageView.INVISIBLE);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context,"TIMELINEACTIVITY",Toast.LENGTH_SHORT).show();
                            //Intent j = new Intent(context, TimelineActivity.class);
                            //context.startActivity(j);
                        }
                    });

                    break;
                case ITEM:

                    imgWrapper.setTag(i);
                    name.setTag(i);

                    // Item Name
                    name.setText(reminder.EPISODE_NAME);

                    // Season Number and Episode Number
                    int sn = reminder.SEASON_NUMBER == -1 ? 0 : reminder.SEASON_NUMBER;
                    String s = sn < 10 ? "0"+sn : sn + "";
                    String ep = reminder.EPISODE_NUMBER < 10 ? "0" + reminder.EPISODE_NUMBER :
                            reminder.EPISODE_NUMBER + "";
                    date.setText("S" + s + "E" + ep);

                    // Fix Image URL and Display it
                    String imgUri = ServerUrls.getImageUrl(context,
                            ServerUrls.fixURL(reminder.IMAGE_URL));
                    imageLoader.displayImage(imgUri, img, BaseSlidingActivity.optionsWithFadeIn);

                    /** Menu Item Set On Click */
                    bottomContainer.setTag(reminder);
                    bottomContainer.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(final View v) {
                            /** Initialize PopupMenu Class */
                            PopupMenuCompat popup = PopupMenuCompat.newInstance(context, v);
                            popup.inflate(R.menu.main_upcoming_menu);
                            popup.setOnMenuItemClickListener(new PopupMenuCompat.OnMenuItemClickListener(){
                                @Override
                                public boolean onMenuItemClick(android.view.MenuItem item) {
                                    Reminder reminder = (Reminder) v.getTag();
                                    int id = item.getItemId();
                                    switch(id){
                                        case R.id.main_upcoming_menu_episode:
                                            Intent i = new Intent(context,
                                                    SeasonEpisodeActivity.class);
                                            i.putExtra(SeasonEpisodeActivity.REMINDER, reminder);
                                            context.startActivity(i);
                                            break;
                                        case R.id.main_upcoming_menu_show:
                                            /** Series Info Activity */
                                            Intent intent = new Intent(context,
                                                    SeriesInfoActivity.class);
                                            intent.putExtra(SeriesInfoActivity.ID, reminder.SERIES_ID);
                                            context.startActivity(intent);
                                            break;
                                    }
                                    return true;
                                }
                            });
                            popup.show();
                        }
                    });
            }

            imgWrapper.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    Reminder r = reminders.get(position);
                    switch(r.TYPE){
                        case ITEM:
                            Intent i = new Intent(context, SeasonEpisodeActivity.class);
                            i.putExtra(SeasonEpisodeActivity.REMINDER, r);
                            context.startActivity(i);
                            break;
                        case EMPTY:
                            break;
                        case MORE:
                            // TODO change fragment
//                            Intent j = new Intent(context, TimelineActivity.class);
//                            context.startActivity(j);
                            listener.onLoadTimelineFragment();
                            break;
                    }
                }
            });
            view.addView(item);
        }
    }

    private Reminder cardEmpty() {
        Reminder r = new Reminder();
        r.TYPE = EMPTY;
        return r;
    }

    private Reminder cardMore() {
        Reminder r = new Reminder();
        r.TYPE = MORE;
        return r;
    }

    /**
     * Database Queries and Utility Functions
     * For Upcoming episodes
     */
    private ArrayList<Reminder> lookForRemindersInSD() {

        dbAdapter.open();
        ArrayList<Reminder> q = dbAdapter.fetchAllReminders();
        dbAdapter.close();

        if(q != null){
            q = sortByCalendar(q);
            q = removeOlderReminders(q);
            return q;
        }
        return null;
    }

    private ArrayList<Reminder> removeOlderReminders(ArrayList<Reminder> R) {
        if(R != null && R.size() >= 1){
            Calendar currentTime = Calendar.getInstance();
            //Log.d(TAG, "currentTime: " + currentTime.getTime().toGMTString());

            currentTime.add(Calendar.DAY_OF_MONTH, -1);
            //Log.d(TAG, "currentTime: " + currentTime.getTime().toGMTString());

            ArrayList<Reminder> temp = new ArrayList<Reminder>();
            //Log.d(TAG, "Reminders size = " + R.size());

            for(int i = R.size()-1; i >= 0; i--){
                Calendar date = getDate(R.get(i));
                //Log.d(TAG, "episode air date = " + getDate(R.get(i)).getTime().toGMTString());
                if(date.after(currentTime)){
                    //Log.d(TAG, "episode is in the future");
                    temp.add(R.get(i));
                }else{
                    break;
                }
            }
            Collections.reverse(temp);
            return temp;
        }
        return null;
    }
    private ArrayList<Reminder> sortByCalendar(ArrayList<Reminder> R){
        if(R != null){
            Comparator<Reminder> comparator = new Comparator<Reminder>(){
                @Override
                public int compare(Reminder object1, Reminder object2) {
                    Calendar object1Calendar = getDate(object1);
                    Calendar object2Calendar = getDate(object2);
                    return object1Calendar.compareTo(object2Calendar);
                }
            };
            if(R.size() > 0){
                Collections.sort(R, comparator);
            }
        }
        return R;
    }
    private Calendar getDate(Reminder reminder){

        String dateString;
        Calendar d = Calendar.getInstance();
        if(reminder.TIME != null && reminder.DATE != null && reminder.TIME.length() == 8){
            SimpleDateFormat sdf = new SimpleDateFormat("KK:mmayyyy-MM-dd");
            dateString = reminder.TIME + reminder.DATE;
            try {
                d.setTime(sdf.parse(dateString.replace(" ", "")));
            } catch (ParseException e) {
                e.printStackTrace();
                d.setTime(new Date(1100, 0, 0));
            }
        }else if(reminder.DATE != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateString = reminder.DATE;
            try {
                d.setTime(sdf.parse(dateString));
            } catch (ParseException e) {
                e.printStackTrace();
                d.setTime(new Date(1100, 0, 0));
            }
        }else{
            d = null;
        }
        return d;
    }

    private boolean isEqualReminders(ArrayList<Reminder> a, ArrayList<Reminder> b){

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

    public void setListener(UpcomingListener l){
        this.listener = l;
    }

    public interface UpcomingListener{
        public void onLoadTimelineFragment();
    }
}
