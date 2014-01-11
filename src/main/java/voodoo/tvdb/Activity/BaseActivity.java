package voodoo.tvdb.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;

import javax.inject.Inject;

import voodoo.tvdb.R;
import voodoo.tvdb.SharedPreferences.DataStore;
import voodoo.tvdb.Utils.CustomTypefaceSpan;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class BaseActivity extends RoboSherlockActivity {

    @Inject
    public DataStore dataStore;

    public ImageLoader imageLoader = ImageLoader.getInstance();
    public static DisplayImageOptions optionsWithFadeIn;
    public static DisplayImageOptions optionsWithoutFadeIn;

    /** Ads */
    private boolean ads = false;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        setActionBarTitle(getResources().getString(R.string.app_name));
        initializeImageLoader();
    }

    private void initializeImageLoader() {
        /** Initial Default Configurations and Options */
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisc(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();
        /** Image Default Config */
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(1)
                .threadPriority(Thread.MIN_PRIORITY)
                        //.memoryCache(new LRULimitedMemoryCache(8 * 1024 * 1024))
                .discCache(new UnlimitedDiscCache(getCacheDir(this)))
                        //.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .memoryCache(new WeakMemoryCache())
                .defaultDisplayImageOptions(options)
                        //.enableLogging()
                .build();
        /** ImageLoader Initialize Config */
        ImageLoader.getInstance().init(config);
        /** ImageLoader Options with Fade In Delay */
        optionsWithFadeIn = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.stub)
                .showImageForEmptyUri(R.drawable.stub_not_found)
                .showImageOnFail(R.drawable.stub_not_found)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(1000))
                .build();
        /** ImageLoader Options without Fade In Delay */
        optionsWithoutFadeIn = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.stub)
                .showImageForEmptyUri(R.drawable.stub_not_found)
                .showImageOnFail(R.drawable.stub_not_found)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
    }

    public void setActionBarTitle(String title){
        setupActionBar();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        CustomTypefaceSpan light = new CustomTypefaceSpan("",font);
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.setSpan(light,0,title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(sb);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        // Search Menu Item
        MenuItem search = menu.add("Search").setIcon(R.drawable.ic_menu_search_holo_light);
        search.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onSearchRequested();
                return true;
            }
        });
        return true;
    }

    /** Get Cache Directory */
    public static File getCacheDir(Context context){

        File cacheDir;
        //Find the directory to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(), context.getPackageName() + "/_cache");
        else
            cacheDir=context.getCacheDir();

        if(!cacheDir.exists())
            cacheDir.mkdirs();

        return cacheDir;
    }

    public boolean viewAds(View view){
        view.setVisibility( !isProInstalled(this) ? View.VISIBLE : View.GONE);
        return ads;
    }

    /** Is Pro version installed */
    protected boolean isProInstalled(Context context){
        PackageManager manager = context.getPackageManager();
        if( manager.checkSignatures(context.getPackageName(), "voodoo.tvdb.key") == PackageManager.SIGNATURE_MATCH){
            // Pro Key installed, and signatures match
            return true;
        }
        return false;
    }
}
