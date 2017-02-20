package com.eventr.app.eventr;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Suraj on 24/08/16.
 */
public class CustomJsonRequest extends JsonObjectRequest {
    private static final int TIMEOUT_MS = 8000;
    private static final int MAX_RETRIES = 2;
    private String accessToken;
    public CustomJsonRequest(int method, String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String accTkn) {
        super(method, url, jsonObject, listener, errorListener);
        accessToken = accTkn;
        setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public CustomJsonRequest(String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String accTkn) {
        super(url, jsonObject, listener, errorListener);
        accessToken = accTkn;
        setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        if (accessToken != null) {
            headers.put("AUTHORIZATION", "Token token=" + accessToken);
        }
        return headers;
    }

}
