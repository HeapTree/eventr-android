package com.eventr.app.eventr;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Suraj on 04/08/16.
 */
public class EventListRecyclerAdapter extends RecyclerView.Adapter<EventListRecyclerAdapter.ViewHolder> {
    private List<String> mItems;

    EventListRecyclerAdapter(List<String> items) {
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String item = mItems.get(i);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            v.getContext().startActivity(intent);
        }
    }
}
