package net.frakbot.imageviewex.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import net.frakbot.imageviewex.ImageAlign;
import net.frakbot.imageviewex.ImageViewNext;

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
        
        // Set A LOT of maximum concurrent threads:
        // this is an exaggeration, please don't do this! :D
        ImageViewNext.setMaximumNumberOfThreads(100);
        
        setContentView(R.layout.next);
           
        // Displays some stats about density
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        TextView textview = (TextView)findViewById(R.id.textview);
        textview.setText(String.format("Density: %f; DensityDpi: %d; ScaledDensity: %f; Pixel size: %d x %d",
        dm.density, dm.densityDpi, dm.scaledDensity, dm.widthPixels, dm.heightPixels));

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
        img1.setDensity(DisplayMetrics.DENSITY_LOW);
        
        // Sets the sources of ImageViewNexts from URL
        img1.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");
        img2.setUrl("http://img.italiansubs.net/news2/data/The%20Simpsons/Stagione%2023/the.simpsons.s23e22.gif");
        img3.setUrl("http://img.italiansubs.net/news2/data/Game%20of%20Thrones/Stagione%202/Game.of.Thrones.S02E08.gif");
        img4.setUrl("http://www.italiansubs.net/forum/Smileys/default/suicidiosenzafronzoli.gif");
        img5.setUrl("http://img.italiansubs.net/news2/data/Lost/Stagione%202/Lost.s02e04-05-06.gif");

        // img1.setImageAlign(ImageAlign.TOP);
    }
}