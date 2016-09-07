package com.eventr.app.eventr.models;

import android.util.Log;

import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Suraj on 05/09/16.
 */
public class EventGroup implements Serializable {
    private int id, ownerId;
    private String fbEventId, name, uuid, eventName, userGroupStatus;
    private Date createdAt;
    private boolean isEventOver, isUserOwner, isUserAdmin;

    public EventGroup(JSONObject group) {
        setGroupDetail(group);
    }

    public void setGroupDetail(JSONObject group) {
        Log.d("GROUP_DETAIL", group.toString());
        try {
            this.id = group.getInt("id");
            this.ownerId = group.getInt("owner_id");
            this.name = group.getString("name");
            this.fbEventId = group.getString("fb_event_id");
            this.uuid = group.getString("uuid");
            this.createdAt = Utils.getDateFromString(group.getString("created_at"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.eventName = group.getString("event_name");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.isEventOver = group.getBoolean("is_event_over");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.isUserAdmin = group.getBoolean("is_current_user_admin");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.isUserOwner = group.getBoolean("is_current_user_owner");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.userGroupStatus = group.getString("current_user_group_status");
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

    public String getUuid() {
        return this.uuid;
    }

    public boolean isEventOver() {
        return this.isEventOver;
    }

    public String getEventName() {
        if (this.eventName != null) {
            return this.eventName;
        } else {
            return "";
        }
    }

    public boolean isUserOwner() {
        return this.isUserOwner;
    }

    public boolean isUserAdmin() {
        return this.isUserAdmin;
    }

    public String joinRequestStatus() {
        return this.userGroupStatus;
    }
}
