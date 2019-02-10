package com.example.lhw.app_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

public class SaleImfoActivity extends AppCompatActivity {

    private int stuck = 1;
    final String fn_delivery_price = "2500";

    ProductModel productModel;
    UserModel userModel;

    TextView user_name; //구매자 이름
    TextView user_addr; // 구매자 주소
    TextView user_phone; // 구매자 핸드폰번호
    TextView product_name; // 상품이름
    TextView product_price; // 상품 가격
    TextView all_product_price; // 상품 가격
    TextView delivery_price; //배송비
    TextView all_price; // 전체가격
    Button bt_purchase; // 구매버튼

    String user_email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_imfo);
        BootpayAnalytics.init(this, "5c2b1b5eb6d49c67e7bf6fa5");

        productModel = (ProductModel)getIntent().getSerializableExtra("productModel");

        bt_purchase= findViewById(R.id.bt_purchase);
        user_name= findViewById(R.id.user_name);
        user_addr= findViewById(R.id.user_addr);
        user_phone= findViewById(R.id.user_phone);
        product_name= findViewById(R.id.product_name);
        product_price= findViewById(R.id.product_price);
        all_product_price= findViewById(R.id.all_product_price);
        delivery_price= findViewById(R.id.delivery_price);
        all_price= findViewById(R.id.all_price);

        SharedPreferences sf = getSharedPreferences("name", 0); //유저 인덱스값을 가지고오기 위해 이메일을 서버에 넘겨줌
        //추후에 Cookie와 Session으로 바꾸는 작업이 필요
        user_email = sf.getString("name", ""); // 키값으로 꺼냄
        Call<JsonObject> getLoginUserModel = NetRetrofit.getInstance().getService().getUserModel(user_email); //로그인 유저 정보 가지고 오기
        getLoginUserModel.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Gson gson = new Gson();
                userModel = gson.fromJson(response.body().toString(), UserModel.class);
                user_name.setText(userModel.user_name);
                user_addr.setText(userModel.user_addr + " " + userModel.user_detail_addr);
                user_phone.setText(userModel.user_phone);
                product_name.setText(productModel.product_title);
                product_price.setText(productModel.product_price+"원");
                all_product_price.setText(productModel.product_price+"원");
                delivery_price.setText(fn_delivery_price+"원");

                int allprice = Integer.parseInt(productModel.product_price) + Integer.parseInt(fn_delivery_price);

                all_price.setText(String.valueOf(allprice) + "원");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });



        bt_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bootpay.init(getFragmentManager())
                        .setApplicationId("5c2b1b5eb6d49c67e7bf6fa5") // 해당 프로젝트(안드로이드)의 application id 값
                        .setPG(PG.INICIS) // 결제할 PG 사
                        .setUserPhone(userModel.user_phone) // 구매자 전화번호
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
                        }).onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                            @Override
                            public void onDone(@Nullable String message) {
                                Log.d("sibal done", message);
                                Call<JsonObject> UpdateProduct = NetRetrofit.getInstance().getService().UpdateProduct(productModel.product_idx, userModel.user_idx);
                                UpdateProduct.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        Toast.makeText(getApplicationContext(), "결제 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                            @Override
                            public void onReady(@Nullable String message) {
                                Log.d("sibal ready", message);
                            }
                        }).onCancel(new CancelListener() { // 결제 취소시 호출
                            @Override
                            public void onCancel(@Nullable String message) {
                                Toast.makeText(getApplicationContext(), "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.d("sibal cancel", message);
                            }
                        }).onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                            @Override
                            public void onError(@Nullable String message) {
                                Log.d(" sibalerror", message);
                            }
                        }).onClose(new CloseListener() { //결제창이 닫힐때 실행되는 부분
                            @Override
                            public void onClose(String message) {
                                Log.d("sibal close", "close");
                            }
                        }).show();
            }
        });
    }
}
