package net.phoenix.imageviewex;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * An OpenGL rework of ImageView that handles any kind of image already supported
 * by ImageView, plus animated GIF images.
 *
 * @author Sebastiano Poggi
 * @author Francesco Pontillo
 */
public class ImageViewExOGL extends GLSurfaceView {

    private static final String TAG = ImageViewExOGL.class.getSimpleName();
    private static boolean canAlwaysAnimate = true;
    private float mScale;

    private static final int IMAGE_SOURCE_UNKNOWN = -1;
    private static final int IMAGE_SOURCE_RESOURCE = 0;
    private static final int IMAGE_SOURCE_DRAWABLE = 1;
    private static final int IMAGE_SOURCE_BITMAP = 2;
    private static final int IMAGE_SOURCE_GIF = 2;

    private int mImageSource;

    private Options mOptions;
    private int overriddenDensity = -1;
    private static int overridenClassDensity = -1;

    private int _frameDuration = 67;
    private Handler handler = new Handler();
    private Thread updater;
    private Drawable mDrawable;
    private final GlRenderer mRenderer;

    ///////////////////////////////////////////////////////////
    ///                  CONSTRUCTORS                       ///
    ///////////////////////////////////////////////////////////

    /**
     * Creates an instance for the class.
     *
     * @param context The context to instantiate the object for.
     */
    public ImageViewExOGL(Context context) {
        super(context);

        if(mOptions == null)
            setInDensity(getDensity());

        mRenderer = new GlRenderer(context);
        setRenderer(mRenderer);
    }

    /**
     * Creates an instance for the class.
     *
     * @param context The context to initialize the instance into.
     * @param attrs   The parameters to initialize the instance with.
     */
    public ImageViewExOGL(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(mOptions == null)
            setInDensity(getDensity());

        mRenderer = new GlRenderer(context);
        setRenderer(mRenderer);
    }

    /**
     * Helper to setOptions(android.graphics.BitmapFactory.Options) that simply sets the inDensity for
     * loaded image.
     * Currently not used.
     *
     * @param inDensity The input density. This should be the current (or desired)
     *                  screen density.
     */
    public void setInDensity(int inDensity) {
        if (mOptions == null) {
            Log.d(TAG, "BitmapFactory Options not created yet. Creating...");
            mOptions = new Options();
            mOptions.inDither = true;
            mOptions.inScaled = true;
            mOptions.inTargetDensity = getContext().getResources().getDisplayMetrics().densityDpi;
            mOptions.inDensity = inDensity;
        }
    }


    static class SavedState extends BaseSavedState {
        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    ///////////////////////////////////////////////////////////
    ///                 PUBLIC SETTERS                      ///
    ///////////////////////////////////////////////////////////

    /**
     * Initalizes the inner variable describing the kind of resource attached to the ImageViewEx.
     */
    public void initializeDefaultValues() {
        setTag(null);
        mImageSource = IMAGE_SOURCE_UNKNOWN;
    }

    /**
     * Sets the image from a byte array.
     *
     * @param src The byte array containing the image to set into the ImageViewEx.
     */
    public void setSource(byte[] src) {
        initializeDefaultValues();

        // Sets the image as a regular Drawable
        Drawable d = Converters.byteArrayToDrawable(src, mOptions, getContext());
        this.setSource(d);
        mImageSource = IMAGE_SOURCE_UNKNOWN;

        this.measure(0, 0);
        this.requestLayout();
        //mRenderer.setBitmap(Converters.byteArrayToBitmap(src, mOptions));
    }

    /**
     * {@inheritDoc}
     */
    public void setSource(int resId) {
        initializeDefaultValues();

        mDrawable = getResources().getDrawable(resId);
        mImageSource = IMAGE_SOURCE_RESOURCE;

        this.measure(0, 0);
        this.requestLayout();
        //mRenderer.setBitmap(Converters.drawableToBitmap(mDrawable, mOptions));
    }

    /**
     * {@inheritDoc}
     */
    public void setSource(Drawable drawable) {
        initializeDefaultValues();

        mDrawable = drawable;
        mImageSource = IMAGE_SOURCE_DRAWABLE;

        this.measure(0, 0);
        this.requestLayout();
        //mRenderer.setBitmap(Converters.drawableToBitmap(mDrawable, mOptions));
    }

    /**
     * {@inheritDoc}
     */
    public void setSource(Bitmap bm) {
        initializeDefaultValues();

        mDrawable = new BitmapDrawable(getResources(), bm);
        mImageSource = IMAGE_SOURCE_BITMAP;

        this.measure(0, 0);
        this.requestLayout();
        //mRenderer.setBitmap(bm);
    }

    ///////////////////////////////////////////////////////////
    ///                 PUBLIC GETTERS                      ///
    ///////////////////////////////////////////////////////////

    /**
     * Gets the Drawable that ImageViewEx is displaying, if any.
     *
     * @return Returns the Drawable that ImageViewEx is displaying,
     *         or null if it's playing an animated Gif or if it's
     *         not displaying anything.
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * Gets the set density of the view, given by the screen density or by value
     * overridden with overrideDensity(int).
     * If the density was not overridden and it can't be retrieved by the context,
     * it simply returns the DENSITY_HIGH constant.
     *
     * @return int representing the current set density of the view.
     */
    public int getDensity() {
        int density;

        // Check for an overridden instance density
        // and if there's none, get the one from the display

        // If the instance density was not overridden, get the one from the display
        if (overriddenDensity == -1) {
            DisplayMetrics metrics = new DisplayMetrics();

            if (!(getContext() instanceof Activity)) {
                density = DisplayMetrics.DENSITY_HIGH;
            } else {
                Activity activity = (Activity) getContext();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                density = metrics.densityDpi;
            }
        } else {
            // Else the density is the overridden one
            density = overriddenDensity;
        }


        return density;
    }

    ///////////////////////////////////////////////////////////
    ///                  EVENT HANDLERS                     ///
    ///////////////////////////////////////////////////////////

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setInDensity(getDensity());

        // If needed, get the scaling factor
        mScale = 1.0f;
        if (mOptions.inScaled) {
            mScale = (float) mOptions.inTargetDensity / mOptions.inDensity;
            if (mScale < 0.1f) mScale = 0.1f;
            if (mScale > 5.0f) mScale = 5.0f;
        }

        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    ///////////////////////////////////////////////////////////
    ///                  PRIVATE HELPERS                    ///
    ///////////////////////////////////////////////////////////

    /**
     * Determines the width of this View
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {

            result = Math.round(mDrawable.getIntrinsicWidth() * mScale) + getPaddingLeft() + getPaddingRight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            if(mDrawable != null)
                result = Math.round(mDrawable.getIntrinsicHeight() * mScale) + getPaddingTop() + getPaddingBottom();
            else
                result = getPaddingTop() + getPaddingBottom();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }
}
