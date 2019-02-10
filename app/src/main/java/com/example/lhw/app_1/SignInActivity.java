package com.example.lhw.app_1;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignInActivity extends AppCompatActivity {

    final String TAG = "SignInActivity";
    final int DAUM_API = 1;

    EditText text_id; //id 텍스트
    EditText text_name;
    EditText text_pass; //패스워드 텍스트
    EditText text_re_pass; //확인 패스워드 텍스트
    EditText text_phone; //휴대폰 텍스트
    TextView text_addr; //주소
    EditText text_detail_addr; //상세주소 텍스트
    Button bt_addr_search; //주소 검색 버튼
    Button bt_signin; //회원가입 버튼
    Button bt_id_check; //아이디 중복체크 버튼

    String id_check = "N"; //아이디 중복체크 확인 변수

    Retrofit retrofit;

    /**
     * 이메일 포맷 체크
     * @param email
     * @return
     */
    public static boolean checkEmail(String email){ //이메일 정규식 체크
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        text_id = findViewById(R.id.text_id);
        text_name = findViewById(R.id.text_name);
        text_pass = findViewById(R.id.text_pass);
        text_re_pass = findViewById(R.id.text_re_pass);
        text_phone = findViewById(R.id.text_phone);
        text_addr = findViewById(R.id.text_addr);
        text_detail_addr = findViewById(R.id.text_detail_addr);
        bt_addr_search = findViewById(R.id.bt_addr_search);
        bt_signin = findViewById(R.id.bt_signin);
        bt_id_check = findViewById(R.id.bt_id_check);

        bt_id_check.setOnClickListener(new View.OnClickListener() { //아이디 중복체크
            @Override
            public void onClick(View v) { //아이디 체크
                Log.d(TAG, "SignInActivity bt_id_check onClick: ");

                String id = text_id.getText().toString();

                if(id == null || id.equals("") || checkEmail(id) == false){
                    Toast.makeText(SignInActivity.this, "아이디를 확인해주세요", Toast.LENGTH_SHORT).show();
                }

                else {
                    Call<JsonObject> res = NetRetrofit.getInstance().getService().getId(id);
                    res.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Gson gson = new Gson();
                            HashMap<String, String> result = gson.fromJson(response.body().toString(), HashMap.class);
                            if(result.get("result").equals("1")){ //등록된 이메일이 있을 경우
                                Toast.makeText(SignInActivity.this, "이미 이메일이 있습니다.", Toast.LENGTH_SHORT).show();
                                id_check = "N";
                            }

                            else { //등록된 이메일이 없을 경우
                                Toast.makeText(SignInActivity.this, "사용 가능합니다.", Toast.LENGTH_SHORT).show();
                                id_check = "Y";
                            }

                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {

                        }
                    });
                }



//                Toast.makeText(SignInActivity.this, "중복확인 하였습니다.", Toast.LENGTH_SHORT).show();
//                id_check = "Y";
            }
        });

        bt_signin.setOnClickListener(new View.OnClickListener() { //회원가입 버튼을 눌렀을 때
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SignInActivity bt_signin onClick: ");

                String user_email = text_id.getText().toString();
                String user_name = text_name.getText().toString();
                String password = text_pass.getText().toString();
                String re_password = text_re_pass.getText().toString();
                String phone = text_phone.getText().toString();
                String addr = text_addr.getText().toString();
                String detail_addr = text_detail_addr.getText().toString();

                boolean blank_check = true;


                if(user_email == null || user_email.equals("") || checkEmail(user_email) == false){
                    Toast.makeText(SignInActivity.this, "아이디를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }

                else if(user_name == null || user_name.equals("")){
                    Toast.makeText(SignInActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }

                else if(password == null || password.equals("") || re_password == null || re_password.equals("")){
                    Toast.makeText(SignInActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }
                else if(phone == null || phone.equals("")){
                    Toast.makeText(SignInActivity.this, "연락처를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }
                else if(addr == null || addr.equals("")){
                    Toast.makeText(SignInActivity.this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }
                else if(detail_addr == null || detail_addr.equals("")){
                    Toast.makeText(SignInActivity.this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    blank_check = false;
                }

                if(blank_check && id_check.equals("Y") && password.equals(re_password)) { //모두다 입력되었을 때
                    UserModel user = new UserModel(user_email, password, addr, user_name, phone, detail_addr);
                    Call<JsonObject> res2 = NetRetrofit.getInstance().getService().setSignin(user);
                    res2.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Gson gson = new Gson();
                            UserModel user2 = gson.fromJson(response.body().toString(), UserModel.class); //유저모델에 json 받아오기
//                            Toast.makeText(SignInActivity.this, user2.user_email, Toast.LENGTH_SHORT).show();
//                           Toast.makeText(SignInActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "onResponse: " + response.body().toString());
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            Toast.makeText(SignInActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(SignInActivity.this, "네트워크에 이상이 있습니당", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                else if(blank_check && id_check.equals("N")){
                    Toast.makeText(SignInActivity.this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                }

                else if(blank_check && password.equals(re_password) == false){
                    Toast.makeText(SignInActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt_addr_search.setOnClickListener(new View.OnClickListener() { //주소 검색 버튼을 눌렀을 때
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchAddrActivity.class);
                startActivityForResult(intent, DAUM_API);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DAUM_API && data != null){
            text_addr.setText(data.getExtras().getString("addr").toString());

        }
    }
}
