package com.example.lhw.app_1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchProductActivity extends AppCompatActivity {

    ArrayList<ProductModel> product_list; //검색결과 상품
    Button bt_search;
    Button bt_change;
    TextView search_input_text;
    TextView product_count;
    RecyclerView recyclerView;

    String search_text;
    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        search_text = getIntent().getStringExtra("search_text");

        bt_search = findViewById(R.id.bt_search);
        bt_change = findViewById(R.id.bt_change);
        search_input_text = findViewById(R.id.search_input_text);
        product_count = findViewById(R.id.product_count);
        recyclerView = findViewById(R.id.recyclerview);

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄
        search_input_text.setText(search_text);

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(search_input_text.getText().equals("") == false){
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String search_date = transFormat.format(calendar.getTime()); //날짜
                    final String search_text = search_input_text.getText().toString(); //검색텍스트
                    Call<JsonObject> SetSearch = NetRetrofit.getInstance().getService().setSearch(user_email, search_input_text.getText().toString(), search_date);
                    SetSearch.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Toast.makeText(getApplicationContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
                            Call<ArrayList<JsonObject>> getSearchProduct = NetRetrofit.getInstance().getService().getSearchProduct(search_input_text.getText().toString(), "current_regist");
                            getSearchProduct.enqueue(new Callback<ArrayList<JsonObject>>() {
                                @Override
                                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                                    Gson gson = new Gson();
                                    product_list.clear();
                                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<ProductModel>>() {}.getType()); //jsonarray 파싱하는법임!
                                    if(product_list.get(0).response.equals("empty")){
                                        Toast.makeText(SearchProductActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    product_count.setText("총 " + String.valueOf(product_list.size())+"개");
                                    recyclerView.getAdapter().notifyDataSetChanged();

                                }

                                @Override
                                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                                    Toast.makeText(SearchProductActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });


        Call<ArrayList<JsonObject>> getSearchProduct = NetRetrofit.getInstance().getService().getSearchProduct(search_input_text.getText().toString(), "current_regist");
        getSearchProduct.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();
                product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<ProductModel>>() {}.getType()); //jsonarray 파싱하는법임!
                if(product_list.get(0).response.equals("empty")){
                    Toast.makeText(SearchProductActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    product_count.setText("총 " + String.valueOf(product_list.size())+"개");
                    recyclerView.setAdapter(new RecyclerViewAdapter());
                    int numberOfColumns = 2;
                    recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), numberOfColumns));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(SearchProductActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        bt_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getApplicationContext(), v);
                getMenuInflater().inflate(R.menu.search_item_menu, p.getMenu());
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.current_regist){
                            Call<ArrayList<JsonObject>> getSearchProduct = NetRetrofit.getInstance().getService().getSearchProduct(search_input_text.getText().toString(), "current_regist");
                            getSearchProduct.enqueue(new Callback<ArrayList<JsonObject>>() {
                                @Override
                                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                                    Gson gson = new Gson();
                                    product_list.clear();
                                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<ProductModel>>() {}.getType()); //jsonarray 파싱하는법임!
                                    if(product_list.get(0).response.equals("empty")){
                                        Toast.makeText(SearchProductActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    recyclerView.getAdapter().notifyDataSetChanged();

                                }

                                @Override
                                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                                    Toast.makeText(SearchProductActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else if(item.getItemId() == R.id.low_price){
                            Call<ArrayList<JsonObject>> getSearchProduct = NetRetrofit.getInstance().getService().getSearchProduct(search_input_text.getText().toString(), "low_price");
                            getSearchProduct.enqueue(new Callback<ArrayList<JsonObject>>() {
                                @Override
                                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                                    Gson gson = new Gson();
                                    product_list.clear();
                                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<ProductModel>>() {}.getType()); //jsonarray 파싱하는법임!
                                    if(product_list.get(0).response.equals("empty")){
                                        Toast.makeText(SearchProductActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                                    Toast.makeText(SearchProductActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else if(item.getItemId() == R.id.high_price){
                            Call<ArrayList<JsonObject>> getSearchProduct = NetRetrofit.getInstance().getService().getSearchProduct(search_input_text.getText().toString(), "high_price");
                            getSearchProduct.enqueue(new Callback<ArrayList<JsonObject>>() {
                                @Override
                                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                                    Gson gson = new Gson();
                                    product_list.clear();
                                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<ProductModel>>() {}.getType()); //jsonarray 파싱하는법임!
                                    if(product_list.get(0).response.equals("empty")){
                                        Toast.makeText(SearchProductActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                                    Toast.makeText(SearchProductActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        bt_change.setText(item.getTitle());
                        return false;
                    }
                });
                p.show();
            }
        });

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<PictureModel> picture_list;

        public RecyclerViewAdapter() { //생성자

        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            ImageView img;
            TextView price;
            TextView title;
            ConstraintLayout item_container;

            public ViewHolder(@NonNull View v) {
                super(v);
                img = v.findViewById(R.id.img);
                price = v.findViewById(R.id.price);
                title = v.findViewById(R.id.title);
                item_container = v.findViewById(R.id.item_container);
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeitem, parent, false);

            return new ViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Log.d("HomeRecyclerViewAdapter", "onBindViewHolder");
            final ViewHolder homeViewHolder = (ViewHolder)holder;
            DisplayMetrics displayMetrics = new DisplayMetrics();

            ((Activity) holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int deviceWidth = displayMetrics.widthPixels;  // 핸드폰의 가로 해상도를 구함.
            int deviceHeight = displayMetrics.heightPixels;  // 핸드폰의 세로 해상도를 구함.
            holder.itemView.getLayoutParams().height = deviceHeight/2;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
            holder.itemView.getLayoutParams().width = deviceWidth/2;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
            holder.itemView.requestLayout(); // 변경 사항 적용
            homeViewHolder.price.setText(product_list.get(position).product_price + "원");
            homeViewHolder.title.setText(product_list.get(position).product_title);

            homeViewHolder.item_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //recyclerview 아이템 클릭했을 때
                    Intent intent = new Intent(getApplicationContext(), SaleReadActivity.class);
                    intent.putExtra("productModel", (Serializable) product_list.get(position));
                    startActivity(intent);
                }
            });

            Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getPicture(product_list.get(position).product_idx); //사진 받아오기
            res.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    Gson gson = new Gson();
                    picture_list = (ArrayList<PictureModel>) gson.fromJson(response.body().toString(),
                            new TypeToken<ArrayList<PictureModel>>() {
                            }.getType()); //jsonarray 파싱하는법임!
                    Uri uri = Uri.parse(PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Log.d("", "onResponse: " + PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Picasso.get().load(uri).into(homeViewHolder.img);
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d("HomeRecyclerViewAdapter", "getItemCount");
            return product_list.size();

        }
    }
}
