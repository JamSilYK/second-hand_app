package com.example.lhw.app_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextView signin; //회원가입 버튼
    TextView search_password; // 패스워드 찾기 버튼
    TextView search_id; //아이디 찾기 버튼
    TextView text_email; //이메일
    TextView text_pass; // 패스워드
    Button bt_login; // 로그인버튼
    CheckBox checkbox; //아이디 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signin = findViewById(R.id.signin);
        search_password = findViewById(R.id.search_password);
        search_id = findViewById(R.id.search_id);
        text_email = findViewById(R.id.text_email);
        text_pass = findViewById(R.id.text_pass);
        bt_login = findViewById(R.id.bt_login);
        checkbox = findViewById(R.id.checkBox);

        SharedPreferences sf = getSharedPreferences("name", 0);
        String str = sf.getString("name", ""); // 키값으로 꺼냄
        if(str.equals("") == false || str != null){
            text_email.setText(str); // EditText에 반영함
            checkbox.setChecked(true);
        }

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_email = text_email.getText().toString();
                String user_pass = text_pass.getText().toString();
                bt_login.setEnabled(false);

                if(user_email.equals("") == false || user_email != null|| user_pass.equals("") == false || user_pass != null){
                    Call<JsonObject> login = NetRetrofit.getInstance().getService().login(user_email, user_pass);
                    login.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            bt_login.setEnabled(true);
                            Gson gson = new Gson();
                            HashMap<String, String> result = gson.fromJson(response.body().toString(), HashMap.class);
                            if(result.get("result").equals("1")){
                                Toast.makeText(LoginActivity.this, "정상적으로 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                                if(checkbox.isChecked()){ //이메일이 저장 버튼이 눌려있으면
                                    SharedPreferences sf = getSharedPreferences("name", 0);
                                    SharedPreferences.Editor editor = sf.edit();//저장하려면 editor가 필요
                                    String str = text_email.getText().toString(); // 사용자가 입력한 값
                                    editor.putString("name", str); // 입력
                                    editor.commit(); // 파일에 최종 반영함
                                }
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "로그인 실패.", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "네트워크에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                else {
                    Toast.makeText(LoginActivity.this, "로그인 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "회원가입 텍스트를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });

        search_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "비밀번호 찾기 텍스트를 눌렀습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        search_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "아이디 찾기 텍스트를 눌렀습니다.", Toast.LENGTH_SHORT).show();
            }
        });


    }

}
