/* Copyright (c) 2009-2011 Matthias Kaeppler
 * Copyright (c) 2012 Francesco Pontillo, Sebastiano Poggi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.phoenix.remote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Handler class that manages returns from an RemoteLoaderJob.
 * 
 * @author Matthias Kaeppler
 * @author Francesco Pontillo
 * @author Sebastiano Poggi
 *
 */
public class RemoteLoaderHandler extends Handler {

	private static final String TAG = RemoteLoaderHandler.class.getSimpleName();
	
    public static final int HANDLER_MESSAGE_ID = 0;
    public static final String OBJECT_EXTRA = "ign:extra_object";
    public static final String URL_EXTRA = "ign:extra_url";
    public static final String TYPE_EXTRA = "ign:extra_type";
    
    public static final String BEFORE_EXTRA = "ign:extra_before";
    public static final String MEM_HIT_EXTRA = "ign:extra_mem_hit";
    public static final String MEM_MISS_EXTRA = "ign:extra_mem_miss";
    public static final String DISK_HIT_EXTRA = "ign:extra_disk_hit";
    public static final String DISK_MISS_EXTRA = "ign:extra_disk_miss";
    public static final String NET_HIT_EXTRA = "ign:extra_net_hit";
    public static final String NET_MISS_EXTRA = "ign:extra_net_miss";
    public static final String SUCCESS_EXTRA = "ign:extra_success";
    public static final String ERROR_EXTRA = "ign:extra_error";
    public static final String AFTER_EXTRA = "ign:extra_after";

    private String resourceUrl;

    public RemoteLoaderHandler(String imageUrl) {
        this.resourceUrl = imageUrl;
        init(imageUrl);
    }

    public RemoteLoaderHandler(Looper looper, String imageUrl) {
        super(looper);
        init(imageUrl);
    }

    private void init(String imageUrl) {
        this.resourceUrl = imageUrl;
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == HANDLER_MESSAGE_ID) {
            handleLoadedMessage(msg);
        }
    }

    protected final void handleLoadedMessage(Message msg) {
        Bundle data = msg.getData();
        
        String extraType = data.getString(TYPE_EXTRA);
        byte[] object = data.getByteArray(OBJECT_EXTRA);
        handleLoaded(object, msg);
    }

    /**
     * Override this method if you need custom handler logic. Note that this method can actually be
     * called directly for performance reasons, in which case the message will be null
     * 
     * @param object
     *            the object returned from the loader
     * @param msg
     *            the handler message; can be null
     * @return TODO: add description
     */
    protected boolean handleLoaded(byte[] object, Message msg) {
        return true;
    }
    
    /**
     * Called before doing anything.
     */
    protected void before() {
    	Log.d(TAG, "Getting resource for key: " + resourceUrl);
    };
    /**
     * Called as soon as there's a memory (RAM) hit.
     * @param object The found object.
     */
    protected void onMemoryHit(byte[] object) {
    	Log.d(TAG, "Key: " + resourceUrl + " found in memory.");
    	success(object);
    }
    /**
     * Called if there's a memory (RAM) miss.
     */
    protected void onMemoryMiss() {
    	Log.d(TAG, "Key: " + resourceUrl + " NOT found in memory.");
    }
    /**
     * Called if there's a disk hit.
     * @param object The found object.
     */
    protected void onDiskHit(byte[] object) {
    	Log.d(TAG, "Key: " + resourceUrl + " found on disk.");
    	success(object);
    }
    /**
     * Called if there's a disk miss.
     */
    protected void onDiskMiss() {
    	Log.d(TAG, "Key: " + resourceUrl + " NOT found on disk.");
    }
    /**
     * Called if there's a network hit.
     * @param object The found object.
     */
    protected void onNetHit(byte[] object) {
    	Log.d(TAG, "Key: " + resourceUrl + " found in the network.");
    	success(object);
    }
    /**
     * Called if there's a network miss.
     */
    protected void onNetMiss() {
    	Log.d(TAG, "Key: " + resourceUrl + " NOT in the network.");
    }
    /**
     * Called if the object exists somewhere in this universe.
     * @param object The found object.
     */
    protected void success(byte[] object) {
    	Log.d(TAG, "Success for key: " + resourceUrl);
    	after(object);
    };
    /**
     * Called if the object does not exists anywhere or if there
     * was an unhandled exception.
     * @param e The {@link Exception}.
     */
    protected void error(Exception e) {
    	Log.d(TAG, "Exception when getting resource for key: " + resourceUrl);
    	Log.d(TAG, e.getMessage());
    	after(null);
    };
    /**
     * Called after the execution, no matter what happened.
     * @param object The object returned, or null if none was found.
     */
    protected void after(byte[] object) {
    	Log.d(TAG, "Got a resource for key: " + resourceUrl);
    };

    public String getImageUrl() {
        return resourceUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.resourceUrl = imageUrl;
    }
}
