package voodoo.tvdb.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdView;

import roboguice.inject.InjectView;
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
        DashboardFragment.DashboardListner, TimelineFragment.TimelineListener, LoginFragment.LoginListener,
        RegisterFragment.RegisterListener{

    private static final String TAG = "MainActivity";

    @InjectView(R.id.content_container)
    FrameLayout contentView;

    int currentFrag = 0;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        selectFragment(contentView, R.id.dashboard_fragment);

        // Ads
        AdView adview = (AdView) findViewById(R.id.adView);
        viewAds(adview);
    }

    private void selectFragment(View view, int nextFrag){
        if(currentFrag != nextFrag){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment frag = new Fragment();

            switch (nextFrag){
                case R.id.dashboard_fragment:
                    frag = new DashboardFragment();
                    break;
                case R.id.favorites_fragment:
                    frag = new FavoritesFragment();
                    break;
                case R.id.timeline_fragment:
                    frag = new TimelineFragment();
                    break;
                case R.id.queue_fragment:
                    frag = new QueueFragment();
                    break;
                case R.id.about_fragment:
                    frag = new AboutFragment();
                    break;
                case R.id.sli_menu_profile_username:
                    frag = new LoginFragment();
                    break;
                case R.id.sli_menu_profile_loginAndOut:
                    frag = new RegisterFragment();
                    break;
            }

            ft.replace(view.getId(), frag);
            ft.commit();
            currentFrag = nextFrag;

            setSlideNavHint(nextFrag);
            if(getSlidingMenu().isMenuShowing()){
                toggle();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                toggle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    public void onSetupTimelineActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.icon);

        setActionBarTitle(getResources().getString(R.string.timeline));
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
