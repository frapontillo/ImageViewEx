package net.frakbot.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import com.jakewharton.DiskLruCache.Editor;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class CacheHelper {
	
	public static File getDiskCacheDir(Context context, String uniqueName) {
	    // Check if media is mounted or storage is built-in, if so,
		// try and use external cache dir, otherwise use internal cache dir.
        final String cachePath =
            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                    !isExternalStorageRemovable() ?
                    getExternalCacheDir(context).getPath() :
                    context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Environment.isExternalStorageRemovable();
    }

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
    
    /**
     * Writes a byte array into a DiskLruCache {@link Editor}.
     * 
     * @param source		The input byte array.
     * @param editor		The {@link Editor} to write the byte array into.
     * 
     * @return				true if there were no errors, false otherwise.
     * @throws IOException	If there was an error while writing the file.
     */
    public static boolean writeByteArrayToEditor(byte[] source, Editor editor) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), source.length);
            editor.newOutputStream(0).write(source);
            return true;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    /**
     * Encodes URLs with the SHA-256 algorithm.
     * @param uri	The URL to encode.
     * 
     * @return		The encoded URL.
     * 
     * @throws NoSuchAlgorithmException		If the SHA-256 algorithm is not found.
     * @throws UnsupportedEncodingException	If the UTF-8 encoding is not supported.
     */
    public static String UriToDiskLruCacheString(String uri) throws
    														 NoSuchAlgorithmException,
    														 UnsupportedEncodingException {
    	MessageDigest digest = MessageDigest.getInstance("SHA-256");
    	byte[] convBytes = digest.digest(uri.getBytes("UTF-8"));
    	String result;
    	StringBuilder sb = new StringBuilder();
    	for (byte b : convBytes) {
    	    sb.append(String.format("%02X", b));
    	}
    	result = sb.toString();
    	result = result.toLowerCase(Locale.US);
    	return result;
    }
}
