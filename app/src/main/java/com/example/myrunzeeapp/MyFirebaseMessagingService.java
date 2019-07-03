package com.example.myrunzeeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = "MessagingService";
    @Override
    public void onNewToken(String s) {
        Log.e(TAG, "onNewToken: new token "+s );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //System.out.println(remoteMessage.getNotification().getBody());
        if(remoteMessage.getData().size()>0){

            String sender = remoteMessage.getData().get("sender");
            String download_url = remoteMessage.getData().get("download_url");
            Intent intent = new Intent(this, CheerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("sender",sender);
            intent.putExtra("download_url",download_url);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "channel id")
                            .setSmallIcon(R.drawable.ic_record_voice_24dp)
                            .setContentTitle("RUNZEE")
                            .setContentText(sender+"로부터 응원메시지가 도착했습니다!")
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("channel id",
                        "Channel Name",
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0, notificationBuilder.build());
        }

    }
}
