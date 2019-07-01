package com.example.myrunzeeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.Iterator;


public class BeltFriendAdapter extends RecyclerView.Adapter<BeltFriendAdapter.MyViewHolder> {

    ArrayList<BeltFriendItem> beltFriends = new ArrayList<>();
    Context context;
    ArrayList<String> myFriendList = new ArrayList<>();
    ArrayList<String> yourFriendList = new ArrayList<>();

    private static String TAG = "BeltFriendAdapter";
    public BeltFriendAdapter(Context context, ArrayList<BeltFriendItem> beltFriends){
        this.context = context;
        this.beltFriends = beltFriends;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.beltfriend_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        Glide.with(myViewHolder.profile).load(beltFriends.get(i).profile).apply(RequestOptions.circleCropTransform()).into(myViewHolder.profile);
        myViewHolder.username.setText(beltFriends.get(i).username);
        myViewHolder.state_message.setText(beltFriends.get(i).statemessage);

    }

    @Override
    public int getItemCount() {
        return beltFriends.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        ImageView profile;
        TextView username;
        TextView state_message;
        public MyViewHolder(View view){
            super(view);
            profile = view.findViewById(R.id.profile);
            username = view.findViewById(R.id.username);
            state_message = view.findViewById(R.id.state_message);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem block = menu.add(Menu.NONE, 1001, 1, "친구 끊기");
            block.setOnMenuItemClickListener(onBlockMenu);
        }
        private final MenuItem.OnMenuItemClickListener onBlockMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case 1001:
                        int position = getAdapterPosition();
                        // 나의 친구목록에서도 이 사람을 제거하고 이 사람의 친구목록에서도 나를 제거한다.

                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        FirebaseAuth auth = FirebaseAuth.getInstance();

                        final String you = beltFriends.get(position).uid;
                        final String me = auth.getCurrentUser().getUid();

                        //내 친구 목록에서 이 사람을 지움
                        database.getReference("userlist").child(me).child("friendList").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                myFriendList = (ArrayList<String>)dataSnapshot.getValue();
                                Iterator<String> iter = myFriendList.iterator();
                                while(iter.hasNext()){
                                    String friend = iter.next();
                                    if(friend.equals(you)) {
                                        iter.remove();
                                    }
                                }
                                /*
                                for(String friend: myFriendList){
                                if(friend.equals(you)){
                                    myFriendList.remove(you);
                                }
                            }*/
                                Log.e(TAG, "onDataChange: "+myFriendList+"를 나에게 다시 씌운다");
                                database.getReference("userlist").child(me).child("friendList").setValue(myFriendList);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //그 사람 친구 목록에서 나를 지움
                        database.getReference("userlist").child(you).child("friendList").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                yourFriendList = (ArrayList<String>)dataSnapshot.getValue();
                                Iterator<String> iter = yourFriendList.iterator();
                                while(iter.hasNext()){
                                    String friend = iter.next();
                                    if(friend.equals(me)) {
                                        iter.remove();
                                    }
                                }
                                /*
                                for(String friend: yourFriendList){
                                    if(friend.equals(me)){
                                        yourFriendList.remove(me);
                                    }
                                }*/
                                Log.e(TAG, "onDataChange: "+yourFriendList+"를 너에게 다시 씌운다");
                                database.getReference("userlist").child(you).child("friendList").setValue(yourFriendList);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        break;
                }
                return true;
            }
        };
    }
}
