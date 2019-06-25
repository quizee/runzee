package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.MyPictureHolder> {

    ArrayList<PictureData> pictureList = new ArrayList<>();
    PictureItemClickListener itemClickListener;
    Context context;

    public PictureAdapter(ArrayList<PictureData> list, Context context){
        this.pictureList = list;
        this.context = context;
    }

    public static class MyPictureHolder extends RecyclerView.ViewHolder{//뷰 홀더
        ImageView medal_picture;
        TextView what_belt;
        TextView what_kilometer;
        ImageView isitMine;

        public MyPictureHolder(View itemView) {
            super(itemView);
            medal_picture = itemView.findViewById(R.id.picture);
            what_belt = itemView.findViewById(R.id.what_belt);
            what_kilometer = itemView.findViewById(R.id.what_kilometer);
            isitMine = itemView.findViewById(R.id.isitMine);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyPictureHolder holder, final int position) {

        final  PictureData pictureData = pictureList.get(position);//리스트에서 아이템을 하나씩 꺼내서 정보를 꺼낸다.
        int medal_id = pictureData.getPicture_id();
        String level = pictureData.getWhat_level();
        double kilometer = pictureData.getWhat_kilometer();
        int blurFrom = 1;

        holder.what_belt.setText(level);
        holder.what_kilometer.setText(String.format("%.2f",kilometer)+" 킬로미터");
        holder.medal_picture.setImageResource(medal_id);

        SharedPreferences runListPref;
        if(LoginActivity.my_info != null) {
            runListPref = context.getSharedPreferences(LoginActivity.my_info.get("email"), Activity.MODE_PRIVATE);
        }else{
            runListPref = context.getSharedPreferences(context.getSharedPreferences("auto",Activity.MODE_PRIVATE).getString("auto_email",""),Activity.MODE_PRIVATE);
        }
        if(runListPref != null){
            Float total_distance = runListPref.getFloat("total_distance",-1);
            if(total_distance != -1){
                if(total_distance<50){
                    blurFrom = 1; //화이트 벨트까지
                }else if(total_distance>=50 && total_distance<250){
                    blurFrom = 2; //옐로우 벨트까지
                }else if(total_distance>=250 && total_distance<500){
                    blurFrom = 3; //블루 벨트까지
                }else if(total_distance>=500 && total_distance<2500){
                    blurFrom = 4; //퍼플 벨트까지
                }else if(total_distance>=2500 && total_distance<10000){
                    blurFrom = 5; //블랙 벨트까지
                }else if(total_distance>=10000){
                    blurFrom = 6; //레드 벨트까지(가려지는 거 없음)
                }
            }
        }
        if(position>=blurFrom){
            holder.isitMine.setBackgroundColor(Color.rgb(0xEE,0xEE,0xEE));
            holder.isitMine.getBackground().setAlpha(220);
            //holder.isitMine.getBackground().setColorFilter(Color.parseColor("#63FFFFFF"), PorterDuff.Mode.);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.OnItemClick(position,pictureData);
            }
        });
    }

    @NonNull
    @Override
    public MyPictureHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.picture_item, viewGroup,false);
        return new MyPictureHolder(view);//여기까지 하면 TextView tv = findViewByid(R.id.tv)까지 해놓은 느낌임.
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }
    public void setOnItemClickListener(PictureItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
