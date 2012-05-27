package net.phoenix.imageviewex;

import net.phoenix.remote.RemoteLoader;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class ImageViewNextActivity extends Activity {
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
        
        // Disables animation, behaving like a regular ImageView,
        // except you can still set byte[] as the source
        // ImageViewEx.setCanAlwaysAnimate(false);
        
        // Sets a default density for all of the images in each ImageViewEx.
        // ImageViewEx.setClassLevelDensity(DisplayMetrics.DENSITY_MEDIUM);
        
        // Sets a density for the img5 only.
        // Changing the density after an object has been set will
        // do nothing, you will have to re-set the object.
        // img5.setDensity(DisplayMetrics.DENSITY_LOW);
        
        img1 = (ImageViewNext)findViewById(R.id.imageViewNext1);
        img2 = (ImageViewNext)findViewById(R.id.imageViewNext2);
        img3 = (ImageViewNext)findViewById(R.id.imageViewNext3);
        img4 = (ImageViewNext)findViewById(R.id.imageViewNext4);
        img5 = (ImageViewNext)findViewById(R.id.imageViewNext5);
        
        // Create the Loader
        RemoteLoader loader = new RemoteLoader(getApplicationContext(), true);
        // Set the loader to the ImageViewNext
        ImageViewNext.setClassLoader(loader);
        
        // Sets the class density to HDPI
        ImageViewNext.setClassLevelDensity(DisplayMetrics.DENSITY_HIGH);
        // Sets the sources of ImageViewNexts from URL
        img1.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");
        img2.setUrl("http://img.italiansubs.net/news2/data/The%20Simpsons/Stagione%2023/the.simpsons.s23e22.gif");
        img3.setUrl("http://img.italiansubs.net/news2/data/Game%20of%20Thrones/Stagione%202/Game.of.Thrones.S02E08.gif");
        img4.setUrl("http://www.italiansubs.net/forum/Smileys/default/suicidiosenzafronzoli.gif");
        // img5.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");
    }
}