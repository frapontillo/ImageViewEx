package net.frakbot.imageviewex.test;

import net.frakbot.imageviewex.Converters;
import net.frakbot.imageviewex.ImageViewEx;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class ImageViewExActivity extends Activity {
    private ImageViewEx img1;
	private ImageViewEx img2;
	private ImageViewEx img3;
	private ImageViewEx img4;
	private ImageViewEx img5;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
           
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
        // img5.setInDensity(DisplayMetrics.DENSITY_LOW);
        
        img1 = (ImageViewEx)findViewById(R.id.imageViewEx1);
        img2 = (ImageViewEx)findViewById(R.id.imageViewEx2);
        img3 = (ImageViewEx)findViewById(R.id.imageViewEx3);
        img4 = (ImageViewEx)findViewById(R.id.imageViewEx4);
        img5 = (ImageViewEx)findViewById(R.id.imageViewEx5);

        // Sets the sources of ImageViewExs as byte arrays
        img1.setSource(Converters.assetToByteArray(getAssets(), "Episodes_thumb.png"));
        img2.setSource(Converters.assetToByteArray(getAssets(), "Lost_anim.gif"));
        img3.setSource(Converters.assetToByteArray(getAssets(), "Lost_thumb.png"));
        img4.setSource(Converters.assetToByteArray(getAssets(), "Simpsons_anim.gif"));
        img5.setSource(Converters.assetToByteArray(getAssets(), "suicidiosenzafronzoli.gif"));
    }
}