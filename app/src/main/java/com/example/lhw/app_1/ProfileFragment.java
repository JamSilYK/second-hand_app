package com.example.lhw.app_1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    final int REQUEST_IMAGE_CAPTURE = 99; //
    final int REQUEST_SELECT_GALLERY = 77;
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 13; //퍼미션 권한 request

    private final String TAG = "ProfileFragment";

    String user_email; //유저이메일



    private ImageView user_profile;
    private TextView likeit_count;
    private TextView product_sale_count;
    private TextView text_email;
    private TextView text_name;
    private TextView text_phone;
    private  TextView purchase_count;
    //private TextView text_addr;
    private Button bt_edit_name;
    private Button bt_edit_phone;
    //private Button bt_edit_addr;
    private Button bt_logout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);


        user_profile = v.findViewById(R.id.user_profile);
        likeit_count = v.findViewById(R.id.likeit_count);
        product_sale_count = v.findViewById(R.id.product_sale_count);
        text_email = v.findViewById(R.id.text_email);
        text_name = v.findViewById(R.id.text_name);
        text_phone = v.findViewById(R.id.text_phone);
        purchase_count = v.findViewById(R.id.purchase_count);
        //text_addr = v.findViewById(R.id.text_addr);

        bt_edit_name = v.findViewById(R.id.bt_edit_name);
        bt_edit_phone = v.findViewById(R.id.bt_edit_phone);
        //bt_edit_addr = v.findViewById(R.id.bt_edit_addr);
        likeit_count.setOnClickListener(this);
        product_sale_count.setOnClickListener(this);
        purchase_count.setOnClickListener(this);

        bt_logout = v.findViewById(R.id.bt_logout);
        bt_edit_name.setOnClickListener(this);
        bt_edit_phone.setOnClickListener(this);
        //bt_edit_addr.setOnClickListener(this);
        bt_logout.setOnClickListener(this);
        user_profile.setOnClickListener(this);

        SharedPreferences sf = getActivity().getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄

        Call<JsonObject> getProductCount = NetRetrofit.getInstance().getService().getProductCount(user_email); //등록한 상품 개수
        getProductCount.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                HashMap<String, String> product_count = gson.fromJson(response.body().toString(), HashMap.class);
                product_sale_count.setText(product_count.get("cnt")); //판매 개수
                purchase_count.setText(product_count.get("cnt2")); //구매 개수
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        }); //등록한 상품 개수

        Call<JsonObject> getLikeitCount = NetRetrofit.getInstance().getService().getLikeitCount(user_email); //장바구니(찜) 개수
        getLikeitCount.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                HashMap<String, String> product_count = gson.fromJson(response.body().toString(), HashMap.class);
                likeit_count.setText(product_count.get("cnt"));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        }); //장바구니(찜) 개수

        Call<JsonObject> res = NetRetrofit.getInstance().getService().getProfile(user_email); //유저 프로필 기본사진 가지고오기
        res.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();

                ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);

                Log.d(TAG, "onResponse:profileuri : " + PictureModel.BASE_URL+ profileModel.profile_path+profileModel.profile_name+".jpg");

                Uri uri = Uri.parse(PictureModel.BASE_URL+ profileModel.profile_path+profileModel.profile_name+".jpg");
                Picasso.get().load(uri).transform(new CircleTransform()).into(user_profile);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        });

        Call<JsonObject> getUserModel = NetRetrofit.getInstance().getService().getUserModel(user_email); //유저 데이터 가지고오기
        getUserModel.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                UserModel userModel= gson.fromJson(response.body(), UserModel.class);
                text_email.setText(userModel.user_email);
                text_name.setText(userModel.user_name);
                text_phone.setText(userModel.user_phone);
                //text_addr.setText(userModel.user_addr);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        });

        return v;

    }

    void show(final TextView tmp, final String check) { //Edit dialog input 형식
        final EditText edittext = new EditText(getActivity());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if(check.equals("Y")){
            builder.setTitle("핸드폰 번호 변경");
            builder.setMessage("01012341234 형식으로 입력해주세요.");
        }
        else {
            builder.setMessage("정보를 입력해주세요.");
        }

        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(check.equals("Y") && edittext.getText().toString() != null){
                            if(isNumeric(edittext.getText().toString())){
                                tmp.setText(edittext.getText().toString()); //정보변경
                                String user_phone = edittext.getText().toString();
                                String user_name = "N";
                                Call<JsonObject> UpdateUser = NetRetrofit.getInstance().getService().UpdateUser(user_email, user_phone, user_name);
                                UpdateUser.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        Toast.makeText(getActivity(), response.body().toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(getActivity(), "01012341234 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        else {
                            tmp.setText(edittext.getText().toString()); //정보변경
                            String user_name = edittext.getText().toString();
                            Call<JsonObject> UpdateUser = NetRetrofit.getInstance().getService().UpdateUser(user_email, "N", user_name);
                            UpdateUser.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    Toast.makeText(getActivity(), response.body().toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    } //Edit dialog input 형식

    void show() { //confirm dialog ex) 예, 아니오
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("로그아웃");
        builder.setMessage("로그아웃 하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //이전 엑티비티 stack 비우기
                        startActivity(intent);
                        getActivity().finish();

                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }//confirm dialog ex) 예, 아니오

    void showPicture() { //카메라 갤러리 select dialog
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("카메라");
        ListItems.add("갤러리");

        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("AlertDialog Title");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                if(selectedText.equals("카메라")){
                    checkPermission();
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                }
                else if(selectedText.equals("갤러리")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_GALLERY);
                }

            }
        });
        builder.show();
    }

    public void checkPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    } //권한요청

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onClick(View v) {
        String check = "N";
        switch (v.getId()) {
            case R.id.bt_edit_name : //이름 정보 변경
                show(text_name, check);
                break ;
            case R.id.bt_edit_phone : //핸드폰 정보 변경
                check = "Y";
                show(text_phone, check);
                break ;
            case R.id.user_profile : //프로필 사진 변경
                checkPermission();
                showPicture();
                break ;
            case R.id.bt_logout : //로그아웃하기
                show();
                break ;
            case R.id.likeit_count : //찜
                if(likeit_count.getText().equals("0")){
                    Toast.makeText(getActivity(), "찜 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getActivity(), listActivity.class);
                    intent.putExtra("check", "likeit");
                    startActivity(intent);
                }
                break ;

            case R.id.purchase_count : //구매
                if(purchase_count.getText().equals("0")){
                    Toast.makeText(getActivity(), "구매 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getActivity(), listActivity.class);
                    intent.putExtra("check", "purchase");
                    startActivity(intent);
                }
                break ;

            case R.id.product_sale_count : //판매
                if(product_sale_count.getText().equals("0")){
                    Toast.makeText(getActivity(), "판매한 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(getActivity(), listActivity.class);
                    intent.putExtra("check", "sale");
                    startActivity(intent);
                }
                break ;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && data != null){ //카메라로 찍었을 때
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            uploadImage(imageToString(bitmap));
            //user_profile.setImageBitmap(bitmap);
        }

        else if(requestCode == REQUEST_SELECT_GALLERY && data != null){ //갤러리에서 사진을 가지고 올 때
            Uri uri = data.getData();
            Bitmap bitmap = resize(getActivity(), uri, 100);
            //user_profile.setImageBitmap(bitmap);
            uploadImage(imageToString(bitmap));

        }
    }

    private String imageToString(Bitmap b2){ //이미지를 string으로 변환하는 메서드
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b2.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    private void uploadImage(String image){ //이미지 서버에 보내는 메서드
        String title = RandomStringUtils.randomAlphanumeric(10);

        Call<JsonObject> profile_upload = NetRetrofit.getInstance().getService().upload_profile(
                title,
                image,
                user_email
        );

        profile_upload.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Toast.makeText(getActivity(), response.body().toString(), Toast.LENGTH_SHORT).show();
                Gson gson = new Gson();
                ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                Picasso.get().load(PictureModel.BASE_URL+profileModel.profile_path+profileModel.profile_name+".jpg").transform(new CircleTransform()).into(user_profile);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Bitmap resize(Context context, Uri uri, int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    public boolean isNumeric(String str) //입력값이 숫자인지 숫자가 아닌지 확인하는 함수
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

}
