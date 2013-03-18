package net.frakbot.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.jakewharton.DiskLruCache.Editor;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
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
                editor.commit();
                return true;
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
}
