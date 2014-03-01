package voodoo.tvdb.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Injector;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import roboguice.RoboGuice;
import voodoo.tvdb.R;
import voodoo.tvdb.alarmServices.ReminderManager;
import voodoo.tvdb.fragments.AboutFragment;
import voodoo.tvdb.fragments.DashboardFragment;
import voodoo.tvdb.fragments.FavoritesFragment;
import voodoo.tvdb.fragments.LoginFragment;
import voodoo.tvdb.fragments.QueueFragment;
import voodoo.tvdb.fragments.RegisterFragment;
import voodoo.tvdb.fragments.TimelineFragment;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.sharedPreferences.DataStore;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;
import voodoo.tvdb.utils.CustomTypefaceSpan;
import voodoo.tvdb.utils.UserFunctions;

/**
 * Created by Voodoo Home on 9/28/13.
 */
public class BaseSlidingActivity extends ActionBarActivity implements View.OnClickListener {

    public DataStore dataStore;
    
    public ImageLoader imageLoader = ImageLoader.getInstance();
    public static DisplayImageOptions optionsWithFadeIn;
    public static DisplayImageOptions optionsWithoutFadeIn;

    public FrameLayout contentView;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private MenuListAdapter adapter;

    private TextView username;
    private TextView login;

    private DatabaseAdapter db;

    public int currentFrag = 0;

    /** Ads */
    private boolean ads = false;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        setContentView(R.layout.voodoo_main);

        Injector i = RoboGuice.getBaseApplicationInjector(this.getApplication());
        dataStore = i.getInstance(DataStore.class);

        contentView = (FrameLayout) findViewById(R.id.content_container);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer_listview);

        db = new DatabaseAdapter(this);

        setActionBarTitle(getResources().getString(R.string.app_name));
        setupSlidingMenu();
        initializeImageLoader();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
    }

    private void setupSlidingMenu() {
        drawerList.addHeaderView(getDrawerHeader());
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        adapter = new MenuListAdapter(
                this,
                getResources().getStringArray(R.array.drawer_list_items),
                getResources().getStringArray(R.array.drawer_list_items_headers));
        drawerList.setAdapter(adapter);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.app_name,
                R.string.app_name
        ){
            public void onDrawerClosed(View view){

            }
            public void onDrawerOpened(View view){

            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private View getDrawerHeader() {

        LinearLayout header = (LinearLayout) LayoutInflater.from(BaseSlidingActivity.this).inflate(R.layout.drawer_list_header, null);
        ImageView avatar = (ImageView) header.findViewById(R.id.drawer_list_header_avatar);
        username = (TextView) header.findViewById(R.id.drawer_list_header_user);
        login = (TextView) header.findViewById(R.id.drawer_list_header_logInOut);
        TextView goPro = (TextView) header.findViewById(R.id.drawer_list_header_go_pro);

        avatar.setOnClickListener(this);
        username.setOnClickListener(this);
        login.setOnClickListener(this);
        goPro.setOnClickListener(this);

        setHeaderContent();

        return header;
    }

    private void setHeaderContent() {
        //Set Text for UserName & LoginAndOut Field
        UserFunctions uf = new UserFunctions(this);

        if(uf.isUserLoggedIn()){

            db.open();
            HashMap<String,String> user = db.getUserDetails();
            db.close();

            username.setText(user.get("username"));
            username.setTag("username");
            login.setText("Logout");
            login.setTag("logout");

        }else{
            username.setText("Login");
            username.setTag("login");
            login.setText("Register");
            login.setTag("register");
        }
    }

    public void selectFragment(View view, int nextFrag){
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
                case R.id.login_fragment:
                    frag = new LoginFragment();
                    break;
                case R.id.register_fragment:
                    frag = new RegisterFragment();
                    break;
            }

            ft.replace(view.getId(), frag);
            ft.commit();
            currentFrag = nextFrag;
        }
    }

    @Override
    public void onClick(View view) {
        Intent i;
        String tag;

        switch(view.getId()){
            case R.id.drawer_list_header_user:
                tag = view.getTag().toString();
                if(tag.equals("username")){
                    Toast.makeText(this, "User Profile Coming Soon", Toast.LENGTH_SHORT).show();
                }else if(tag.equals("login")){
                    selectFragment(contentView, R.id.login_fragment);
                }
                break;
            case R.id.drawer_list_header_logInOut:
                tag = view.getTag().toString();
                if(tag.equals("logout")){
                    // Log out user
                    UserFunctions uf = new UserFunctions(this);
                    if(uf.isUserLoggedIn()){
                        // Log out user and set User content again
                        uf.logoutUser();
                        setHeaderContent();
                    }
                }else if(tag.equals("register")){
                    selectFragment(contentView, R.id.register_fragment);
                }
                break;
            case R.id.drawer_list_header_go_pro:
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=voodoo.tvdb.key"));
                startActivity(i);

                break;
        }

        if(drawerLayout.isDrawerOpen(drawerList)){
            drawerLayout.closeDrawer(drawerList);
        }
    }

    public class MenuListAdapter extends BaseAdapter implements View.OnClickListener{

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_HEADER = 1;
        private static final int TYPE_MAX_COUNT = TYPE_HEADER + 1;

        private Context context;
        private ArrayList<String> titles;
        private ArrayList<String> headers;
        private LayoutInflater inflater;
        private String hint;

        private class ViewHolder{
            LinearLayout container;
            TextView title;
        }


        public MenuListAdapter(Context c, String[] t, String[] h){
            this.context = c;
            this.titles = new ArrayList<String>(Arrays.asList(t));
            this.headers = new ArrayList<String>(Arrays.asList(h));
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.hint = t[0];
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public int getViewTypeCount(){
            return TYPE_MAX_COUNT;
        }

        @Override
        public int getItemViewType(int position){
            return headers.contains(titles.get(position)) ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public Object getItem(int i) {
            return titles.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public void setHint(String hint){
            this.hint = hint;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                holder = new ViewHolder();
                switch(getItemViewType(i)){
                    case TYPE_HEADER:
                        view = inflater.inflate(R.layout.drawer_list_item_header, viewGroup, false);
                        break;
                    default:
                        view = inflater.inflate(R.layout.drawer_list_item, viewGroup, false);
                        break;
                }
                holder.container = (LinearLayout) view.findViewById(R.id.drawer_list_item_container);
                holder.title = (TextView) view.findViewById(R.id.drawer_list_item_title);

                holder.title.setTag(i);
                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }

            // Title
            switch (getItemViewType(i)){
                case TYPE_HEADER:
                    holder.title.setText(titles.get(i));
                    holder.title.setTextColor(getResources().getColor(R.color.sliding_menu_title));
                    break;
                default:
                    holder.title.setText(titles.get(i));
                    if(hint.equals(titles.get(i))){
                        holder.title.setTextColor(getResources().getColor(R.color.blue));
                    }else{
                        holder.title.setTextColor(getResources().getColor(R.color.sliding_menu_text_color_selector));
                    }
                    break;
            }

            // On Click Listener
            holder.container.setOnClickListener(getItemViewType(i) == TYPE_HEADER ? null : this);

            return view;
        }

        @Override
        public void onClick(View view) {
            if(drawerLayout.isDrawerOpen(drawerList)){
                drawerLayout.closeDrawer(drawerList);
            }

            int position = (Integer)(view.findViewById(R.id.drawer_list_item_title).getTag());
            String title = titles.get(position);
            setHint(title);

            if(title.equals("Main")){
                selectFragment(contentView, R.id.dashboard_fragment);
            }else if(title.equals("Favorites")){
                selectFragment(contentView, R.id.favorites_fragment);
            }else if(title.equals("Timeline")){
                selectFragment(contentView, R.id.timeline_fragment);
            }else if(title.equals("Queue")){
                selectFragment(contentView, R.id.queue_fragment);
            }else if(title.equals("Sync")){
                // TODO disable this button...after clicked...or while sync going on.
                ReminderManager manager = new ReminderManager(context);
                manager.setNowService();
            }else if(title.equals("Settings")){
                startActivity(new Intent(context, Prefs.class));
            }else if(title.equals("Contact Me")){
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + context.getString(R.string.email) +
                                "?subject=" + Uri.encode(context.getString(R.string.subject)) +
                                "&body=" + Uri.encode("")));
                startActivity(Intent.createChooser(intent, "Email"));
            }else if(title.equals("About")){
                selectFragment(contentView, R.id.about_fragment);
            }
        }
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
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().setIcon(R.drawable.icon);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getSupportActionBar().setIcon(R.drawable.logo);
                return false;
            }
        });


        return true;
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

