package com.example.lhw.app_1;

import android.Manifest;
import android.app.Activity;
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

public class FaceTalkActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    public static Activity _FaceTalkActivity;

    public static int check_int = 0;

    private static String API_KEY = "46251632";
    private static String SESSION_ID = "1_MX40NjI1MTYzMn5-MTU0NzcwMjMyNTU3NH5PWW1SMlM0OG5HTmg0L3laTWFKeEpWdFN-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjI1MTYzMiZzaWc9YjBhNjQ0OWEwNWIyMDk0NTMzYTljYjczZjdhNzZhNmQyMDYxMDVhNDpzZXNzaW9uX2lkPTFfTVg0ME5qSTFNVFl6TW41LU1UVTBOemN3TWpNeU5UVTNOSDVQV1cxU01sTTBPRzVIVG1nMEwzbGFUV0ZLZUVwV2RGTi1mZyZjcmVhdGVfdGltZT0xNTQ3NzAyNzEyJm5vbmNlPTAuMjE4NTMwMjQwOTYzNzA0ODgmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU1MDI5NDcwOCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS = 123;

    private Session session; //openTok session

    private ConstraintLayout publisherContainer;
    private FrameLayout SubscriberContainer;

    private Publisher publisher;

    private Subscriber subscriber;

    private Button bt_cancel;

    String send_user_email;
    String product_user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_talk);

        send_user_email = getIntent().getStringExtra("send_user_email");
        product_user_email = getIntent().getStringExtra("receive_user_email");



        publisherContainer = findViewById(R.id.publisher_container);
        SubscriberContainer = (FrameLayout) findViewById(R.id.subscriber_container);
        bt_cancel = findViewById(R.id.bt_cancel);

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                String chat_date = transFormat.format(calendar.getTime()); //날짜
                Call<JsonObject> setChat = NetRetrofit.getInstance().getService().setChat(send_user_email, product_user_email, chat_date, "영상통화가 종료되었습니다.");
                setChat.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        session.disconnect();
                        finish();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
        _FaceTalkActivity = FaceTalkActivity.this;
        check_int++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        check_int = 0;
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
        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(this);
        publisherContainer.addView(publisher.getView());
        session.publish(publisher);
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
            SubscriberContainer.addView(subscriber.getView());
            publisherContainer.removeAllViews();
            publisherContainer.addView(publisher.getView());
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
