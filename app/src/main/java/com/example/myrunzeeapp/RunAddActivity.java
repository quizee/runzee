package com.example.myrunzeeapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class RunAddActivity extends AppCompatActivity {
    //startActivityforResult를 사용한다.

    EditText name_edit_a;
    EditText date_edit_a;
    EditText distance_edit_a;
    EditText time_edit_a;
    EditText pace_edit_a;
    Button save;

    //static Dialog dialog;

    private TextWatcher paceTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String distanceInput = distance_edit_a.getText().toString().trim();
            String timeInput = time_edit_a.getText().toString().trim();
            String words[] = timeInput.split(":");

            int time_send = 0;
            int paceInput = 0;
            if(words.length==3){
                time_send = Integer.parseInt(words[0])*60*60 + Integer.parseInt(words[1])*60 + Integer.parseInt(words[2]);
            }else if(words.length == 2){
                time_send = Integer.parseInt(words[0])*60 + Integer.parseInt(words[1]);
            }
            if(!distanceInput.isEmpty()) {
                double run_dist = Double.parseDouble(distanceInput.substring(0, 4).trim());
                paceInput = (int)((double)time_send/run_dist);
            }
            if(!distanceInput.isEmpty() && !timeInput.isEmpty()){
                pace_edit_a.setText(paceInput/60+"\'"+paceInput%60+"\'\'");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher saveTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String nameInput = name_edit_a.getText().toString().trim();
            String dateInput = date_edit_a.getText().toString().trim();
            String distanceInput = distance_edit_a.getText().toString().trim();
            String timeInput = time_edit_a.getText().toString().trim();
            String paceInput = pace_edit_a.getText().toString().trim();

            save.setEnabled(!nameInput.isEmpty()&& !dateInput.isEmpty()&& !distanceInput.isEmpty() && !timeInput.isEmpty() && !paceInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_add);

        name_edit_a = findViewById(R.id.name_edit_a);
        date_edit_a = findViewById(R.id.date_edit_a);
        distance_edit_a = findViewById(R.id.distance_edit_a);
        time_edit_a = findViewById(R.id.time_edit_a);
        pace_edit_a = findViewById(R.id.pace_edit_a);

        // 페이스와 저장 버튼을 위한 리스너
        distance_edit_a.addTextChangedListener(paceTextWatcher);
        time_edit_a.addTextChangedListener(paceTextWatcher);
        name_edit_a.addTextChangedListener(saveTextWatcher);
        date_edit_a.addTextChangedListener(saveTextWatcher);
        distance_edit_a.addTextChangedListener(saveTextWatcher);
        time_edit_a.addTextChangedListener(saveTextWatcher);
        pace_edit_a.addTextChangedListener(saveTextWatcher);

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("name_edit",name_edit_a.getText().toString());
                intent.putExtra("date_edit",date_edit_a.getText().toString());
                intent.putExtra("distance_edit",distance_edit_a.getText().toString());
                String words[] = time_edit_a.getText().toString().split(":");
                int time_send = 0;
                if(words.length==3){
                    time_send = Integer.parseInt(words[0])*60*60 + Integer.parseInt(words[1])*60 + Integer.parseInt(words[2]);
                }else if(words.length == 2){
                    time_send = Integer.parseInt(words[0])*60 + Integer.parseInt(words[1]);
                }
                Log.e("time send", "onClick: "+time_send);
                intent.putExtra("time_edit",time_send);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        date_edit_a.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    dateShow();
                }
            }
        });
        date_edit_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateShow();
            }
        });
        distance_edit_a.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    distanceShow();
                }
            }
        });
        distance_edit_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distanceShow();
            }
        });
        time_edit_a.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    timeShow();
                }
            }
        });
        time_edit_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeShow();
            }
        });
    }
    public void dateShow(){
        final Dialog d = new Dialog(RunAddActivity.this);
        d.setTitle("날짜 선택");
        d.setContentView(R.layout.date_dialog);
        //다이어로그 안의 요소들을 부른다
        Button decide = (Button) d.findViewById(R.id.decide);
        final DatePicker dp = (DatePicker) d.findViewById(R.id.datePicker);
        final TimePicker tp = (TimePicker) d.findViewById(R.id.timePicker);
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        dp.updateDate(yy, mm, dd);
        c.set(Calendar.YEAR, yy);
        c.set(Calendar.MONTH, mm);
        c.set(Calendar.DAY_OF_MONTH,dd);
        dp.setMaxDate(c.getTimeInMillis());

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateDate = dp.getYear()+"."+(dp.getMonth()+1)+"."+dp.getDayOfMonth();
                String dayOrNight="";
                if(tp.getCurrentHour()<12){
                    dayOrNight = "오전";
                }else{
                    dayOrNight= "오후";
                }
                date_edit_a.setText(updateDate+" "+dayOrNight);
                d.dismiss();
            }
        });
        d.show();
    }

    public void distanceShow(){
        final Dialog d = new Dialog(RunAddActivity.this);
        d.setTitle("러닝 거리");
        d.setContentView(R.layout.distance_dialog);
        final NumberPicker kilometer = (NumberPicker) d.findViewById(R.id.kilometer);
        final NumberPicker underKilometer = (NumberPicker) d.findViewById(R.id.underKilometer);
        Button decide = (Button) d.findViewById(R.id.decide_dist);
        kilometer.setMaxValue(99);
        kilometer.setMinValue(0);

        double nums[] = new double[100];
        String input_nums[] = new String[100];
        //.00 부터 .99까지
        for(int i = 0; i<100; i++){
            nums[i] = Math.round(((double)i/100.00)*100)/100.00;
            input_nums[i] = String.valueOf(nums[i]);
        }
        underKilometer.setDisplayedValues(input_nums);
        underKilometer.setWrapSelectorWheel(false);
        underKilometer.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        underKilometer.setMinValue(0);
        underKilometer.setMaxValue(nums.length-1);

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateKm = kilometer.getValue()+"."+underKilometer.getDisplayedValues()[underKilometer.getValue()].substring(2)+"  km";
                distance_edit_a.setText(updateKm);
                d.dismiss();
            }
        });
        d.show();
    }
    public void timeShow(){
        final Dialog d = new Dialog(RunAddActivity.this);
        d.setTitle("운동 시간");
        d.setContentView(R.layout.time_dialog);

        final NumberPicker hour = (NumberPicker)d.findViewById(R.id.hour);
        final NumberPicker minute = (NumberPicker)d.findViewById(R.id.minute);
        final NumberPicker seconds = (NumberPicker)d.findViewById(R.id.seconds);
        Button decide = (Button) d.findViewById(R.id.decide_time);

        String input_hours[] = new String[24];
        String input_mintes[] = new String[60];
        String input_seconds[] = new String[60];

        for(int i = 0; i<24; i++){
                input_hours[i] = i+"시간";
        }

        for(int i =0; i<60; i++){
            input_mintes[i] = i+"분";
            input_seconds[i] = i+"초";
        }
        hour.setDisplayedValues(input_hours);
        hour.setWrapSelectorWheel(false);
        hour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        hour.setMinValue(0);
        hour.setMaxValue(input_hours.length-1);

        minute.setDisplayedValues(input_mintes);
        minute.setWrapSelectorWheel(false);
        minute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minute.setMinValue(0);
        minute.setMaxValue(input_mintes.length-1);

        seconds.setDisplayedValues(input_seconds);
        seconds.setWrapSelectorWheel(false);
        seconds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        seconds.setMinValue(0);
        seconds.setMaxValue(input_seconds.length-1);

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updateKm="";
                String m;
                String h = hour.getValue()+"";
                String s;
                if(minute.getValue()<10){
                    m = "0"+minute.getValue();
                }else{
                    m = minute.getValue()+"";
                }

                if(seconds.getValue()<10){
                    s = "0"+seconds.getValue();
                }else{
                    s= seconds.getValue()+"";
                }

                if(hour.getValue() == 0){
                    updateKm = m+":"+s;
                }else{
                    updateKm = h+":"+m+":"+s;
                }
                time_edit_a.setText(updateKm);
                d.dismiss();
            }
        });
        d.show();
    }
}
