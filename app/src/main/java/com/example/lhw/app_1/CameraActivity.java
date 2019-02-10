package com.example.lhw.app_1;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "*******";
    private static String SESSION_ID = "****************************";
    private static String TOKEN = "************************************";
    private static String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS = 123;

    private Session session; //openTok session

    private ConstraintLayout publisherContainer;
    private FrameLayout SubscriberContainer;

    private Publisher publisher;

    private Subscriber subscriber;

    private Button bt_cancel;

    String broadcaster_email; //방송자 이메일
    String user_email; // 현재 로그인 유저 이메일
    /*방송자 이메일과 로그인 유저 이메일을 활용하여 publishscriber, subscriber 확인할예정*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_talk);

        broadcaster_email = getIntent().getStringExtra("broadcaster_email");
        SharedPreferences sf = getSharedPreferences("name", 0); //현재 로그인 이메일
        user_email = sf.getString("name", "");

        publisherContainer = findViewById(R.id.publisher_container);
        SubscriberContainer = (FrameLayout) findViewById(R.id.subscriber_container);

        bt_cancel = findViewById(R.id.bt_cancel);

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.disconnect();
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            session.disconnect();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_SETTINGS)
    private void requestPermissions(){
        Log.d(TAG, "requestPermissions: "); //1
        String[] perm = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perm)){
            session = new Session.Builder(this, API_KEY, SESSION_ID).build();
            session.setSessionListener(this);
            session.connect(TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this, "This app needs to access your camera and mic", RC_SETTINGS, perm);
        }
    }

    @Override
    public void onConnected(Session session) { //2
        Log.d(TAG, "onConnected: ");
        if(user_email.equals(broadcaster_email)){
            publisher = new Publisher.Builder(this).build();
            publisher.setPublisherListener(this);
            SubscriberContainer.addView(publisher.getView());
            session.publish(publisher);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: ");

    }



    @Override
    public void onStreamReceived(Session session, Stream stream) { //3
        Log.d(TAG, "onStreamReceived: ");

        if(subscriber == null){
            subscriber = new Subscriber.Builder(this, stream).build();
            session.subscribe(subscriber);

            if(user_email.equals(broadcaster_email) == false){
                SubscriberContainer.removeAllViews();
                SubscriberContainer.addView(subscriber.getView());
            }

        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: ");
        if(subscriber != null){
            subscriber = null;
            SubscriberContainer.removeAllViews();
            finish();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: ");

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) { //4
        Log.d(TAG, "onStreamCreated: ");

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: ");

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: ");

    }
}
