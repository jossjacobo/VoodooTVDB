package voodoo.tvdb.Preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import voodoo.tvdb.Activity.TutorialActivity;
import voodoo.tvdb.AlarmServices.ReminderManager;
import voodoo.tvdb.R;
import voodoo.tvdb.Utils.UserFunctions;

public class Prefs extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	/**
	 * Option Names and Defaults values
	 */
	private static final String OPT_UPDATE_TIME = "update_service";
	//private static final String TAG = "Prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceManager.setDefaultValues(Prefs.this, R.xml.preferences, false);

        for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
         initSummary(getPreferenceScreen().getPreference(i));
        }
        
        /**
         * Preference for Clearing Cache
         */
        Preference cacheClear = findPreference("CacheClear");
        cacheClear.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			
        	@Override
			public boolean onPreferenceClick(Preference preference) {
				
        		AlertDialog.Builder builder = new AlertDialog.Builder(Prefs.this);
				builder.setCancelable(true);
				builder.setMessage("Are you sure you want to delete all the images cache?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Clear Cache
						//Log.d(TAG, "Clear Cache");
						ImageLoader imageLoader = ImageLoader.getInstance();
					    imageLoader.clearDiscCache();
					    imageLoader.clearMemoryCache();
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
        	
        });
        
        /**
         * User Profile
         */
        Preference profile = (Preference) findPreference("user_profile");
        profile.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {

				Toast.makeText(Prefs.this, "User Profile Coming Soon", Toast.LENGTH_SHORT).show();
				
				return false;
			}
        	
        });
        
        /**
         * Tutorial
         */
        Preference tutorial = (Preference) findPreference("tutorial");
        tutorial.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {

				//Display tutorial
	        	Intent i = new Intent(Prefs.this, TutorialActivity.class);
	        	startActivity(i);

				return false;
			}
        	
        });
        
        /**
         * Update Frequency
         */
        ListPreference updateFreq = (ListPreference) findPreference("update_frequency");
        updateFreq.setOnPreferenceChangeListener(new OnPreferenceChangeListener (){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				
            	ReminderManager manager = new ReminderManager(Prefs.this);
    			manager.setFavoriteUpdateServiceWithFreq(Integer.valueOf(newValue.toString()));
    			
				return true;
			}
        	
        });
	}
	
	@Override 
    protected void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override 
    protected void onPause() { 
        super.onPause();
        // Unregister the listener whenever a key changes             
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);     
    } 
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { 
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p){
       if (p instanceof PreferenceCategory){
            PreferenceCategory pCat = (PreferenceCategory)p;
            for(int i=0;i<pCat.getPreferenceCount();i++){
                initSummary(pCat.getPreference(i));
            }
        }else{
            updatePrefSummary(p);
        }

    }

    private void updatePrefSummary(Preference p){
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p; 
            p.setSummary(listPref.getEntry() != null ? listPref.getEntry() : listPref.getValue());
            
        }
        
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p; 
            p.setSummary(editTextPref.getText()); 
        }
        
        if(p instanceof Preference){
        	
        	String key = p.getKey();
        	
        	if(key.equals("user_profile")){
        		
        		UserFunctions user = new UserFunctions(this);
				
				if(user.isUserLoggedIn()){
					
					p.setEnabled(true);
					
				}else{
					
					p.setEnabled(false);
					
				}
        		
        	}else if(key.equals("log")){
        		
        		UserFunctions user = new UserFunctions(this);
				
				if(user.isUserLoggedIn()){
					
					p.setEnabled(true);
					
				}else{
					
					p.setEnabled(false);
					
				}
				
        	}
        	
        }
        
		if(p instanceof CheckBoxPreference){
			CheckBoxPreference checkBoxPreference = (CheckBoxPreference) p;
			
			String key = p.getKey();
			
			if(key.equals("network_mode")){
				
				p.setSummary(checkBoxPreference.isChecked() ? "Online Mode" : "Offline Mode");
				
			}else if(key.equals(UserFunctions.KEY_SYNC)){
				
				UserFunctions user = new UserFunctions(this);
				
				if(user.isUserLoggedIn()){
					
					p.setEnabled(true);
					
				}else{
					
					p.setEnabled(false);
					
				}
				
				p.setSummary(checkBoxPreference.isChecked() ? "Keep Device Synchronize" : "Do not keep device synchronized");
				
			}else if(key.equals("drawer")){
				
				p.setSummary(checkBoxPreference.isChecked() ? "Open drawer on launch" : "Do not open drawer on launch");
				
			}
			
		}
		if(p instanceof TimePreference){

			TimePreference timePreference = (TimePreference) p;
			p.setSummary(timePreference.getSetTime());
			
			// Set Reminder
			ReminderManager rm = new ReminderManager(this);
			rm.setFavoriteUpdateService();
		}

    }

	/**
	 * Getter from the items
	 */
	public static String getUpdateTime(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_UPDATE_TIME, "12:00");
	}
	
}




















