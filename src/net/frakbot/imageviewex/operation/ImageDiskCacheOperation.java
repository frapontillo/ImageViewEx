package net.frakbot.imageviewex.operation;

import java.io.IOException;

import net.frakbot.imageviewex.Converters;
import net.frakbot.imageviewex.ImageViewNext;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LruCache;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Snapshot;

/**
 * Operation to search for an image in the disk cache.
 * Requested input:
 * - ImageMemCacheOperation.PARAM_IMAGE_URL, the URL of the image
 * Given output:
 * - ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT, the byte array of the image
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
		Snapshot cacheEntry;
		try {
			cacheEntry = diskCache.get(PARAM_IMAGE_URL);
		} catch (IOException e) {
			throw new DataException("No value for URL " + url);
		}
		if (cacheEntry == null) throw new DataException("No value for URL " + url);
		
		// Convert the InputStream
		byte[] image = Converters.inputStreamToByteArray(
				cacheEntry.getInputStream(0),
				(int)cacheEntry.getLength(0));
		
		// Saves the image in the in-memory cache
		LruCache<String, byte[]> memCache = ImageViewNext.getMemCache();
		memCache.put(url, image);
		
		Bundle b = new Bundle();
		b.putByteArray(PARAM_IMAGE_URL, image);
		return b;
	}

}
