package com.example.myrunzeeapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CrewFeedAdapter extends RecyclerView.Adapter<CrewFeedAdapter.ViewHolder> {

    ArrayList<FeedItem> feedItems;
    Context context;

    public CrewFeedAdapter(ArrayList<FeedItem> feedItems, Context context){
        this.feedItems = feedItems;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView cardview_image;

        public ViewHolder(View itemView){
            super(itemView);
            cardview_image = itemView.findViewById(R.id.cardview_image);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(holder.cardview_image).load(feedItems.get(position).getImg_url()).into(holder.cardview_image);
        holder.cardview_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed_url = feedItems.get(position).getInfo_url();
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(feed_url));
                context.startActivity(browse);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feedcrew_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }
}
