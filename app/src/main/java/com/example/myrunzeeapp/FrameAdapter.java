package com.example.myrunzeeapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder> {
    ArrayList<FrameItem> frameList;
    PickFrameClickListener itemClickListener;
    private Context context;
    int row_index = -1;

    public FrameAdapter(ArrayList<FrameItem> frameList, Context context){
        this.frameList = frameList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView frame_img;
        ConstraintLayout picked_layout;
        public ViewHolder(View itemView){
            super(itemView);
            frame_img = itemView.findViewById(R.id.frame_img);
            picked_layout = itemView.findViewById(R.id.picked_layout);
        }
    }
    public void setOnItemClickListener(PickFrameClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FrameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.frame_layout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FrameAdapter.ViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.OnItemClick(position,frameList.get(position));
                row_index = position;
                notifyDataSetChanged();
            }
        });
        if(row_index == position){
            holder.picked_layout.setBackgroundColor(Color.parseColor("#FFC0CB"));
        }else{
            holder.picked_layout.setBackgroundColor(Color.TRANSPARENT);
        }
        Glide.with(holder.frame_img).load(frameList.get(position).getImg_url()).override(300,200).centerCrop().into(holder.frame_img);
    }

    @Override
    public int getItemCount() {
        return frameList.size();
    }
}
