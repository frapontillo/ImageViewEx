package net.frakbot.imageviewex.listener;

import net.frakbot.imageviewex.ImageViewNext;

import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

public abstract class ImageViewExRequestListener implements RequestListener {
	protected ImageViewNext mImageViewNext;
	
	public ImageViewExRequestListener(ImageViewNext imageViewNext) {
		this.mImageViewNext = imageViewNext;
	}
}
