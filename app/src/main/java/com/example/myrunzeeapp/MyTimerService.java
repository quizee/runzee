package com.example.myrunzeeapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MyTimerService extends Service {

    private boolean IsTimerOn;
    private boolean IsthreadOn;
    private int count_time = 0;
    int intGoal;

    class MyTimerServiceBinder extends Binder {
        public MyTimerService getService(){
            return MyTimerService.this;
        }
    }

    private IBinder myBinder = new MyTimerServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onDestroy() { //서비스가 멈춰지면 이 메소드가 호출된다.
        super.onDestroy();
        stopTimer();
        Log.e("MyTimerService", "onDestroy: 서비스종료");
    }

    //여기서 서비스를 멈출 수 있는 방법을 구현
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//여기로 intent가 전달됨. 다시 시작되면 intent가 null일 수도 있다.
        Log.e("MyTimerService", "onStartCommand: thread id "+Thread.currentThread().getId() );
        if(intent != null) {
            intGoal = intent.getIntExtra("goal_time", 0);
            intGoal = intGoal * 60;//분 단위로 입력받았으므로 바꿔준다.
            IsthreadOn = true;
            IsTimerOn = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(IsthreadOn) {
                        if(IsthreadOn) {
                            startTimer();//onStartCommand에 직접 넣으면 안된다. 서비스는 메인스레드에서 작동하기 때문이다.
                        }
                    }
                }
            }).start();
        }

        return START_STICKY;//꺼지면 자동으로 intent값을 null로 초기화시켜서 재시작
        //꺼졌을 때 시점을 복원하고 싶다면 START_REDELIVER_INTENT
    }

    //isTimerOn 이 true여야 작동한다.
    public void startTimer(){
        while(IsTimerOn){
            try {
                Thread.sleep(1000);
                //Thread.sleep(200);//시현용
                if(IsTimerOn){
                    count_time++;
                    intGoal --;
                    Log.e("MyTimerService: ", "Thread id: " + Thread.currentThread().getId()+"time: "+count_time);
                    Log.e("MyTimerService: ", "left time: "+intGoal+"seconds" );
                }
            }catch (InterruptedException e){
                Log.i("MyTimerService: ", "Thread Interrupted");
            }
        }
    }

    public  void stopTimer(){
        IsTimerOn = false;
    }
    public void restartTimer(){
        IsTimerOn = true;
    }
    public boolean getTimer(){
        return IsTimerOn;
    }
    public void completeTimer(){
        IsthreadOn = false;
    }
    public int getCount_time(){
        return count_time;
    }
    public int getInt_Goal(){
        return  intGoal;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e("MyTimerService: ", "onStart");
    }
}
