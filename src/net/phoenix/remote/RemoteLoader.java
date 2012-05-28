/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.phoenix.remote;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import net.phoenix.cache.BytesCache;

import android.content.Context;

/**
 * Realizes a background loader that downloads any data from a URL, optionally backed by a
 * two-level FIFO cache. A thread from a thread pool will be used to download the data in the
 * background and call the appropriate callbacks of a handler.
 * 
 * @author Matthias Kaeppler
 * @author Francesco Pontillo
 * @author Sebastiano Poggi
 * 
 */
public class RemoteLoader {

    // the default thread pool size
    private static final int DEFAULT_POOL_SIZE = 30;
    // expire images after a day
    // TODO: this currently only affects the in-memory cache, so it's quite pointless
    private static final int DEFAULT_TTL_MINUTES = 24 * 60;
    private static final int DEFAULT_NUM_RETRIES = 3;
    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private ThreadPoolExecutor executor;
    private BytesCache cache;
    private int numRetries = DEFAULT_NUM_RETRIES;
    private int defaultBufferSize = DEFAULT_BUFFER_SIZE;
    private long expirationInMinutes = DEFAULT_TTL_MINUTES;

    public RemoteLoader(Context context) {
        this(context, true);
    }

    /**
     * Creates a new RemoteLoader that is backed by an {@link BytesCache}. The cache will by default
     * cache to the device's external storage, and expire after 1 day. You can set useCache
     * to false and then supply your own cache instance via {@link #setCache(BytesCache)},
     * or fine-tune the default one through {@link #getCache()}.
     * 
     * @param context
     *            the current context
     * @param createCache
     *            whether to create a default {@link BytesCache} used for caching
     */
    public RemoteLoader(Context context, boolean createCache) {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        if (createCache) {
            cache = new BytesCache(25, expirationInMinutes, DEFAULT_POOL_SIZE);
            cache.enableDiskCache(context.getApplicationContext(), BytesCache.DISK_CACHE_SDCARD);
        }
    }

    /**
     * @param numThreads
     *            the maximum number of threads that will be started to download data in parallel
     */
    public void setThreadPoolSize(int numThreads) {
        executor.setMaximumPoolSize(numThreads);
    }

    /**
     * @param numAttempts
     *            how often the loader should retry the download if network connection fails
     */
    public void setMaxDownloadAttempts(int numAttempts) {
        numRetries = numAttempts;
    }

    /**
     * If the server you're loading data from does not report file sizes via the Content-Length
     * header, then you can use this method to tell the downloader how much space it should allocate
     * by default when downloading some data into memory.
     * 
     * @param defaultBufferSize
     *            how big the buffer should be into which the file is read. This should be big
     *            enough to hold the largest data you expect to download
     */
    public void setDefaultBufferSize(int defaultBufferSize) {
        this.defaultBufferSize = defaultBufferSize;
    }

    /**
     * Sets the cache used for caching data.
     * @param cache a {@link BytesCache} for caching data
     */
    public void setCache(BytesCache cache) {
        this.cache = cache;
    }

    /**
     * Clears the cache, if it's used. A good candidate for calling in
     * {@link android.app.Application#onLowMemory()}.
     */
    public void clearBytesCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Returns the cache backing this loader.
     * 
     * @return the {@link BytesCache}
     */
    public BytesCache getCache() {
        return cache;
    }

    /**
     * Triggers the loader for the given url. The data loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded data will be
     * posted back to the default RemoteLoaderHandler upon completion. This method will use the default
     * {@link RemoteLoaderHandler} to process the data after downloading it.
     * 
     * @param url
     *            the URL of the data to download
     */
    public void load(String url) {
    	load(url, new RemoteLoaderHandler(url));
    }

    /**
     * Triggers the data loader for the given url. The data loading will be performed
     * concurrently to the UI main thread, using a fixed size thread pool. The loaded data will be
     * posted back to the given RemoteLoaderHandler upon completion.
     * 
     * @param url
     *            the URL of the image to download
     * @param handler
     *            the handler that will process the data after completion
     */
    public void load(String url, RemoteLoaderHandler handler) {
        if (cache != null && cache.containsKeyInMemory(url)) {
            // do not go through message passing, handle directly instead
            handler.success(cache.get(url));
        } else {
            executor.execute(new RemoteLoaderJob(url, handler, cache, numRetries, defaultBufferSize));
        }
    }
}
