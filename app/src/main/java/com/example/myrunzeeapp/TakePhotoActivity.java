package com.example.myrunzeeapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TakePhotoActivity extends AppCompatActivity {

    public final int CAPTURE_REQUEST = 5;
    private static final String TAG = "TakePhotoActivity ";
    String currentImagePath = null;
    ImageView run_picture;

    ImageView finish;
    String hmt_string;
    String km_string;
    Bitmap frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        run_picture = findViewById(R.id.run_picture);
        finish = findViewById(R.id.finish);

        Intent intent2 = getIntent();
        int howmuchTime = intent2.getIntExtra("howmuchTIme",0);
        hmt_string = howmuchTime/60+"분 "+howmuchTime%60+"초";
        km_string = String.format("%.2f",ReadyActivity.runningItem.getKm())+" km";

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            File imageFile = null;

            try {
                imageFile = getImageFile();
            }catch(IOException e){
                e.printStackTrace();
            }
            //이미지 파일을 성공적으로 생성했는지
            //생성했다면 카메라앱을 킬 수 있음
            if(imageFile !=null){
                Uri imageUri = FileProvider.getUriForFile(this,"com.example.android.fileprovider",imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,CAPTURE_REQUEST);

            }
        }

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap saveBm = ((BitmapDrawable)run_picture.getDrawable()).getBitmap();
                try{
                    String filename = saveImageFile(saveBm);
                    ReadyActivity.runningItem.setFilename(filename);//파일경로및 이름을 러닝아이템 속성에 저장 -> 만약 편집이라면 바뀐 경로가 들어가 있겠지.
                    ReadyActivity.runningItem.setModified(true);
                }catch (IOException e){

                }
                boolean isEditingPicture = getIntent().getBooleanExtra("editing_picture",false);
                Intent intent;
                if(isEditingPicture){
                    if(ReadyActivity.runningItem.isDirectRunning()){
                        intent = new Intent(TakePhotoActivity.this, TodayActivity.class);
                    }else{
                        intent = new Intent(TakePhotoActivity.this, MachineTodayActivity.class);
                    }
                }else{
                    intent = new Intent(TakePhotoActivity.this, RecordActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });



    }
    public String saveImageFile(Bitmap bitmap) throws IOException{
        FileOutputStream out = null;
        File file = getImageFile();
        String filename = file.getAbsolutePath();
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }

    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CAPTURE_REQUEST){
                Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
                //rotate 관련 처리
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                matrix.postScale(0.5f, 0.5f);
                int newWidth = bitmap.getWidth();
                int newHeight = bitmap.getHeight();
                Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight,matrix,true);//기본사진

                Canvas canvas = new Canvas(newBitmap);//그 원본 사진을 도화지로 하고 그림을 그린다.
                Paint paint = new Paint();
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.WHITE);
                paint.setTextSize(100);//글씨 세팅
                int xpos = canvas.getWidth()/2;
                int ypos = (int) ((canvas.getHeight() / 2)- 2*((paint.descent() + paint.ascent()) / 2)) ;//글씨 위치
                canvas.drawText(hmt_string,xpos,ypos, paint);//몇분 몇초 뛰었는지를 그린다.
                canvas.drawText(km_string,xpos,ypos+120,paint);
                Glide.with(this).load(newBitmap).into(run_picture);
            }
        }

    }
    //그 그림을 원본사진위에 덮어씌워주는 메소드
    public void overlayFrame(int drawable_id){

        Drawable drawable = getResources().getDrawable(drawable_id);
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        //Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        //DrawableCompat.setTint(wrappedDrawable,Color.WHITE);

        frame = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);//원본
        //rotate 관련 처리
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.postScale(0.5f, 0.5f);
        int newWidth = bitmap.getWidth();
        int newHeight = bitmap.getHeight();
        final Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight,matrix,true);//기본사진

        //매번 도화지를 새로 주는게 맞는 것 같다.
        Bitmap resized_frame = Bitmap.createScaledBitmap(frame, newWidth/3, newHeight/3,true);//원본크기에 맞게 액자도 조정

        Canvas canvas = new Canvas(newBitmap);//그 원본 사진을 다시 새 도화지로 하고 그림을 그린다.
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);//글씨 세팅
        int xpos = canvas.getWidth()/2;
        int ypos = (int) ((canvas.getHeight() / 2)- 2*((paint.descent() + paint.ascent()) / 2)) ;//글씨 위치
        canvas.drawText(hmt_string,xpos,ypos, paint);//몇분 몇초 뛰었는지를 그린다.
        canvas.drawText(km_string,xpos,ypos+120,paint);//아래다 그린다.
        canvas.drawBitmap(resized_frame,150,800,null);//액자를 그린다.
        Glide.with(TakePhotoActivity.this).load(newBitmap).into(run_picture);

//
//

    }

//
//

//


}
