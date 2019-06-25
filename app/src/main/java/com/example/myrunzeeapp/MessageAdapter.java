package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
    private long time_now = System.currentTimeMillis();
    public final static int request_code = 6;
    public final static int cheer_code = 7;

    public MessageAdapter(Context context, ArrayList<MessageItem> messageItems){
        this.messageItems = messageItems;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView msg_content;
        TextView time_ago;
        ImageView sender_profile;
        public MyViewHolder(View itemView){
            super(itemView);
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

        //지난 시간
        long passed_time = time_now - messageItems.get(i).msg.when_made;
        SimpleDateFormat time_format = new SimpleDateFormat("MM-dd-HH-mm");
        String string_time = time_format.format(passed_time);
        String words[] = string_time.split("-");

        String time_ago = "";
        if (Integer.parseInt(words[0]) > 0) {//월이 있다면
            time_ago = words[0] + "달 전";
        } else if (Integer.parseInt(words[1]) > 0) {//일이 있다면
            time_ago = words[1] + "일 전";
        } else if(Integer.parseInt(words[2])>0){//시간이 있다면
            time_ago = words[2]+"시간 전";
        } else if(Integer.parseInt(words[3])>0){//분이 있다면
            time_ago = words[3]+"분 전";
        }
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
                        intent1.putExtra("username",messageItems.get(i).sender_name);//username으로 충분할지는 지켜봐야함
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



}
