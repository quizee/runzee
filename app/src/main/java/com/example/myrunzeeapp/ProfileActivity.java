package com.example.myrunzeeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

public class ProfileActivity extends MenuActivity {

    private static final int EDIT_PROFILE = 11;
    //private static final int GALLERY_CODE = 10;
    private static final String TAG = "ProfileActivity";

    TextView email_profile;
    TextView name_profile;
    ImageView profile_picture;
    Button out_member;
    Button edit_profile;
    Button logout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private File tempFile;
    FirebaseUser user;
    PhysicInfo physicInfo;
    String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setToolbarMenu();

        logout = findViewById(R.id.logout);
        edit_profile = findViewById(R.id.edit_profile);
        email_profile = findViewById(R.id.email_profile);
        name_profile = findViewById(R.id.name_profile);
        out_member = findViewById(R.id.out_member);
        profile_picture = findViewById(R.id.profile_picture);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        user = mAuth.getCurrentUser();

        Drawable drawable = getResources().getDrawable(R.drawable.profile);
        Glide.with(ProfileActivity.this).load(drawable).apply(RequestOptions.circleCropTransform()).into(profile_picture);

        //로그아웃
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                editor.clear().apply();
                Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mAuth.signOut();
                finish();

            }
        });
        //회원 탈퇴
        out_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                auto.edit().clear().apply();//자동 로그인 삭제
                //데이터베이스와 회원 정보를 모두 없앤다
                mAuth.signOut();
                user.delete();

                Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("name",name_profile.getText().toString());
                intent.putExtra("profile_url",url);
                intent.putExtra("physicInfo",physicInfo);
                startActivityForResult(intent,EDIT_PROFILE);
            }
        });
        account_lt.setImageResource(R.drawable.ic_account_black_24dp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //이메일은 authentication으로부터
        email_profile.setText(user.getEmail());
        Log.e(TAG, "onStart: 유아이디가 나올 것이다 "+database.getReference().child("userlist").child(user.getUid()).getKey());

        database.getReference().child("userlist").child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().equals("name")){
                    name_profile.setText(dataSnapshot.getValue(String.class));//이름
                    Log.e(TAG, "onChildAdded: 이름 예상 "+dataSnapshot);
                }
                if(dataSnapshot.getKey().equals("profile_url")) {//프사
                    url = dataSnapshot.getValue(String.class);
                    Glide.with(ProfileActivity.this).load(url).apply(RequestOptions.circleCropTransform()).into(profile_picture);
                    Log.e(TAG, "onChildAdded: 프사 예상 "+dataSnapshot);
                }
                if(dataSnapshot.getKey().equals("physicInfo")) {
                    physicInfo = dataSnapshot.getValue(PhysicInfo.class);
                    if(physicInfo == null){
                        physicInfo = new PhysicInfo("female");
                    }
                    Log.e(TAG, "onChildAdded: 신체정보 예상 "+dataSnapshot);
                }
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
//        database.getReference().child("userlist").child(user.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
//                    //이름과 프사는 데이터베이스로부터
//                    name_profile.setText(snapshot.child("name").getValue(String.class));//이름
//                    if(snapshot.child("profile_url").exists()){//프사
//                        url = snapshot.child("profile_url").getValue(String.class);
//                        Glide.with(ProfileActivity.this).load(url).apply(RequestOptions.circleCropTransform()).into(profile_picture);
//                    }else{
//                        Drawable default_icon = getResources().getDrawable(R.drawable.profile);
//                        Glide.with(ProfileActivity.this).load(default_icon).apply(RequestOptions.circleCropTransform()).into(profile_picture);
//                    }
//                    physicInfo = snapshot.child("physicInfo").getValue(PhysicInfo.class);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == EDIT_PROFILE) {
                //저장을 누른 시점에 데이터베이스에 수정이 되고 이 액티비티는 데이터베이스로부터 가져올 뿐이다
            }
        }
    }
}
