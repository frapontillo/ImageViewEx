package net.phoenix.imageviewex;

import net.phoenix.cache.BytesCache;
import net.phoenix.remote.RemoteLoader;
import net.phoenix.remote.RemoteLoaderHandler;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;

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
	private RemoteLoader loader = null;
	private static RemoteLoader classLoader = null;
	
	private Drawable loadingD;
	private static int classLoadingResId;
	
	private Drawable errorD;
	private static int classErrorResId;

	/**
     * Creates an instance for the class.
     *
     * @param context	The context to initialize the instance into.
     * @param attrs		The parameters to initialize the instance with.
     */
    public ImageViewNext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	/**
	 * Sets the loading {@link Drawable} to be used for every {@link ImageViewNext}.
	 * @param classLoadingDrawable	the {@link int} resource ID of the Drawable
	 * 								while loading an image.
	 */
	public static void setClassLoadingDrawable(int classLoadingDrawableResId) {
		classLoadingResId = classLoadingDrawableResId;
	}
	
	/**
	 * Sets the loading {@link Drawable} to be used for this {@link ImageViewNext}.
	 * @param loadingDrawable the {@link Drawable} to display while loading an image.
	 */
	public void setLoadingDrawable(Drawable loadingDrawable) {
		loadingD = loadingDrawable;
	}
	
	/**
	 * Gets the {@link Drawable} to display while loading an image.
	 * @return {@link Drawable} to display while loading.
	 */
	public Drawable getLoadingDrawable() {
		return (loadingD != null ? loadingD : getResources().getDrawable(classLoadingResId));
	}
	
	/**
	 * Sets the error {@link Drawable} to be used for every {@link ImageViewNext}.
	 * @param 	classErrorDrawableResId the {@link int} resource ID of the Drawable
	 * 			to display after an error getting an image.
	 */
	public static void setClassErrorDrawable(int classErrorDrawableResId) {
		classErrorResId = classErrorDrawableResId;
	}

	/**
	 * Sets the error {@link Drawable} to be used for this {@link ImageViewNext}.
	 * @param classErrorDrawable the {@link Drawable} to display after an error getting an image.
	 */
	public void setErrorDrawable(Drawable errorDrawable) {
		errorD = errorDrawable;
	}
	
	/**
	 * Gets the {@link Drawable} to display after an error loading an image.
	 * @return {@link Drawable} to display after an error loading an image.
	 */
	public Drawable getErrorDrawable() {
		return (errorD != null ? errorD : getResources().getDrawable(classErrorResId));
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
	public static void setClassLoader(RemoteLoader classLoader) {
		ImageViewNext.classLoader = classLoader;
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
	 */
	public RemoteLoaderHandler setUrl(String url) {
		RemoteLoaderHandler handler = new RemoteLoaderHandlerNext(url, this);
		
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
		private ImageViewNext imageViewNext = null;

		public RemoteLoaderHandlerNext(Looper looper, String resUrl, ImageViewNext imageViewNext) {
			super(looper, resUrl);
			this.imageViewNext = imageViewNext;
		}
		
		public RemoteLoaderHandlerNext(String resUrl, ImageViewNext imageViewNext) {
			super(resUrl);
			this.imageViewNext = imageViewNext;
		}
		
		@Override 
		protected void success(byte[] object) {
			super.success(object);
			if (object != null) {
				setSource(object);
			}
		}
		
		@Override
		protected void onMemoryMiss() {
			super.onMemoryMiss();
			Drawable loadingDrawable = getLoadingDrawable();
			if (loadingDrawable != null) {
				imageViewNext.setImageDrawable(loadingDrawable);
				if (loadingDrawable instanceof AnimationDrawable)
					((AnimationDrawable)loadingDrawable).start();
			}
			
		}
		
		@Override
		protected void error(Exception e) {
			super.error(e);
			Drawable errorDrawable = getErrorDrawable();
			if (getErrorDrawable() != null)
				imageViewNext.setImageDrawable(errorDrawable);
				if (errorDrawable instanceof AnimationDrawable)
					((AnimationDrawable)errorDrawable).start();
		}

	}
}
