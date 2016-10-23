package com.eventr.app.eventr.models;

import android.util.Log;

import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Suraj on 05/09/16.
 */
public class EventGroup implements Serializable {
    private int id, ownerId;
    private String fbEventId,
            name,
            channelName,
            uuid,
            eventName,
            userGroupStatus,
            inviteText,
            userAttendedStatus = "",
            userRole = MEMBER_ROLE;
    private Date createdAt;
    private boolean isEventOver, isUserOwner, isUserAdmin;

    private static final String ADMIN_ROLE = "admin";
    private static final String OWNER_ROLE = "owner";
    private static final String MEMBER_ROLE = "member";

    private static final String ATTENDED_TEXT = "attended";
    private static final String NOT_ATTENDED_TEXT = "not_attended";

    public EventGroup(JSONObject group) {
        setGroupDetail(group);
    }

    public void setGroupDetail(JSONObject group) {
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
            this.channelName = group.getString("channel_url");
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
            this.userRole = ADMIN_ROLE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.isUserOwner = group.getBoolean("is_current_user_owner");
            this.userRole = OWNER_ROLE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.userGroupStatus = group.getString("current_user_group_status");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.inviteText = group.getString("invite_text");
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

    public  void setUserGroupStatus(String status) {
        this.userGroupStatus = status;
    }

    public void setUserGroupStatusFromMembers(List<GroupMember> members, String userFbId) {
        GroupMember mem = null;
        for(int i = 0; i < members.size(); i++) {
            if (members.get(i).getFbId().equals(userFbId)) {
                mem = members.get(i);
                break;
            }
        }

        if (mem == null) {
            this.userGroupStatus = "not_requested";
            this.isUserAdmin = false;
            this.isUserOwner = false;
            return;
        }

        if (mem.getRole().equals(OWNER_ROLE)) {
            this.isUserOwner = true;
            this.isUserAdmin = true;
            this.userGroupStatus = "approved";
            return;
        }

        if (mem.getRole().equals(ADMIN_ROLE)) {
            this.isUserAdmin = true;
            this.userGroupStatus = "approved";
            return;
        }

        if (mem.isEventAttended()) {
            this.userAttendedStatus = ATTENDED_TEXT;
        } else {
            this.userAttendedStatus = NOT_ATTENDED_TEXT;
        }

        this.userGroupStatus = mem.getStatus();
    }

    public String getUserRole() {
        return this.userRole;
    }

    public String attendedEventStatus() {
        return this.userAttendedStatus;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public String getInviteText() {
        return this.inviteText;
    }
}
