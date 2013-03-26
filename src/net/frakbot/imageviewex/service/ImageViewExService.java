/**
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package net.frakbot.imageviewex.service;

import net.frakbot.imageviewex.ImageViewNext;
import net.frakbot.imageviewex.operation.ImageDiskCacheOperation;
import net.frakbot.imageviewex.operation.ImageDownloadOperation;
import net.frakbot.imageviewex.operation.ImageMemCacheOperation;
import net.frakbot.imageviewex.requestmanager.ImageViewExRequestFactory;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;

/**
 * This class is called by the {@link ImageViewExRequestManager}
 * through the {@link Intent} system.
 *
 * @author Foxykeep, Francesco Pontillo
 */
public class ImageViewExService extends RequestService {
	
	@Override
    protected int getMaximumNumberOfThreads() {
        return ImageViewNext.getMaximumNumberOfThreads();
    }

    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
            case ImageViewExRequestFactory.REQUEST_TYPE_IMAGE_MEM_CACHE:
                return new ImageMemCacheOperation();
            case ImageViewExRequestFactory.REQUEST_TYPE_IMAGE_DISK_CACHE:
                return new ImageDiskCacheOperation();
            case ImageViewExRequestFactory.REQUEST_TYPE_IMAGE_DOWNLOAD:
                return new ImageDownloadOperation();
        }
        return null;
    }

    @Override
    protected Bundle onCustomRequestException(Request request, CustomRequestException exception) {
        return super.onCustomRequestException(request, exception);
    }
    
}
