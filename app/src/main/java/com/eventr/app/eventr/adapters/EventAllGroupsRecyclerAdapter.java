package com.eventr.app.eventr.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.eventr.app.eventr.GroupDetailActivity;
import com.eventr.app.eventr.R;
import com.eventr.app.eventr.models.EventGroup;

import java.security.acl.Group;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 05/09/16.
 */
public class EventAllGroupsRecyclerAdapter extends RecyclerView.Adapter<EventAllGroupsRecyclerAdapter.ViewHolder> {
    private List<EventGroup> mItems;

    public EventAllGroupsRecyclerAdapter(List<EventGroup> items) {
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_group_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        try {
            EventGroup group = mItems.get(i);
            viewHolder.alphabetIndexView.setText(group.getName().substring(0, 1).toUpperCase());
            viewHolder.groupNameView.setText(group.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.group_alphabet_index) public TextView alphabetIndexView;
        @BindView(R.id.group_title) public TextView groupNameView;
        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int groupIndex = getAdapterPosition();
            Intent intent = new Intent(v.getContext(), GroupDetailActivity.class);
            intent.putExtra(v.getContext().getString(R.string.intent_event_detail_key), mItems.get(groupIndex));
            v.getContext().startActivity(intent);
        }
    }
}
