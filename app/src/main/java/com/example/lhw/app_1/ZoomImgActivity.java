package com.example.lhw.app_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ZoomImgActivity extends AppCompatActivity {

    Bitmap bitmap;
    PhotoView photoView;
    Button bt_img_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_img);

        photoView = findViewById(R.id.imageView);
        bt_img_download = findViewById(R.id.bt_img_download);
        bt_img_download.setOnClickListener(new View.OnClickListener() { //이미지 다운로드 클릭했을때
            @Override
            public void onClick(View v) {
                checkVerify(); //권한 묻기
            }
        });

        final String st_uri = getIntent().getStringExtra("img_uri");
        Log.d("st_uri", "onCreate: " + st_uri);

        Thread mThread = new Thread() { //http url을 bitmap으로 변환
            @Override
            public void run() {
                try {
                    URL url = new URL(st_uri);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mThread.start();

        try { //다운로드 Thread를 main Thread와 Join 후에 url bitmap을 img에 넣기
            mThread.join();
            if(bitmap.getHeight() > bitmap.getWidth()){ //세로
                photoView.setImageBitmap(bitmap);
            }
            else {
                photoView.setImageBitmap(bitmap);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void download(){
        OutputStream outStream = null;
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String simpledate = transFormat.format(calendar.getTime());
        String myDir = extStorageDirectory + "DCIM/Camera/"+simpledate+".jpg";
        File file = new File(extStorageDirectory, "DCIM/Camera/"+simpledate+".jpg");
        Log.d("filegetpath", "download: " + file.getPath());
        Log.d("filegetpath", "download: " + "file://"+ file.getPath() + "/" +"DCIM/Camera/"+simpledate+".jpg");
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream);
            outStream.flush();
            outStream.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ file.getPath())));
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else { //이미 퍼미션 동의가 되어있을때 다운로드
            download();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                int check = 0;
                for (int i=0; i<grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면.
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();
                        check++;
                        return;
                    }
                }
                if(check == 0){ //퍼미션 모두 동의했을때 다운로드
                    download();
                }
            }
        }
    }

}
