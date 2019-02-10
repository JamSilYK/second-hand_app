package com.example.lhw.app_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.HashMap;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.BootpayAnalytics;
import kr.co.bootpay.CancelListener;
import kr.co.bootpay.CloseListener;
import kr.co.bootpay.ConfirmListener;
import kr.co.bootpay.DoneListener;
import kr.co.bootpay.ErrorListener;
import kr.co.bootpay.ReadyListener;
import kr.co.bootpay.enums.Method;
import kr.co.bootpay.enums.PG;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.view.View.GONE;

public class SaleReadActivity extends AppCompatActivity {

    final String TAG = "SaleReadActivity";

    private int stuck = 1;

    ArrayList<PictureModel> picture_list; // 사진리스트~~

    String user_email; //고유값, 키값으로 사용하고있음
    String user_idx; //로그인 유저의 user_idx값
    String user_name; //로그인한 유저의 user_name값
    ProductModel productModel;

    UserModel ProductUserModel; // 해당 게시물의 유저 정보
    ArrayList<CommentModel> comment_list;

    ImageView picture_user_profile;
    TextView text_username;
    LinearLayout picture_container;
    TextView text_explain;
    TextView text_price;
    TextView text_title;
    TextView product_info;
    EditText comment_text;
    Button bt_send_comment;
    Button bt_like;
    Button bt_call;
    Button bt_chat;
    Button bt_sale;
    ScrollView scrollview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_read);
        BootpayAnalytics.init(this, "5c2b1b5eb6d49c67e7bf6fa5");
        picture_user_profile = findViewById(R.id.picture_user_profile); //유저 프로필 사진
        text_username = findViewById(R.id.text_username); // 유저 이름
        picture_container = findViewById(R.id.picture_container); //사진 컨테이너
        text_explain = findViewById(R.id.text_explain); // 사진 본문
        comment_text = findViewById(R.id.comment_text); // 댓글 text
        bt_send_comment = findViewById(R.id.bt_send_comment); //댓글 버튼
        bt_like = findViewById(R.id.bt_like); // 장바구니
        bt_call = findViewById(R.id.bt_call); // 판매자한테 전화하기
        bt_chat = findViewById(R.id.bt_chat); // 채팅하기 버튼
        bt_sale = findViewById(R.id.bt_sale); //구매 버튼
        text_price = findViewById(R.id.text_price); //상품 가격
        text_title = findViewById(R.id.text_title); //상품 제목
        product_info = findViewById(R.id.product_info);
        scrollview = findViewById(R.id.scrollview);

        bt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                intent.putExtra("user_email", user_email);
                intent.putExtra("product_user_email", ProductUserModel.user_email);
                startActivity(intent);
            }
        });

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄
        Call<JsonObject> getLoginUserModel = NetRetrofit.getInstance().getService().getUserModel(user_email); //로그인 유저 정보 가지고 오기
        getLoginUserModel.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                UserModel userModel = gson.fromJson(response.body().toString(), UserModel.class);
                user_idx = userModel.user_idx;
                user_name = userModel.user_name;
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        productModel = (ProductModel) intent.getSerializableExtra("productModel");
        if(productModel.product_check.equals("Y")){ //판매완료 상품일때
            bt_like.setText("");
            bt_like.setEnabled(false);
            bt_call.setText("");
            bt_call.setEnabled(false);
            bt_chat.setText("");
            bt_chat.setEnabled(false);
            bt_sale.setEnabled(false);
            bt_sale.setText("판매완료");
        }

        Toast.makeText(this, productModel.user_idx, Toast.LENGTH_SHORT).show();

        Call<ArrayList<JsonObject>> getComment = NetRetrofit.getInstance().getService().getComment(user_email, productModel.product_idx); //댓글 리스트 가지고 오기
        getComment.enqueue(new Callback<ArrayList<JsonObject>>() { //
            @Override
            public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {

                Gson gson = new Gson();
                comment_list = (ArrayList<CommentModel>) gson.fromJson(response.body().toString(),
                        new TypeToken<ArrayList<CommentModel>>() {
                    }.getType()); //jsonarray 파싱하는법임!
                if(comment_list.get(0).response.equals("fail") == false){
                    final LinearLayout con = findViewById(R.id.comment_container);
                    for(int i = 0; i<comment_list.size(); i++) {
                        final View v = LayoutInflater.from(con.getContext()).inflate(R.layout.comment_item, con, false);
                        Button bt = v.findViewById(R.id.bt_commtent_del);
                        if(comment_list.get(i).user_idx.equals(user_idx) || productModel.user_idx.equals(user_idx)){ //자신이 작성한 댓글이거나 상품을 올린 유저일 경우 댓글 삭제 가능
                            final int finalI = i;
                            bt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) { //댓글 삭제
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleReadActivity.this);
                                    builder.setTitle("댓글 삭제");
                                    builder.setMessage("댓글을 삭제하시겠습니까?");
                                    builder.setPositiveButton("예",new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            String comment_idx = comment_list.get(finalI).comment_idx;
                                            con.removeView(v);
                                            Call<JsonObject> DelComment = NetRetrofit.getInstance().getService().DelComment(comment_idx);
                                            DelComment.enqueue(new Callback<JsonObject>() {
                                                @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    Toast.makeText(SaleReadActivity.this, "성공", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                                    Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                    builder.setNegativeButton("아니오",
                                            new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    builder.show();
                                }
                            });
                        }
                        else if((comment_list.get(i).user_idx.equals(user_idx) || productModel.user_idx.equals(user_idx)) == false){
                            bt = v.findViewById(R.id.bt_commtent_del);
                            bt.setVisibility(GONE);
                        }

                        Call<JsonObject> getCommentProfile = NetRetrofit.getInstance().getService().getCommentProfile(comment_list.get(i).user_idx); //유저 프로필 사진 가지고 오기
                        getCommentProfile.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                Gson gson = new Gson();
                                ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                                Uri uri = Uri.parse(PictureModel.BASE_URL+ profileModel.profile_path+profileModel.profile_name+".jpg");
                                ImageView comment_profile = v.findViewById(R.id.comment_profile);
                                Picasso.get().load(uri).transform(new CircleTransform()).into(comment_profile);
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        });

                        TextView comment_text = v.findViewById(R.id.comment_text);
                        comment_text.setText(comment_list.get(i).comment_contents);
                        TextView comment_namedate = v.findViewById(R.id.comment_namedate);
                        comment_namedate.setText(comment_list.get(i).user_name + " / " + comment_list.get(i).comment_date);
                        con.addView(v);
                    }
                }

            }

            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });


        bt_send_comment.setOnClickListener(new View.OnClickListener() { //댓글남기기
            @Override
            public void onClick(View v) {
                if(comment_text.getText().toString().equals("") == false){
                    bt_send_comment.setEnabled(false);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final String date = transFormat.format(calendar.getTime());

                    Call<JsonObject> SetComment = NetRetrofit.getInstance().getService().SetComment(
                            user_email,
                            productModel.product_idx,
                            comment_text.getText().toString(),
                            date);
                    SetComment.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            final LinearLayout con = findViewById(R.id.comment_container);
                            final View v = LayoutInflater.from(con.getContext()).inflate(R.layout.comment_item, con, false);
                            Gson gson = new Gson();
                            HashMap<String, String> hashMap = gson.fromJson(response.body().toString(), HashMap.class);
                            if(hashMap.get("result").equals("Fail") == false){
                                final String comment_idx = hashMap.get("result");
                                Call<JsonObject> getCommentProfile = NetRetrofit.getInstance().getService().getCommentProfile(user_idx);
                                getCommentProfile.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        Gson gson = new Gson();
                                        ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                                        Uri uri = Uri.parse(PictureModel.BASE_URL+ profileModel.profile_path+profileModel.profile_name+".jpg");
                                        ImageView comment_profile = v.findViewById(R.id.comment_profile);
                                        Picasso.get().load(uri).transform(new CircleTransform()).into(comment_profile);
                                        TextView item_comment_text = v.findViewById(R.id.comment_text);
                                        item_comment_text.setText(comment_text.getText().toString());
                                        TextView comment_namedate = v.findViewById(R.id.comment_namedate);
                                        comment_namedate.setText(user_name + " / " + date);
                                        con.addView(v);
                                        Button bt = v.findViewById(R.id.bt_commtent_del);
                                        bt.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {


                                                AlertDialog.Builder builder = new AlertDialog.Builder(SaleReadActivity.this);
                                                builder.setTitle("댓글 삭제");
                                                builder.setMessage("댓글을 삭제하시겠습니까?");
                                                builder.setPositiveButton("예",new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Call<JsonObject> DelComment = NetRetrofit.getInstance().getService().DelComment(comment_idx);
                                                        DelComment.enqueue(new Callback<JsonObject>() {
                                                            @Override
                                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                                con.removeView(v);
                                                                Toast.makeText(SaleReadActivity.this, "성공", Toast.LENGTH_SHORT).show();
                                                            }

                                                            @Override
                                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                                builder.setNegativeButton("아니오",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                builder.show();
                                            }
                                        });
                                        comment_text.setText("");
                                        bt_send_comment.setEnabled(true);
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                else {
                    Toast.makeText(SaleReadActivity.this, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt_sale.setOnClickListener(new View.OnClickListener() { //구매 or 수정 버튼 눌렀을 때
            @Override
            public void onClick(View v) {
                if(bt_sale.getText().equals("수정")){
                    show("MODIFY");
                }

                else if(bt_sale.getText().equals("구매")){
                    show("SALE");
                }
            }
        });

        Call<JsonObject> getUserModel = NetRetrofit.getInstance().getService().getProductUser(productModel.user_idx); //해당 상품 유저 데이터 가지고 오기
        getUserModel.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                ProductUserModel = gson.fromJson(response.body().toString(), UserModel.class);
                text_username.setText(ProductUserModel.user_name);
                Call<JsonObject> res = NetRetrofit.getInstance().getService().getProfile(ProductUserModel.user_email); //유저 프로필 기본사진 가지고오기
                if(user_email.equals(ProductUserModel.user_email)){
                    bt_sale.setText("수정");
                    bt_like.setText("삭제");
                    bt_call.setText("");
                    bt_call.setEnabled(false);
                    bt_chat.setText("");
                    bt_chat.setEnabled(false);
                    if(productModel.product_check.equals("Y")){
                        bt_sale.setEnabled(false);
                        bt_sale.setText("판매완료");
                        bt_like.setEnabled(true);
                    }
                }
                res.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Gson gson = new Gson();
                        ProfileModel profileModel = gson.fromJson(response.body(), ProfileModel.class);
                        Uri uri = Uri.parse(PictureModel.BASE_URL+ profileModel.profile_path+profileModel.profile_name+".jpg");
                        Picasso.get().load(uri).transform(new CircleTransform()).into(picture_user_profile);
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        bt_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //유저한테 전화하기
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+ProductUserModel.user_phone));
                startActivity(intent);
            }
        });

        final Call<JsonObject> getLikeit = NetRetrofit.getInstance().getService().getLikeit(user_email, productModel.product_idx); //찜이 되어있는지 안되어있는지 확인
        getLikeit.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                HashMap<String, String> result = gson.fromJson(response.body().toString(), HashMap.class);
                if(result.get("result").equals("Y")){ //찜이 되어있을때
                    //Toast.makeText(SaleReadActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                    bt_like.setTextColor(RED);
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        bt_like.setOnClickListener(new View.OnClickListener() { //찜하기
            @Override
            public void onClick(View v) {//찜하기
                if(bt_like.getText().equals("삭제")){
                    show("DELETE");
                }
                else if(bt_like.getText().equals("찜")){
                    String product_idx = productModel.product_idx;
                    Call<JsonObject> SetLike = NetRetrofit.getInstance().getService().SetLike(user_email, product_idx);
                    SetLike.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Gson gson = new Gson();
                            HashMap<String, String>result = gson.fromJson(response.body().toString(), HashMap.class);
                            if(result.get("result").equals("정상적으로 찜되었습니다.")){
                                bt_like.setTextColor(RED);
                            }
                            else if(result.get("result").equals("정상적으로 해제되었습니다.")){
                                bt_like.setTextColor(BLACK);
                            }
                            Toast.makeText(SaleReadActivity.this, result.get("result"), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
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

                LinearLayout ll = findViewById(R.id.picture_container);

                for(int i = 0; i<picture_list.size(); i++) {

                    LinearLayout con = findViewById(R.id.picture_container);

                    View v = LayoutInflater.from(con.getContext()).inflate(R.layout.sale_read_item, con, false);
                    Uri uri = Uri.parse(PictureModel.BASE_URL+ picture_list.get(i).picture_path+picture_list.get(i).picture_name+".jpg");
                    ImageView img = v.findViewById(R.id.img);
                    Picasso.get().load(uri).into(img);
                    ll.addView(v);

                }

                Log.d(TAG, "onResponse: " + String.valueOf(picture_list.size()));
                text_title.setText(productModel.product_title); //상품 제목
                text_price.setText(productModel.product_price + "원"); //상품가격
                text_explain.setText(productModel.product_contents);//상품 설명

            }


            @Override
            public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
                Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    void show(String check) { //confirm dialog ex) 예, 아니오
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(check.equals("DELETE")){
            builder.setTitle("게시물 삭제");
            builder.setMessage("게시물을 삭제하시겠습니까?");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Call<JsonObject> DelProduct = NetRetrofit.getInstance().getService().DelProduct(user_email, productModel.product_idx);
                            DelProduct.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    Toast.makeText(SaleReadActivity.this, "정상적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
            builder.show();
        }

        else if(check.equals("MODIFY")){
            builder.setTitle("게시물 수정");
            builder.setMessage("게시물을 수정하시겠습니까?");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), SaleModifyActivity.class);
                            intent.putExtra("productModel", productModel);
                            startActivity(intent);

                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
            builder.show();
        }

        else if(check.equals("SALE")){
            builder.setTitle("상품 구매");
            builder.setMessage("상품을 구매하시겠습니까?");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SaleReadActivity.this, SaleImfoActivity.class);
                            intent.putExtra("productModel", (Serializable) productModel);
                            startActivity(intent);

/*                            Bootpay.init(getFragmentManager())
                                    .setApplicationId("5c2b1b5eb6d49c67e7bf6fa5") // 해당 프로젝트(안드로이드)의 application id 값
                                    .setPG(PG.INICIS) // 결제할 PG 사
                                    .setUserPhone(ProductUserModel.user_phone) // 구매자 전화번호
                                    .setMethod(Method.CARD) // 결제수단
                                    .setName(productModel.product_title) // 결제할 상품명
                                    .setOrderId("12345") //고유 주문번호로, 생성하신 값을 보내주셔야 합니다.
                                    .setPrice(Integer.parseInt(productModel.product_price)) // 결제할 금액
                                    //.setAccountExpireAt("2018-09-22") // 가상계좌 입금기간 제한 ( yyyy-mm-dd 포멧으로 입력해주세요. 가상계좌만 적용됩니다. 오늘 날짜보다 더 뒤(미래)여야 합니다 )
                                    .setQuotas(new int[] {0,2,3}) // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
                                    .addItem(productModel.product_title, 1, "ITEM_CODE", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
                                    .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                                        @Override
                                        public void onConfirm(@Nullable String message) {
                                            if (0 < stuck) Bootpay.confirm(message); // 재고가 있을 경우.
                                            else Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                                            Log.d("sibal confirm", message);
                                        }
                                    })
                                    .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                                        @Override
                                        public void onDone(@Nullable String message) {
                                            Log.d("sibal done", message);
                                            Call<JsonObject> UpdateProduct = NetRetrofit.getInstance().getService().UpdateProduct(productModel.product_idx, user_idx);
                                            UpdateProduct.enqueue(new Callback<JsonObject>() {
                                                @Override
                                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                    Toast.makeText(SaleReadActivity.this, "결제 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }

                                                @Override
                                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                                    Toast.makeText(SaleReadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                        }
                                    })
                                    .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                                        @Override
                                        public void onReady(@Nullable String message) {
                                            Log.d("sibal ready", message);
                                        }
                                    })
                                    .onCancel(new CancelListener() { // 결제 취소시 호출
                                        @Override
                                        public void onCancel(@Nullable String message) {
                                            Toast.makeText(SaleReadActivity.this, "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                                            Log.d("sibal cancel", message);
                                        }
                                    })
                                    .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                                        @Override
                                        public void onError(@Nullable String message) {
                                            Log.d(" sibalerror", message);
                                        }
                                    })
                                    .onClose(new CloseListener() { //결제창이 닫힐때 실행되는 부분
                                        @Override
                                        public void onClose(String message) {
                                            Log.d("sibal close", "close");
                                        }
                                    })
                                    .show();*/
                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
            builder.show();
        }
    }


}
