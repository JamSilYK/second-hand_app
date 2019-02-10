package com.example.lhw.app_1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static org.webrtc.ContextUtils.getApplicationContext;

public class AuctionListFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<AuctionModel> auction_list; //현재 방송중인 방(Room) + 유저정보

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_auction_list, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);

        Button bt_auction_start = v.findViewById(R.id.bt_auction_start);

        Call<ArrayList<JsonObject>> getAuction = NetRetrofit.getInstance().getService().getAuction();
        getAuction.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) { //옥션 리스트 정보 가지고 오기
                Gson gson = new Gson();
                auction_list = (ArrayList<AuctionModel>) gson.fromJson(response.body().toString(),
                        new TypeToken<ArrayList<AuctionModel>>() {
                        }.getType()); //jsonarray 파싱하는법임!
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(new AuctionListAdapter());
            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            }
        });

        bt_auction_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
                /*경매 제목 입력받기.  기존 채팅방 item layout을 사용할까 생각했지만 디자인이 별로일듯*/
            }

        });
        return v;
    }

    void show() { //경매하기 다이얼로그 :: 이메일 입력 받기 -> 유저 정보 확인 -> 액티비티이동
        final EditText edittext = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("경매하기");
        builder.setMessage("경매 제목을 적어주세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(edittext.getText() != null){

                            String auction_title = edittext.getText().toString();
                            SharedPreferences sf = getActivity().getSharedPreferences("name", 0); //현재 로그인 이메일
                            String user_email = sf.getString("name", "");
                            Call<JsonObject> setAuction = NetRetrofit.getInstance().getService().setAuction(user_email, auction_title);
                            setAuction.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    Toast.makeText(getActivity(), response.body().toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Intent intent = new Intent(getActivity(), CameraActivity.class);
                            intent.putExtra("broadcaster_email", user_email); //방송자 이메일 넘기기
                            startActivity(intent);
                        }

                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    class AuctionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public AuctionListAdapter() { //생성자

        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView auction_title;
            TextView broadcaster_user;
            ConstraintLayout item_container;

            public ViewHolder(@NonNull View v) {
                super(v);
                auction_title = v.findViewById(R.id.auction_title);
                broadcaster_user = v.findViewById(R.id.broadcaster_user);
                item_container = v.findViewById(R.id.item_container);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) { //유저정보 가지고오기
            final ViewHolder homeViewHolder = (ViewHolder)holder;
            Call<JsonObject> getUser = NetRetrofit.getInstance().getService().getUserModel(auction_list.get(position).broadcaster_email);
            getUser.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Gson gson = new Gson();
                    UserModel userModel = gson.fromJson(response.body().toString(), UserModel.class);
                    homeViewHolder.auction_title.setText(auction_list.get(position).auction_title);
                    homeViewHolder.broadcaster_user.setText(userModel.user_name);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                }
            });

            homeViewHolder.item_container.setOnClickListener(new View.OnClickListener() { //아이템 클릭했을때 item email 확인 후  activity 이동
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CameraActivity.class);
                    intent.putExtra("broadcaster_email", auction_list.get(position).broadcaster_email);
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return auction_list.size();

        }
    }

}
