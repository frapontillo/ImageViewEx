package net.phoenix.imageviewex;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class ImageViewExOGLActivity extends Activity {
    private ImageViewExOGL img;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_ogl);
           
        // Displays some stats about density
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        TextView textview = (TextView)findViewById(R.id.textview);
        textview.setText(String.format("Density: %f; DensityDpi: %d; ScaledDensity: %f; Pixel size: %d x %d",
        dm.density, dm.densityDpi, dm.scaledDensity, dm.widthPixels, dm.heightPixels));
        
        img = (ImageViewExOGL)findViewById(R.id.imageViewExOGL);

        // Sets the sources of ImageViewExs as byte arrays
        img.setSource(R.drawable.test_texture);
    }
}