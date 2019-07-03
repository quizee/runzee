package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    ArrayList<MessageItem> messageItems = new ArrayList<>();
    Context context;
    public long time_now = System.currentTimeMillis();
    public final static int request_code = 6;
    public final static int cheer_code = 7;

    public MessageAdapter(Context context, ArrayList<MessageItem> messageItems){
        this.messageItems = messageItems;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout message;
        TextView msg_content;
        TextView time_ago;
        ImageView sender_profile;
        public MyViewHolder(View itemView){
            super(itemView);
            message = itemView.findViewById(R.id.message);
            msg_content = itemView.findViewById(R.id.msg_content);
            time_ago = itemView.findViewById(R.id.time_ago);
            sender_profile = itemView.findViewById(R.id.sender_profile);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item,viewGroup,false);
        return new MessageAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {

        //프로필사진
        Glide.with(context).load(messageItems.get(i).sender_url).apply(RequestOptions.circleCropTransform()).into(myViewHolder.sender_profile);

        //내용
        myViewHolder.msg_content.setText(messageItems.get(i).contents);

        //지난 (ms)
        long passed_time = time_now - messageItems.get(i).msg.when_made;
        //분으로 환산
        passed_time = passed_time/60000;

        String time_ago = "";
        if ((passed_time/1440)>0) {//일이 있다면
            time_ago = passed_time/1440 + "일 전";
        } else if ((passed_time/60) > 0) {//시간이 있다면
            time_ago = passed_time/60 + "시간 전";
        } else if((passed_time>0)){
            time_ago = passed_time+"분 전";
        } else{
            time_ago = "방금";
        }

        Log.e("NotifActivity time", "onBindViewHolder: "+passed_time+"분 흘렀음" );
        myViewHolder.time_ago.setText(time_ago);

        //클릭했을 때 내용
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (messageItems.get(i).msg.message_type){
                    /*
                     *  친구 요청 == request
                     *  친구 수락 == accept
                     *  응원메세지 == cheer
                     * */
                    case "request":
                    case "accept"://친구 요청이나 수락일 경우
                        Intent intent = new Intent(context,RequestMessageActivity.class);
                        intent.putExtra("msg_item",messageItems.get(i));
                        //message uid, sender uid, type, username, url, content, time 다 들어가 있음
                        ((Activity)context).startActivityForResult(intent, request_code);
                        break;
                    case "cheer"://응원메시지일 경우
                        Intent intent1 = new Intent(context,CheerActivity.class);
                        intent1.putExtra("sender",messageItems.get(i).sender_name);
                        intent1.putExtra("download_url",messageItems.get(i).msg.download_url);
                        ((Activity)context).startActivityForResult(intent1, cheer_code);
                        break;
                        default:break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    public void removeItem(int position){
        messageItems.remove(position);
        notifyItemRemoved(position);
    }



}
