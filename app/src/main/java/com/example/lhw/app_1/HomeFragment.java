package com.example.lhw.app_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    final String TAG = "HomeFragment";

    private TextView text_search; // 게시물 검색
    private RecyclerView recyclerView;
    FloatingActionButton floatingActionButton; //글쓰기 버튼
    ArrayList<ProductModel> product_list; //상품모델리스트

    final String BASE_PATH = "http://54.180.88.214/android/uploads";
    final int SALE_ITEM_CLICK = 26;

    final int WRITE_PRODUCT = 23;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        text_search = v.findViewById(R.id.text_search);
        text_search.setBackgroundResource(R.drawable.text_solid); //검색 text
        recyclerView = v.findViewById(R.id.recyclerview);

        text_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { //스크롤 이동할때 floating 버튼 숨기기
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 ||dy<0 && floatingActionButton.isShown()) {
                    floatingActionButton.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    floatingActionButton.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        }); //스크롤 이동할때 floating 버튼 숨기기

        Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getProduct();
        res.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();
                //ProductModel productModel  = gson.fromJson(response.body().toString(), ProductModel.class); //유저모델에 json 받아오기
                if(response.body() != null){
                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(),
                            new TypeToken<ArrayList<ProductModel>>() {
                            }.getType()); //jsonarray 파싱하는법임!

                    for(int i = 0; i<product_list.size(); i++) {
                        Log.d("confirm", product_list.get(i).toString());
                    }

                    recyclerView.setAdapter(new HomeRecyclerViewAdapter());
                    int numberOfColumns = 2;
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        }); //상품 리스트 받아오기

        floatingActionButton = v.findViewById(R.id.floatingActionButton); //판매 버튼
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SaleActivity.class);
                startActivityForResult(intent, WRITE_PRODUCT);
            }
        });

        return v;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WRITE_PRODUCT){ //사진 등록했을때 데이터 초기화하기
            if(product_list != null && product_list.size() > 0)
            product_list.clear();
            Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getProduct();
            res.enqueue(new Callback<ArrayList<JsonObject>>() {
                @Override
                public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                    Gson gson = new Gson();
                    //ProductModel productModel  = gson.fromJson(response.body().toString(), ProductModel.class); //유저모델에 json 받아오기
                    product_list = (ArrayList<ProductModel>) gson.fromJson(response.body().toString(),
                            new TypeToken<ArrayList<ProductModel>>() {
                            }.getType()); //jsonarray 파싱하는법임!
                    if(product_list != null){

                        try {
                            Log.d("product_list", "onResponse: " + String.valueOf(product_list.size()));
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }

                        catch (NullPointerException ne){
                            recyclerView.setAdapter(new HomeRecyclerViewAdapter());
                            int numberOfColumns = 2;
                            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numberOfColumns));
                        }
                    }

                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class HomeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<PictureModel> picture_list;

        public HomeRecyclerViewAdapter() { //생성자

        }

        private class HomeViewHolder extends RecyclerView.ViewHolder {

            ImageView img;
            TextView price;
            TextView title;
            ConstraintLayout item_container;

            public HomeViewHolder(@NonNull View v) {
                super(v);
                img = v.findViewById(R.id.img);
                price = v.findViewById(R.id.price);
                title = v.findViewById(R.id.title);
                item_container = v.findViewById(R.id.item_container);
                Log.d("HomeRecyclerViewAdapter", "HomeViewHolder");
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            Log.d("HomeRecyclerViewAdapter", "onCreateViewHolder1");
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeitem, parent, false);
            Log.d("HomeRecyclerViewAdapter", "onCreateViewHolder2");

            return new HomeViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Log.d("HomeRecyclerViewAdapter", "onBindViewHolder");
            final HomeViewHolder homeViewHolder = (HomeViewHolder)holder;
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
                    Intent intent = new Intent(getActivity(), SaleReadActivity.class);
                    intent.putExtra("productModel", (Serializable) product_list.get(position));
                    startActivityForResult(intent, SALE_ITEM_CLICK);
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
                    Log.d(TAG, "onResponse: uri : " + PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Log.d("", "onResponse: " + PictureModel.BASE_URL + picture_list.get(0).picture_path + picture_list.get(0).picture_name + ".jpg");
                    Picasso.get().load(uri).into(homeViewHolder.img);
                }

                @Override
                public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
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
