package net.phoenix.imageviewex;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Autore: rock3r
 * Creato il 27/05/12 alle 19.33
 */
public class GifTestActivity extends Activity {

    private static final String TAG = GifTestActivity.class.getSimpleName();

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_gif);

        // Displays some stats about density
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        TextView textview = (TextView)findViewById(R.id.textview);
        textview.setText(String.format("Density: %f; DensityDpi: %d; ScaledDensity: %f; Pixel size: %d x %d",
                dm.density, dm.densityDpi, dm.scaledDensity, dm.widthPixels, dm.heightPixels));

        imageView = (ImageView)findViewById(R.id.imageView);


    }
}
