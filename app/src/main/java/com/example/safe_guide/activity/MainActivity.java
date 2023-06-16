package com.example.safe_guide.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.safe_guide.Fragement.BoardFragment;
import com.example.safe_guide.Fragement.HomeFragment;
import com.example.safe_guide.Fragement.MyInfoFragment;
import com.example.safe_guide.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // 바텀 네비게이션
    BottomNavigationView bottomNavigationView;

    // 프래그먼트 변수
    Fragment homeFragment;
    Fragment boardFragment;
    Fragment myInfoFragment;

    private long backBtnTime = 0;

    public static final int REQUEST_CODE = 100;

    // SharedPreference
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String rememberID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 프래그먼트 생성
        homeFragment = new HomeFragment();
        boardFragment = new BoardFragment();
        myInfoFragment = new MyInfoFragment();

        // 바텀 네비게이션
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 초기 프래그먼트 설정
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, homeFragment).commitAllowingStateLoss();

        // 리스너 등록
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, homeFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.action_board:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, boardFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.action_myInfo:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, myInfoFragment).commitAllowingStateLoss();
                        return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
