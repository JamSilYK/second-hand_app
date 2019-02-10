package com.example.lhw.app_1;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaleModifyActivity extends AppCompatActivity {

    final int PICTURE_REQUEST_CODE = 64;

    RecyclerView salerecyclerView;

    Button bt_picture_regist;
    Button bt_regist;

    EditText product_title;
    EditText product_contents;
    EditText product_price;

    String user_email; // 유저이메일
    ProductModel productModel;

    ArrayList<PictureModel> picture_list;

    ArrayList<Uri> uri_list;

    //ArrayList<String> image_list; //이미지를 String 리스트로 변형하려고 선언

    ArrayList<String> contenturl_list;
    ArrayList<String> httpurl_list;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_modify);
        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요

        productModel = (ProductModel) getIntent().getSerializableExtra("productModel");
        Toast.makeText(this, productModel.product_idx, Toast.LENGTH_SHORT).show();

        user_email = sf.getString("name", ""); // 키값으로 꺼냄

        salerecyclerView = findViewById(R.id.salerecyclerView);
        bt_picture_regist = findViewById(R.id.bt_picture_regist);
        bt_regist = findViewById(R.id.bt_regist);

        product_title = findViewById(R.id.product_title);
        product_contents = findViewById(R.id.product_contents);
        product_price = findViewById(R.id.product_price);

        product_title.setText(productModel.product_title);
        product_contents.setText(productModel.product_contents);
        product_price.setText(productModel.product_price);

        uri_list = new ArrayList<>();

        bt_regist.setOnClickListener(new View.OnClickListener() { //등록버튼 눌렀을때
            @Override
            public void onClick(View v) {
                Log.d("urisize", "onClick: " + String.valueOf(uri_list.size()));
                uri_list.remove(uri_list.size()-1);
                String title = product_title.getText().toString(); //상품 제목
                String price = product_price.getText().toString(); // 상품 가격
                String contents = product_contents.getText().toString(); // 상품 설명

                if(title.equals("") || title==null || price.equals("") || price==null || contents.equals("") || contents==null){
                    Toast.makeText(getApplicationContext(), "상품 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }

                else {
                    if(uri_list != null && uri_list.size()>0){ //사진 등록했을때
                        contenturl_list = new ArrayList<>();
                        httpurl_list = new ArrayList<>();
                        //ProductModel productModel = new ProductModel(title, price, contents);
                        productModel.product_title = title;
                        productModel.product_price = price;
                        productModel.product_contents = contents;

                        for(int i = 0; i<uri_list.size(); i++) {
                            if(uri_list.get(i).toString().contains("content")){
                                Bitmap bitmap = resize(getApplicationContext(), uri_list.get(i), 100);
                                contenturl_list.add(imageToString(bitmap)); //비트맵을 String 형식으로 변환
                            }

                            else if(uri_list.get(i).toString().contains("http")){
                                httpurl_list.add(uri_list.get(i).toString());
                            }

                        }

                        uploadImage(contenturl_list, httpurl_list, productModel);

                    }

                    else { //사진 등록 안했을때
                        Toast.makeText(getApplicationContext(), "사진은 한장 이상 등록하셔야 합니다.", Toast.LENGTH_SHORT).show();

                    }

                }
            }
        });

        Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getPicture(productModel.product_idx); //사진 받아오기
        res.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();
                picture_list = (ArrayList<PictureModel>) gson.fromJson(response.body().toString(),
                        new TypeToken<ArrayList<PictureModel>>() {
                        }.getType()); //jsonarray 파싱하는법임!

                for(int i = 0; i<picture_list.size(); i++) {
                    Uri uri = Uri.parse(PictureModel.BASE_URL+ picture_list.get(i).picture_path+picture_list.get(i).picture_name+".jpg");
                    uri_list.add(uri);
                }
                uri_list.add(null);
                salerecyclerView.setAdapter(new SaleModifyRecyclerViewAdapter());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                salerecyclerView.setLayoutManager(mLayoutManager);

            }


            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(SaleModifyActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK || data != null) {
                uri_list.remove(uri_list.size()-1);
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
                }
                uri_list.add(null);
                salerecyclerView.setAdapter(new SaleModifyRecyclerViewAdapter());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
                mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                salerecyclerView.setLayoutManager(mLayoutManager);

            }

            else if(data == null){
                try {
                    salerecyclerView.getAdapter().notifyDataSetChanged();
                }

                catch (NullPointerException ne){
                    ne.printStackTrace();
                }

            }
        }
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

    private String imageToString(Bitmap bitmap){ //이미지를 string으로 변환하는 메서드
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    private void uploadImage(ArrayList<String> contenturl_list, ArrayList<String> httpurl_list, ProductModel productModel){ //이미지와 상품정보들을 서버에 보내는 메서드
        ArrayList<String> image_title = new ArrayList<>();
        for(int i = 0; i<contenturl_list.size(); i++){
            String ramdom = RandomStringUtils.randomAlphanumeric(10);
            image_title.add(ramdom);
        }

        Call<ImageModel> UploadModify =
                NetRetrofit.getInstance().getService().uploadmodify(
                        image_title,
                        contenturl_list,
                        httpurl_list,
                        productModel.user_idx,
                        productModel.product_idx,
                        productModel.product_title,
                        productModel.product_contents,
                        productModel.product_price);

        UploadModify.enqueue(new Callback<ImageModel>() {
            @Override
            public void onResponse(Call<ImageModel> call, Response<ImageModel> response) {
                ImageModel imageModel = response.body();
                Toast.makeText(getApplicationContext(), "Server Response: " + imageModel.getResponse(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ImageModel> call, Throwable t) {
                Toast.makeText(SaleModifyActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

    }


    class SaleModifyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public SaleModifyRecyclerViewAdapter() { //생성자

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
            final SaleViewHolder saleViewHolder = (SaleViewHolder)holder;
            //saleViewHolder.img.setImageURI(uri_list.get(position));
            //Picasso.get().load(f).resize(100, 100).into(saleViewHolder.img);
            //Bitmap bitmap = resize(getApplicationContext(), uri_list.get(position), 70);
            if(uri_list.get(position) != null){
                String uri_check = uri_list.get(position).toString();

                if(uri_check.contains("http")){ //인터넷 uri
                    Picasso.get().load(uri_list.get(position)).into(saleViewHolder.img);
                    saleViewHolder.bt_del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            uri_list.remove(uri_list.get(position));
                            notifyDataSetChanged();
                        }
                    });
                }

                else { //content uri
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

                }
            }

            else {
                saleViewHolder.img.setImageResource(R.drawable.ic_baseline_add_24px);
                saleViewHolder.bt_del.setVisibility(View.GONE);
                saleViewHolder.sale_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        //사진을 여러개 선택할수 있도록 한다
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);
                    }
                });
            }



        }

        @Override
        public int getItemCount() {
            Log.d("SaleRecyclerview", "getItemCount");
            return uri_list.size();

        }
    }
}
