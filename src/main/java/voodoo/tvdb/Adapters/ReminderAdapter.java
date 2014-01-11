package voodoo.tvdb.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import voodoo.tvdb.Activity.SeasonEpisodeActivity;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.R;
import voodoo.tvdb.Utils.ServerUrls;

@SuppressLint("SimpleDateFormat")
public class ReminderAdapter extends BaseAdapter implements View.OnClickListener {

    //Item Types
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

    //Context & Activity
    private Context context;

    //Inflater
    private LayoutInflater inflater = null;

    //Image Loader
    public ImageLoader imageLoader;
    private DisplayImageOptions optionsWithDelay;
    private DisplayImageOptions optionsWithoutDelay;

    //List of items and separators
    private ArrayList<Integer> separatorSet;
    private ArrayList<Reminder> items;

    public ReminderAdapter(Context context){

        this.context = context;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        items = new ArrayList<Reminder>();
        separatorSet = new ArrayList<Integer>();

        imageLoader = ImageLoader.getInstance();

        optionsWithDelay = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.stub)
                .showImageForEmptyUri(R.drawable.stub_not_found)
                .showImageOnFail(R.drawable.stub_not_found)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(1000))
                .build();

        optionsWithoutDelay = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.stub)
                .showImageForEmptyUri(R.drawable.stub_not_found)
                .showImageOnFail(R.drawable.stub_not_found)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
    }

    public void addItem(final Reminder item){
        items.add(item);
        notifyDataSetChanged();
    }

    public void addSeparatorItem(final Reminder item){
        items.add(item);
        //save the separator position
        separatorSet.add(items.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position){
        return separatorSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount(){
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        if(items != null){
            return items.size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(ArrayList<Reminder> reminders){
        this.items = new ArrayList<Reminder>();

        if(reminders != null){
            //Add Separators according to date
            reminders = addSeparators(reminders);

            for(int i = 0; i < reminders.size(); i++){
                if(reminders.get(i).TYPE == Reminder.TYPE_ITEM){
                    addItem(reminders.get(i));
                }else if(reminders.get(i).TYPE == Reminder.TYPE_SEPARATOR){
                    addSeparatorItem(reminders.get(i));
                }

            }
        }
    }

    private ArrayList<Reminder> addSeparators(ArrayList<Reminder> reminders) {

        // FIRST TEST add "Yesterday", "Today", and "Tomorrow" separators
        Calendar today = Calendar.getInstance();
        today.clear(Calendar.HOUR);
        today.clear(Calendar.HOUR_OF_DAY);
        today.clear(Calendar.MINUTE);
        today.clear(Calendar.SECOND);
        today.clear(Calendar.MILLISECOND);
        //Log.d(TAG, "Today " + today.getTime().toString());

        Calendar yesterday = Calendar.getInstance();
        yesterday.clear(Calendar.HOUR);
        yesterday.clear(Calendar.HOUR_OF_DAY);
        yesterday.clear(Calendar.MINUTE);
        yesterday.clear(Calendar.SECOND);
        yesterday.clear(Calendar.MILLISECOND);
        yesterday.roll(Calendar.DAY_OF_MONTH, false);
        //Log.d(TAG, "yesterday " + yesterday.getTime().toString());

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.clear(Calendar.HOUR);
        tomorrow.clear(Calendar.HOUR_OF_DAY);
        tomorrow.clear(Calendar.MINUTE);
        tomorrow.clear(Calendar.SECOND);
        tomorrow.clear(Calendar.MILLISECOND);
        tomorrow.roll(Calendar.DAY_OF_MONTH, true);
        //Log.d(TAG, "tomorrow " + tomorrow.getTime().toString());

        Calendar previousDivider = Calendar.getInstance();

        for(int i = 0; i < reminders.size(); i++){
            Calendar reminderDate = getDate(reminders.get(i));
            //Log.d(TAG, "reminderDate " + reminderDate.getTime().toString());

            if(yesterday.equals(reminderDate)){
                if(!reminderDate.equals(previousDivider)){
                    //Add Yesterday divider
                    Reminder r = new Reminder();
                    r.TYPE = Reminder.TYPE_SEPARATOR;
                    r.DATE = reminders.get(i).DATE;
                    r.DIVIDER_TEXT = "Yesterday";

                    reminders.add(i, r);
                    i++;

                    //Set previousDivider as yesterday
                    previousDivider = reminderDate;

                }
            }else if(today.equals(reminderDate)){
                if(!reminderDate.equals(previousDivider)){
                    //Add Yesterday divider
                    Reminder r = new Reminder();
                    r.TYPE = Reminder.TYPE_SEPARATOR;
                    r.DATE = reminders.get(i).DATE;
                    r.DIVIDER_TEXT = "Today";

                    reminders.add(i, r);
                    i++;

                    //Set previousDivider as yesterday
                    previousDivider = reminderDate;

                }
            }else if(tomorrow.equals(reminderDate)){
                if(!reminderDate.equals(previousDivider)){
                    //Add Yesterday divider
                    Reminder r = new Reminder();
                    r.TYPE = Reminder.TYPE_SEPARATOR;
                    r.DATE = reminders.get(i).DATE;
                    r.DIVIDER_TEXT = "Tomorrow";

                    reminders.add(i, r);
                    i++;

                    //Set previousDivider as yesterday
                    previousDivider = reminderDate;

                }
            }else{
                if(!reminderDate.equals(previousDivider)){
                    //Add Yesterday divider
                    Reminder r = new Reminder();
                    r.TYPE = Reminder.TYPE_SEPARATOR;
                    r.DATE = reminders.get(i).DATE;

                    reminders.add(i, r);
                    i++;

                    //Set previousDivider as yesterday
                    previousDivider = reminderDate;

                }
            }

        }
        return reminders;
    }
    @SuppressLint("SimpleDateFormat")
    private Calendar getDate(Reminder r){
        String dateString;
        Calendar d = Calendar.getInstance();
        if(r.DATE != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateString = r.DATE;
            try{
                d.setTime(sdf.parse(dateString));
            }catch(ParseException e){
                e.printStackTrace();
                d.setTime(new Date(1100, 0, 0));
            }
        }else{
            d = null;
        }
        return d;
    }

    class ViewHolder{
        public TextView reminderDateAndTime;
        public TextView reminderEpisodeName;
        public TextView reminderSeriesName;
        public ImageView reminderImage;
        public TextView reminderSeasonAndEpisodeNumber;
        public TextView reminderSeparator;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        Reminder reminder = items.get(position);
        int type = getItemViewType(position);

        if(convertView == null){
            holder = new ViewHolder();
            switch(type){
                case TYPE_ITEM:
                    vi = inflater.inflate(R.layout.reminder_item, parent, false);

                    holder.reminderDateAndTime = (TextView) vi.findViewById(R.id.reminder_date_and_time);
                    holder.reminderEpisodeName = (TextView) vi.findViewById(R.id.reminder_episode_name);
                    holder.reminderSeriesName = (TextView) vi.findViewById(R.id.reminder_series_name);
                    holder.reminderImage = (ImageView) vi.findViewById(R.id.reminder_image);
                    holder.reminderSeasonAndEpisodeNumber = (TextView) vi.findViewById(R.id.reminder_episode_season_and_episode_number);

                    vi.setOnClickListener(this);
                    break;
                case TYPE_SEPARATOR:

                    vi = inflater.inflate(R.layout.divider, null);
                    vi.setBackgroundColor(context.getResources().getColor(R.color.white));

                    holder.reminderSeparator = (TextView) vi.findViewById(R.id.divider_text);
                    holder.reminderSeparator.setTextColor(context.getResources().getColor(R.color.blue));
                    holder.reminderSeparator.setTextSize(20);
                    holder.reminderSeparator.setPadding(8, 4, 8, 4);

                    break;
            }

            vi.setTag(holder);
        }else{
            holder = (ViewHolder) vi.getTag();
        }

        switch(type){
            case TYPE_ITEM:
                holder.reminderDateAndTime.setTag(reminder);

                String dateNtime = reminder.TIME != null ? reminder.TIME + " " : "";
                dateNtime += reminder.DATE != null? dateFormat(reminder.DATE) : "";
                holder.reminderDateAndTime.setText(dateNtime);
                holder.reminderEpisodeName.setText(reminder.EPISODE_NAME);
                holder.reminderSeriesName.setText(reminder.SERIES_NAME);

                /** Image Stuff */
                String url = ServerUrls.getImageUrl(context, ServerUrls.fixURL(reminder.IMAGE_URL));

                if(!MemoryCacheUtil.findCachedBitmapsForImageUri(url, imageLoader.getMemoryCache()).isEmpty()){
                    imageLoader.displayImage(url, holder.reminderImage, this.optionsWithoutDelay);
                }else{
                    imageLoader.displayImage(url, holder.reminderImage, this.optionsWithDelay);
                }

                //Set Season and Episode Number (i.e. S01E15)
                int sn = reminder.SEASON_NUMBER == -1 ? 0 : reminder.SEASON_NUMBER;
                String s = sn < 10 ? "0"+sn : sn + "";
                String e = reminder.EPISODE_NUMBER < 10 ? "0" + reminder.EPISODE_NUMBER : reminder.EPISODE_NUMBER + "";
                holder.reminderSeasonAndEpisodeNumber.setText("S" + s + "E" + e);

                break;
            case TYPE_SEPARATOR:
                holder.reminderSeparator.setText(reminder.DIVIDER_TEXT != null ?
                        reminder.DIVIDER_TEXT :
                        reminder.DATE != null ? dateFormat(reminder.DATE) : "");
                break;
        }

        return vi;
    }

    @SuppressLint("SimpleDateFormat")
    private String dateFormat(String first_Aired) {
        if(first_Aired != null){
            SimpleDateFormat s = new SimpleDateFormat("MMMM dd, yyyy");
            SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd");

            try {
                return s.format(ss.parse(first_Aired));
            } catch (ParseException e) {
                e.printStackTrace();
                return first_Aired;
            }
        }else{
            return first_Aired;
        }

    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(context, SeasonEpisodeActivity.class);
        i.putExtra("REMINDER", (Reminder) v.findViewById(R.id.reminder_date_and_time).getTag());
        context.startActivity(i);
    }
}
