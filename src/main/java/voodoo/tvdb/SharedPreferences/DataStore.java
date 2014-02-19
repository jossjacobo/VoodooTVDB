package voodoo.tvdb.sharedPreferences;

import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by PUTITO-TV on 9/27/13.
 */

@Singleton
public class DataStore {

    private static final String DEVICE_VERSION = "DeviceVersion";
    private static final String FIRST_START = "first_start";

    @Inject
    EncryptedSharedPreferences encryptedSharedPreferences;

    private SharedPreferences.Editor getEditor() {
        return encryptedSharedPreferences.edit();
    }

    private SharedPreferences getPrefs() {
        return encryptedSharedPreferences;
    }
    public int getVersion() {
        return getPrefs().getInt(DEVICE_VERSION, 0);
    }
    public void persistVersion(int version) {
        getEditor().putInt(DEVICE_VERSION, version).commit();
    }

    public boolean getFirstRun(){
        return getPrefs().getBoolean(FIRST_START,true);
    }

    public void persistFirstRun(){
        getEditor().putBoolean(FIRST_START,false).commit();
    }

    /**
    public void clearLoginUser(){
        getEditor().remove(USER);
    }

    public boolean getUserLoginToken(){
        return getPrefs().getBoolean(USER_TOKEN,false);
    }

    public void persistUserLoginToken(){
        getEditor().putBoolean(USER_TOKEN,true).commit();
    }
    */
}
