package com.example.myrunzeeapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

public class CheerActivity extends AppCompatActivity {

    TextView username;

    private static final String TAG = "CheerActivity";
    private static String fileName = null;
    private MediaPlayer player = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "Channel ID";
            String channelName = "Channel Name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }
/*
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed");
                        }
                        String token = task.getResult().getToken();
                        Log.e(TAG, "onComplete: 연결 성공했다면!!!! "+token );
                    }
                });*/
        username = findViewById(R.id.username);

        Intent intent = getIntent();
        String who_sent = intent.getStringExtra("sender");
        String download_url = intent.getStringExtra("download_url");
        if(who_sent!=null && download_url !=null) {
            username.setText(who_sent.toUpperCase());
            Log.e(TAG, "onCreate: username "+ who_sent);
            Log.e(TAG, "onCreate: download "+download_url );
            startPlaying(download_url);
        }else{
            Log.e(TAG, "onCreate: 둘중 하나를 못받았습ㄴ디ㅏ...." );
        }

    }

    private void startPlaying(String url) {
        player = new MediaPlayer();
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopPlaying();
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }
}
