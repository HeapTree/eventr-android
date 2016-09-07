package com.eventr.app.eventr;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.eventr.app.eventr.utils.LruBitmapCache;

/**
 * Created by Suraj on 21/08/16.
 */
public class EventrRequestQueue extends Application {
    private RequestQueue mRequestQueue;
    private static EventrRequestQueue mInstance;
    private ImageLoader mImageLoader;

    private static final String DEFAULT_TAG = "request_tag";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static synchronized EventrRequestQueue getInstance () {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void cancel() {
        mRequestQueue.cancelAll(DEFAULT_TAG);
    }

    public void cancel(String tag) {
        mRequestQueue.cancelAll(tag);
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }
}
