package com.eventr.app.eventr.models;

import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Suraj on 05/09/16.
 */
public class EventGroup implements Serializable {
    private int id, ownerId;
    private String fbEventId, name;
    private Date createdAt;
    public EventGroup(JSONObject group) {
        setGroupDetail(group);
    }

    public void setGroupDetail(JSONObject group) {
        try {
            this.id = group.getInt("id");
            this.ownerId = group.getInt("owner_id");
            this.name = group.getString("name");
            this.fbEventId = group.getString("fb_event_id");
            this.createdAt = Utils.getDateFromString(group.getString("created_at"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public String getFbEventId() {
        return this.fbEventId;
    }

    public String getName() {
        return this.name;
    }

    public String getDateAndMonth() {
        return Utils.getDateAndMonth(this.createdAt);
    }

    public String getTimeString() {
        return Utils.getTimeString(this.createdAt);
    }
}
