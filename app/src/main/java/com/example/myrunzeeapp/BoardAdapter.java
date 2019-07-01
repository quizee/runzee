package com.example.myrunzeeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.MyViewHolder> {
    Context context;
    ArrayList<ChallengeItem> boardItems = new ArrayList<>();
    double maxDistance;

    public BoardAdapter(Context context, double maxDistance, ArrayList<ChallengeItem> boardItems){
        this.context = context;
        this.maxDistance = maxDistance;
        this.boardItems = boardItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.board_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.rank.setText(String.valueOf(i+1));
        myViewHolder.username.setText(boardItems.get(i).userDTO.name);
        Glide.with(myViewHolder.profile).load(boardItems.get(i).userDTO.profile_url).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile);
        if(boardItems.get(i).userDistance >= 0.0){
            myViewHolder.userkm.setText("0.00 km");
        }else{
            myViewHolder.userkm.setText(String.format("%.2f",-boardItems.get(i).userDistance)+" km");
        }
        // / 7.0 km
        myViewHolder.total.setText(" / "+String.format("%.2f",maxDistance)+" km");
        myViewHolder.visualize.setIndeterminate(true);
        myViewHolder.visualize.setMax((int)maxDistance);
        myViewHolder.visualize.setProgress((int)boardItems.get(i).userDistance);

        ValueAnimator animator = ValueAnimator.ofInt(0, (int)boardItems.get(i).userDistance);
        animator.setDuration(800);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation){
                myViewHolder.visualize.setProgress((Integer)animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // start your activity here
            }
       });
        animator.start();

    }

    @Override
    public int getItemCount() {
        return boardItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rank;
        ImageView profile;
        TextView username;
        ProgressBar visualize;
        TextView userkm;
        TextView total;
        public MyViewHolder(View itemView){
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            profile = itemView.findViewById(R.id.profile);
            username = itemView.findViewById(R.id.username);
            visualize = itemView.findViewById(R.id.visualize);
            userkm = itemView.findViewById(R.id.userkm);
            total = itemView.findViewById(R.id.total);
        }
    }

}
