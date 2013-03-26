/**
 * 2012 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package net.frakbot.imageviewex.requestmanager;

import net.frakbot.imageviewex.operation.ImageDiskCacheOperation;
import net.frakbot.imageviewex.operation.ImageDownloadOperation;
import net.frakbot.imageviewex.operation.ImageMemCacheOperation;

import com.foxykeep.datadroid.requestmanager.Request;

/**
 * Class used to create the {@link Request}s.
 *
 * @author Foxykeep, Francesco Pontillo
 */
public final class ImageViewExRequestFactory {
	// Request types
    public static final int REQUEST_TYPE_IMAGE_MEM_CACHE = 0;
    public static final int REQUEST_TYPE_IMAGE_DISK_CACHE = 1;
    public static final int REQUEST_TYPE_IMAGE_DOWNLOAD = 2;
    
    // Response data
    public static final String BUNDLE_EXTRA_OBJECT =
            "net.frakbot.imageviewex.extra.object";
    public static final String BUNDLE_EXTRA_IMAGE_URL =
            "net.frakbot.imageviewex.extra.imageUrl";
    
    private ImageViewExRequestFactory() {
        // no public constructor
    }

    /**
     * Create the request to get an image from the memory cache.
     * 
     * @param url 	The URL of the image.
     * @return 		The request.
     */
    public static Request getImageMemCacheRequest(String url) {
        Request request = new Request(REQUEST_TYPE_IMAGE_MEM_CACHE);
        request.put(ImageMemCacheOperation.PARAM_IMAGE_URL, url);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    /**
     * Create the request to get an image from the the disk cache.
     * 
     * @param url 	The URL of the image.
     * @return 		The request.
     */
    public static Request getImageDiskCacheRequest(String url) {
        Request request = new Request(REQUEST_TYPE_IMAGE_DISK_CACHE);
        request.put(ImageDiskCacheOperation.PARAM_IMAGE_URL, url);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    /**
     * Create the request to get an image from the network.
     * 
     * @param url 	The URL of the image.
     * @return 		The request.
     */
    public static Request getImageDownloaderRequest(String url) {
        Request request = new Request(REQUEST_TYPE_IMAGE_DOWNLOAD);
        request.put(ImageDownloadOperation.PARAM_IMAGE_URL, url);
        request.setMemoryCacheEnabled(true);
        return request;
    }
}
