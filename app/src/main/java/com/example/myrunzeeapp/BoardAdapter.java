package com.example.myrunzeeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    class UpdateNumbers extends AsyncTask<Void, Integer, Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

    }

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
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
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
        //myViewHolder.visualize.setIndeterminate(true);

        //myViewHolder.visualize.invalidate();
       // myViewHolder.visualize.setMax((int)maxDistance);
        myViewHolder.visualize.setMax(100);
        int my_race = (int)((- boardItems.get(i).userDistance/ maxDistance) *100);
        Log.e("BoardAdapter", "onBindViewHolder:내 기록: "+my_race );
        Log.e("BoardAdapter", "onBindViewHolder: "+boardItems.get(i).userDistance);

        if(my_race>100){
            myViewHolder.visualize.setProgress(100);
        }else{
            myViewHolder.visualize.setProgress(my_race);
        }

        //myViewHolder.visualize.setProgress((int)boardItems.get(i).userDistance);
        //myViewHolder.visualize.getProgressDrawable().mutate();

/*
        new AsyncTask<Void, Integer, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                myViewHolder.visualize.setMax((int)maxDistance);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                myViewHolder.visualize.setProgress(values[0]);
               // myViewHolder.userkm.setText();

            }

            @Override
            protected Integer doInBackground(Void... voids) {

                int max = 0;

                if((int)boardItems.get(i).userDistance > (int)maxDistance) {//뛴 거리가 목표거리보다 높다면
                    max = (int)maxDistance;//목표거리까지 막아놓기기
                }else{
                   max= (int) boardItems.get(i).userDistance;
                }

               // int[] values = new int[2];

                for (int k = 0; k < max; k++) {
                    //values[0] = k;
                    publishProgress(k);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
            }
        }.execute();
*/

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
