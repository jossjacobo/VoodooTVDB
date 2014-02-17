package voodoo.tvdb.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import voodoo.tvdb.R;

/**
 * Created by Voodoo Home on 9/28/13.
 */
public class SlideNaveItem extends RelativeLayout {

    public int itemId;
    TextView textView;

    public SlideNaveItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClickable(true);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackground(context.getResources().getDrawable(R.drawable.card_bottom_container_bg_selector));

        //LayoutInflater.from(context).inflate(R.layout.slide_menu_item, this, true);
    }

}
