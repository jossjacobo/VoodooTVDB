package voodoo.tvdb.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.google.inject.Injector;

import roboguice.RoboGuice;
import voodoo.tvdb.sharedPreferences.DataStore;
import voodoo.tvdb.utils.CustomTypefaceSpan;

/**
 * Created by PUTITO-TV on 10/30/13.
 */
public class BaseFragment extends Fragment {

    public ActionBarActivity context;
    public DataStore dataStore;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        this.context = (ActionBarActivity) getActivity();

        Injector i = RoboGuice.getBaseApplicationInjector(context.getApplication());
        dataStore = i.getInstance(DataStore.class);
    }

    public void setActionBarTitle(String title){
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        CustomTypefaceSpan bold = new CustomTypefaceSpan("", font);
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.setSpan(bold, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        context.getSupportActionBar().setTitle(sb);
    }
}
