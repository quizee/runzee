package com.example.myrunzeeapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class MakeFriendActivity extends AppCompatActivity {

    private static String TAG = "MakeFriendActivity";

    MaterialSearchView searchView;
    RecyclerView friend_recycler;
    ArrayList<FriendItem> friendItems = new ArrayList<>();
    ArrayList<FriendItem> defaultItems = new ArrayList<>();
    FriendListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_friend);
        database = FirebaseDatabase.getInstance();

        //사람 추가될 때마다 호출
        database.getReference().child("userlist").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e(TAG, "onChildAdded: "+dataSnapshot);
                String name = dataSnapshot.child("name").getValue(String.class);
                String url = dataSnapshot.child("profile_url").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String uid = dataSnapshot.child("uid").getValue(String.class);

                friendItems.add(new FriendItem(name,url,email,uid));
                adapter.notifyDataSetChanged();
                Log.e(TAG, "onChildAdded: "+friendItems.size() );
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

//        database.getReference().child("userlist").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//글자가 하나 바뀔 때마다 데이터가 넘어온다. 자동으로 새로고침
//
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        //friendItems.add(new FriendItem("지윤","https://i.kisscc0.com/20180813/qje/kisscc0-rgb-color-model-computer-icons-encapsulated-postsc-rgb-rgb-pattern-5b7120ba3e3828.8901997715341406022549.png"));
        //friendItems.add(new FriendItem("은흠","https://i.kisscc0.com/20180705/ble/kisscc0-geometry-software-design-pattern-symmetry-color-colorful-geometric-pattern-5b3ddd67940658.5929628215307810316063.png"));

        friend_recycler = findViewById(R.id.friend_recycler);
        adapter = new FriendListAdapter(MakeFriendActivity.this, defaultItems);//처음에는 아무것도 안보이게 !
        layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        friend_recycler.setAdapter(adapter);
        friend_recycler.setLayoutManager(layoutManager);

        //원래는 데이터베이스에서 가져왔다고 치고! 일단 더미 데이터를 넣어서 검색이 되는지 봐야겠다

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//액션바를 없애고 이거로 쓰겠다.
        getSupportActionBar().setTitle("친구 이메일 검색");
        toolbar.setTitleTextColor(Color.WHITE);

        searchView = findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                FriendListAdapter adapter1 = new FriendListAdapter(MakeFriendActivity.this, defaultItems);
                friend_recycler.setAdapter(adapter1);
            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText != null && !newText.isEmpty()){
                    ArrayList<FriendItem> found = new ArrayList<FriendItem>();
                    Log.e(TAG, "onQueryTextChange: 친구 총 몇명? "+friendItems.size());
                    for(FriendItem item: friendItems){
                        if(item.name.contains(newText))
                            found.add(item);
                    }
                    FriendListAdapter adapter1 = new FriendListAdapter(MakeFriendActivity.this, found);
                    friend_recycler.setAdapter(adapter1);

                }else{
                    //아무것도 안쳤을 때
                    FriendListAdapter adapter1 = new FriendListAdapter(MakeFriendActivity.this,defaultItems);
                    friend_recycler.setAdapter(adapter1);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }
}
