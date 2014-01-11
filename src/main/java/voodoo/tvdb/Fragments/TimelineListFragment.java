package voodoo.tvdb.Fragments;

import android.os.Bundle;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import voodoo.tvdb.Adapters.ReminderAdapter;
import voodoo.tvdb.Objects.Reminder;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

public class TimelineListFragment extends BaseListFragment {

    private static final String TAG = "TimelineListFragment";

    public static final String TYPE = "type";
    public static final int TYPE_OLDER = 0;
    public static final int TYPE_UPCOMING = 1;

    ArrayList<Reminder> reminders;
    ArrayList<Reminder> displayReminders;
    ReminderAdapter adapter;

    int type;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        Bundle b = getArguments();
        type = b.getInt(TYPE);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        reminders = lookForRemindersInDB();
        switch (type){
            case TYPE_OLDER:
                displayReminders = getOlder(reminders);
                break;
            case TYPE_UPCOMING:
                displayReminders = getUpcoming(reminders);
                break;
        }

        //Initialize
        adapter = new ReminderAdapter(context);
        adapter.setItems(displayReminders);
        getListView().setAdapter(adapter);

	}
	
	@Override
	public void onResume(){
		super.onResume();
        // TODO Look for changes
	}
	@Override
	public void onPause(){
		super.onPause();
	}

    /**
     * Database Queries and Utility Functions
     */
    private ArrayList<Reminder> lookForRemindersInDB() {
        DatabaseAdapter dbAdapter = new DatabaseAdapter(getSherlockActivity());

        dbAdapter.open();
        reminders = dbAdapter.fetchAllReminders();
        dbAdapter.close();

        if(reminders != null){
            return sortByCalendar(reminders);
        }else{
            return null;
        }
    }
    private ArrayList<Reminder> getUpcoming(ArrayList<Reminder> R){

        if(R != null && R.size() >= 1){
            Calendar currentTime = Calendar.getInstance();
            currentTime.add(Calendar.DAY_OF_MONTH, -1);

            ArrayList<Reminder> temp = new ArrayList<Reminder>();
            for(int i = R.size()-1; i >= 0; i--){
                Calendar date = getDate(R.get(i));
                if(date.after(currentTime)){
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

    private ArrayList<Reminder> getOlder(ArrayList<Reminder> R){
        if(R != null){

            Calendar currentTime = Calendar.getInstance();
            currentTime.set(Calendar.HOUR_OF_DAY, 0);
            currentTime.set(Calendar.MINUTE, 0);
            currentTime.add(Calendar.DAY_OF_MONTH, -1);

            Calendar olderDate = Calendar.getInstance();
            olderDate.set(Calendar.HOUR_OF_DAY, 0);
            olderDate.set(Calendar.MINUTE, 0);
            olderDate.add(Calendar.MONTH, -1);

            //Return the set of episode from today's date to a month back
            ArrayList<Reminder> older = new ArrayList<Reminder>();
            for(int i = 0; i < R.size(); i++){
                Calendar date = getDate(R.get(i));
                if(date.before(currentTime)){
                    if(date.after(olderDate)){
                        older.add(R.get(i));
                    }
                }else{
                    break;
                }
            }

            Collections.reverse(older);
            return older;
        }
        return R;
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
}






























