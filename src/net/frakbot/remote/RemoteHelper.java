package net.frakbot.remote;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * Helper class that exposes some utility methods for retrieving
 * objects from the network.
 * 
 * @author Francesco Pontillo
 *
 */
public class RemoteHelper {
    private static final String LOG_TAG = "Loader";
    
	private static final int defaultBufferSize = 2048;
    
	/**
	 * Download an object from the network.
	 * 
	 * @param resourceUrl	The URL of then rsource.
	 * @return				Byte array of the downloaded object.
	 * @throws IOException	If the connection cannot be established.
	 */
    public static byte[] download(String resourceUrl) throws IOException {
        URL url = new URL(resourceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        
        // determine the image size and allocate a buffer
        int fileSize = connection.getContentLength();
        Log.d(LOG_TAG, "fetching " + resourceUrl
        		+ " (" + (fileSize <= 0 ? "size unknown" : Integer.toString(fileSize)) + ")");

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

}
