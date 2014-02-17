package voodoo.tvdb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.google.inject.Injector;

import javax.inject.Inject;

import roboguice.RoboGuice;
import voodoo.tvdb.activity.MainActivity;
import voodoo.tvdb.SharedPreferences.DataStore;

public class SplashActivity extends Activity {

    @Inject
    DataStore dataStore;

    private static final int MSG_FIRST_LAUNCH = 0, MSG_NOT_FIRST_LAUNCH = 1,
        SPLASH_TIME = 1400;

    private final Handler splashHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
             Intent i = new Intent();
             switch(msg.what){
                 case MSG_FIRST_LAUNCH:
                     // TODO Download the first time "What's Hot" shows
                     i = new Intent(SplashActivity.this, MainActivity.class);
                     break;
                 case MSG_NOT_FIRST_LAUNCH:
                     i = new Intent(SplashActivity.this, MainActivity.class);
                     break;
             }
             startActivity(i);
             overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
             finish();

             super.handleMessage(msg);
         }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Injector i = RoboGuice.getBaseApplicationInjector(this.getApplication());
        dataStore = i.getInstance(DataStore.class);

        Message msg = new Message();
        msg.what = dataStore.getFirstRun() ? MSG_FIRST_LAUNCH : MSG_NOT_FIRST_LAUNCH;

        splashHandler.sendMessageDelayed(msg,SPLASH_TIME);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = this.getWindow();

        // Eliminates color banding
        window.setFormat(PixelFormat.RGBA_8888);
    }

}