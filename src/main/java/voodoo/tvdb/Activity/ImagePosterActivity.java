package voodoo.tvdb.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import oak.widget.SwankyImageView;
import voodoo.tvdb.R;
import voodoo.tvdb.utils.ServerUrls;

/**
 * Created by jossayjacobo on 4/15/14.
 */
public class ImagePosterActivity extends ActionBarActivity {

    public static final String URL = "url";
    public static final String TITLE = "title";

    private ProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_poster_activity);

        SwankyImageView swankyImageView = (SwankyImageView) findViewById(R.id.image_poster);
        progress = (ProgressBar) findViewById(R.id.image_progress);

        String url = getIntent().getStringExtra(URL);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(ServerUrls.getImageUrlOriginal(this, url), swankyImageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ImagePosterActivity.this, "Failed to load Image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ImagePosterActivity.this, "Failed to load Image", Toast.LENGTH_SHORT).show();
            }
        });

        String title = getIntent().getStringExtra(TITLE);
        getSupportActionBar().setTitle(title != null ? title : "Image");
    }
}
