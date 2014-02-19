package voodoo.tvdb.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import voodoo.tvdb.R;

/**
 * Created by Voodoo Home on 11/2/13.
 */
public class AboutFragment extends BaseFragment {

    TextView text2;
    TextView text3;
    TextView text4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.page_7, container, false);

        //Action Bar
        setupActionBar();

        // Hide top images and divider
        RelativeLayout topImages = (RelativeLayout) view.findViewById(R.id.tutorial_page7_top_images);
        topImages.setVisibility(LinearLayout.GONE);
        View divider = view.findViewById(R.id.tutorial_page7_top_divider);
        divider.setVisibility(View.GONE);

        //VoodooTVDB.com
        text2 = (TextView) view.findViewById(R.id.tutorial_page7_text2);
        text2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.website)));
                startActivity(i);
            }
        });

        //JossJacobo@gmail.com
        text3 = (TextView) view.findViewById(R.id.tutorial_page7_text4);
        text3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //This is a hack to only send the intent to EMAIL applications
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse( "mailto:" + getString(R.string.email) +
                                "?subject=" + Uri.encode(getString(R.string.subject)) +
                                "&body=" + Uri.encode("")));
                startActivity(Intent.createChooser(intent, "your chooser title"));
            }});

        //@VoodooXTC
        text4 = (TextView) view.findViewById(R.id.tutorial_page7_text6);
        setColor(text4,new String[]{"@VoodooXTC"}, getResources().getColor(R.color.blue));
        text4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://twitter.com/intent/user?screen_name=@" + getString(R.string.twitter_username)));
                startActivity(i);
            }
        });

        return view;
    }

    private void setupActionBar() {
        context.getSupportActionBar().setDisplayShowCustomEnabled(false);
        context.getSupportActionBar().setDisplayShowTitleEnabled(true);
        context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context.getSupportActionBar().setHomeButtonEnabled(true);
        context.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        context.getSupportActionBar().setIcon(R.drawable.icon);

        setActionBarTitle(getResources().getString(R.string.sliding_menu_about));
    }

    private void setColor(TextView view, String[] subtext, int color) {
        view.setText(view.getText().toString(), TextView.BufferType.SPANNABLE);

        Spannable str = (Spannable) view.getText();
        for(String sub: subtext){
            int index = view.getText().toString().indexOf(sub);
            str.setSpan(new ForegroundColorSpan(color), index, index + sub.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}
