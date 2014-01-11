package voodoo.tvdb;

import android.app.Application;
import android.content.pm.PackageManager;

import com.google.inject.Inject;
import com.google.inject.Injector;

import roboguice.RoboGuice;
import voodoo.tvdb.SharedPreferences.DataStore;

/**
 * Created by PUTITO-TV on 10/9/13.
 */
public class MainApp extends Application {

    public static String TAG = "Voodoo TVDB";

    @Inject
    DataStore dataStore;

    @Override
    public void onCreate(){
        super.onCreate();

        Injector i = RoboGuice.getBaseApplicationInjector(this);
        dataStore = i.getInstance(DataStore.class);

        // Check for Version Upgrade
        try{
            int newVersion = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
            int oldVersion = dataStore.getVersion();

            if(oldVersion != 0 && oldVersion!= newVersion){
                onVersionUpdate(oldVersion,newVersion);
            }
            dataStore.persistVersion(newVersion);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    private void onVersionUpdate(int oldVersion, int newVersion) {
        // This Method is called when the version code changes,
        // use comparison operators to control migration
    }
}
