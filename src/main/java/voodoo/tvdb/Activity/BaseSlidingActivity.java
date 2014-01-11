package voodoo.tvdb.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Locale;

import javax.inject.Inject;

import voodoo.tvdb.Fragments.SlidingMenuFragment;
import voodoo.tvdb.R;
import voodoo.tvdb.SharedPreferences.DataStore;
import voodoo.tvdb.Utils.CustomTypefaceSpan;

/**
 * Created by Voodoo Home on 9/28/13.
 */
public class BaseSlidingActivity extends SlidingFragmentActivity implements SlidingMenu.OnClosedListener,
    SlidingMenu.OnOpenedListener{

    private static final String TAG = "BaseSlidingActivity";
    public boolean blocked = false;
    public boolean home = false;

    @Inject
    public DataStore dataStore;
    
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public static DisplayImageOptions optionsWithFadeIn;
    public static DisplayImageOptions optionsWithoutFadeIn;

    public SlidingMenuFragment slidingMenuFragment;

    /** Ads */
    private boolean ads = false;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        setContentView(R.layout.voodoo_main);
        setBehindContentView(R.layout.menu_frame);

        //contentView = (FrameLayout) findViewById(R.id.content_container);

        setActionBarTitle(getResources().getString(R.string.app_name));
        setupSlidingMenu();
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
        sb.setSpan(light,0,title.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(sb);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupSlidingMenu() {
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setShadowWidthRes(R.dimen.slide_navigation_shadow_width);
        getSlidingMenu().setShadowDrawable(R.drawable.shadow);
        getSlidingMenu().setBehindOffsetRes(R.dimen.slide_navigation_behind_offset);
        getSlidingMenu().setFadeDegree(0.35f);
        getSlidingMenu().setOnClosedListener(this);
        getSlidingMenu().setOnOpenedListener(this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        slidingMenuFragment = new SlidingMenuFragment();
        ft.replace(R.id.menu_frame, slidingMenuFragment);
        ft.commit();
    }

    public void setSlideNavHint(int fragId){
        slidingMenuFragment.setSlideNavHint(fragId);
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

    @Override
    public void onClosed() {
        blocked = false;
    }

    @Override
    public void onOpened() {
        blocked = true;
    }

    public static String slugify(String input) throws UnsupportedEncodingException {
        if(input == null || input.length() == 0){
            return "";
        }
        String toReturn = Normalizer.normalize(input, Normalizer.Form.NFD);
        toReturn = toReturn.replaceAll("^\\p{ASCII}]", "");
        toReturn = toReturn.replace(" ", "-");
        toReturn = toReturn.toLowerCase(Locale.ENGLISH);
        toReturn = URLEncoder.encode(toReturn, "UTF-8");
        return toReturn;
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



















