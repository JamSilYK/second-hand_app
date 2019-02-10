package com.example.lhw.app_1;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;


import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.Contacts.SettingsColumns.KEY;

public class SaleActivity extends AppCompatActivity {
    final int PICTURE_REQUEST_CODE = 100;

    Button bt_picture_regist; //사진등록 버튼
    Button bt_regist; // 전송(등록)버튼
    RecyclerView salerecyclerview;
    ArrayList<Uri> uri_list; //갤러리에서 가지고 온 사진 uri 리스트
    EditText product_title;
    EditText product_contents;
    EditText product_price;
    ArrayList<String> image_list; //이미지를 String 리스트로 변형하려고 선언
    Bitmap b2;

    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄


        product_title = findViewById(R.id.product_title);
        product_contents = findViewById(R.id.product_contents);
        product_price = findViewById(R.id.product_price);
        bt_regist = findViewById(R.id.bt_regist);
        bt_regist.setOnClickListener(new View.OnClickListener() { //전송
            @Override
            public void onClick(View v) {

                String title = product_title.getText().toString(); //상품 제목
                String price = product_price.getText().toString(); // 상품 가격
                String contents = product_contents.getText().toString(); // 상품 설명

                if(title.equals("") || title==null || price.equals("") || price==null || contents.equals("") || contents==null){
                    Toast.makeText(SaleActivity.this, "상품 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }

                else {
                    if(uri_list != null && uri_list.size()>0){ //사진 등록했을때
                        image_list = new ArrayList<>();
                        ProductModel productModel = new ProductModel(title, price, contents);
                        for(int i = 0; i<uri_list.size(); i++) {
                            b2 = resize(getApplicationContext(), uri_list.get(i), 300); //사진 300 * 300 근사치 resize
                            image_list.add(imageToString(b2)); //비트맵을 String 형식으로 변환
//                            try {
//                                b2 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_list.get(i)); //uri 비트맵으로 변환
//                                image_list.add(imageToString(b2)); //비트맵을 String 형식으로 변환
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }

                        uploadImage(image_list, productModel);

                    }

                    else { //사진 등록 안했을때
                        Toast.makeText(SaleActivity.this, "사진은 한장 이상 등록하셔야 합니다.", Toast.LENGTH_SHORT).show();

                    }

                }
            }
        });

        bt_picture_regist = findViewById(R.id.bt_picture_regist);
        bt_picture_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICTURE_REQUEST_CODE);

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                //사진을 여러개 선택할수 있도록 한다
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);

            }
        });
    }

    private String imageToString(Bitmap b2){ //이미지를 string으로 변환하는 메서드
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b2.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    private void uploadImage(ArrayList<String> image_list, ProductModel productModel){ //이미지와 상품정보들을 서버에 보내는 메서드
        ArrayList<String> image_title = new ArrayList<>();
        for(int i = 0; i<image_list.size(); i++){
            String ramdom = RandomStringUtils.randomAlphanumeric(10);
            image_title.add(ramdom);
        }

        Call<ImageModel> uploadImage =
                NetRetrofit.getInstance().getService().upload(
                        image_title,
                        image_list,
                        user_email,
                        productModel.product_title,
                        productModel.product_contents,
                        productModel.product_price);

        uploadImage.enqueue(new Callback<ImageModel>() {
            @Override
            public void onResponse(Call<ImageModel> call, Response<ImageModel> response) {
                ImageModel imageModel = response.body();
                Toast.makeText(SaleActivity.this, "Server Response: " + imageModel.getResponse(), Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();

            }

            @Override
            public void onFailure(Call<ImageModel> call, Throwable t) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICTURE_REQUEST_CODE) {
            uri_list = new ArrayList<>();
            if (resultCode == RESULT_OK || data != null) {

                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();

                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if(clipData!=null) {

                    for(int i = 0; i < clipData.getItemCount(); i++) {
                        if(i<clipData.getItemCount()){
                            Uri urione =  clipData.getItemAt(i).getUri();
                            uri_list.add(urione);


                        }
                    }

                }
                else if(uri != null) {
                    Toast.makeText(this, "uri :" + uri.toString(), Toast.LENGTH_SHORT).show();
                    uri_list.add(uri);
                    b2 = resize(getApplicationContext(), uri, 300);
//                    try {
//                        b2 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                salerecyclerview = findViewById(R.id.salerecyclerView);
                salerecyclerview.setAdapter(new SaleRecyclerViewAdapter());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
                mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                salerecyclerview.setLayoutManager(mLayoutManager);

//                PagerSnapHelper helper = new PagerSnapHelper();
//                helper.attachToRecyclerView(salerecyclerview);

            }

            else if(data == null){
                uri_list.clear();
                try {
                    salerecyclerview.getAdapter().notifyDataSetChanged();
                }

                catch (NullPointerException ne){
                    ne.printStackTrace();
                }

            }
        }

    }

    public Bitmap resize(Context context, Uri uri, int resize){ //사진 사이즈 줄이기
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
    class SaleRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public SaleRecyclerViewAdapter() { //생성자

        }

        private class SaleViewHolder extends RecyclerView.ViewHolder {

            ImageView img;
            Button bt_del;
            ConstraintLayout sale_container;
            public SaleViewHolder(@NonNull View v) {
                super(v);
                img = v.findViewById(R.id.img);
                bt_del = v.findViewById(R.id.bt_del);
                sale_container = v.findViewById(R.id.sale_container);
                Log.d("SaleRecyclerview", "SaleViewHolder");
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            Log.d("SaleRecyclerview", "onCreateViewHolder1");
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.saleitem, parent, false);
            Log.d("SaleRecyclerview", "onCreateViewHolder2");

            return new SaleViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Log.d("SaleRecyclerview", "onBindViewHolder1");
            SaleViewHolder saleViewHolder = (SaleViewHolder)holder;
            //saleViewHolder.img.setImageURI(uri_list.get(position));
            //Picasso.get().load(f).resize(100, 100).into(saleViewHolder.img);
            //Bitmap bitmap = resize(getApplicationContext(), uri_list.get(position), 70);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_list.get(position));
            } catch (IOException e) {
                e.printStackTrace();
            }

            saleViewHolder.img.setImageBitmap(bitmap);
            saleViewHolder.bt_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uri_list.remove(uri_list.get(position));
                    notifyDataSetChanged();
                }
            });

            saleViewHolder.sale_container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    
                    return false;
                }
            });
            Log.d("SaleRecyclerview", "onBindViewHolder2");
//            int viewtype = holder.getItemViewType();

        }

        @Override
        public int getItemCount() {
            Log.d("SaleRecyclerview", "getItemCount");
            return uri_list.size();

        }
    }


}
