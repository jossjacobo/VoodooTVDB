package voodoo.tvdb.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.google.ads.AdView;

import voodoo.tvdb.Fragments.AboutFragment;
import voodoo.tvdb.Fragments.DashboardFragment;
import voodoo.tvdb.Fragments.FavoritesFragment;
import voodoo.tvdb.Fragments.LoginFragment;
import voodoo.tvdb.Fragments.QueueFragment;
import voodoo.tvdb.Fragments.RegisterFragment;
import voodoo.tvdb.Fragments.SlidingMenuFragment;
import voodoo.tvdb.Fragments.TimelineFragment;
import voodoo.tvdb.R;

/**
 * Created by Voodoo Home on 9/28/13.
 */
public class MainActivity extends BaseSlidingActivity implements SlidingMenuFragment.SlidingMenuListener,
        DashboardFragment.DashboardListner, LoginFragment.LoginListener,
        RegisterFragment.RegisterListener{

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        selectFragment(contentView, R.id.dashboard_fragment);

        // Ads
        AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
    }

    @Override
    public void onBackPressed(){
        if(currentFrag != R.id.dashboard_fragment){
            selectFragment(contentView, R.id.dashboard_fragment);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onFragmentChanged(View view) {
        selectFragment(contentView, view.getId());
    }

    @Override
    public void onHome(boolean isHome) {
        home = isHome;
    }

    @Override
    public void onLoadQueue() {
        selectFragment(contentView, R.id.queue_fragment);
    }

    @Override
    public void onLoadTimeline() {
        selectFragment(contentView, R.id.timeline_fragment);
    }

    @Override
    public void onLogin() {
        selectFragment(contentView, R.id.dashboard_fragment);
    }

    @Override
    public void onRegisterClicked() {
        selectFragment(contentView, R.id.sli_menu_profile_loginAndOut);
    }

    @Override
    public void onRegister() {
        selectFragment(contentView, R.id.dashboard_fragment);
    }

    @Override
    public void onLoginClicked() {
        selectFragment(contentView, R.id.sli_menu_profile_username);
    }
}
