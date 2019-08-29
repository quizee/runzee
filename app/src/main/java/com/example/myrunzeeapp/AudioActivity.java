package com.example.myrunzeeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.service.autofill.UserData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AudioActivity extends AppCompatActivity {

    ImageButton record_btn;
    TextView towhom;
    TextView howto;
    String file_name;
    String receiver_name;
    private ProgressDialog progressDialog;

    private static final String TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private MediaRecorder recorder = null;
    private StorageReference mStorage;

    private MediaPlayer player = null;
    FirebaseDatabase database;
    FirebaseAuth auth;
    long now;

    // Requesting permission to RECORD_AUDIO

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        mStorage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://my-running-31fee.appspot.com");
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        record_btn = findViewById(R.id.record_btn);
        towhom = findViewById(R.id.towhom);
        howto = findViewById(R.id.howto);
        progressDialog = new ProgressDialog(this);

        fileName = getExternalCacheDir().getAbsolutePath();
        now = System.currentTimeMillis();
        fileName += "/"+now+".3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        Intent intent = getIntent();
        receiver_name = intent.getStringExtra("receiver_name");
        file_name = intent.getStringExtra("file_name");//receiver uid
        Log.e(TAG, "onCreate: receivername="+receiver_name+" receiver uid= "+file_name );

        towhom.setText(receiver_name+"에게 보내는 응원메시지!");

        record_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //tab on the button
                    startRecording();
                    howto.setText("녹음 중 ....");
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    //out of button
                    stopRecording();
                    howto.setText("누른 채로 유지");
                }
                return false;
            }
        });
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        uploadAudio();
    }

    private void uploadAudio() {

        progressDialog.setMessage("전송 중..");
        progressDialog.show();

        final StorageReference riversRef = mStorage.child("audio").child(file_name).child(now+"");

        Uri uri = Uri.fromFile(new File(fileName));
        UploadTask uploadTask = riversRef.putFile(uri);

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
                    final String downloadURL = downloadUri.toString();

                    Toast.makeText(AudioActivity.this, receiver_name+"님에게 전송완료!", Toast.LENGTH_LONG).show();

                    final String key = database.getReference("messages").child(file_name).push().getKey();
                    final MessageDTO messageDTO = new MessageDTO(auth.getCurrentUser().getUid(),key,"cheer");
                    messageDTO.when_made = now;//storage에 폴더명과 같다
                    messageDTO.download_url = downloadURL;
                    database.getReference("userlist").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if(dataSnapshot.getKey().equals("name")){
                                String my_name = dataSnapshot.getValue(String.class);
                                Toast.makeText(AudioActivity.this,"내이름:"+my_name,Toast.LENGTH_SHORT);
                                sendPostToFCM(file_name,my_name, downloadURL);
                                database.getReference("messages").child(file_name).child(key).setValue(messageDTO);
                                progressDialog.dismiss();
                                finish();
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

                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private void sendPostToFCM(String file_name, final String my_name, final String downloadURL) {
        //누구에게 가는 어떤 푸시 메시지이고 누르면 어떤 음성이 나올지

        database.getReference("userlist")
                .child(file_name).child("fcmToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String token = dataSnapshot.getValue(String.class);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // FMC 메시지 생성 start
                                    JSONObject root = new JSONObject();
                                    JSONObject data = new JSONObject();
                                    data.put("sender",my_name);
                                    data.put("download_url",downloadURL);
//                                    JSONObject notification = new JSONObject();
//                                    notification.put("body", message);
//                                    notification.put("title", getString(R.string.app_name));
                                    root.put("data", data);
                                    root.put("to", token);

                                    // FMC 메시지 생성 end

                                    URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);
                                    conn.addRequestProperty("Authorization", "key=" + "AAAAbIReuEk:APA91bELlRqy67058sv4DJs_50_OKajyunrJrJYe5ZQAq7ohNxukL5BWg9eVRy5YMEY7fREM6OgCiidw0Eih18_90Fl4aEksi3w1OmbA5vSKHfaqjEGJYeYUm4P4wvAoy-ic0LT7JTX6");
                                    conn.setRequestProperty("Accept", "application/json");
                                    conn.setRequestProperty("Content-type", "application/json");
                                    OutputStream os = conn.getOutputStream();
                                    os.write(root.toString().getBytes("utf-8"));
                                    os.flush();
                                    conn.getResponseCode();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
