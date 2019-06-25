package com.example.myrunzeeapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<FriendItem> friendItems;
    String key;

    public FriendListAdapter(Context context, ArrayList<FriendItem> friendItems){
        this.context = context;
        this.friendItems = friendItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profile_picture;
        TextView user_name;
        TextView know_eachother;
        TextView state;
        public MyViewHolder(View itemView){
            super(itemView);
            profile_picture = itemView.findViewById(R.id.profile_picture);
            user_name = itemView.findViewById(R.id.user_name);
            know_eachother = itemView.findViewById(R.id.know_eachother);
            state = itemView.findViewById(R.id.state);
        }
    }

    @NonNull
    @Override
    public FriendListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_item,viewGroup,false);
        return new FriendListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {

        myViewHolder.state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myViewHolder.state.getText().equals("친구 신청")) {
                    //그 사람 수신함에 친구 신청이 간다
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    key = database.getReference().child("messages").child(friendItems.get(i).uid).push().getKey();
                    //sender_uid, message_uid, message_type
                    MessageDTO msgDTO = new MessageDTO(auth.getCurrentUser().getUid(),key,"request");
                    database.getReference().child("messages").child(friendItems.get(i).uid).child(key).setValue(msgDTO);//수신자의 메세지함에 메세지가 하나씩 쌓인다.
                    myViewHolder.state.setText("신청 취소");

                }else if(myViewHolder.state.getText().equals("신청 취소")){
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference().child("messages").child(friendItems.get(i).uid).child(key).removeValue();//해당 메세지를 지운다
                    myViewHolder.state.setText("친구 신청");
                }
            }
        });
        if(friendItems.get(i).img_url == null){
            Drawable default_icon = context.getResources().getDrawable(R.drawable.profile);
            Glide.with(context).load(default_icon).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
        }
        else {
            Glide.with(context).load(friendItems.get(i).img_url).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
        }
        myViewHolder.user_name.setText(friendItems.get(i).name);

    }

    @Override
    public int getItemCount() {
        return friendItems.size();
    }
}
