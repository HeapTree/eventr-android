package com.eventr.app.eventr;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suraj on 04/08/16.
 */
public class EventListRecyclerAdapter extends RecyclerView.Adapter<EventListRecyclerAdapter.ViewHolder> {
    private List<Events> mItems;

    EventListRecyclerAdapter(List<Events> items) {
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        try {
            Events item = mItems.get(i);
            viewHolder.titleView.setText(item.getName());
            viewHolder.imageView.setImageUrl(item.getPicUrl(), EventrRequestQueue.getInstance().getImageLoader());
            viewHolder.dateView.setText(item.getDateAndMonth());
            viewHolder.locationView.setText(item.getLocationString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
//        return mItems.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView titleView;
        protected NetworkImageView imageView;
        protected TextView dateView;
        protected TextView locationView;
        ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
            this.titleView = (TextView) v.findViewById(R.id.event_card_title);
            this.imageView = (NetworkImageView) v.findViewById(R.id.event_pic);
            this.dateView = (TextView) v.findViewById(R.id.start_time);
            this.locationView = (TextView) v.findViewById(R.id.event_location);
        }

        @Override
        public void onClick(View v) {
            int eventIndex = getAdapterPosition();
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra(v.getContext().getString(R.string.intent_event_detail_key), mItems.get(eventIndex));
            v.getContext().startActivity(intent);
        }
    }
}
