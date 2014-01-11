package voodoo.tvdb.Fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;

import voodoo.tvdb.R;
import voodoo.tvdb.Utils.CustomTypefaceSpan;

/**
 * Created by PUTITO-TV on 10/30/13.
 */
public class BaseListFragment extends RoboSherlockListFragment {

    public Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites,
                container, false);
        this.context = getSherlockActivity();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getListView().setCacheColorHint(getActivity().getResources().getColor(R.color.transparent));
    }

    public void setActionBarTitle(String title){
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        CustomTypefaceSpan bold = new CustomTypefaceSpan("", font);
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.setSpan(bold, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        getSherlockActivity().getSupportActionBar().setTitle(sb);
    }
}
