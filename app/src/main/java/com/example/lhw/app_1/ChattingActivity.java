package com.example.lhw.app_1;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.lhw.app_1.FaceTalkActivity._FaceTalkActivity;
import static com.example.lhw.app_1.FaceTalkActivity.check_int;


public class ChattingActivity extends AppCompatActivity {

    final String TAG = "ChattingActivity";
    final int FACETALK = 24;
    final int PICTURE_SEND_CODE = 69;

    private String html;
    private Socket socket;
    private DataInputStream dis; //바이너리 형태로 입력된것을 자바 기본 데이터 타입으로 읽어오는것
    private DataOutputStream dos; //바이너리 형태로 데이터 저장

//    private String ip = "192.168.219.179";
//    private String ip = "192.168.219.130";
//    private String ip = "192.168.0.214"; //팀노바 ip주소
//    private String ip = "192.168.56.4"; // 로커 ip
    private String ip = "54.180.105.119"; //ec2
//    private String ip = "192.168.219.179";
    private int port = 9999;

    private boolean check = true;
    boolean room_check = true;

    UserModel myusermodel;
    UserModel productusermodel;

    Button btn;
    ImageButton bt_plus;

    String user_email;
    String product_user_email;


    RecyclerView recyclerView;
    TextView room_name;

    ArrayList<ChatModel> chat_list;

    String contents;

    ArrayList<Uri> uri_list;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        uri_list = new ArrayList<>();

        final EditText et = (EditText) findViewById(R.id.EditText01);
        btn = findViewById(R.id.Button01);
        bt_plus = findViewById(R.id.bt_plus);
        bt_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        room_name = findViewById(R.id.room_name);

        // "send" button listener

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getText().toString() != null && !et.getText().toString().equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm a");
                    String chat_date = transFormat.format(calendar.getTime()); //날짜
                    Call<JsonObject> setChat = NetRetrofit.getInstance().getService().setChat(user_email, product_user_email, chat_date, et.getText().toString());
                    setChat.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Toast.makeText(ChattingActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                String return_msg = et.getText().toString();
                                et.setText(""); // clear edit text view
                                dos.writeUTF(return_msg);
                                Log.d("Threadcheck", "run: 1");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

    }

    void show() { //영상통화 , 사진전송 선택 다이얼로그
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("영상통화");
        ListItems.add("사진전송");

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                if(selectedText.equals("영상통화")){
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                String return_msg = "&페이스톡해요#";
                                dos.writeUTF(return_msg);
                                Log.d("Threadcheck", "run: 1");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    Intent intent = new Intent(getApplicationContext(), FaceTalkActivity.class);

                    intent.putExtra("send_user_email", user_email);
                    intent.putExtra("receive_user_email", product_user_email);

                    startActivity(intent);
                }
                else if(selectedText.equals("사진전송")){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICTURE_SEND_CODE);
                }

            }
        });
        builder.show();
    }

    private Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);

        Log.d(TAG, options.inSampleSize + " sample method bitmap ... "
                + actuallyUsableBitmap.getWidth() + " " + actuallyUsableBitmap.getHeight());

        return actuallyUsableBitmap;
    }

    private String imageToString(Bitmap b2){ //이미지를 string으로 변환하는 메서드
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b2.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }
    private void uploadImage(String img) { //이미지와 상품정보들을 서버에 보내는 메서드
        String img_title = RandomStringUtils.randomAlphanumeric(10);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm a");
        String chat_date = transFormat.format(calendar.getTime()); //날짜
        Call<JsonObject> chatimg = NetRetrofit.getInstance().getService().chatimg(img_title, img, user_email, product_user_email, chat_date);
        chatimg.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, final Response<JsonObject> response) {
                Gson gson = new Gson();
                final HashMap<String, String> hash_chat = gson.fromJson(response.body().toString(), HashMap.class);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String return_msg = hash_chat.get("result");
                            dos.writeUTF(return_msg);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_SEND_CODE) {
            if (resultCode == RESULT_OK || data != null) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    String img = imageToString(bitmap);
                    uploadImage(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    protected void onStop() {
        super.onStop();
        check = false;
        room_check = false;
//        try {
//            socket.close();
//            check = false;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        room_check = true;
        user_email = getIntent().getStringExtra("user_email");
        product_user_email = getIntent().getStringExtra("product_user_email");

        Call<JsonObject> getUserModel = NetRetrofit.getInstance().getService().getUserModel(product_user_email);
        getUserModel.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                UserModel userModel = gson.fromJson(response.body().toString(), UserModel.class);
                room_name.setText(userModel.user_name+"님 채팅방");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        Call<ArrayList<JsonObject>> getChat = NetRetrofit.getInstance().getService().getChat(user_email, product_user_email);
        getChat.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();
                chat_list = (ArrayList<ChatModel>) gson.fromJson(response.body().toString(),
                        new TypeToken<ArrayList<ChatModel>>() {
                        }.getType()); //jsonarray 파싱하는법임!
                if(chat_list.get(0).response.equals("no") == false){
                    recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.scrollToPosition(chat_list.size()-1);
                }
                else {
                    chat_list.clear();
                }

            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        // Socket making thread
        new Thread(new Runnable() {
            public void run() {

                try {
                    setSocket(ip, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    dos.writeUTF("user_email:"+user_email+","+"product_user_email:"+product_user_email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        html = dis.readUTF();
                        if (html != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(html.contains("$"+user_email+"!") || html.contains("$"+product_user_email+"!")){
                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm a");
                                        String chat_date = transFormat.format(calendar.getTime()); //날짜

                                        int idx = html.indexOf(":");
                                        String useremail = html.substring(0, idx);
                                        useremail = useremail.replace("$", "");
                                        useremail = useremail.replace("!", "");
                                        contents = html.substring(idx+1);
                                        final ChatModel chatModel = new ChatModel();
                                        if(useremail.equals(useremail)){
                                            chatModel.user_email = useremail;
                                        }

                                        if((contents.contains("&^No#$") && check_int > 0) || contents.equals("영상통화가 종료")){
                                            FaceTalkActivity FA = (FaceTalkActivity)FaceTalkActivity._FaceTalkActivity;
                                            check_int = 0;
                                            FA.finish();
                                            Toast.makeText(FA, "상대방이 영상통화를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                                        }

                                        else {
                                            if(contents.equals("&페이스톡해요#") && chatModel.user_email.equals(user_email)){
                                                contents = contents.replace("&", "");
                                                contents = contents.replace("#", "");

                                            }
                                            else if(contents.equals("&페이스톡해요#") && (chatModel.user_email.equals(user_email) == false)){
                                                contents = contents.replace("&", "");
                                                contents = contents.replace("#", "");

                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChattingActivity.this);
                                                builder.setTitle("영상통화가 왔습니다.");
                                                builder.setMessage("영상통화를 하시겠습니까?");
                                                builder.setPositiveButton("예",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(getApplicationContext(), FaceTalkActivity.class);
                                                                intent.putExtra("send_user_email", user_email);
                                                                intent.putExtra("receive_user_email", chatModel.user_email);
                                                                startActivityForResult(intent, FACETALK);
                                                            }
                                                        });
                                                builder.setNegativeButton("아니오",new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        final List<String> ListItems = new ArrayList<>();
                                                        ListItems.add("나중에 다시 연락드리겠습니다.");
                                                        ListItems.add("회의중입니다.");

                                                        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChattingActivity.this);
                                                        builder.setTitle("AlertDialog Title");
                                                        builder.setItems(items, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int pos) {
                                                                String selectedText = items[pos].toString();
                                                                String return_msg = null;
                                                                String db_msg = null;
                                                                if(selectedText.equals("나중에 다시 연락드리겠습니다.")){
                                                                    return_msg = "&^No#$1";
                                                                    db_msg = "나중에 다시 연락드리겠습니다.";
                                                                }
                                                                else if(selectedText.equals("회의중입니다.")){
                                                                    return_msg = "&^No#$2";
                                                                    db_msg = "회의중입니다.";
                                                                }

                                                                final String finalReturn_msg = return_msg;

                                                                Calendar calendar = Calendar.getInstance();
                                                                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm a");
                                                                String chat_date = transFormat.format(calendar.getTime()); //날짜
                                                                Call<JsonObject> setChat = NetRetrofit.getInstance().getService().setChat(user_email, product_user_email, chat_date, db_msg);
                                                                setChat.enqueue(new Callback<JsonObject>() {
                                                                    @Override
                                                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                                        if(finalReturn_msg != null){
                                                                            new Thread(new Runnable() {
                                                                                public void run() {
                                                                                    try {
                                                                                        dos.writeUTF(finalReturn_msg);
                                                                                    }catch (Exception e){
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                            }).start();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                                                        Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                            }
                                                        });
                                                        builder.show();

                                                    }
                                                });
                                                builder.show();
                                            }

                                            chatModel.chat_contents = contents;
                                            chatModel.chat_date = chat_date;

                                            if(chatModel.chat_contents.contains("&^No#$")){
                                                if(chatModel.chat_contents.equals("&^No#$1")){
                                                    chatModel.chat_contents = "나중에 다시 연락드리겠습니다.";
                                                }
                                                else if(chatModel.chat_contents.equals("&^No#$2")){
                                                    chatModel.chat_contents = "회의중입니다.";
                                                }
                                            }

                                            if(chatModel.chat_contents.contains("페이스톡") && (chatModel.user_email.equals(product_user_email) == false)){
                                                chatModel.chat_contents = "페이스톡이 시작되었습니다.";
                                                Call<JsonObject> setChat = NetRetrofit.getInstance().getService().setChat(
                                                        user_email,
                                                        product_user_email,
                                                        chatModel.chat_date,
                                                        chatModel.chat_contents);

                                                setChat.enqueue(new Callback<JsonObject>() {
                                                    @Override
                                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                        chat_list.add(chatModel);
                                                        try {
                                                            recyclerView.getAdapter().notifyItemInserted(chat_list.size()-1);
                                                            recyclerView.scrollToPosition(chat_list.size()-1);
                                                        }
                                                        catch (Exception e){
                                                            recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                                                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                                            recyclerView.getAdapter().notifyItemInserted(chat_list.size()-1);
                                                            recyclerView.scrollToPosition(chat_list.size()-1);
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                                        Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            if((chatModel.chat_contents.contains("페이스톡") || chatModel.chat_contents.contains("&^No#$")) == false){
                                                if(useremail.equals(user_email) == false && room_check == false){
                                                    notification(useremail, contents);
                                                }
                                                chat_list.add(chatModel);
                                                try {
                                                    recyclerView.getAdapter().notifyItemInserted(chat_list.size()-1);
                                                    recyclerView.scrollToPosition(chat_list.size()-1);
                                                }
                                                catch (Exception e){
                                                    recyclerView.setAdapter(new ChatRecyclerViewAdapter());
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                                    recyclerView.getAdapter().notifyItemInserted(chat_list.size()-1);
                                                    recyclerView.scrollToPosition(chat_list.size()-1);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void notification(final String useremail, final String contents){
        final String channelId = "channel";
        final String channelName = "Channel Name";

        Call<JsonObject> res = NetRetrofit.getInstance().getService().getProfile(useremail); //유저 프로필 기본사진 가지고오기
        res.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                final ProfileModel profileModel = gson.fromJson(response.body().toString(), ProfileModel.class);
                String pp = profileModel.profile_path+profileModel.profile_name+".jpg";
                Log.d(TAG, "onResponse: " + PictureModel.BASE_URL+pp);
                Toast.makeText(ChattingActivity.this, PictureModel.BASE_URL+pp, Toast.LENGTH_LONG).show();
                Picasso.get().load(PictureModel.BASE_URL+pp).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        NotificationManager notifManager = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH; // 중요도 설정
                            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                            notifManager.createNotificationChannel(mChannel);
                        }

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

                        Intent notificationIntent = new Intent(getApplicationContext(), ChattingActivity.class); // 클릭했을때 어떤 엑티비티로 갈지 정함
                        notificationIntent.putExtra("user_email", user_email);
                        notificationIntent.putExtra("product_user_email", useremail);

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        int requestID = (int) System.currentTimeMillis();

                        PendingIntent pendingIntent
                                = PendingIntent.getActivity(getApplicationContext()
                                , requestID
                                , notificationIntent
                                , PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentTitle(profileModel.user_name) // required
                                .setContentText(contents)  // required
                                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setSmallIcon(android.R.drawable.btn_star)
                                .setLargeIcon(bitmap)
                                .setBadgeIconType(R.drawable.ic_launcher_background)
                                .setContentIntent(pendingIntent);

                        notifManager.notify(0, builder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(ChattingActivity.this, "onBitmapFailed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });


    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        String name;
        String date;
        String path;

        public ChatRecyclerViewAdapter() { //생성자

        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView my_comment_profile;
            TextView friend_name;
            TextView mTextView;
            TextView time;
            ImageView chat_img;

            public ViewHolder(@NonNull View v) {
                super(v);
                my_comment_profile = v.findViewById(R.id.my_comment_profile);
                friend_name = v.findViewById(R.id.friend_name);
                mTextView = v.findViewById(R.id.mTextView);
                time = v.findViewById(R.id.time);
                chat_img = v.findViewById(R.id.imageView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            Log.d(TAG, "getItemViewType:chat_list.get(position).user_email" + chat_list.get(position).user_email);
            Log.d(TAG, "getItemViewType:user_email " + user_email);
            if(chat_list.get(position).user_email.equals(user_email) && chat_list.get(position).chat_contents.contains(".jpg") == false){
                return 1;
            }
            else if(chat_list.get(position).user_email.equals(product_user_email) && chat_list.get(position).chat_contents.contains(".jpg") == false){
                return 2;
            }
            else if(chat_list.get(position).user_email.equals(user_email) && chat_list.get(position).chat_contents.contains(".jpg")){ //사진이고 내가 보낸거
                return 3;
            }
            else {
                return 4;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            if(viewType == 1){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rightchat, parent, false);
            }

            else if(viewType == 2){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leftchat, parent, false);
            }

            else if(viewType == 3){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_img_right, parent, false);
            }

            else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_img_left, parent, false);
            }

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder homeViewHolder = (ViewHolder)holder;
            String str = chat_list.get(position).chat_date;
            String result = str.substring(str.lastIndexOf("/")+1);
            chat_list.get(position).chat_date = result;

            int viewtype = holder.getItemViewType();
            if(viewtype == 1){ //내가 보낸 채팅일경우
//                homeViewHolder.friend_name.setText(chat_list.get(position).user_name);
                homeViewHolder.mTextView.setText(chat_list.get(position).chat_contents);
                homeViewHolder.time.setText(chat_list.get(position).chat_date);

            }
            else if(viewtype == 2){ // 상대방이 보낸 채팅 내용일경우

                if(chat_list.get(position).chat_idx == null){
                    Call<JsonObject> res = NetRetrofit.getInstance().getService().getProfile(chat_list.get(position).user_email); //유저 프로필 기본사진 가지고오기
                    res.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Gson gson = new Gson();
                            ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                            chat_list.get(position).profile_path = profileModel.profile_path+profileModel.profile_name+".jpg";
                            chat_list.get(position).user_name = profileModel.user_name;
                            Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).profile_path).transform(new CircleTransform()).into(homeViewHolder.my_comment_profile);
                            homeViewHolder.friend_name.setText(chat_list.get(position).user_name);
                            homeViewHolder.mTextView.setText(chat_list.get(position).chat_contents);
                            homeViewHolder.time.setText(chat_list.get(position).chat_date);
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Log.d("chat_list", PictureModel.BASE_URL+chat_list.get(position).profile_path);
                    Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).profile_path).transform(new CircleTransform()).into(homeViewHolder.my_comment_profile);
                    homeViewHolder.friend_name.setText(chat_list.get(position).user_name);
                    homeViewHolder.mTextView.setText(chat_list.get(position).chat_contents);
                    homeViewHolder.time.setText(chat_list.get(position).chat_date);
                }
            }

            else if(viewtype == 3){
                Log.d(TAG, "onBindViewHolder11: " + PictureModel.BASE_URL+chat_list.get(position).chat_contents);
                Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).chat_contents).into(homeViewHolder.chat_img);
                homeViewHolder.time.setText(chat_list.get(position).chat_date);
                homeViewHolder.chat_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ZoomImgActivity.class);
                        intent.putExtra("img_uri", PictureModel.BASE_URL+chat_list.get(position).chat_contents);
                        startActivity(intent);
                    }
                });
            }

            else {
                if(chat_list.get(position).chat_idx == null){
                    Call<JsonObject> res = NetRetrofit.getInstance().getService().getProfile(chat_list.get(position).user_email); //유저 프로필 기본사진 가지고오기
                    res.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Gson gson = new Gson();
                            ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                            chat_list.get(position).profile_path = profileModel.profile_path+profileModel.profile_name+".jpg";
                            chat_list.get(position).user_name = profileModel.user_name;
                            Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).profile_path).transform(new CircleTransform()).into(homeViewHolder.my_comment_profile);
                            homeViewHolder.friend_name.setText(chat_list.get(position).user_name);
                            Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).chat_contents).into(homeViewHolder.chat_img);
                            homeViewHolder.time.setText(chat_list.get(position).chat_date);
                            homeViewHolder.chat_img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), ZoomImgActivity.class);
                                    intent.putExtra("img_uri", PictureModel.BASE_URL+chat_list.get(position).chat_contents);
                                    startActivity(intent);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(ChattingActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Log.d("chat_list", PictureModel.BASE_URL+chat_list.get(position).profile_path);
                    Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).profile_path).transform(new CircleTransform()).into(homeViewHolder.my_comment_profile);
                    homeViewHolder.friend_name.setText(chat_list.get(position).user_name);
                    Picasso.get().load(PictureModel.BASE_URL+chat_list.get(position).chat_contents).into(homeViewHolder.chat_img);
                    homeViewHolder.time.setText(chat_list.get(position).chat_date);
                    homeViewHolder.chat_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), ZoomImgActivity.class);
                            intent.putExtra("img_uri", PictureModel.BASE_URL+chat_list.get(position).chat_contents);
                            startActivity(intent);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return chat_list.size();

        }
    }

}
