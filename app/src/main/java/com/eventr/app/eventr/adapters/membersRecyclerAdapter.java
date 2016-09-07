package com.eventr.app.eventr.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.eventr.app.eventr.EventrRequestQueue;
import com.eventr.app.eventr.R;
import com.eventr.app.eventr.models.EventGroup;
import com.eventr.app.eventr.models.GroupMember;

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

    public MembersRecyclerAdapter(List<GroupMember> items) {
        mItems = items;
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
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.member_pic) CircleImageView picView;
        @BindView(R.id.member_name) TextView nameView;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
