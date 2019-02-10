package com.example.lhw.app_1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.moshi.Json;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class listActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String user_email;
    TextView listtext;

    ArrayList<ProductModel> productList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        user_email = sf.getString("name", ""); // 키값으로 꺼냄
        recyclerView = findViewById(R.id.recyclerview);
        listtext = findViewById(R.id.listtext);
        final Gson gson = new Gson();
        String check = getIntent().getStringExtra("check");

        if(check.equals("likeit")){ //찜목록
            Call<ArrayList<JsonObject>> getLikteitList = NetRetrofit.getInstance().getService().getLikteitList(user_email);
            getLikteitList.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    if(response.body() != null){
                        productList = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(),
                                new TypeToken<ArrayList<ProductModel>>() {
                                }.getType()); //jsonarray 파싱하는법임!
                    }
                    Toast.makeText(listActivity.this, String.valueOf(productList.size()), Toast.LENGTH_SHORT).show();
                    listtext.setText("찜목록");
                    recyclerView.setAdapter(new LikeRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(listActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(check.equals("purchase")){ // 구매목록
            listtext.setText("구매 목록");
            Call<ArrayList<JsonObject>> getPurchaseList = NetRetrofit.getInstance().getService().getPurchaseList(user_email);
            getPurchaseList.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    if(response.body() != null){
                        productList = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(),
                                new TypeToken<ArrayList<ProductModel>>() {
                                }.getType()); //jsonarray 파싱하는법임!
                    }
                    Toast.makeText(listActivity.this, String.valueOf(productList.size()), Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new LikeRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(listActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(check.equals("sale")){ //판매 목록
            listtext.setText("판매 목록");
            Call<ArrayList<JsonObject>> getSaleList = NetRetrofit.getInstance().getService().getSaleList(user_email);
            getSaleList.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    if(response.body() != null){
                        productList = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(),
                                new TypeToken<ArrayList<ProductModel>>() {
                                }.getType()); //jsonarray 파싱하는법임!
                    }
                    Toast.makeText(listActivity.this, String.valueOf(productList.size()), Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new LikeRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {

                }
            });

        }
    }

    class LikeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<PictureModel> picture_list;

        public LikeRecyclerViewAdapter() { //생성자

        }

        private class LikeViewHolder extends RecyclerView.ViewHolder {

            ImageView profile;
            TextView title;
            TextView price;
            ConstraintLayout itemcontainer;

            public LikeViewHolder(@NonNull View v) {
                super(v);
                profile = v.findViewById(R.id.profile);
                price = v.findViewById(R.id.price);
                title = v.findViewById(R.id.title);
                itemcontainer = v.findViewById(R.id.itemcontainer);
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);

            return new LikeViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final LikeViewHolder likeViewHolder = (LikeViewHolder)holder;
            likeViewHolder.price.setText(productList.get(position).product_price + "원");
            likeViewHolder.title.setText(productList.get(position).product_title);
            if(productList.get(position).product_check.equals("Y")){
                likeViewHolder.title.setText(productList.get(position).product_title + "(판매완료)");
            }


            Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getPicture(productList.get(position).product_idx); //사진 받아오기
            res.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    Gson gson = new Gson();
                    picture_list = (ArrayList<PictureModel>) gson.fromJson(response.body().toString(),
                            new TypeToken<ArrayList<PictureModel>>() {
                            }.getType()); //jsonarray 파싱하는법임!
                    Uri uri = Uri.parse(PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Log.d("", "onResponse: " + PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Picasso.get().load(uri).transform(new CircleTransform()).into(likeViewHolder.profile);
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(listActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
            likeViewHolder.itemcontainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SaleReadActivity.class);
                    intent.putExtra("productModel", (Serializable) productList.get(position));
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return productList.size();

        }
    }

}
