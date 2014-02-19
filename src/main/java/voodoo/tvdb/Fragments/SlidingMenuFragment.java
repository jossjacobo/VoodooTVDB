package voodoo.tvdb.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import roboguice.inject.InjectView;
import voodoo.tvdb.alarmServices.ReminderManager;
import voodoo.tvdb.preferences.Prefs;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.UserFunctions;
import voodoo.tvdb.sqlitDatabase.DatabaseAdapter;

/**
 * Created by Voodoo Home on 9/28/13.
 */
public class SlidingMenuFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SlidingMenuFragment";

    @InjectView(R.id.sli_menu_profile_username)
    private TextView username;
    @InjectView(R.id.sli_menu_profile_loginAndOut)
    private TextView loginAndOut;
    @InjectView(R.id.sli_menu_profile_go_pro)
    private TextView goPro;
    @InjectView(R.id.sli_menu_sync)
    private TextView sync;
    @InjectView(R.id.sli_menu_settings)
    private TextView settings;
    @InjectView(R.id.sli_menu_contact)
    private TextView contact;
    @InjectView(R.id.dashboard_fragment)
    public TextView main;

    @InjectView(R.id.favorites_fragment)
    private TextView favorites;
    @InjectView(R.id.timeline_fragment)
    private TextView timeline;
    @InjectView(R.id.queue_fragment)
    private TextView queue;
    @InjectView(R.id.about_fragment)
    private TextView about;

    DatabaseAdapter db;

    private SlidingMenuListener listener;
    private View v;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.sliding_menu_items, null);
        db = new DatabaseAdapter(getActivity());

        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUserContent();

        main.setOnClickListener(this);
        favorites.setOnClickListener(this);
        timeline.setOnClickListener(this);
        queue.setOnClickListener(this);
        sync.setOnClickListener(this);
        settings.setOnClickListener(this);
        about.setOnClickListener(this);
        contact.setOnClickListener(this);
        username.setOnClickListener(this);
        loginAndOut.setOnClickListener(this);

        goPro.setOnClickListener(this);
        goPro.setVisibility( !isProInstalled(getActivity()) ? View.VISIBLE : View.GONE);

        main.setTextColor(getResources().getColor(R.color.blue));
    }

    @Override
    public void onResume(){
        super.onResume();
        setUserContent();
    }

    public void setUserContent(){

        //Set Text for UserName & LoginAndOut Field
        UserFunctions uf = new UserFunctions(getActivity());

        if(uf.isUserLoggedIn()){
            db.open();
            HashMap<String,String> user = db.getUserDetails();
            db.close();
            username.setText(user.get("username"));
            username.setTag("username");
            loginAndOut.setText("Logout");
            loginAndOut.setTag("logout");
        }else{
            username.setText("Login");
            username.setTag("login");
            loginAndOut.setText("Register");
            loginAndOut.setTag("register");
        }
    }

    @Override
    public void onClick(View view) {

        Context context = getActivity();
        Intent i;
        String tag;

        switch(view.getId()){
            case R.id.sli_menu_profile_username:
                tag = view.getTag().toString();
                if(tag.equals("username")){
                    Toast.makeText(getActivity(), "Launch User Profile", Toast.LENGTH_SHORT).show();
                }else if(tag.equals("login")){
                    listener.onFragmentChanged(view);
                }
                break;
            case R.id.sli_menu_profile_loginAndOut:
                tag = view.getTag().toString();
                if(tag.equals("logout")){
                    // Log out user
                    UserFunctions uf = new UserFunctions(SlidingMenuFragment.this.getActivity());
                    if(uf.isUserLoggedIn()){
                       // Log out user and set User content again
                       uf.logoutUser();
                       setUserContent();
                        //Toast and hide sliding menu
                        //Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
                        //if(getisMenuShowing())
                        //	sm.toggle();
                   }
                }else if(tag.equals("register")){
                    listener.onFragmentChanged(view);
//                    i = new Intent(context, RegisterActivity.class);
//                    startActivity(i);
                }
                break;
            case R.id.sli_menu_profile_go_pro:
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=voodoo.tvdb.key"));
                startActivity(i);

                break;

            case R.id.dashboard_fragment:
                listener.onFragmentChanged(view);
                break;
            case R.id.favorites_fragment:
                listener.onFragmentChanged(view);
                break;
            case R.id.timeline_fragment:
                listener.onFragmentChanged(view);
                break;
            case R.id.queue_fragment:
                listener.onFragmentChanged(view);
                break;

            case R.id.sli_menu_sync:
                // TODO disable this button...after clicked...or while sync going on.
                ReminderManager manager = new ReminderManager(context);
                manager.setNowService();
                break;
            case R.id.sli_menu_settings:
                startActivity(new Intent(context, Prefs.class));
                break;
            case R.id.about_fragment:
                listener.onFragmentChanged(view);
                break;
            case R.id.sli_menu_contact:
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + context.getString(R.string.email) +
                                "?subject=" + Uri.encode(context.getString(R.string.subject)) +
                                "&body=" + Uri.encode("")));
                startActivity(Intent.createChooser(intent, "Email"));
                break;
        }
    }

    private boolean isProInstalled(Context context){
        PackageManager manager = context.getPackageManager();
        if( manager.checkSignatures(context.getPackageName(), "voodoo.tvdb.key") == PackageManager.SIGNATURE_MATCH){
            // Pro Key installed, and signatures match
            return true;
        }
        return false;
    }

    public void setSlideNavHint(int nextFrag) {
        if(isAdded()){
            removeNavHints();
            int blue = getResources().getColor(R.color.blue);
            switch (nextFrag){
                case R.id.dashboard_fragment:
                    main.setTextColor(blue);
                    break;
                case R.id.favorites_fragment:
                    favorites.setTextColor(blue);
                    break;
                case R.id.timeline_fragment:
                    timeline.setTextColor(blue);
                    break;
                case R.id.queue_fragment:
                    queue.setTextColor(blue);
                    break;
                case R.id.about_fragment:
                    about.setTextColor(blue);
                    break;
                case R.id.sli_menu_profile_loginAndOut:
                    loginAndOut.setTextColor(blue);
                    break;
                case R.id.sli_menu_profile_username:
                    username.setTextColor(blue);
                    break;
            }
        }
    }

    private void removeNavHints() {
        int black = getResources().getColor(R.color.black);
        main.setTextColor(black);
        favorites.setTextColor(black);
        timeline.setTextColor(black);
        queue.setTextColor(black);
        about.setTextColor(black);
        loginAndOut.setTextColor(black);
        username.setTextColor(black);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        if(activity instanceof SlidingMenuListener){
            listener = (SlidingMenuListener) activity;
        }else{
            throw new ClassCastException(activity.toString()
                    + " must implement SlidingMenuFragment.SlidingMenuListener");
        }

    }

    public interface SlidingMenuListener{
        public void onFragmentChanged(View view);
    }
}
