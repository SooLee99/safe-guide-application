package com.example.safe_guide.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.safe_guide.R;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;


public class SignUpActivity extends AppCompatActivity {
    private EditText etId, etPwd, etCFPwd, etName, etPhoneNumber, etAddress;
    private Spinner spGender;
    private DatePicker dpBirth;
    private Button btnSignUp;
    private String birthDate = "2010-01-01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initWidgets();
    }

    private void initWidgets() {
        etId = findViewById(R.id.etId);
        etPwd = findViewById(R.id.etPwd);
        etCFPwd = findViewById(R.id.etCFPwd);
        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        dpBirth = findViewById(R.id.dpBirth);
        etAddress = findViewById(R.id.etAddress);
        spGender = findViewById(R.id.spGender);
        btnSignUp = findViewById(R.id.btnSignUp);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);

        dpBirth.init(2010, 01, 01, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                birthDate = year + "-" + monthOfYear + "-" + dayOfMonth;
            }
        });

        // 회원가입 버튼 클릭 이벤트 메서드
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = etId.getText().toString();
                String userPassword = etPwd.getText().toString();
                String userCFPassword = etCFPwd.getText().toString();
                String userName = etName.getText().toString();
                String userPhoneNumber = etPhoneNumber.getText().toString();
                String userAddress = etAddress.getText().toString();
                String userGender = spGender.getSelectedItem().toString();

                // 모든 정보 입력 확인
                if(userId.equals("") || userPassword.equals("") || userName.equals("") || userPhoneNumber.equals("") || userAddress.equals("")) {
                    Toast.makeText(getApplicationContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 재입력 확인
                if(!userPassword.equals(userCFPassword)) {
                    Toast.makeText(getApplicationContext(), "비밀번호 재입력을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 아이디 길이제한
                if(userId.length() < 6) {
                    Toast.makeText(getApplicationContext(), "아이디를 6글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 길이제한
                if(userPassword.length() < 6) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 6글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    signUp(userId, userPassword, userName, userPhoneNumber, birthDate, userGender, userAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void signUp(String userId, String userPassword, String userName, String userPhoneNumber, String birthDate, String userGender, String userAddress) throws JSONException {
        String url = "https://safe-guide.herokuapp.com/api/v1/users/join";
        OkHttpClient client = new OkHttpClient();

        // JSON 문자열 생성
        String json = new JSONObject()
                .put("userId", userId)
                .put("password", userPassword)
                .put("userName", userName)
                .put("phoneNumber", userPhoneNumber)
                .put("birth", birthDate)
                .put("gender", userGender)
                .put("address", userAddress)
                .toString();
        Log.d("요청1", json);

        // JSON 형식의 RequestBody 생성
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json
        );

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d("요청2", String.valueOf(request));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignUpActivity.this, "요청이 실패했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 회원가입 성공
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent= new Intent(SignUpActivity.this, LoginActivity.class);
                            intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);    // 액티비티 이동 시 애니메이션 제거.
                            startActivity(intent);
                        }
                    });
                } else {
                    // 회원가입 실패
                    final String errorBody = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 에러 코드 처리
                            handleSignUpError(errorBody);
                        }
                    });
                }
                response.close();
            }
        });
    }

    private void handleSignUpError(String errorBody) {
        try {
            JSONObject jsonObject = new JSONObject(errorBody);
            String errorCode = jsonObject.getString("errorCode");
            String errorMessage = jsonObject.getString("errorMessage");

            // 에러 코드에 따른 처리
            if (errorCode.equals("DUPLICATED_USER_ID")) {
                Toast.makeText(SignUpActivity.this, "중복된 사용자 아이디입니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignUpActivity.this, "회원가입 실패: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.d("에러", errorBody);
            Toast.makeText(SignUpActivity.this, " 서버 응답을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}