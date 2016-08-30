package com.eventr.app.eventr;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Suraj on 24/08/16.
 */
public class Events {
    private String name, id, description, picUrl;
    private JSONObject location;

    public Events() {

    }

    public Events(String id, String name, String description, String picUrl, JSONObject location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.picUrl = picUrl;
        this.location = location;
    }

    public  Events(JSONObject event) {
        try {
            this.id = event.getString("id");
            this.name = event.getString("name");
            this.picUrl = (event.getJSONObject("cover")).getString("source");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return this.picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
