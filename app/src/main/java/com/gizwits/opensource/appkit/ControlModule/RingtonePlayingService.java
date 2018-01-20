package com.gizwits.opensource.appkit.ControlModule;

/**
 * Created by Wang Dong on 2018/1/14.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.gizwits.opensource.appkit.R;


public class RingtonePlayingService extends Service {

    int startId;
    MediaPlayer media_song1;
    MediaPlayer media_song2;
    MediaPlayer media_song3;
    MediaPlayer media_song4;
    MediaPlayer media_song5;
    boolean isRunning1;
    boolean isRunning2;
    boolean isRunning3;
    boolean isRunning4;
    boolean isRunning5;
    String state;

    public IBinder onBind(Intent intent){
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        state = intent.getExtras().getString("extra");

        if (state.equals("alarm on1") || state.equals("alarm on2") || state.equals("alarm on3")
                || state.equals("alarm on4") || state.equals("alarm on5")){
            startId = 1;
        } else {
            startId = 0;
        }

        //消息通知
        if(!this.isRunning1 && !this.isRunning2 && !this.isRunning3 && !this.isRunning4 && !this.isRunning5 && startId == 1){
            NotificationManager notify_manager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            Intent intent_main_activity = new Intent(this.getApplicationContext(), GosDeviceControlActivity.class);
            PendingIntent pending_intent_main_activity = PendingIntent.getActivity(this, 0,
                    intent_main_activity, 0);
            Notification notification_popup = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("智能药盒")
                    .setContentText("服药时间到了")
                    .setContentIntent(pending_intent_main_activity)
                    .setAutoCancel(true)
                    .build();
            notify_manager.notify(0, notification_popup);
        }

        //第一个闹钟
        if(!this.isRunning1 && state.equals("alarm on1")){
            media_song1 = MediaPlayer.create(this, R.raw.ring);
            media_song1.start();
            this.isRunning1 = true;
        }
        if (this.isRunning1 && state.equals("alarm off1")) {
            media_song1.stop();
            media_song1.reset();
            this.isRunning1 = false;
        }

        //第二个闹钟
        if(!this.isRunning2 && state.equals("alarm on2")){
            media_song2 = MediaPlayer.create(this, R.raw.ring);
            media_song2.start();
            this.isRunning2 = true;
        }
        if (this.isRunning2 && state.equals("alarm off2")) {
            media_song2.stop();
            media_song2.reset();
            this.isRunning2 = false;
        }

        //第三个闹钟
        if(!this.isRunning3 && state.equals("alarm on3")){
            media_song3 = MediaPlayer.create(this, R.raw.ring);
            media_song3.start();
            this.isRunning3 = true;
        }
        if (this.isRunning3 && state.equals("alarm off3")) {
            media_song3.stop();
            media_song3.reset();
            this.isRunning3 = false;
        }

        //第四个闹钟
        if(!this.isRunning4 && state.equals("alarm on4")){
            media_song4 = MediaPlayer.create(this, R.raw.ring);
            media_song4.start();
            this.isRunning4 = true;
        }
        if (this.isRunning4 && state.equals("alarm off4")) {
            media_song4.stop();
            media_song4.reset();
            this.isRunning4 = false;
        }

        //第五个闹钟
        if(!this.isRunning5 && state.equals("alarm on5")){
            media_song5 = MediaPlayer.create(this, R.raw.ring);
            media_song5.start();
            this.isRunning5 = true;
        }
        if (this.isRunning5 && state.equals("alarm off5")) {
            media_song5.stop();
            media_song5.reset();
            this.isRunning5 = false;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isRunning1 = false;
        this.isRunning2 = false;
        this.isRunning3 = false;
        this.isRunning4 = false;
        this.isRunning5 = false;
    }
}