package net.phoenix.imageviewex;

import net.phoenix.cache.BytesCache;
import net.phoenix.remote.RemoteLoader;
import net.phoenix.remote.RemoteLoaderHandler;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;

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
	private BytesCache cache = null;
	private static BytesCache classCache = null;
	
	private RemoteLoader loader = null;
	private static RemoteLoader classLoader = null;
	
	private Drawable loadingD;
	private static Drawable classLoadingD;
	
	private Drawable errorD;
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
	 * Sets an {@link BytesCache} to use for the caching and retrieving
	 * of images in this particular instance.
	 * 
	 * @param cache {@link BytesCache} to use for this instance.
	 */
	public void setCache(BytesCache cache) {
		this.cache = cache;
	}

	/**
	 * Sets an {@link BytesCache} to use for the caching and retrieving
	 * of images in all of the instances of the class.
	 * 
	 * @param cache {@link BytesCache} to use for this class.
	 */
	public static void setClassCache(BytesCache cache) {
		ImageViewNext.classCache = cache;
	}
	
	/**
	 * Gets the current cache set for this ImageViewEx. If a local cache has
	 * not been set, it returns the global cache.
	 * 
	 * @return the cache used for this ImageViewNext, or null if none has been set.
	 */
	public BytesCache getCache() {
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
	
	/**
	 * Sets the {@link RemoteLoader} for this instance.
	 * @param loader {@link RemoteLoader} for the current instance.
	 */
	public void setLoader(RemoteLoader loader) {
		this.loader = loader;
	}

	/**
	 * Sets the {@link RemoteLoader} for this class.
	 * @param loader {@link RemoteLoader} for the class.
	 */
	public void setClassLoader(RemoteLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/**
	 * Gets the {@link RemoteLoader} to be used for this instance.
	 * return the {@link RemoteLoader} for the current instance.
	 */
	public RemoteLoader getLoader() {
		return (loader != null ? loader : classLoader);
	}
	
	/**
	 * Checks if a loader exists for this instance.
	 * @return true if there's a loader, false otherwise.
	 */
	public boolean isLoaderSet() {
		return (getLoader() != null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ImageViewNext(Context context) {
		super(context);
	}
	
	/**
	 * Sets the content of the {@link ImageViewNext} with the data to be downloaded
	 * from the provided URL.
	 * 
	 * @param url The URL to download the image from. It can be an animated GIF.
	 * @return The {@link RemoteLoaderHandler} used to handle callbacks to the UI.
	 * @throws Exception If a {@link RemoteLoader} has not been set for this instance.
	 */
	public RemoteLoaderHandler setUrl(String url) throws Exception {
		RemoteLoaderHandler handler = new RemoteLoaderHandlerNext(url);
		
		if (!isLoaderSet()) {
			throw new Exception("A RemoteLoader has not been set for this instance.");
		}
		
		getLoader().load(url, handler);
		
		return handler;
	}
	
	/** Custom handler that manages callbacks from a {@link RemoteLoader}
	 * and sets the right images into the {@link ImageViewNext}.
	 * 
	 * @author Francesco Pontillo
	 *
	 */
	protected class RemoteLoaderHandlerNext extends RemoteLoaderHandler {

		public RemoteLoaderHandlerNext(Looper looper, String resUrl) {
			super(looper, resUrl);
		}
		
		public RemoteLoaderHandlerNext(String resUrl) {
			super(resUrl);
		}
		
		@Override
		protected void success(byte[] object) {
			super.success(object);
			if (object != null) {
				// TODO: handle the image setting
			}
		}
		
		@Override
		protected void onMemoryMiss() {
			super.onMemoryMiss();
			// TODO: set the temporary animated drawable
		}
		
		@Override
		protected void error(Exception e) {
			super.error(e);
			// TODO: set the error drawable
		}

	}
}
