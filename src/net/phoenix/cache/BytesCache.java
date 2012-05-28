package net.phoenix.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.ignition.support.cache.*;

/**
 * Implements a cache capable of caching byte arrays.
 * It exposes helper methods to immediately access binary data as {@link byte}[] objects.
 * 
 * @author Matthias Kaeppler
 * @author Francesco Pontillo
 * 
 */
public class BytesCache extends AbstractCache<String, byte[]> {
	
    public BytesCache(int initialCapacity, long expirationInMinutes, int maxConcurrentThreads) {
        super("BytesCache", initialCapacity, expirationInMinutes, maxConcurrentThreads);
    }

    public synchronized void removeAllWithPrefix(String urlPrefix) {
        CacheHelper.removeAllWithStringPrefix(this, urlPrefix);
    }

    @Override
    public String getFileNameForKey(String imageUrl) {
        return CacheHelper.getFileNameFromUrl(imageUrl);
    }

    @Override
    protected byte[] readValueFromDisk(File file) throws IOException {
        BufferedInputStream istream = new BufferedInputStream(new FileInputStream(file));
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException("Cannot read files larger than " + Integer.MAX_VALUE + " bytes");
        }

        int dataLength = (int) fileSize;

        byte[] data = new byte[dataLength];
        istream.read(data, 0, dataLength);
        istream.close();

        return data;
    }

    @Override
    protected void writeValueToDisk(File file, byte[] data) throws IOException {
        BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(file));
        ostream.write(data);
        ostream.close();
    }
}
