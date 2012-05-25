package net.phoenix.remote;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.github.ignition.support.cache.AbstractCache;
import com.github.ignition.support.cache.ImageCache;

/**
 * Runnable that retrieves a resource from the web and an {@link AbstractCache}
 * (if it has been set), and calls several methods of {@link RemoteLoaderHandler}
 * to inform the caller of resource and search state changes.
 * 
 * @author Matthias Kaeppler
 * @author Francesco Pontillo
 * @author Sebastiano Poggi
 *
 */
@SuppressWarnings("rawtypes")
public class RemoteLoaderJob implements Runnable {

    private static final String LOG_TAG = "Phoenix/Loader";

    private static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 1000;
    
    private String resourceUrl;
    private RemoteLoaderHandler handler;
	private AbstractCache cache;
    private int numRetries, defaultBufferSize;

    public RemoteLoaderJob(String url, RemoteLoaderHandler handler, AbstractCache cache,
            int numRetries, int defaultBufferSize) {
        this.resourceUrl = url;
        this.handler = handler;
        this.cache = cache;
        this.numRetries = numRetries;
        this.defaultBufferSize = defaultBufferSize;
    }
    
    // TODO: continue here.
    
    /**
     * The job method run on a worker thread. It will first query the cache, and on a miss,
     * download the image from the Web.
     */
    @Override
    public void run() {
        Bitmap bitmap = null;

        if (cache != null) {
            // at this point we know the image is not in memory, but it could be cached to SD card
            bitmap = cache.getBitmap(resourceUrl);
        }

        if (bitmap == null) {
            bitmap = downloadImage();
        }

        notifyImageLoaded(resourceUrl, bitmap);
    }

    // TODO: we could probably improve performance by re-using connections instead of closing them
    // after each and every download
    protected Bitmap downloadImage() {
        int timesTried = 1;

        while (timesTried <= numRetries) {
            try {
                byte[] imageData = retrieveImageData();

                if (imageData == null) {
                    break;
                }

                if (cache != null) {
                    cache.put(resourceUrl, imageData);
                }

                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            } catch (Throwable e) {
                Log.w(LOG_TAG, "download for " + resourceUrl + " failed (attempt " + timesTried + ")");
                e.printStackTrace();
                SystemClock.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
                timesTried++;
            }
        }

        return null;
    }

    protected byte[] retrieveImageData() throws IOException {
        URL url = new URL(resourceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // determine the image size and allocate a buffer
        int fileSize = connection.getContentLength();
        Log.d(LOG_TAG, "fetching image " + resourceUrl + " (" + (fileSize <= 0 ? "size unknown" : Integer.toString(fileSize)) + ")");

        BufferedInputStream istream = new BufferedInputStream(connection.getInputStream());

        try {   
            if (fileSize <= 0) {
                Log.w(LOG_TAG,
                        "Server did not set a Content-Length header, will default to buffer size of "
                                + defaultBufferSize + " bytes");
                ByteArrayOutputStream buf = new ByteArrayOutputStream(defaultBufferSize);
                byte[] buffer = new byte[defaultBufferSize];
                int bytesRead = 0;
                while (bytesRead != -1) {
                    bytesRead = istream.read(buffer, 0, defaultBufferSize);
                    if (bytesRead > 0)
                        buf.write(buffer, 0, bytesRead);
                }
                return buf.toByteArray();
            } else {
                byte[] imageData = new byte[fileSize];
        
                int bytesRead = 0;
                int offset = 0;
                while (bytesRead != -1 && offset < fileSize) {
                    bytesRead = istream.read(imageData, offset, fileSize - offset);
                    offset += bytesRead;
                }
                return imageData;
            }
        } finally {
            // clean up
            try {
                istream.close();
                connection.disconnect();
            } catch (Exception ignore) { }
        }
    }

    protected void notifyImageLoaded(String url, Bitmap bitmap) {
        Message message = new Message();
        message.what = RemoteLoaderHandler.HANDLER_MESSAGE_ID;
        Bundle data = new Bundle();
        data.putString(RemoteLoaderHandler.IMAGE_URL_EXTRA, url);
        Bitmap image = bitmap;
        data.putParcelable(RemoteLoaderHandler.BITMAP_EXTRA, image);
        message.setData(data);

        handler.sendMessage(message);
    }
}
