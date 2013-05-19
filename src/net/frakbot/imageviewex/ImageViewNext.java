package net.frakbot.imageviewex;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.jakewharton.disklrucache.DiskLruCache;
import net.frakbot.cache.CacheHelper;
import net.frakbot.imageviewex.broadcastreceiver.ConnectivityChangeBroadcastReceiver;
import net.frakbot.imageviewex.listener.ImageViewExRequestListener;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestFactory;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestManager;

import java.io.File;
import java.io.IOException;

/**
 * Extension of the ImageViewEx that handles the download and caching of
 * images and animated GIFs.
 *
 * @author Francesco Pontillo, Sebastiano Poggi
 */
public class ImageViewNext extends ImageViewEx {

    private static final String TAG = ImageViewNext.class.getSimpleName();
    private static final int DISK_CACHE_VALUE_COUNT = 1;

    private Drawable mLoadingD;
    private static int mClassLoadingResId;
    private Drawable mErrorD;
    private static int mClassErrorResId;

    private boolean mAutoRetryFromNetwork;
    private static boolean mClassAutoRetryFromNetwork;
    private boolean hasFailedDownload;

    private String mUrl;
    private ImageLoadCompletionListener mLoadCallbacks;

    protected ImageViewExRequestManager mRequestManager;
    protected Request mCurrentRequest;
    protected RequestListener mCurrentRequestListener;

    private Context mContext;

    private static int mMemCacheSize = 10 * 1024 * 1024; // 10MiB
    private static LruCache<String, byte[]> mMemCache;
    private static int mAppVersion = 1;
    private static int mDiskCacheSize = 50 * 1024 * 1024; // 50MiB
    private static DiskLruCache mDiskCache;
    private static boolean mCacheInit = false;
    private static int mConcurrentThreads = 10;

    private ConnectivityChangeBroadcastReceiver mReceiver;
    private static final String RECEIVER_ACTION = android.net.ConnectivityManager.CONNECTIVITY_ACTION;

    /** Represents a cache level. */
    public enum CacheLevel {
        /** The first level of cache: the memory cache */
        MEMORY,
        /** The second level of cache: the disk cache */
        DISK,
        /** No caching, direct fetching from the network */
        NETWORK
    }

    /** {@inheritDoc} */
    public ImageViewNext(Context context) {
        super(context);
        init(context);
    }

    /**
     * Creates an instance for the class.
     * Initializes the auto retry from network to true.
     *
     * @param context The context to initialize the instance into.
     * @param attrs   The parameters to initialize the instance with.
     */
    public ImageViewNext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initializes a few instance level variables.
     *
     * @param context The Context used for initialization.
     */
    private void init(Context context) {
        mContext = context;
        mRequestManager = ImageViewExRequestManager.from(context);
        mClassAutoRetryFromNetwork = true;
        mAutoRetryFromNetwork = true;
        hasFailedDownload = false;
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerReceiver();
    }

    /** {@inheritDoc} */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
    }

    /** Register the {@link ConnectivityChangeBroadcastReceiver} for this instance. */
    private void registerReceiver() {
        // If the receiver does not exist
        if (mReceiver == null) {
            mReceiver = new ConnectivityChangeBroadcastReceiver(this);
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RECEIVER_ACTION);
            mContext.registerReceiver(mReceiver, intentFilter);
        }
    }

    /** Unregister the {@link ConnectivityChangeBroadcastReceiver} for this instance. */
    private void unregisterReceiver() {
        // If the receiver does exists
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    /** Gets the current image loading callback, if any */
    public ImageLoadCompletionListener getLoadCallbacks() {
        return mLoadCallbacks;
    }

    /**
     * Sets the image loading callback.
     *
     * @param loadCallbacks The listener instance, or null to clear it.
     */
    public void setLoadCallbacks(ImageLoadCompletionListener loadCallbacks) {
        mLoadCallbacks = loadCallbacks;
    }

    /** @return The in-memory cache. */
    public static LruCache<String, byte[]> getMemCache() {
        return mMemCache;
    }

    /** @return The disk cache. */
    public static DiskLruCache getDiskCache() {
        return mDiskCache;
    }

    /** @return The in-memory cache size, in bits. */
    public static int getMemCacheSize() {
        return mMemCacheSize;
    }

    /** @param memCacheSize The in-memory cache size to set, in bits. */
    public static void setMemCacheSize(int memCacheSize) {
        mMemCacheSize = memCacheSize;
    }

    /** @return The version of the app. */
    public static int getAppVersion() {
        return mAppVersion;
    }

    /** @param appVersion The app version to set. */
    public static void setAppVersion(int appVersion) {
        ImageViewNext.mAppVersion = appVersion;
    }

    /**
     * Sets the image loading callbacks listener.
     *
     * @param l The listener, or null to clear it.
     */
    public void setImageLoadCallbacks(ImageLoadCompletionListener l) {
        mLoadCallbacks = l;
    }

    /**
     * Gets the current image loading callbacks listener, if any.
     *
     * @return Returns the callbacks listener.
     */
    public ImageLoadCompletionListener getImageLoadCallbacks() {
        return mLoadCallbacks;
    }

    /** @return The disk cache max size, in bits. */
    public static int getDiskCacheSize() {
        return mDiskCacheSize;
    }

    /** @param diskCacheSize The disk cache max size to set, in bits. */
    public static void setDiskCacheSize(int diskCacheSize) {
        ImageViewNext.mDiskCacheSize = diskCacheSize;
    }

    /**
     * Initializes both the in-memory and the disk-cache
     * at class-level, if it hasn't been done already.
     * This method is idempotent.
     */
    public static void initCaches(Context context) {
        if (!mCacheInit) {
            mMemCache = new LruCache<String, byte[]>(mMemCacheSize) {
                protected int sizeOf(String key, byte[] value) {
                    return value.length;
                }
            };
            File diskCacheDir =
                CacheHelper.getDiskCacheDir(context, "imagecache");
            try {
                mDiskCache = DiskLruCache.open(
                    diskCacheDir, mAppVersion, DISK_CACHE_VALUE_COUNT, mDiskCacheSize);
            }
            catch (IOException ignored) {
            }
            mCacheInit = true;
        }
    }

    /**
     * Sets the loading {@link Drawable} to be used for every {@link ImageViewNext}.
     *
     * @param classLoadingDrawableResId the {@link int} resource ID of the Drawable
     *                                  while loading an image.
     */
    public static void setClassLoadingDrawable(int classLoadingDrawableResId) {
        mClassLoadingResId = classLoadingDrawableResId;
    }

    /**
     * Sets the loading {@link Drawable} to be used for this {@link ImageViewNext}.
     *
     * @param loadingDrawable the {@link Drawable} to display while loading an image.
     */
    public void setLoadingDrawable(Drawable loadingDrawable) {
        mLoadingD = loadingDrawable;
    }

    /**
     * Gets the {@link Drawable} to display while loading an image.
     *
     * @return {@link Drawable} to display while loading.
     */
    public Drawable getLoadingDrawable() {
        if (mLoadingD != null) {
            return mLoadingD;
        }
        else {
            return mClassLoadingResId > 0 ? getResources().getDrawable(mClassLoadingResId) : null;
        }
    }

    /**
     * Sets the error {@link Drawable} to be used for every {@link ImageViewNext}.
     *
     * @param classErrorDrawableResId the {@link int} resource ID of the Drawable
     *                                to display after an error getting an image.
     */
    public static void setClassErrorDrawable(int classErrorDrawableResId) {
        mClassErrorResId = classErrorDrawableResId;
    }

    /**
     * Sets the error {@link Drawable} to be used for this {@link ImageViewNext}.
     *
     * @param errorDrawable the {@link Drawable} to display after an error getting an image.
     */
    public void setErrorDrawable(Drawable errorDrawable) {
        mErrorD = errorDrawable;
    }

    /**
     * Gets the {@link Drawable} to display after an error loading an image.
     *
     * @return {@link Drawable} to display after an error loading an image.
     */
    public Drawable getErrorDrawable() {
        return mErrorD != null ? mErrorD : getResources().getDrawable(mClassErrorResId);
    }

    /**
     * Checks if a request is already in progress.
     *
     * @return true if there is a pending request, false otherwise.
     */
    private boolean isRequestInProgress() {
        return mCurrentRequest != null
                && mRequestManager.isRequestInProgress(mCurrentRequest);
    }

    /** Aborts the current request, if any, and stops everything else. */
    private void abortEverything() {
        // Abort the current request before starting another one
        if (isRequestInProgress()) {
            mRequestManager.removeRequestListener(mCurrentRequestListener);
        }

        stop();
        stopLoading();
    }

    /**
     * Sets the content of the {@link ImageViewNext} with the data to be downloaded
     * from the provided URL.
     *
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    public void setUrl(String url) {
        mUrl = url;

        // Abort the pending request (if any) and stop animating/loading
        abortEverything();

        // Start the whole retrieval chain
        getFromMemCache(url);
    }

    /**
     * Returns the current URL set to the {@link ImageViewNext}.
     * The URL will be returned regardless of the existence of
     * the image or of the caching/downloading progress.
     *
     * @return The URL set for this {@link ImageViewNext}.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Returns true if this instance will automatically retry the download from
     * the network when it becomes available once again.
     * The instance level settings has priority over the class level's.
     *
     * @return true if the instance retries to download the image when the
     *         network is once again available, false otherwise.
     */
    public boolean isAutoRetryFromNetwork() {
        return mAutoRetryFromNetwork;
    }

    /**
     * Sets the value of auto retry from network for this instance, set it to
     * true if this instance has to automatically retry the download from the
     * network when it becomes available once again, false otherwise. The
     * instance level settings has priority over the class level's.
     * <p/>
     * If the instance was previously forbidden to auto-retry, it will be
     * allowed as soon as this method is called with a true argument.
     * <p/>
     * If the instance was previously allowed to auto-retry, it will be
     * forbidden as soon as this method is called with a false argument.
     *
     * @param autoRetryFromNetwork The instance value for the auto retry.
     */

    public void setAutoRetryFromNetwork(boolean autoRetryFromNetwork) {
        boolean registerAfter;
        boolean unregisterAfter;

        // If nothing changes, do nothing
        if (mAutoRetryFromNetwork == autoRetryFromNetwork) return;

        // Set the "after" booleans
        registerAfter = !mAutoRetryFromNetwork;
        unregisterAfter = !autoRetryFromNetwork;

        // Set the state value
        mAutoRetryFromNetwork = autoRetryFromNetwork;

        // Register or unregister the receiver according to the new value
        if (registerAfter) {
            registerReceiver();
        }
        else if (unregisterAfter) {
            unregisterReceiver();
        }
    }

    /**
     * Returns true if every ImageViewNext will automatically retry the download from
     * the network when it becomes available once again.
     *
     * @return true if ImageViewNext retries to download the image when the
     *         network is once again available, false otherwise.
     */

    public static boolean isClassAutoRetryFromNetwork() {
        return ImageViewNext.mClassAutoRetryFromNetwork;
    }

    /**
     * Sets the value of auto retry from network for ImageViewNext, set it to true
     * if ImageViewNext has to automatically retry the download from
     * the network when it becomes available once again, false otherwise.
     * <p/>
     * All of the existing constructed instances won't be affected by this.
     *
     * @param classAutoRetryFromNetwork The instance value for the auto retry.
     */

    public static void setClassAutoRetryFromNetwork(
        boolean classAutoRetryFromNetwork) {
        ImageViewNext.mClassAutoRetryFromNetwork = classAutoRetryFromNetwork;
    }

    /**
     * Checks if the auto retry can be applied for the current instance.
     *
     * @return true if this instance is allowed to auto retry network ops, false
     *         otherwise.
     */
    private boolean isAutoRetryTrueSomewhere() {
        return isAutoRetryFromNetwork() || isClassAutoRetryFromNetwork();
    }

    /**
     * Tries to retrieve the image from network, if and only if:
     * <ul>
     * <li>No requests are pending for this instance.</li>
     * <li>A previous download failed.</li>
     * <li>The instance-level or class-level auto retry is set to true, with
     * this priority.</li>
     * </ul>
     */
    public void retryFromNetworkIfPossible() {
        // Only retry to get the image from the network:
        // - if no requests are in progress
        // - if the download previously failed
        // - auto retry is set to true for the instance or the class (in order)
        if (!isRequestInProgress() && hasFailedDownload && isAutoRetryTrueSomewhere()) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Autoretry: true somewhere, retrying...");
            // Abort the pending request (if any) and stop animating/loading
            abortEverything();
            // Initalize caches
            ImageViewNext.initCaches(mContext);
            // Starts the retrieval from the network once again
            getFromNetwork(getUrl());
            // Cross ye fingers
        }
        else {
            if (BuildConfig.DEBUG) Log.i(TAG, "Autoretry: false, sorry.");
        }
    }

    /**
     * Tries to get the image from the memory cache.
     *
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromMemCache(String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Memcache: getting for URL " + url + " @" + hashCode());

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadStarted(this, CacheLevel.MEMORY);
        }

        // Get the URL from the input Bundle
        if (url == null || "".equals(url)) return;

        // Initializes the caches, if they're not initialized already
        ImageViewNext.initCaches(mContext);

        LruCache<String, byte[]> cache = ImageViewNext.getMemCache();
        byte[] image = cache.get(url);

        if (image == null) {
            handleMemCacheMiss();
        }
        else {
            onMemCacheHit(image, url);
        }
    }

    /** Generic function to handle the mem cache miss. */
    private void handleMemCacheMiss() {
        // Calls the class callback
        onMemCacheMiss();
        // Starts searching in the disk cache
        getFromDiskCache(getUrl());
    }

    /**
     * Tries to get the image from the disk cache.
     *
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromDiskCache(String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Diskcache: getting for URL " + url + " @" + hashCode());
        Request mRequest =
            ImageViewExRequestFactory.getImageDiskCacheRequest(url);
        mCurrentRequestListener = new ImageDiskCacheListener(this);
        mRequestManager.execute(mRequest, mCurrentRequestListener);

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadStarted(this, CacheLevel.DISK);
        }
    }

    /**
     * Tries to get the image from the network.
     *
     * @param url The URL to download the image from. It can be an animated GIF.
     */
    private void getFromNetwork(String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Network: getting for URL " + url + " @" + hashCode());
        Request mRequest =
            ImageViewExRequestFactory.getImageDownloaderRequest(url);
        mCurrentRequestListener = new ImageDownloadListener(this);
        mRequestManager.execute(mRequest, mCurrentRequestListener);

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadStarted(this, CacheLevel.NETWORK);
        }
    }

    /**
     * Called when the image is got from whatever the source.
     * Override this to get the appropriate callback.
     *
     * @param image The image as a byte array.
     */
    protected void onSuccess(byte[] image) {
        setByteArray(image);
    }

    /**
     * Called when the image is got from whatever the source.
     * Checks if the original URL matches the current one set
     * in the instance of ImageViewNext.
     *
     * @param image The image as a byte array.
     * @param url   The URL of the retrieved image.
     */
    private void onPreSuccess(byte[] image, String url) {
        // Only set the image if the current url equals to the retrieved image's url
        if (url != null && url.equals(getUrl())) {
            onSuccess(image);
        }
    }

    /**
     * Called when the image is got from the memory cache.
     * Override this to get the appropriate callback.
     *
     * @param image The image as a byte array.
     * @param url   The URL of the retrieved image.
     */
    protected void onMemCacheHit(byte[] image, String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Memory cache HIT @" + hashCode());
        onPreSuccess(image, url);

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadCompleted(this, CacheLevel.MEMORY);
        }
    }

    /**
     * Called when there is a memory cache miss for the image.
     * Override this to get the appropriate callback.
     */
    protected void onMemCacheMiss() {
        Drawable loadingDrawable = getLoadingDrawable();
        if (loadingDrawable != null) {
            ScaleType scaleType = getScaleType();
            if (scaleType != null) {
                setScaleType(scaleType);
            }
            else {
                setScaleType(ScaleType.CENTER_INSIDE);
            }
            setImageDrawable(loadingDrawable);
            if (loadingDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) loadingDrawable).start();
            }
        }
        else {
            setImageDrawable(mEmptyDrawable);    // This also stops any ongoing loading process
        }

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadError(this, CacheLevel.MEMORY);
        }
    }

    /**
     * Called when the image is got from the disk cache.
     * Override this to get the appropriate callback.
     *
     * @param image The image as a byte array.
     * @param url   The URL of the retrieved image.
     */
    protected void onDiskCacheHit(byte[] image, String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Disk cache HIT @" + hashCode());
        onPreSuccess(image, url);

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadCompleted(this, CacheLevel.DISK);
        }
    }

    /**
     * Called when there is a disk cache miss for the image.
     * Override this to get the appropriate callback.
     */
    protected void onDiskCacheMiss() {
        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadError(this, CacheLevel.DISK);
        }
    }

    /**
     * Called when the image is got from the network.
     * Override this to get the appropriate callback.
     *
     * @param image The image as a byte array.
     * @param url   The URL of the retrieved image.
     */
    protected void onNetworkHit(byte[] image, String url) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Network HIT @" + hashCode());
        onPreSuccess(image, url);
        hasFailedDownload = false;

        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadCompleted(this, CacheLevel.NETWORK);
        }
    }

    /**
     * Called when there is a network miss for the image,
     * usually a 404.
     * Override this to get the appropriate callback.
     */
    protected void onNetworkMiss() {
        if (mLoadCallbacks != null) {
            mLoadCallbacks.onLoadError(this, CacheLevel.NETWORK);
        }
        hasFailedDownload = true;
    }

    /**
     * Called when the image could not be found anywhere.
     * Override this to get the appropriate callback.
     */
    protected void onMiss() {
        Drawable errorDrawable = getErrorDrawable();
        if (getErrorDrawable() != null) {
            ScaleType scaleType = getScaleType();
            if (scaleType != null) {
                setScaleType(scaleType);
            }
            else {
                setScaleType(ScaleType.CENTER_INSIDE);
            }
            setImageDrawable(errorDrawable);
            if (errorDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) errorDrawable).start();
            }
        }
    }

    /**
     * Sets the image from a byte array.
     *
     * @param image The image to set.
     */
    private void setByteArray(final byte[] image) {
        if (image != null) {
            ScaleType scaleType = getScaleType();
            if (scaleType != null) {
                setScaleType(scaleType);
            }
            setSource(image);
        }
    }

    /**
     * Returns the maximum number of concurrent worker threads
     * used to get images from cache/network.
     *
     * @return Maximum number of concurrent threads.
     */
    public static int getMaximumNumberOfThreads() {
        return mConcurrentThreads;
    }

    /**
     * Define the maximum number of concurrent worker threads
     * used to get images from cache/network.
     * By default only 10 concurrent worker threads are used at
     * the same time.
     * The value will be set once and for all when the first
     * ImageViewNext is instantiated. Calling this function again
     * after an ImageViewNext is instantiated will have no effect.
     *
     * @param concurrentThreads The number of concurrent threads.
     */
    public static void setMaximumNumberOfThreads(int concurrentThreads) {
        mConcurrentThreads = concurrentThreads;
    }

    /**
     * Operation listener for the disk cache retrieval operation.
     *
     * @author Francesco Pontillo
     */
    private class ImageDiskCacheListener extends ImageViewExRequestListener {

        public ImageDiskCacheListener(ImageViewNext imageViewNext) {
            super(imageViewNext);
        }

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            byte[] image =
                resultData.getByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT);
            String url =
                resultData.getString(ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL);
            if (image == null) {
                handleMiss();
            }
            else {
                mImageViewNext.onDiskCacheHit(image, url);
            }
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

        /** Generic function to handle the cache miss. */
        private void handleMiss() {
            // Calls the class callback
            mImageViewNext.onDiskCacheMiss();
            // Starts searching in the network
            getFromNetwork(mImageViewNext.getUrl());
        }

    }

    /**
     * Operation listener for the network retrieval operation.
     *
     * @author Francesco Pontillo
     */
    private class ImageDownloadListener extends ImageViewExRequestListener {

        public ImageDownloadListener(ImageViewNext imageViewNext) {
            super(imageViewNext);
        }

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            byte[] image =
                resultData.getByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT);
            String url =
                resultData.getString(ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL);
            if (image == null || image.length == 0) {
                handleMiss();
            }
            else {
                mImageViewNext.onNetworkHit(image, url);
            }
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

        /** Generic function to handle the network miss. */
        private void handleMiss() {
            // Calls the class callback
            mImageViewNext.onNetworkMiss();
            // Calss the final miss class callback
            mImageViewNext.onMiss();
        }
    }

    /** A simple interface for image loading callbacks. */
    public interface ImageLoadCompletionListener {

        /**
         * Loading of a resource has been started by invoking {@link #setUrl(String)}.
         *
         * @param v     The ImageViewNext on which the loading has begun
         * @param level The cache level involved. You will receive a pair of calls, one
         *              to onLoadStarted and one to onLoadCompleted or to onLoadError,
         *              for each cache level, in this order: memory->disk->network
         *              (for MISS on both memory and disk caches)
         */
        public void onLoadStarted(ImageViewNext v, CacheLevel level);

        /**
         * Loading of a resource has been completed. This corresponds to a cache HIT
         * for the memory and disk cache levels, or a successful download from the net.
         *
         * @param v     The ImageViewNext on which the loading has completed
         * @param level The cache level involved. You will receive a pair of calls, one
         *              to onLoadStarted and one to onLoadCompleted or to onLoadError,
         *              for each cache level, in this order: memory->disk->network
         *              (for MISS on both memory and disk caches).
         */
        public void onLoadCompleted(ImageViewNext v, CacheLevel level);

        /**
         * Loading of a resource has failed. This corresponds to a cache MISS
         * for the memory and disk cache levels, or a successful download from the net.
         *
         * @param v     The ImageViewNext on which the loading has begun
         * @param level The cache level involved. You will receive a pair of calls, one
         *              to onLoadStarted and one to onLoadCompleted or to onLoadError,
         *              for each cache level, in this order: memory->disk->network
         *              (for MISS on both memory and disk caches)
         */
        public void onLoadError(ImageViewNext v, CacheLevel level);
    }
}
