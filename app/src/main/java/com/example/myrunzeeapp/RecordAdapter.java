package com.example.myrunzeeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.service.autofill.UserData;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {

    private ArrayList<RunningItem> items;// 아이템을 전달받는다.
    OnItemClickListener onItemClickListener;
    public RecordAdapter(ArrayList<RunningItem> items){//어뎁터 만들 때 매개변수로 아이템 arraylist 전달해야함
        this.items = items;
    }

    //layout manager가 recyclerview에 각 아이템을 생성하기 위해 이 메소드를 부른다
    //뷰 홀더 객체를 반환한다.
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.record_recycler,viewGroup,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    //적절한 뷰에 적절한 정보를 위치시키기 위해 layout manager가 이 메소드를 부른다.
    // 그러면 일단 데이터가 필요하겠지
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, final int position) {
        final RunningItem item = items.get(position);//각 데이터 객체에 들어있는 정보를 뷰에 적용시킨다.

        //객체에 담긴 정보는 다섯가지
        String date = item.getDate();
        String title = item.getTitle();
        double km = item.getKm();
        int pace_seconds = item.getPace_seconds();
        int runtime_seconds = item.getRuntime_seconds();
        String filename = item.getFilename();

        int minute = runtime_seconds/60;
        int left_second = runtime_seconds%60;
        String runtime;
        if(minute<10){
            if(left_second<10){
                runtime = "0"+minute+":0"+left_second;
            }else{
                runtime = "0"+minute+":"+left_second;
            }
        }else{
            if(left_second<10){
                runtime = minute+":0"+left_second;
            }else{
                runtime = minute+":"+left_second;
            }
        }//뛴 시간

        km = Math.round(km*100)/100.0;//뛴 거리

        String pace = pace_seconds/60+"\'"+pace_seconds%60+"\'\'"+"/km";//페이스

        //일단 제목은 전처리 건너뛴다(사용자가 직접 입력하는 식)

        viewHolder.RunDate.setText(date);//받을 때부터 잘 받자
        viewHolder.RunTime.setText(runtime);
        viewHolder.RunKm.setText(km+"km");
        viewHolder.RunPace.setText(pace);
        viewHolder.RunTitle.setText(title);

        viewHolder.InsideBtn.setImageResource(R.drawable.ic_right_24dp);

        //사진 설정하는 부분
        Bitmap bm = BitmapFactory.decodeFile(filename);
        if(filename!=null) {
            Glide.with(viewHolder.RunflowImage.getContext()).load(bm).into(viewHolder.RunflowImage);
        }else{
            viewHolder.RunflowImage.setImageResource(R.drawable.ic_clip_24dp);//사진 안찍었으면 클립모양 기본으로
        }

        // 아맞다 뷰 홀더 만들 때 레이아웃을 매개변수로 받지...
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position,item);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.onItemClickListener = itemClickListener;
    }
    //layout manager가 adapter의 적절한 메소드를 호출해서 각 아이템을 만든다.
    @Override
    public int getItemCount() {
        return items.size();
    }

    //each item is object of the viewholder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        //아이템 하나에 뭐가 들어가는지
        ImageView RunflowImage;
        ImageView InsideBtn;
        TextView RunDate;
        TextView RunTitle;
        TextView RunKm;
        TextView RunPace;
        TextView RunTime;

        public MyViewHolder(View itemView) {//레이아웃 객체를 전달받는다
            super(itemView);
            RunflowImage = itemView.findViewById(R.id.runflow_image);
            InsideBtn = itemView.findViewById(R.id.inside_btn);
            RunDate = itemView.findViewById(R.id.run_date);
            RunTitle = itemView.findViewById(R.id.run_title);
            RunKm = itemView.findViewById(R.id.run_km);
            RunPace = itemView.findViewById(R.id.run_pace);
            RunTime = itemView.findViewById(R.id.run_time);

        }
    }
    public void UpdateData(int position, RunningItem runningItem){
        //왜 어뎁터에서 리스트를 받아서 할까
        items.remove(position);
        items.add(position,runningItem);//그 위치에 아이템을 넣는다.
        notifyItemChanged(position);
        notifyDataSetChanged();
    }
}
