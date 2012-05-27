package net.phoenix.remote;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import net.phoenix.cache.BytesCache;

import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

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
public class RemoteLoaderJob implements Runnable {

    private static final String LOG_TAG = "Phoenix/Loader";

    private static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 1000;
    
    private String resourceUrl;
    private RemoteLoaderHandler handler;
	private BytesCache cache;
    private int numRetries, defaultBufferSize;

    public RemoteLoaderJob(String url, RemoteLoaderHandler handler, BytesCache cache,
            int numRetries, int defaultBufferSize) {
        this.resourceUrl = url;
        this.handler = handler;
        this.cache = cache;
        this.numRetries = numRetries;
        this.defaultBufferSize = defaultBufferSize;
    }
    
    /**
     * The job method run on a worker thread. It will first query the cache, and on a miss,
     * download the object from the Web.
     * The message handler will be called for every steps, informing about the status.
     */
    @Override
    public void run() {
    	byte[] object = null;
    	
    	// Put everything in a try block, so to get any error and call for ERROR_EXTRA
    	try {
	    	// BEFORE
	    	notifySomething(object, null, RemoteLoaderHandler.BEFORE_EXTRA);
	    	
	    	// See if we have a cache
	    	if (cache != null) {
	    		// Look for the object in cache
	    		boolean inMem = false;
	    		boolean onDisk = false;
	    		inMem = cache.containsKeyInMemory(resourceUrl);
	    		if (!inMem && cache.isDiskCacheEnabled()) onDisk = cache.containsKeyOnDisk(resourceUrl);
	    		// If is is in cache, get it
	    		if (inMem || onDisk)
	    			object = cache.get(resourceUrl);
	    		
	    		// Call the proper hit/miss callbacks
	    		if (object == null) {
	    			// If the object wasn't found, call both MISSes
	    			notifySomething(object, null, RemoteLoaderHandler.MEM_MISS_EXTRA);
	    			notifySomething(object, null, RemoteLoaderHandler.DISK_MISS_EXTRA);
	    		}
	    		else if (inMem) notifySomething(object, null, RemoteLoaderHandler.MEM_HIT_EXTRA);
	    		else if (onDisk) notifySomething(object, null, RemoteLoaderHandler.DISK_HIT_EXTRA);
	    		// At this point, the object can still be null
	    	}
	    	
	    	// Checks if the object was already got
	    	if (object == null) {
	    		// Gets the object from the network
	    		object = downloadObject();
		    	// NET_HIT if found in network cache
		    	if (object != null) notifySomething(object, null, RemoteLoaderHandler.NET_HIT_EXTRA);
		    	// NET_MISS if not found in network cache
		    	else notifySomething(object, null, RemoteLoaderHandler.NET_MISS_EXTRA);
	    	}
    	} catch (Throwable e) {
    		// ERROR if there's an exception and the object is not found
    	}

		if (object == null) notifySomething(
				null, new Exception("Object was not found."), 
				RemoteLoaderHandler.ERROR_EXTRA);
        
        // AFTER is automatically called by the success/error methods.
    }
    

    // TODO: we could probably improve performance by re-using connections instead of closing them
    // after each and every download
    protected byte[] downloadObject() {
        int timesTried = 1;
        
        // Tries to download for a few times, if set
        while (timesTried <= numRetries) {
            try {
                byte[] data = retrieveData();

                if (data == null) {
                    break;
                }

                if (cache != null) {
                    cache.put(resourceUrl, data);
                }

                return data;

            } catch (Throwable e) {
                Log.w(LOG_TAG, "download for " + resourceUrl + " failed (attempt " + timesTried + ")");
                e.printStackTrace();
                SystemClock.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
                timesTried++;
            }
        }

        return null;
    }
    

    protected byte[] retrieveData() throws IOException {
        URL url = new URL(resourceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        
        // determine the image size and allocate a buffer
        int fileSize = connection.getContentLength();
        Log.d(LOG_TAG, "fetching " + resourceUrl + " (" + (fileSize <= 0 ? "size unknown" : Integer.toString(fileSize)) + ")");

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
                byte[] data = new byte[fileSize];
        
                int bytesRead = 0;
                int offset = 0;
                while (bytesRead != -1 && offset < fileSize) {
                    bytesRead = istream.read(data, offset, fileSize - offset);
                    offset += bytesRead;
                }
                return data;
            }
        } finally {
            // clean up
            try {
                istream.close();
                connection.disconnect();
            } catch (Exception ignore) { }
        }
    }


    protected void notifySomething(byte[] object, Throwable e, String messageType) {
    	// Create a message
        Message message = new Message();
        message.what = RemoteLoaderHandler.HANDLER_MESSAGE_ID;
        Bundle data = new Bundle();
        
        // Sets the message type
        data.putString(RemoteLoaderHandler.TYPE_EXTRA, messageType);
        data.putByteArray(RemoteLoaderHandler.OBJECT_EXTRA, object);
        data.putSerializable(RemoteLoaderHandler.EXCEPTION_EXTRA, e);
        
        // Set the data and send the message to the handler
        message.setData(data);
        handler.sendMessage(message);
    }
}
