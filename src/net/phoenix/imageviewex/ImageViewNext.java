package net.phoenix.imageviewex;

import com.github.ignition.support.cache.AbstractCache;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Extension of the ImageViewEx that handles the download and caching of 
 * images and animated GIFs using ignition's caching and downloading system.
 * 
 * @author Francesco Pontillo
 * @author Sebastiano Poggi
 * 
 */
@SuppressWarnings("rawtypes")
public class ImageViewNext extends ImageViewEx {
	private AbstractCache cache = null;
	private static AbstractCache classCache = null;
	
	private Drawable loadingD;
	private Drawable errorD;
	private static Drawable classLoadingD;
	private static Drawable classErrorD;

	/**
	 * Sets the loading {@link Drawable} to be used for every {@link ImageViewNext}.
	 * @param classLoadingDrawable 	the {@link Drawable} to display after an error
	 * 								getting an image.
	 */
	public static void setClassLoadingDrawable(Drawable classLoadingDrawable) {
		classLoadingD = classLoadingDrawable;
	}
	
	/**
	 * Sets the error {@link Drawable} to be used for every {@link ImageViewNext}.
	 * @param classErrorDrawable the {@link Drawable} to display waiting for an image.
	 */
	public static void setClassErrorDrawable(Drawable classErrorDrawable) {
		classErrorD = classErrorDrawable;
	}
	
	/**
	 * Sets an {@link AbstractCache} to use for the caching and retrieving
	 * of images in this particular instance.
	 * 
	 * @param cache {@link AbstractCache} to use for this instance.
	 */
	public void setCache(AbstractCache cache) {
		this.cache = cache;
	}

	/**
	 * Sets an {@link AbstractCache} to use for the caching and retrieving
	 * of images in all of the instances of the class.
	 * 
	 * @param cache {@link AbstractCache} to use for this class.
	 */
	public static void setClassCache(AbstractCache cache) {
		ImageViewNext.classCache = cache;
	}
	
	/**
	 * Gets the current cache set for this ImageViewEx. If a local cache has
	 * not been set, it returns the global cache.
	 * 
	 * @return the cache used for this ImageViewNext, or null if none has been set.
	 */
	public AbstractCache getCache() {
		return (cache == null ? classCache : cache);
	}
	
	/**
	 * Checks if there is a cache for this ImageViewNext.
	 * 
	 * @return true if a cache has been set, false otherwise.
	 */
	public boolean isCacheSet() {
		return (getCache() != null);
	}
	
	// TODO: carry on.
	
	/**
	 * {@inheritDoc}
	 */
	public ImageViewNext(Context context) {
		super(context);
	}
}
