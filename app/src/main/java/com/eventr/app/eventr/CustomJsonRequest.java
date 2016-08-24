package com.eventr.app.eventr;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Suraj on 24/08/16.
 */
public class CustomJsonRequest extends JsonObjectRequest {
    private String accessToken;
    public CustomJsonRequest(int method, String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String accTkn) {
        super(method, url, jsonObject, listener, errorListener);
        accessToken = accTkn;
    }

    public CustomJsonRequest(String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String accTkn) {
        super(url, jsonObject, listener, errorListener);
        accessToken = accTkn;
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
