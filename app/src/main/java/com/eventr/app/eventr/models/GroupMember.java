package com.eventr.app.eventr.models;

import android.util.Log;

import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

/**
 * Created by Suraj on 07/09/16.
 */
public class GroupMember {
    private int id;
    private String picUrl, name, fbId, email, role, userUuid, uuid, status;
    private boolean eventAttended;

    public GroupMember(JSONObject member) {
        try {
            this.id = member.getInt("id");
            this.name = member.getString("name");
            this.picUrl = member.getString("pic_url");
            this.email = member.getString("email");
            this.role = member.getString("role");
            this.eventAttended = member.getBoolean("event_attended");
            this.userUuid = member.getString("user_uuid");
            this.fbId = member.getString("fb_id");
            this.uuid = member.getString("member_uuid");
            this.status = member.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPicUrl() {
        return this.picUrl;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getUserUuid() {
        return this.userUuid;
    }

    public String getFbId() {
        return this.fbId;
    }

    public int getId() {
        return this.id;
    }

    public boolean isEventAttended() {
        return this.eventAttended;
    }

    public String getRole() {
        return this.role;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getStatus() {
        return this.status;
    }
}
