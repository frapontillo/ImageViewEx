package net.phoenix.imageviewex;

import net.phoenix.remote.RemoteLoader;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class ImageViewNextActivity extends Activity {
    // Create the Loader
    private static RemoteLoader loader = null;
    
    private ImageViewNext img1;
	private ImageViewNext img2;
	private ImageViewNext img3;
	private ImageViewNext img4;
	private ImageViewNext img5;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next);
           
        // Displays some stats about density
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        TextView textview = (TextView)findViewById(R.id.textview);
        textview.setText(String.format("Density: %f; DensityDpi: %d; ScaledDensity: %f; Pixel size: %d x %d",
        dm.density, dm.densityDpi, dm.scaledDensity, dm.widthPixels, dm.heightPixels));
        
        // Of a loader has never been set up, create one with disk cache enabled as well
        // This should go in your Application class, so it should be treated as a singleton,
        // even if it's not.
        if (loader == null) loader = new RemoteLoader(getApplicationContext(), true);
        // Set the loader to the ImageViewNext class, so that every instance will
        // share the same pool and cache.
        // This should be done in your Application class, so that you won't do the repeat
        // the same code for every instance.
        ImageViewNext.setClassLoader(loader);
        // Sets the loading/error drawables (can be animated drawables!!!) for every instance
        // of the class.
        ImageViewNext.setClassErrorDrawable(R.drawable.empty_newsthumb);
        ImageViewNext.setClassLoadingDrawable(R.drawable.loader);
        
        img1 = (ImageViewNext)findViewById(R.id.imageViewNext1);
        img2 = (ImageViewNext)findViewById(R.id.imageViewNext2);
        img3 = (ImageViewNext)findViewById(R.id.imageViewNext3);
        img4 = (ImageViewNext)findViewById(R.id.imageViewNext4);
        img5 = (ImageViewNext)findViewById(R.id.imageViewNext5);
        
        // Sets the class density to HDPI
        // ImageViewNext.setClassLevelDensity(DisplayMetrics.DENSITY_HIGH);
        // Sets the first image density to medium (bigger than the others)
        // img1.setDensity(DisplayMetrics.DENSITY_MEDIUM);
        
        // Sets the sources of ImageViewNexts from URL
        img1.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");
        img2.setUrl("http://img.italiansubs.net/news2/data/The%20Simpsons/Stagione%2023/the.simpsons.s23e22.gif_for_ERROR");
        img3.setUrl("http://img.italiansubs.net/news2/data/Game%20of%20Thrones/Stagione%202/Game.of.Thrones.S02E08.gif");
        img4.setUrl("http://www.italiansubs.net/forum/Smileys/default/suicidiosenzafronzoli.gif");
        img5.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");
    }
}