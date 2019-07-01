package com.example.myrunzeeapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MakeChallengeActivity extends AppCompatActivity {

    private static final String TAG = "MakeChallengeActivity";
    RecyclerView recyclerView;
    ArrayList<FrameItem> frameList = new ArrayList<>();
    FrameAdapter frameAdapter;
    ProgressBar wait_progressbar;
    RecyclerView.LayoutManager layoutManager;
    Button finish;

    TextView challenge_title;
    TextView challenge_distance;
    TextView challenge_start;
    TextView challenge_end;
    TextView friend_list;
    TextView friend_result;

    String startDate;
    String endDate;
    String distanceKm;

    //end가 start보다 앞서지 않도록 주의한다.

    //파이어베이스 업로드
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    //초대된 친구 리스트
    ArrayList<String> inviteList;
    ArrayList<ChallengeItem> boardList = new ArrayList<>();


    int count_page = 0;
    int count = 0;
    String last_url = "";
    int row_index = -1;
    Calendar start_cal = Calendar.getInstance();

    private TextWatcher saveTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String titleInput = challenge_title.getText().toString().trim();
            String distanceInput = challenge_distance.getText().toString().trim();
            String startDate = challenge_start.getText().toString().trim();
            String endDate = challenge_end.getText().toString().trim();
            String friendResult = friend_result.getText().toString().trim();

            finish.setEnabled(!titleInput.isEmpty()&& !startDate.isEmpty()&& !distanceInput.isEmpty() && !endDate.isEmpty() && friendResult.contains("러너"));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_challenge);
        wait_progressbar = findViewById(R.id.wait_progressbar);
        finish = findViewById(R.id.finish);

        new PatternCrawler().execute();
        recyclerView = findViewById(R.id.cover_recycler);
        frameAdapter = new FrameAdapter(frameList,MakeChallengeActivity.this);
        //layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        layoutManager = new CenterLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(frameAdapter);

        challenge_title = findViewById(R.id.challenge_title);
        challenge_distance = findViewById(R.id.challenge_distance);
        challenge_start = findViewById(R.id.challenge_start);
        challenge_end = findViewById(R.id.challenge_end);
        friend_list = findViewById(R.id.friend_list);
        friend_result = findViewById(R.id.friend_result);

        challenge_title.addTextChangedListener(saveTextWatcher);
        challenge_distance.addTextChangedListener(saveTextWatcher);
        challenge_start.addTextChangedListener(saveTextWatcher);
        challenge_end.addTextChangedListener(saveTextWatcher);
        friend_result.addTextChangedListener(saveTextWatcher);

        //파이어베이스 업로드
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        frameAdapter.setOnItemClickListener(new PickFrameClickListener() {
            @Override
            public void OnItemClick(int position, FrameItem frameItem) {
                recyclerView.smoothScrollToPosition(position);//가운데로 놓고
                Log.e(TAG, "OnItemClick: "+position+"번 클릭했습니다");
                last_url = frameList.get(position).getImg_url();
                Log.e(TAG, "OnItemClick: "+recyclerView.getChildCount()+"개의 아이템이 있습니다 ");
                frameAdapter.notifyDataSetChanged();

            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!recyclerView.canScrollHorizontally(1)){
                    Log.e(TAG, "onScrollStateChanged: 끝에 도달했으니 새로 로딩해야함" );
                    if(count_page<8) {
                        new PatternCrawler().execute();
                        frameAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //데이터베이스에 저장함
                //String challenge_id, String cover_url, String title, double distance, String start_date, String end_date
                String title = challenge_title.getText().toString();
                double distance = Double.parseDouble(distanceKm);
                String start_date = challenge_start.getText().toString();
                String end_date = challenge_end.getText().toString();
                final String key = database.getReference().child("challenge").push().getKey();

                final ChallengeDTO cto = new ChallengeDTO(key,last_url,title,distance,start_date,end_date);
                //챌린지 생성

                //여기서 해시맵을 저장하자. 해시맵에는 각 사람의 uid와 뛴 거리가 있다.(리더보드)
                HashMap<String, Double> boardMap = new HashMap<>();//해시맵은 데이터베이스 최초 업데이트용이기 때문에 사실상 쓰지는 않느다.
                for(String runner : inviteList){
                    boardMap.put(runner,0.0);//0.0으로 초기화시켜놓는다.
                }
                database.getReference().child("challenge").child(key).setValue(boardMap);

                //각 사람들에게 참여중인 챌린지 목록도 추가한다.
                database.getReference("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if(inviteList.contains(snapshot.getKey())){
                                count++;
                                //누가 무슨 챌린지를 참여하는지
                                database.getReference("participate").child(snapshot.getKey()).child(key).setValue(cto);
                                //이름정보도 가져온다.
                                boardList.add(new ChallengeItem(snapshot.getValue(UserDTO.class), 0.0));

                                if(count == inviteList.size()) {
                                    //챌린지가 만들어진 화면으로 넘어감
                                    Intent intent = new Intent(MakeChallengeActivity.this, CreatedChallengeActivity.class);
                                    //챌린지 정보는 여기에
                                    intent.putExtra("challenge_info", cto);
                                    //리더보드 정보는 여기에
                                    intent.putExtra("leader_board",boardList);//순서를 주기 위해 리스트로 바꾼다.
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //굳이 안해도 되지 않을까?
//                new Thread(){
//                    public void run(){
//                    Bitmap bitmap = getBitmapFromURL(last_url);//비트맵으로 바꾸고
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
//                    byte[] byteArray = stream.toByteArray();
//                    Bundle bundle = new Bundle();//번들로 실어서 보낸다.
//                    bundle.putByteArray("image_bitmap",byteArray);
//                    Message msg = handler.obtainMessage();
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                    }
//                }.start();
            }
        });

        challenge_start.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    dateShow_start();
                }
            }
        });
        challenge_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateShow_start();
            }
        });
        challenge_end.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    dateShow_end();
                }
            }
        });
        challenge_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateShow_end();
            }
        });
        challenge_distance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    distanceShow();
                }
            }
        });
        challenge_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distanceShow();
            }
        });

        friend_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeChallengeActivity.this, AddFriendActivity.class);
                startActivityForResult(intent,22);
            }
        });
        friend_list.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Intent intent = new Intent(MakeChallengeActivity.this, AddFriendActivity.class);
                    startActivityForResult(intent,22);
                }
            }
        });

        frameAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==RESULT_OK){//친구 추가한 결과를 받았을 때
            if(requestCode == 22){
                inviteList = (ArrayList<String>) data.getSerializableExtra("inviteList");
                inviteList.add(auth.getCurrentUser().getUid());//나도 포함
                friend_result.setText(inviteList.size()-1+"명의 러너");
            }
        }
    }

    public void distanceShow(){
        final Dialog d = new Dialog(MakeChallengeActivity.this);
        d.setTitle("거리 선택");
        d.setContentView(R.layout.distance_dialog);
        final NumberPicker kilometer = d.findViewById(R.id.kilometer);
        final NumberPicker underKilometer = d.findViewById(R.id.underKilometer);
        Button decide = d.findViewById(R.id.decide_dist);
        kilometer.setMaxValue(200);
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
                distanceKm = kilometer.getValue()+"."+underKilometer.getDisplayedValues()[underKilometer.getValue()].substring(2);
                String updateKm = distanceKm+"  km";
                challenge_distance.setText(updateKm);
                d.dismiss();
            }
        });
        d.show();
    }
    public void dateShow_start(){
        final Dialog d = new Dialog(MakeChallengeActivity.this);
        d.setTitle("시작날짜 선택");
        d.setContentView(R.layout.challenge_date);
        //다이어로그 안의 요소들을 부른다
        Button decide = d.findViewById(R.id.decide);
        final DatePicker dp = d.findViewById(R.id.datePicker);
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        dp.updateDate(yy, mm, dd);
        c.set(Calendar.YEAR, yy);
        c.set(Calendar.MONTH, mm);
        c.set(Calendar.DAY_OF_MONTH,dd);
        dp.setMinDate(c.getTimeInMillis());

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate = dp.getYear()+"."+(dp.getMonth()+1)+"."+dp.getDayOfMonth();
             //   start_cal = Calendar.getInstance();
                start_cal.set(Calendar.YEAR,dp.getYear());
                start_cal.set(Calendar.MONTH,dp.getMonth());
                start_cal.set(Calendar.DAY_OF_MONTH,dp.getDayOfMonth());
                challenge_start.setText(startDate);
                d.dismiss();
            }
        });
        d.show();
    }
    public void dateShow_end(){
        final Dialog d = new Dialog(MakeChallengeActivity.this);
        d.setTitle("종료날짜 선택");
        d.setContentView(R.layout.challenge_date);
        //다이어로그 안의 요소들을 부른다
        Button decide = d.findViewById(R.id.decide);
        final DatePicker dp = d.findViewById(R.id.datePicker);
        Calendar c = Calendar.getInstance();
        int yy = start_cal.get(Calendar.YEAR);
        int mm = start_cal.get(Calendar.MONTH);
        int dd = start_cal.get(Calendar.DAY_OF_MONTH);
        dp.updateDate(yy, mm, dd);
        dp.setMinDate(start_cal.getTimeInMillis());

        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDate = dp.getYear()+"."+(dp.getMonth()+1)+"."+dp.getDayOfMonth();
                challenge_end.setText(endDate);
                d.dismiss();
            }
        });
        d.show();
    }

    private class PatternCrawler extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wait_progressbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                count_page ++ ;
                String url;
                if(count_page == 1){
                    url = "https://www.kisscc0.com/free-pattern/";
                    Log.e(TAG, "doInBackground 첫번재 url: "+url);
                }else{
                    url = "https://www.kisscc0.com/free-pattern/"+count_page+".html";
                }

                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select("img[class=lazy]");
                for(Element e : elements){
                    String img_url = e.attr("data-original");
                    frameList.add(new FrameItem(img_url));
                   // frameAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            wait_progressbar.setVisibility(View.GONE);
            Log.e(TAG, "onPostExecute: 완료" );
        }
    }

    //url로부터 비트맵을 받았을 때
    /*

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            byte[] byteArray = bundle.getByteArray("image_bitmap");
            //Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);//보냈던 번들을 받아서 비트맵으로 복원하고

            //파이어베이스에 저장한다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://my-running-31fee.appspot.com");//스토리지 서버로 가는 것

            final StorageReference riversRef = storageRef.child("cover_images/"+last_url);
            UploadTask uploadTask = riversRef.putBytes(byteArray);

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
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String downloadURL = downloadUri.toString();
                        Log.e("EditProfileActivity", "onComplete: !!!!!!!!!!!!!!!!!"+downloadURL);

                        ChallengeDTO challengeDTO = new ChallengeDTO();
                        challengeDTO.cover_url = downloadURL;
                        challengeDTO.distance = Double.parseDouble(distanceKm);
                        challengeDTO.start_date = startDate;
                        challengeDTO.end_date = endDate;
                        challengeDTO.title = challenge_title.getText().toString();
                        challengeDTO.uid = auth.getCurrentUser().getUid();

                        database.getReference().child("cover_images").push().setValue(challengeDTO);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }
    };

    public Bitmap getBitmapFromURL(String src){
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

*/

}
