package com.example.myrunzeeapp;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final int GALLERY_CODE = 10;
    private static final String TAG = "EditProfileActivity";
    ImageView profile_picture;
    TextView name_edit;
    CheckBox use_default;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private File tempFile;
    private FirebaseDatabase database;
    String path;
    TextView cancel;
    TextView save_complete;
    TextView why_need;
    String name;
    String url;
    PhysicInfo physicInfo;
    EditText what_cm;
    EditText what_kg;
    RadioGroup gender_radio;
    RadioButton wm_btn;
    RadioButton m_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        profile_picture = findViewById(R.id.profile_picture);
        name_edit = findViewById(R.id.name_edit);
        cancel = findViewById(R.id.cancel);
        save_complete = findViewById(R.id.save_complete);
        why_need = findViewById(R.id.why_need);
        use_default = findViewById(R.id.use_default);
        what_cm = findViewById(R.id.what_cm);
        what_kg = findViewById(R.id.what_kg);
        gender_radio = findViewById(R.id.gender_radio);
        wm_btn = findViewById(R.id.wm_btn);
        m_btn = findViewById(R.id.m_btn);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //인텐트 받은 값으로 초기화 하는 과정
        Intent intent = getIntent();
        physicInfo = (PhysicInfo) intent.getSerializableExtra("physicInfo");
        name = intent.getStringExtra("name");
        url = intent.getStringExtra("profile_url");

        //이름
        name_edit.setText(name);

        //프사
        if(url == null){
            Drawable default_icon = getResources().getDrawable(R.drawable.profile);
            Glide.with(EditProfileActivity.this).load(default_icon).apply(RequestOptions.circleCropTransform()).into(profile_picture);
        }else {
            Glide.with(EditProfileActivity.this).load(url).apply(RequestOptions.circleCropTransform()).into(profile_picture);
        }

        //신체정보
        // 1) physic info 로부터 수정 여부 받아서 체크
        if(physicInfo.edit_by_user){//사용자가 직접 수정했다면
            use_default.setChecked(false);
        }else{//기본값 사용중이라면
            use_default.setChecked(true);
        }

        // 2) physic info 로부터 성별을 받아서 체크
        String gender =  physicInfo.gender;
        switch (gender){
            case "male":
                m_btn.setChecked(true);
                break;
            case "female":
                wm_btn.setChecked(true);
                break;
        }
        // 3) physic info 로부터 키를 받아서 체크
        int height = physicInfo.height;
        what_cm.setText(String.valueOf(height));

        // 4) physic info 로부터 몸무게를 받아서 체크
        int weight = physicInfo.weight;
        what_kg.setText(String.valueOf(weight));


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                use_default.setChecked(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        what_cm.addTextChangedListener(textWatcher);
        what_kg.addTextChangedListener(textWatcher);
        //둘 중하나 건들면 기본값 체크 해제

        use_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(use_default.isChecked()){
                    if(wm_btn.isChecked()){
                        physicInfo = new PhysicInfo("female");
                        what_cm.setText(String.valueOf(physicInfo.height));
                        what_kg.setText(String.valueOf(physicInfo.weight));
                    }else if(m_btn.isChecked()){
                        physicInfo = new PhysicInfo("male");
                        what_cm.setText(String.valueOf(physicInfo.height));
                        what_kg.setText(String.valueOf(physicInfo.weight));
                    }
                    use_default.setChecked(true);
                }
            }
        });

        //권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,GALLERY_CODE);
            }
        });
        save_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이름 수정
                String name_update = name_edit.getText().toString().trim();
                String gender_update = "";
                if(use_default.isChecked()){
                    upload(path, name_update,physicInfo);
                }else{
                    //성별 수정
                    if(wm_btn.isChecked()){
                        gender_update= "female";
                    }else if(m_btn.isChecked()){
                        gender_update = "male";
                    }
                    //키 몸무게 수정
                    int height_update = Integer.parseInt(what_cm.getText().toString().trim());
                    int weight_update = Integer.parseInt(what_kg.getText().toString().trim());
                    PhysicInfo info_update = new PhysicInfo(gender_update,height_update,weight_update);
                    upload(path, name_update,info_update);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == GALLERY_CODE){
            if(resultCode == RESULT_OK){
                path = getPath(data.getData());
                Log.e("카메라 앨범 절대 경로", "onActivityResult: "+path);
                setProfile_picture(path);
            }
        }
    }

    public String getPath(Uri uri){
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }
    public void upload(String uri, final String name_update, final PhysicInfo info_update){
        StorageReference storageRef = storage.getReferenceFromUrl("gs://my-running-31fee.appspot.com");//스토리지 서버로 가는 것
        if(uri!=null){
            Uri file = Uri.fromFile(new File(uri));
            final StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());//사진을 먼저 업로드 하고

            UploadTask uploadTask = riversRef.putFile(file);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {//스토리지에 저장이 완료됐으면 스토리지의 경로를 받아와서 데이터베이스에도 저장한다.
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String downloadURL = downloadUri.toString();
                        Log.e("EditProfileActivity", "onComplete: !!!!!!!!!!!!!!!!!"+downloadURL);

                        //새로 생성이 아니라 수정이다
                        //profile_url,
                        UserDTO userDTO = new UserDTO(auth.getCurrentUser().getUid(),auth.getCurrentUser().getEmail(),name_update);
                        userDTO.physicInfo = info_update;
                        userDTO.profile_url = downloadURL;
//                        Map<String,Object> userMap = userDTO.toMap();
//                        database.getReference().child("userlist").child(auth.getCurrentUser().getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Toast.makeText(getApplicationContext(),"수정 완료!",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        database.getReference().child("userlist").child(auth.getCurrentUser().getUid()).setValue(userDTO);
                        Intent intent = new Intent();
                        setResult(RESULT_OK,intent);
                        finish();
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }


    }
    public void setProfile_picture(String uri){
        Uri file = Uri.fromFile(new File(uri));//앨범 경로를 넣어줌
        Glide.with(profile_picture).load(file).apply(RequestOptions.circleCropTransform()).into(profile_picture);
       // profile_picture.setImageURI(file);
    }
}
