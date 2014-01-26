package voodoo.tvdb.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Window;

import voodoo.tvdb.Adapters.TutorialPagerAdapter;
import voodoo.tvdb.R;

/**
 * Created by PUTITO-TV on 10/9/13.
 */
public class TutorialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.tutorial_view_pager);
        TutorialPagerAdapter adapter = new TutorialPagerAdapter(this);
        ViewPager pager = (ViewPager) findViewById(R.id.pager_tutorial);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);

    }

}
