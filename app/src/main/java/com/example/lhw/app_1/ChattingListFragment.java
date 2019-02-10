package com.example.lhw.app_1;

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


public class ChattingListFragment extends Fragment {

    RecyclerView recyclerView;
    String user_email;
    ArrayList<ChatRoomMoldel> chatroomlist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chatting_list, container, false);
        recyclerView = v.findViewById(R.id.recyclerview);

        SharedPreferences sf = getActivity().getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Call<ArrayList<JsonObject>> getChatRoom = NetRetrofit.getInstance().getService().getChatRoom(user_email);
        getChatRoom.enqueue(new Callback<ArrayList<JsonObject>>() {
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
                Gson gson = new Gson();

                chatroomlist = (ArrayList<ChatRoomMoldel>) gson.fromJson(response.body().toString(),
                        new TypeToken<ArrayList<ChatRoomMoldel>>() {
                        }.getType()); //jsonarray 파싱하는법임!
                if(chatroomlist.get(0).response.equals("ok")){
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(new ChatRoomRecyclerViewAdapter());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {

            }
        });
    }

    class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public ChatRoomRecyclerViewAdapter() { //생성자

        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView profile;
            TextView name;
            TextView last_contents;
            TextView date;
            ConstraintLayout item_container;

            public ViewHolder(@NonNull View v) {
                super(v);
                profile = v.findViewById(R.id.profile);
                name = v.findViewById(R.id.name);
                last_contents = v.findViewById(R.id.last_contents);
                date = v.findViewById(R.id.date);
                item_container = v.findViewById(R.id.item_container);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final ViewHolder homeViewHolder = (ViewHolder)holder;

            Picasso.get().load(PictureModel.BASE_URL+chatroomlist.get(position).profile_path).transform(new CircleTransform()).into(homeViewHolder.profile);
            homeViewHolder.name.setText(chatroomlist.get(position).user_name);
            if(chatroomlist.get(position).chat_date != null){
                String str = chatroomlist.get(position).chat_date;
                String result = str.substring(str.lastIndexOf("/")+1);
                chatroomlist.get(position).chat_date = result;
            }
            homeViewHolder.date.setText(chatroomlist.get(position).chat_date);
            try {
                if(chatroomlist.get(position).chat_contents.contains(".jpg")){
                    homeViewHolder.last_contents.setText("사진");
                }
                else {
                    homeViewHolder.last_contents.setText(chatroomlist.get(position).chat_contents);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            homeViewHolder.item_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ChattingActivity.class);
                    intent.putExtra("user_email", user_email);
                    if(chatroomlist.get(position).chatroom_reciever.equals(user_email)){
                        intent.putExtra("product_user_email", chatroomlist.get(position).chatroom_sender);
                    }
                    else {
                        intent.putExtra("product_user_email", chatroomlist.get(position).chatroom_reciever);
                    }

                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatroomlist.size();

        }
    }

}
