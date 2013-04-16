package net.frakbot.imageviewex.operation;

import net.frakbot.cache.CacheHelper;
import net.frakbot.imageviewex.Converters;
import net.frakbot.imageviewex.ImageViewNext;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestFactory;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LruCache;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;

/**
 * Operation to search for an image in the disk cache.
 * Requested input:
 * - ImageMemCacheOperation.PARAM_IMAGE_URL, the URL of the image
 * Given output:
 * - ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT, the byte array of the image
 * - ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL, the requested URL of the image
 * 
 * @author Francesco Pontillo
 *
 */
public class ImageDiskCacheOperation implements Operation {
	
	public static final String PARAM_IMAGE_URL = 
            "net.frakbot.imageviewex.extra.url";

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		
		// Get the URL from the input Bundle
		String url = request.getString(PARAM_IMAGE_URL);
		if (url == null || url.equals("")) throw new DataException("No value for URL " + url);
		
		// Initializes the caches, if they're not initialized already
		ImageViewNext.initCaches(context);
		
		// Get the entry
		DiskLruCache diskCache = ImageViewNext.getDiskCache();
		Snapshot cacheEntry = null;
		try {
			cacheEntry = diskCache.get(CacheHelper.UriToDiskLruCacheString(url));
		} catch (Exception e) {
			throw new DataException("DISK CACHE: Error while getting value for URL " + url);
		}
		
		byte[] image = null;
		
		// If the object is not null, convert it
		if (cacheEntry != null) {
			// Convert the InputStream
			image = Converters.inputStreamToByteArray(
					cacheEntry.getInputStream(0),
					(int)cacheEntry.getLength(0));
			
			// Saves the image in the in-memory cache
			LruCache<String, byte[]> memCache = ImageViewNext.getMemCache();
			memCache.put(url, image);
		}
		
		Bundle b = new Bundle();
		b.putByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT, image);
		b.putString(ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL, url);
		return b;
	}

}
