package com.eventr.app.eventr;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.eventr.app.eventr.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Suraj on 04/08/16.
 */
public class EventViewPagerFragment extends Fragment {
    private static final String TAB_POSITION = "tab_position";
    private static final String EVENTS_URL = "http://52.26.148.176/api/v1/events?rsvp_state=attending";
    private EventListRecyclerAdapter listAdapter;
    private SharedPreferences userPreferences;
    private String accessToken;
    private SwipeRefreshLayout swipeRefreshLayout;
//    private JSONArray items = new JSONArray();
    private ArrayList<Events> items = new ArrayList<Events>();

    public EventViewPagerFragment() {

    }

    public static EventViewPagerFragment newInstance(int tabPosition) {
        EventViewPagerFragment fragment = new EventViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int tabPosition = args.getInt(TAB_POSITION);
        View v =  inflater.inflate(R.layout.event_list_fragment, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeRefreshEvents);
        setSwipeRefreshListener();

        int screenSize = getActivity().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                break;
            default:
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        }

        try {
            listAdapter = new EventListRecyclerAdapter(items);
            recyclerView.setAdapter(listAdapter);
        } catch (Exception e) {

        }

        userPreferences = getContext().getSharedPreferences(getContext().getString(R.string.user_preference_file_key), Context.MODE_PRIVATE);
        accessToken = userPreferences.getString(getContext().getString(R.string.access_token_key), null);


        fetchEvents(false);

        return v;
    }

    private void fetchEvents(final Boolean swipeRefresh) {
        boolean isInternetConnected = Utils.isInternetConnected(getContext());
        Log.d("INTERNET", "is internet connected " + isInternetConnected);
        if (!isInternetConnected) {
            if (swipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
            }
            onInternetFail();
        } else {
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (swipeRefresh) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    try {
                        JSONArray events = (response.getJSONObject("data")).getJSONArray("data");
                        for (int i = 0; i < events.length(); i++) {
                            JSONObject event = (JSONObject) events.get(i);
                            Events item = new Events(event);
                            items.add(item);
                        }
                        listAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (swipeRefresh) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    error.printStackTrace();

                    if (error.networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            onInternetFail();
                        }

                        if (error.getClass().equals(NoConnectionError.class)) {
                            onInternetFail();
                        }
                    } else {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse.statusCode == 401) {
                            Utils.logout(getContext());
                        }
                    }
                }
            };

            String eventsUrl = EVENTS_URL;
            Log.d("ACCESS_TOKEN", accessToken);
            JsonObjectRequest request = new CustomJsonRequest(eventsUrl, null, listener, errorListener, accessToken);
            EventrRequestQueue.getInstance().add(request);
        }
    }

    private void onInternetFail() {
        Utils.showActionableSnackBar(getActivity().findViewById(R.id.coordinator_main), "Internet connection failed", "Retry", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchEvents(false);
            }
        });
    }

    private void setSwipeRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchEvents(true);
            }
        });
    }
}
