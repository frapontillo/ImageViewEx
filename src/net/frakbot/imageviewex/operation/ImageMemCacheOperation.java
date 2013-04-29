package net.frakbot.imageviewex.operation;

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

/**
 * Operation to search for an image in the in-memory cache.
 * Requested input:
 * - ImageMemCacheOperation.PARAM_IMAGE_URL, the URL of the image
 * Given output:
 * - ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT, the byte array of the image
 * - ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL, the requested URL of the image
 * 
 * @deprecated Retrieving in an async way from the mem cache is slow.
 * @author Francesco Pontillo
 *
 */
public class ImageMemCacheOperation implements Operation {
	
	public static final String PARAM_IMAGE_URL = 
            "net.frakbot.imageviewex.extra.url";

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		// Get the URL from the input Bundle
		String url = request.getString(PARAM_IMAGE_URL);
		if (url == null || url.equals("")) throw new DataException("MEM CACHE: Empty URL " + url);
		
		// Initializes the caches, if they're not initialized already
		ImageViewNext.initCaches(context);
		
		LruCache<String, byte[]> cache = ImageViewNext.getMemCache();
		byte[] image = cache.get(url);
		
		Bundle b = new Bundle();
		b.putByteArray(ImageViewExRequestFactory.BUNDLE_EXTRA_OBJECT, image);
		b.putString(ImageViewExRequestFactory.BUNDLE_EXTRA_IMAGE_URL, url);
		return b;
	}

}
