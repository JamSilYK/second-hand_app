package com.example.lhw.app_1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.moshi.Json;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private TextView text_search;
    private Button bt_search;
    private RecyclerView recyclerView;
    ArrayList<SearchModel> search_list;
    String user_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄

        text_search = findViewById(R.id.text_search);
        text_search.setBackgroundResource(R.drawable.text_solid); //검색 text
        recyclerView = findViewById(R.id.recyclerview);
        bt_search = findViewById(R.id.bt_search);

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(text_search.getText().equals("") == false){
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String search_date = transFormat.format(calendar.getTime()); //날짜
                    final String search_text = text_search.getText().toString(); //검색텍스트
                    text_search.setText("");
                    Call<JsonObject> SetSearch = NetRetrofit.getInstance().getService().setSearch(user_email, search_text, search_date);
                    SetSearch.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Toast.makeText(SearchActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                            Intent inetnt = new Intent(getApplicationContext(), SearchProductActivity.class);
                            inetnt.putExtra("search_text", search_text);
                            startActivity(inetnt);
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(SearchActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Call<ArrayList<JsonObject>> getSearch = NetRetrofit.getInstance().getService().getSearch(user_email);
        getSearch.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();
                //Toast.makeText(SearchActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                search_list = (ArrayList<SearchModel>) gson.fromJson(response.body().toString(), new TypeToken<ArrayList<SearchModel>>() {}.getType()); //jsonarray 파싱하는법임!
                if(search_list.get(0).response.equals("empty") == false){
                    recyclerView.setAdapter(new SearchitemRecyclerViewAdapter());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class SearchitemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public SearchitemRecyclerViewAdapter() { //생성자

        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            TextView item_search_text;
            TextView item_search_date;
            ConstraintLayout item_container;
            Button bt_del;

            public ViewHolder(@NonNull View v) {
                super(v);
                item_search_text = v.findViewById(R.id.item_search_text);
                item_search_date = v.findViewById(R.id.item_search_date);
                item_container = v.findViewById(R.id.item_container);
                bt_del= v.findViewById(R.id.bt_del);
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);

            return new ViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder homeViewHolder = (ViewHolder)holder;

            homeViewHolder.item_search_text.setText(search_list.get(position).search_text);
            homeViewHolder.item_search_date.setText(search_list.get(position).search_date);

            homeViewHolder.item_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //recyclerview 아이템 클릭했을 때
                    Intent inetnt = new Intent(getApplicationContext(), SearchProductActivity.class);
//                    Toast.makeText(SearchActivity.this, search_list.get(position).search_text, Toast.LENGTH_SHORT).show();
                    inetnt.putExtra("search_text", search_list.get(position).search_text);
                    startActivity(inetnt);
                }
            });

            homeViewHolder.bt_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<JsonObject> delSearch = NetRetrofit.getInstance().getService().delSearch(search_list.get(position).search_idx, search_list.get(position).user_idx);
                    delSearch.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Toast.makeText(SearchActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                            search_list.remove(position);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(SearchActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return search_list.size();

        }
    }
}
