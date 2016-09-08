package com.eventr.app.eventr.adapters;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.CustomJsonRequest;
import com.eventr.app.eventr.EventrRequestQueue;
import com.eventr.app.eventr.R;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.models.GroupMember;
import com.eventr.app.eventr.utils.CustomDialogFragment;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Suraj on 07/09/16.
 */
public class MembersRecyclerAdapter extends RecyclerView.Adapter<MembersRecyclerAdapter.ViewHolder> {
    private List<GroupMember> mItems;
    private ImageLoader imageLoader;
    private String userRole;
    private FragmentManager fm;
    private Context mContext;
    private String accessToken;
    private String actionType;
    private CustomDialogFragment createAdminDialog, markAttendanceDialog, requestActionDialog;

    public MembersRecyclerAdapter(List<GroupMember> items, String actionType, FragmentManager fm, Context context, String accessToken) {
        mItems = items;
        this.fm = fm;
        this.mContext = context;
        this.accessToken = accessToken;
        this.actionType = actionType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        GroupMember item = mItems.get(i);
        viewHolder.nameView.setText(item.getName());

        if (item.getRole().equals("admin") || item.getRole().equals("owner")) {
            viewHolder.memberRole.setVisibility(View.VISIBLE);
            viewHolder.memberRole.setText(Utils.capitalize(item.getRole()));
        }

        imageLoader = EventrRequestQueue.getInstance().getImageLoader();
        String picUrl = item.getPicUrl();
        if (picUrl != null) {
            imageLoader.get(picUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (response.getBitmap() != null) {
                        viewHolder.picView.setImageBitmap(response.getBitmap());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
        }

        if (viewHolder.showAttendanceStatus) {
            if (item.isEventAttended()) {
                viewHolder.memberAttendanceLabel.setText("Attended");
            } else {
                viewHolder.memberAttendanceLabel.setText("Unattended");
            }
            viewHolder.memberAttendanceLabel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.memberAttendanceLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.member_pic) CircleImageView picView;
        @BindView(R.id.member_name) TextView nameView;
        @BindView(R.id.member_role) TextView memberRole;
        @BindView(R.id.member_attendance_label) TextView memberAttendanceLabel;

        private static final String ROLE_ADMIN = "admin";
        private static final String ROLE_OWNER = "owner";
        private static final String CREATE_ADMIN_URL = "http://52.26.148.176/api/v1/make-admin/";
        private static final String MARK_ATTENDANCE_URL = "http://52.26.148.176/api/v1/mark-attendance/";
        private static final String REQUEST_TAG = "group_detail_actions";
        private static final String REQUEST_ACTION_URL = "http://52.26.148.176/api/v1/confirm-member/";
        private boolean showAttendanceStatus = false;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setClickable(true);

            if (actionType.equals("EVENT_OVER_OWNER_ACTIONS")) {
                showAttendanceStatus = true;
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (actionType.equals("EVENT_OVER_ADMIN_ACTIONS")) {
                        return;
                    }

                    if (actionType.equals("EVENT_ACTIVE_ADMIN_REQUEST_LIST_ACTIONS")) {
                        requestClickHandler();
                        return;
                    }

                    if (actionType.equals("EVENT_ACTIVE_ADMIN_ACTIVE_LIST_ACTIONS")) {
                        return;
                    }

                    if (actionType.equals("EVENT_OVER_OWNER_ACTIONS")) {
                        markAttendanceClickHandler();
                        return;
                    }

                    if (actionType.equals("EVENT_ACTIVE_OWNER_REQUEST_LIST_ACTIONS")) {
                        requestClickHandler();
                        return;
                    }

                    if (actionType.equals("EVENT_ACTIVE_OWNER_ACTIVE_LIST_ACTIONS")) {
                        makeAdminClickHandler();
                    }
                }
            });
        }

        public void makeAdminClickHandler() {
            int index = getAdapterPosition();
            if (mItems.get(index).getRole().equals(ROLE_ADMIN) || mItems.get(index).getRole().equals(ROLE_OWNER)) {
                return;
            }
            createAdminDialog = CustomDialogFragment.newInstance("confirm", false);

            createAdminDialog.setTitle("Make Admin");
            createAdminDialog.setMessage("Do you want to make this user admin");

            createAdminDialog.setPositiveButton("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makeAdmin();
                }
            });
            createAdminDialog.setNegativeButton("No", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createAdminDialog.dismiss();
                }
            });

            createAdminDialog.show(fm, "CREATE_ADMIN_DIALOG");
        }

        private void markAttendanceClickHandler() {
            int index = getAdapterPosition();
            if (mItems.get(index).isEventAttended()) {
                return;
            }
            markAttendanceDialog = CustomDialogFragment.newInstance("confirm", true);

            markAttendanceDialog.setTitle("Mark Attendance");
            markAttendanceDialog.setMessage("Did user attend this event");

            markAttendanceDialog.setPositiveButton("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    markAttendance(true);
                }
            });
            markAttendanceDialog.setNegativeButton("No", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    markAttendance(false);
                }
            });

            markAttendanceDialog.show(fm, "MARK_ATTENDANCE_DIALOG");
        }

        private void requestClickHandler() {
            int index = getAdapterPosition();
            if (mItems.get(index).getRole().equals(ROLE_ADMIN) || mItems.get(index).getRole().equals(ROLE_OWNER)) {
                return;
            }
            requestActionDialog = CustomDialogFragment.newInstance("confirm", true);

            requestActionDialog.setTitle("Group Join Request");
            requestActionDialog.setMessage("Accept Join Request");

            requestActionDialog.setPositiveButton("Accept", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupRequest(true);
                }
            });
            requestActionDialog.setNegativeButton("Reject", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupRequest(false);
                }
            });

            requestActionDialog.show(fm, "MARK_ATTENDANCE_DIALOG");
        }

        private void makeAdmin() {
            boolean isInternetConnected = Utils.isInternetConnected(mContext);
            if (!isInternetConnected) {
                makeAdminOnInternetFail();
                return;
            }
            createAdminDialog.showProgressBar();

            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    createAdminDialog.hideProgressBar();
                    createAdminDialog.hideError();
                    createAdminDialog.dismiss();
                    onCreateAdmin();
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    createAdminDialog.hideProgressBar();
                    if (error.networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            makeAdminOnInternetFail();
                        }

                        if (error.getClass().equals(NoConnectionError.class)) {
                            makeAdminOnInternetFail();
                        }
                    } else {
                        NetworkResponse networkResponse = error.networkResponse;
                        createAdminDialog.dismiss();
                        if (networkResponse.statusCode == 401) {
                            Utils.logout(mContext);
                        }
                    }
                }
            };

            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, CREATE_ADMIN_URL + mItems.get(getAdapterPosition()).getUuid(), null, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        }

        private void makeAdminOnInternetFail() {
            createAdminDialog.showError("No internet connection");
        }

        private void onCreateAdmin() {
            mItems.get(getAdapterPosition()).setRole(ROLE_ADMIN);
            notifyDataSetChanged();
        }

        private void markAttendance(boolean state) {
            boolean isInternetConnected = Utils.isInternetConnected(mContext);
            if (!isInternetConnected) {
                markAttendanceOnInternetFail();
                return;
            }
            markAttendanceDialog.showProgressBar();

            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    markAttendanceDialog.hideProgressBar();
                    markAttendanceDialog.hideError();
                    markAttendanceDialog.dismiss();
                    onMarkAttendance();
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    markAttendanceDialog.hideProgressBar();
                    if (error.networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            markAttendanceOnInternetFail();
                        }

                        if (error.getClass().equals(NoConnectionError.class)) {
                            markAttendanceOnInternetFail();
                        }
                    } else {
                        NetworkResponse networkResponse = error.networkResponse;
                        markAttendanceDialog.dismiss();
                        if (networkResponse.statusCode == 401) {
                            Utils.logout(mContext);
                        }
                    }
                }
            };

            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, MARK_ATTENDANCE_URL + mItems.get(getAdapterPosition()).getUuid(), null, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        }

        private void groupRequest(boolean state) {
            boolean isInternetConnected = Utils.isInternetConnected(mContext);
            if (!isInternetConnected) {
                groupRequestOnInternetFail();
                return;
            }
            requestActionDialog.showProgressBar();

            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    requestActionDialog.hideProgressBar();
                    requestActionDialog.hideError();
                    requestActionDialog.dismiss();
                    onRequestAction();
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    requestActionDialog.hideProgressBar();
                    if (error.networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            groupRequestOnInternetFail();
                        }

                        if (error.getClass().equals(NoConnectionError.class)) {
                            groupRequestOnInternetFail();
                        }
                    } else {
                        NetworkResponse networkResponse = error.networkResponse;
                        requestActionDialog.dismiss();
                        if (networkResponse.statusCode == 401) {
                            Utils.logout(mContext);
                        }
                    }
                }
            };

            JSONObject requestObject = new JSONObject();
            try {
                if (state) {
                    requestObject.put("state", "1");
                } else {
                    requestObject.put("state", "2");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new CustomJsonRequest(Request.Method.POST, REQUEST_ACTION_URL + mItems.get(getAdapterPosition()).getUuid(), requestObject, listener, errorListener, accessToken);
            request.setTag(REQUEST_TAG);
            EventrRequestQueue.getInstance().add(request);
        }

        private void markAttendanceOnInternetFail() {
            markAttendanceDialog.showError("No internet connection");
        }

        private void groupRequestOnInternetFail() {
            requestActionDialog.showError("No internet connection");
        }

        private void onMarkAttendance() {
            mItems.get(getAdapterPosition()).markAttended();
            notifyDataSetChanged();
        }

        private void onRequestAction() {
            mItems.remove(getAdapterPosition());
            notifyDataSetChanged();
        }
    }
}
