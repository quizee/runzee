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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<FriendItem> friendItems;
    private HashMap<String, Integer> friendMap;
    String key;

    public FriendListAdapter(Context context, HashMap<String,Integer> friendMap, ArrayList<FriendItem> friendItems){
        this.context = context;
        this.friendMap = friendMap;
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
        //이미 친구 신청 내역이 있는지 확인해본다.
        //친구 신청인 경우에만 내 uid를 키로 메시지를 보낸다.
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(friendItems.get(i).uid!=null) {
            //상대방 메세지함에 이미 내 uid를 키로 한 메시지가 간 내역이 있는지 확인한다.
            database.getReference().child("messages").child(friendItems.get(i).uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.child(auth.getCurrentUser().getUid()).exists()) {//아직 보낸 내역이 없을 때
                        myViewHolder.state.setText("친구 신청");
                    } else {//보낸 내역이 있을 때
                        myViewHolder.state.setText("신청 취소");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            myViewHolder.state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myViewHolder.state.getText().equals("친구 신청")) {
                        //그 사람 수신함에 친구 신청이 간다
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        //key = database.getReference().child("messages").child(friendItems.get(i).uid).push().getKey();
                        //sender_uid, message_uid, message_type
                        MessageDTO msgDTO = new MessageDTO(auth.getCurrentUser().getUid(), auth.getCurrentUser().getUid(), "request");//친구 신청할 때는 sender uid가 곧 message uid이다
                        database.getReference().child("messages").child(friendItems.get(i).uid).child(auth.getCurrentUser().getUid()).setValue(msgDTO);//수신자의 메세지함에 메세지가 하나씩 쌓인다.
                        myViewHolder.state.setText("신청 취소");

                    } else if (myViewHolder.state.getText().equals("신청 취소")) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        database.getReference().child("messages").child(friendItems.get(i).uid).child(auth.getCurrentUser().getUid()).removeValue();//해당 메세지를 지운다
                        myViewHolder.state.setText("친구 신청");
                    }
                }
            });
            if (friendItems.get(i).img_url == null) {
                Drawable default_icon = context.getResources().getDrawable(R.drawable.profile);
                Glide.with(context).load(default_icon).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
            } else {
                Glide.with(context).load(friendItems.get(i).img_url).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
            }
            myViewHolder.user_name.setText(friendItems.get(i).name);

            if (friendMap.containsKey(friendItems.get(i).uid)) {
                myViewHolder.know_eachother.setText(friendMap.get(friendItems.get(i).uid) + "명의 서로 아는 친구");
            } else {
                myViewHolder.know_eachother.setText("");
            }
        }

    }

    @Override
    public int getItemCount() {
        return friendItems.size();
    }
}
