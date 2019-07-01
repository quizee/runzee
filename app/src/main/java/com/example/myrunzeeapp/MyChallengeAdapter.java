package com.example.myrunzeeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MyChallengeAdapter extends RecyclerView.Adapter<MyChallengeAdapter.MyViewHolder> {

    Context context;
    ArrayList<ChallengeDTO> ctos;//이게 어댑터에서 사용하는 리스트
    ArrayList<ChallengeItem> challengeItems = new ArrayList<>();//넘겨주기용

    int count = 0;
    int child_count = 0;

    public MyChallengeAdapter(Context context, ArrayList<ChallengeDTO> ctos){
        this.context = context;
        this.ctos = ctos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_challenge_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        Glide.with(context).load(ctos.get(i).cover_url).into(myViewHolder.cover);
        myViewHolder.date.setText(ctos.get(i).start_date+" - "+ctos.get(i).end_date);
        myViewHolder.title.setText(ctos.get(i).title);

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeItems.clear();
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("challenge").child(ctos.get(i).challenge_id).orderByValue().addChildEventListener(new ChildEventListener() {
                    //거리순으로 한명씩 uid를 가져온다.
                    @Override
                    public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                        //한명씩 거리를 가져옴 dataSnapshot -> andrew : 5.5km
                        Log.e("MyChallengeAdapter", "onChildAdded: "+dataSnapshot.getKey()+"의 프로필을 가져오겠습니다.");
                        child_count++;

                        database.getReference("userlist").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                //dataSnapshot2 -> andrew의 프사
                                count ++;
                                Log.e("MyChallengeAdapter", "onDataChange: "+ dataSnapshot2.getValue(UserDTO.class).uid+"의 프로필 가져왔슴돠ㅏㅏㅏㅏㅏ");
                                ChallengeItem challengeItem = new ChallengeItem(dataSnapshot2.getValue(UserDTO.class),dataSnapshot.getValue(Double.class));
                                if(!challengeItems.contains(challengeItem)) {
                                    challengeItems.add(challengeItem);
                                }
                                Log.e("MyChallengeAdapter", count+"만큼 모였습니다.");
                                if(count == child_count){
                                    Log.e("MyChallengeAdapter", "다 모였으니 넘어갈게여");
                                    count = 0;
                                    child_count = 0;
                                    Intent intent = new Intent(context,CreatedChallengeActivity.class);
                                    intent.putExtra("challenge_info",ctos.get(i));
                                    intent.putExtra("leader_board",challengeItems);
                                    ((Activity) context).startActivityForResult(intent, 24);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //멤버 수만큼 다 모이고 나면 액티비티 이동을 한다.

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return ctos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        //cover title date
        ImageView cover;
        TextView title;
        TextView date;

        public MyViewHolder(View itemView){
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
        }
    }
}
