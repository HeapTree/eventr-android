package com.eventr.app.eventr.models;

import android.util.Log;

import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Suraj on 24/08/16.
 */
public class Event implements Serializable {
    private String name, id, description, picUrl;
    private String location;
    private Date date;
    private String rsvpStatus;
    private int attendingCount;

    public Event() {
    }

    public  Event(JSONObject event, String rsvpStatus, boolean alt) {
        setDetails(event, rsvpStatus, alt);
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

    public String getPicUrl() {
        return this.picUrl;
    }

    public String getDateAndMonth() {
        return Utils.getDateAndMonth(this.date);
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocationString() {
        try {
            JSONObject placeObj = new JSONObject(this.location);
            String name = placeObj.getString("name");
            JSONObject loc = placeObj.getJSONObject("location");
            String city = loc.getString("city");
            String country = loc.getString("country");
            return name + ", " + city + ", " + country;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDetails(JSONObject event, String rsvpStatus, boolean alt) {
        this.rsvpStatus = rsvpStatus;
        try {
            this.id = event.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.name = event.getString("name");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!event.isNull("description")) {
            try {
                this.description = event.getString("description");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (alt) {
                this.picUrl = event.getString("coverPicture");

            } else {
                this.picUrl = (event.getJSONObject("cover")).getString("source");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (alt) {
                this.location = event.getJSONObject("venue").toString();
            } else {
                this.location = event.getJSONObject("place").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (alt) {
                this.attendingCount = (event.getJSONObject("stats")).getInt("attending");
            } else {
                this.attendingCount = event.getInt("attending_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (alt) {
                this.date = Utils.getDateFromString(event.getString("startTime"));
            } else {
                this.date = Utils.getDateFromString(event.getString("start_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTimeString() {
        return Utils.getTimeString(this.date);
    }

    public String getRsvpStatus() {
        return this.rsvpStatus;
    }

    public int getAttendingCount() {
        return this.attendingCount;
    }

    public String getAttendingString() {
        switch(this.attendingCount) {
            case 0: {
                return "No one is going to this event as of now";
            }
            case 1: {
                return "One person is going to this event";
            }
            default: {
                return this.attendingCount + " people are going to this event";
            }
        }
    }

    public void setRsvpStatus(String status) {
        this.rsvpStatus = status;
    }
}
