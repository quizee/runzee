package com.example.myrunzeeapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;


public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<AddFriendItem> friendItems;

    public AddFriendAdapter(Context context, ArrayList<AddFriendItem> friendItems){
        this.context = context;
        this.friendItems = friendItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profile_picture;
        TextView user_name;
        CheckBox invite_friend;

        public MyViewHolder(View itemView){
            super(itemView);
            profile_picture = itemView.findViewById(R.id.profile_picture);
            user_name = itemView.findViewById(R.id.user_name);
            invite_friend = itemView.findViewById(R.id.invite_friend);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_friend_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.user_name.setText(friendItems.get(i).username);

        if (friendItems.get(i).profileUrl == null) {
            Drawable default_icon = context.getResources().getDrawable(R.drawable.profile);
            Glide.with(context).load(default_icon).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
        } else {
            Glide.with(context).load(friendItems.get(i).profileUrl).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile_picture);
        }

        myViewHolder.invite_friend.setChecked(friendItems.get(i).isInvited);
        if(friendItems.get(i).alreadyInvited){
            Log.e("AddFriendAdapter", "onBindViewHolder: "+friendItems.get(i).username+"씨는 체크박스 비활성화" );
            myViewHolder.invite_friend.setChecked(true);
            myViewHolder.invite_friend.setEnabled(false);
        }

        myViewHolder.invite_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭한 결과가 체크된 상태라면
                if(myViewHolder.invite_friend.isChecked()){
                    friendItems.get(i).isInvited = true;
                }else{
                    friendItems.get(i).isInvited = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendItems.size();
    }

}
