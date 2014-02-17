package voodoo.tvdb.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResizeableImageView extends ImageView {

	public ResizeableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		Drawable d = getDrawable();
		
		if(d != null){
			
			// Ceiling not round - avoid thin vertical gaps along the left/right
			// edges
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = (int) Math.ceil( (float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
			setMeasuredDimension(width, height);
			
		}else{
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			
		}
		
	}

}
