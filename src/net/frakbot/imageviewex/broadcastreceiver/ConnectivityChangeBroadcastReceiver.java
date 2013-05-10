package net.frakbot.imageviewex.broadcastreceiver;

import net.frakbot.imageviewex.ImageViewNext;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * BroadcastReceiver for receiving information about the network state.
 * 
 * @author Francesco Pontillo
 */
public class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {

	private ImageViewNext mImageViewNext;

	/**
	 * Constructor, initializes the ImageViewNext to be used to retry the
	 * network operation after the connection is restored.
	 * 
	 * @param imageViewNext
	 *            The ImageViewNext instance.
	 */
	public ConnectivityChangeBroadcastReceiver(ImageViewNext imageViewNext) {
		mImageViewNext = imageViewNext;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Get the NetworkInfo Parcelable
		NetworkInfo networkInfo = intent
				.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

		// Check for the connection
		boolean isConnected = networkInfo.isConnected();
		if (isConnected) {
			mImageViewNext.retryFromNetworkIfPossible();
		}
	}

}
