package voodoo.tvdb.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import voodoo.tvdb.Utils.CustomTypefaceSpan;

/**
 * Created by PUTITO-TV on 10/30/13.
 */
public class BaseFragment extends RoboSherlockFragment {

    SherlockFragmentActivity context;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        this.context = getSherlockActivity();
    }

    public void setActionBarTitle(String title){
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        CustomTypefaceSpan bold = new CustomTypefaceSpan("", font);
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.setSpan(bold, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        getSherlockActivity().getSupportActionBar().setTitle(sb);
    }
}
