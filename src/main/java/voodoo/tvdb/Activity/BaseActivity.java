package voodoo.tvdb.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.inject.Injector;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;

import roboguice.RoboGuice;
import voodoo.tvdb.R;
import voodoo.tvdb.sharedPreferences.DataStore;
import voodoo.tvdb.utils.CustomTypefaceSpan;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class BaseActivity extends ActionBarActivity {

    public DataStore dataStore;

    public ImageLoader imageLoader = ImageLoader.getInstance();
    public static DisplayImageOptions optionsWithFadeIn;
    public static DisplayImageOptions optionsWithoutFadeIn;

    /** Ads */
    private boolean ads = false;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        Injector i = RoboGuice.getBaseApplicationInjector(this.getApplication());
        dataStore = i.getInstance(DataStore.class);

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
        getMenuInflater().inflate(R.menu.menu_item_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

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
