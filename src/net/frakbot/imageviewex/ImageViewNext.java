package net.frakbot.imageviewex;

import java.io.File;
import java.io.IOException;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.jakewharton.DiskLruCache;

import net.frakbot.cache.CacheHelper;
import net.frakbot.imageviewex.listener.ImageViewExRequestListener;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestFactory;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestManager;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;

/**
 * Extension of the ImageViewEx that handles the download and caching of
 * images and animated GIFs.
 *
 * @author Francesco Pontillo, Sebastiano Poggi
 */
public class ImageViewNext extends ImageViewEx {

    private Drawable loadingD;
    private static int classLoadingResId;

    private Drawable errorD;
    private static int classErrorResId;
    
    private String mUrl;
    
    protected ImageViewExRequestManager mRequestManager;
    protected Request mCurrentRequest;
    protected RequestListener mCurrentRequestListener;
    
    private static int mMemCacheSize = 10 * 1024 * 1024; // 10MiB
	private static LruCache<String, byte[]> mMemCache;
	
	private static int mAppVersion = 1;
	private static int mDiskCacheValueCount = 100;
    private static int mDiskCacheSize = 50 * 1024 * 1024; // 50MiB
	private static DiskLruCache mDiskCache;
	
	private static boolean cacheInit = false;

	/** {@inheritDoc} */
    public ImageViewNext(Context context) {
        super(context);
        mRequestManager = ImageViewExRequestManager.from(context);
    }
    
    /**
     * Creates an instance for the class.
     *
     * @param context The context to initialize the instance into.
     * @param attrs   The parameters to initialize the instance with.
     */
    public ImageViewNext(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRequestManager = ImageViewExRequestManager.from(context);
    }
    
    /**
	 * @return The in-memory cache.
	 */
	public static LruCache<String, byte[]> getMemCache() {
		return mMemCache;
	}

	/**
	 * @return The disk cache.
	 */
	public static DiskLruCache getDiskCache() {
		return mDiskCache;
	}

	/**
	 * @return The in-memory cache size, in bits.
	 */
	public static int getMemCacheSize() {
		return mMemCacheSize;
	}

	/**
	 * @param mMemCacheSize The in-memory cache size to set, in bits.
	 */
	public static void setMemCacheSize(int memCacheSize) {
		ImageViewNext.mMemCacheSize = memCacheSize;
	}

	/**
	 * @return The version of the app.
	 */
	public static int getAppVersion() {
		return mAppVersion;
	}

	/**
	 * @param mAppVersion The app version to set.
	 */
	public static void setAppVersion(int appVersion) {
		ImageViewNext.mAppVersion = appVersion;
	}

	/**
	 * @return The disk cache max value count.
	 */
	public static int getDiskCacheValueCount() {
		return mDiskCacheValueCount;
	}

	/**
	 * @param mDiskCacheValueCount The disk cache max value count to set.
	 */
	public static void setDiskCacheValueCount(int diskCacheValueCount) {
		ImageViewNext.mDiskCacheValueCount = diskCacheValueCount;
	}

	/**
	 * @return The disk cache max size, in bits.
	 */
	public static int getDiskCacheSize() {
		return mDiskCacheSize;
	}

	/**
	 * @param mDiskCacheSize The disk cache max size to set, in bits.
	 */
	public static void setDiskCacheSize(int diskCacheSize) {
		ImageViewNext.mDiskCacheSize = diskCacheSize;
	}

    /**
	 * Initializes both the in-memory and the disk-cache
	 * at class-level, if it hasn't been done already.
	 * This method is idempotent.
	 */
    public static void initCaches(Context context) {
    	if (!cacheInit) {
	    	mMemCache = new LruCache<String, byte[]>(mMemCacheSize) {
				protected int sizeOf(String key, byte[] value) {
					return value.length;
				}
			};
			File diskCacheDir =
				CacheHelper.getDiskCacheDir(context, "imagecache");
			try {
				mDiskCache = DiskLruCache.open(
					diskCacheDir, mAppVersion, mDiskCacheValueCount, mDiskCacheSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cacheInit = true;
    	}
    }

    /**
     * Sets the loading {@link Drawable} to be used for every {@link ImageViewNext}.
     *
     * @param classLoadingDrawableResId the {@link int} resource ID of the Drawable
     *                                  while loading an image.
     */
    public static void setClassLoadingDrawable(int classLoadingDrawableResId) {
        classLoadingResId = classLoadingDrawableResId;
    }

    /**
     * Sets the loading {@link Drawable} to be used for this {@link ImageViewNext}.
     *
     * @param loadingDrawable the {@link Drawable} to display while loading an image.
     */
    public void setLoadingDrawable(Drawable loadingDrawable) {
        loadingD = loadingDrawable;
    }

    /**
     * Gets the {@link Drawable} to display while loading an image.
     *
     * @return {@link Drawable} to display while loading.
     */
    public Drawable getLoadingDrawable() {
        if (loadingD != null) {
            return loadingD;
        }
        else {
            return classLoadingResId > 0 ? getResources().getDrawable(classLoadingResId) : null;
        }
    }

    /**
     * Sets the error {@link Drawable} to be used for every {@link ImageViewNext}.
     *
     * @param classErrorDrawableResId the {@link int} resource ID of the Drawable
     *                                to display after an error getting an image.
     */
    public static void setClassErrorDrawable(int classErrorDrawableResId) {
        classErrorResId = classErrorDrawableResId;
    }

    /**
     * Sets the error {@link Drawable} to be used for this {@link ImageViewNext}.
     *
     * @param errorDrawable the {@link Drawable} to display after an error getting an image.
     */
    public void setErrorDrawable(Drawable errorDrawable) {
        errorD = errorDrawable;
    }

    /**
     * Gets the {@link Drawable} to display after an error loading an image.
     *
     * @return {@link Drawable} to display after an error loading an image.
     */
    public Drawable getErrorDrawable() {
        return (errorD != null ? errorD : getResources().getDrawable(classErrorResId));
    }
    
    /**
     * Sets the content of the {@link ImageViewNext} with the data to be downloaded
     * from the provided URL.
     *
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    public void setUrl(String url) {
    	this.mUrl = url;
    	// Abort the current request before starting another one
    	if (mCurrentRequest != null
    		&& mRequestManager.isRequestInProgress(mCurrentRequest)) {
    		mRequestManager.removeRequestListener(mCurrentRequestListener);
    	}
    	// Start the whole retrieval chain
    	getFromMemCache(url);
        return;
    }
    
    /**
     * Returns the current URL set to the {@link ImageViewNext}.
     * The URL will be returned regardless of the existence of 
     * the image or of the caching/downloading progress.
     * 
     * @return The URL set for this {@link ImageViewNext}.
     */
    public String getUrl() {
    	return this.mUrl;
    }
    
    /**
     * Tries to get the image from the memory cache.
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromMemCache(String url) {
    	Request mRequest = 
				ImageViewExRequestFactory.getImageMemCacheRequest(url);
		mCurrentRequestListener = new ImageMemCacheListener(this);
		mRequestManager.execute(mRequest, mCurrentRequestListener);
		return;
    }

    /**
     * Tries to get the image from the disk cache.
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromDiskCache(String url) {
    	Request mRequest = 
				ImageViewExRequestFactory.getImageDiskCacheRequest(url);
		mCurrentRequestListener = new ImageDiskCacheListener(this);
		mRequestManager.execute(mRequest, mCurrentRequestListener);
		return;
    }

    /**
     * Tries to get the image from the network.
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromNetwork(String url) {
    	Request mRequest = 
				ImageViewExRequestFactory.getImageDownloaderRequest(url);
		mCurrentRequestListener = new ImageDownloadListener(this);
		mRequestManager.execute(mRequest, mCurrentRequestListener);
		return;
    }
    
    /**
     * Called when the image is got from whatever the source.
     * Override this to get the appropriate callback.
     * @param image The image as a byte array.
     */
    protected void onSuccess(byte[] image) {
    	setByteArray(image);
    }
    
    /**
     * Called when the image is got from the memory cache.
     * Override this to get the appropriate callback.
     * @param image The image as a byte array.
     */
    protected void onMemCacheHit(byte[] image) {
    	onSuccess(image);
    }
    
    /**
     * Called when there is a memory cache miss for the image.
     * Override this to get the appropriate callback.
     */
    protected void onMemCacheMiss() {
    	Drawable loadingDrawable = getLoadingDrawable();
        if (loadingDrawable != null) {
    		ScaleType scaleType = this.getScaleType();
            if (scaleType != null) {
                this.setScaleType(scaleType);
            } else {
                this.setScaleType(ScaleType.CENTER_INSIDE);
            }
            this.setImageDrawable(loadingDrawable);
            if (loadingDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) loadingDrawable).start();
            }
        }
    }
    
    /**
     * Called when the image is got from the disk cache.
     * Override this to get the appropriate callback.
     * @param image The image as a byte array.
     */
    protected void onDiskCacheHit(byte[] image) {
    	onSuccess(image);
    }
    
    /**
     * Called when there is a disk cache miss for the image.
     * Override this to get the appropriate callback.
     */
    protected void onDiskCacheMiss() {
    	// No default implementation
    }
    
    /**
     * Called when the image is got from the network.
     * Override this to get the appropriate callback.
     * @param image The image as a byte array.
     */
    protected void onNetworkHit(byte[] image) {
    	onSuccess(image);
    }
    
    /**
     * Called when there is a network miss for the image,
     * usually a 404.
     * Override this to get the appropriate callback.
     */
    protected void onNetworkMiss() {
    	// No default implementation
    }
    
    /**
     * Called when the image could not be found anywhere.
     * Override this to get the appropriate callback.
     */
    protected void onMiss() {
    	Drawable errorDrawable = getErrorDrawable();
        if (getErrorDrawable() != null) {
        	ScaleType scaleType = this.getScaleType();
        	if (scaleType != null) {
                this.setScaleType(scaleType);
            } else {
                this.setScaleType(ScaleType.CENTER_INSIDE);
            }
        	this.setImageDrawable(errorDrawable);
            if (errorDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) errorDrawable).start();
            }
        }
    }

    /**
     * Sets the image from a byte array.
     * @param image The image to set.
     */
    private void setByteArray(final byte[] image) {
    	if (image != null) {
            ScaleType scaleType = this.getScaleType();
            if (scaleType != null) {
                this.setScaleType(scaleType);
            }
			this.setSource(image);
        }
    }
    
    /**
     * Operation listener for the memory cache retrieval operation.
     * @author Francesco Pontillo
     *
     */
    private class ImageMemCacheListener extends ImageViewExRequestListener {

    	public ImageMemCacheListener(ImageViewNext imageViewNext) {
    		super(imageViewNext);
    	}

    	@Override
    	public void onRequestFinished(Request request, Bundle resultData) {
    		byte[] image = 
    				resultData.getByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT);
    		mImageViewNext.onMemCacheHit(image);
    	}

    	@Override
    	public void onRequestConnectionError(Request request, int statusCode) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestDataError(Request request) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestCustomError(Request request, Bundle resultData) {
    		handleMiss();
    	}
    	
    	/**
    	 * Generic function to handle the cache miss.
    	 */
    	private void handleMiss() {
    		// Calls the class callback
    		mImageViewNext.onMemCacheMiss();
    		// Starts searching in the disk cache
    		getFromDiskCache(mImageViewNext.getUrl());
    	}

    }

    /**
     * Operation listener for the disk cache retrieval operation.
     * @author Francesco Pontillo
     *
     */
    private class ImageDiskCacheListener extends ImageViewExRequestListener {

    	public ImageDiskCacheListener(ImageViewNext imageViewNext) {
    		super(imageViewNext);
    	}

    	@Override
    	public void onRequestFinished(Request request, Bundle resultData) {
    		byte[] image = 
    				resultData.getByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT);
    		mImageViewNext.onDiskCacheHit(image);
    	}

    	@Override
    	public void onRequestConnectionError(Request request, int statusCode) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestDataError(Request request) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestCustomError(Request request, Bundle resultData) {
    		handleMiss();
    	}
    	
    	/**
    	 * Generic function to handle the cache miss.
    	 */
    	private void handleMiss() {
    		// Calls the class callback
    		mImageViewNext.onDiskCacheMiss();
    		// Starts searching in the network
    		getFromNetwork(mImageViewNext.getUrl());
    	}

    }

    /**
     * Operation listener for the network retrieval operation.
     * @author Francesco Pontillo
     *
     */
    private class ImageDownloadListener extends ImageViewExRequestListener {

    	public ImageDownloadListener(ImageViewNext imageViewNext) {
    		super(imageViewNext);
    	}

    	@Override
    	public void onRequestFinished(Request request, Bundle resultData) {
    		byte[] image = 
    				resultData.getByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT);
    		mImageViewNext.onNetworkHit(image);
    	}

    	@Override
    	public void onRequestConnectionError(Request request, int statusCode) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestDataError(Request request) {
    		handleMiss();
    	}

    	@Override
    	public void onRequestCustomError(Request request, Bundle resultData) {
    		handleMiss();
    	}
    	
    	/**
    	 * Generic function to handle the network miss.
    	 */
    	private void handleMiss() {
    		// Calls the class callback
    		mImageViewNext.onNetworkMiss();
    		// Calss the final miss class callback
    		// Starts searching in the disk cache
    		mImageViewNext.onMiss();
    	}

    }
}
