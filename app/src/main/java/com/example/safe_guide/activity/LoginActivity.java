package com.example.safe_guide.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safe_guide.R;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private EditText etId, etPwd;
    private Button btnLogin, btnSignUp;
    private CheckBox cbAutoLogin;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String rememberID;

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoLoginUser();
        initWidgets();
    }

    private void initWidgets() {
        setContentView(R.layout.activity_login);

        // GPS 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // 카메라 권한 요청
        int MY_PERMISSIONS_REQUEST_CAMERA=0;

        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(LoginActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        etId = findViewById(R.id.etId);
        etPwd = findViewById(R.id.etPwd);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbAutoLogin = findViewById(R.id.cbAutoLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = etId.getText().toString();
                String userPassword = etPwd.getText().toString();
                if(userId.equals("") || userPassword.equals("")) {
                    Toast.makeText(getApplicationContext(), "정보를 정확히 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                /*try {
                    login(userId, userPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);    // 액티비티 이동 시 애니메이션 제거.
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(LoginActivity.this, SignUpActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);    // 액티비티 이동 시 애니메이션 제거.
                startActivity(intent);
            }
        });
    }

    private void login(String userId, String password) throws JSONException {
        String url = "https://safe-guide.herokuapp.com/api/v1/users/login";

        String json = new JSONObject()
                .put("userId", userId)
                .put("password", password)
                .toString();

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json
        );

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("응답", String.valueOf(response));

                if (response.isSuccessful()) {
                    // TODO: 요청이 성공적으로 처리되었을 때의 동작
                    String responseBody = response.body().string();
                    Log.d("로그인", responseBody);
                    Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                    //intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();

                } else {
                    int statusCode = response.code();
                    String errorBody = response.body().string();
                    handleError(statusCode, errorBody);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: 요청 실패 처리
                e.printStackTrace();
            }
        });
    }

    private void handleError(int statusCode, String errorBody) {
        // TODO: 에러 처리 코드 리팩토링
        if (statusCode == 405) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("에러", errorBody);
                    Toast.makeText(getApplicationContext(), "가입되어 있지 않은 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("에러", errorBody);
                    Toast.makeText(getApplicationContext(), "에러 발생: " + statusCode, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void autoLoginUser() {
        // TODO: 자동 로그인 수행
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();
        rememberID = pref.getString("UserID", "_");   // String 불러오기 (저장해둔 값 없으면 초기값인 _으로 불러옴)

        if(!rememberID.equals("_")) {
            Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}