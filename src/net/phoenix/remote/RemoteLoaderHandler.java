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
    public static final String EXCEPTION_EXTRA = "ign:extra_exception";
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

    public RemoteLoaderHandler(String resUrl) {
        this.resourceUrl = resUrl;
        init(resUrl);
    }

    public RemoteLoaderHandler(Looper looper, String resUrl) {
        super(looper);
        init(resUrl);
    }

    private void init(String resUrl) {
        this.resourceUrl = resUrl;
    }

    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == HANDLER_MESSAGE_ID) {
            handleLoadedMessage(msg);
        }
    }
    
    /**
     * Handles a message from the runnable. Depending on the type of message,
     * the proper callback is called.
     * You should not override this, override the callback methods instead.
     * 
     * @param msg The message sent by the Runnable.
     */
    protected final void handleLoadedMessage(Message msg) {
        Bundle data = msg.getData();
        
        String extraType = data.getString(TYPE_EXTRA);
        byte[] object = data.getByteArray(OBJECT_EXTRA);
        
        if (extraType == BEFORE_EXTRA) {
        	before();
        } else if (extraType == MEM_HIT_EXTRA) {
        	onMemoryHit(object);
        } else if (extraType == MEM_MISS_EXTRA) {
        	onMemoryMiss();
        } else if (extraType == DISK_HIT_EXTRA) {
        	onDiskHit(object);
        } else if (extraType == DISK_MISS_EXTRA) {
        	onDiskMiss();
        } else if (extraType == NET_HIT_EXTRA) {
        	onNetHit(object);
        } else if (extraType == NET_MISS_EXTRA) {
        	onNetMiss();
        } else if (extraType == SUCCESS_EXTRA) {
        	success(object);
        } else if (extraType == ERROR_EXTRA) {
        	error((Exception) data.get(EXCEPTION_EXTRA));
        }
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
    	Log.d(TAG, "The RemoteLoader has finished working for key: " + resourceUrl);
    };

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resUrl) {
        this.resourceUrl = resUrl;
    }
}
