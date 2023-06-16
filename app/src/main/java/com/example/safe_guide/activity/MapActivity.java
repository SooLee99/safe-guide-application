package com.example.safe_guide.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.safe_guide.R;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    // T Map
    private String API_Key = "l7xxb927c2c48f4b402192ef7872a0e0754a";
    private TMapView tMapView = null;
    private TMapGpsManager tMapGPS = null;
    private ArrayList<TMapPoint> alTMapPoint = new ArrayList<TMapPoint>();
    private LinearLayout linearLayoutTmap;
    // 상단바
    private LinearLayout layoutBtnSearch;
    private ImageButton btnBack;

    @Override
    public void onBackPressed() {
        finish();
    }

    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initWidgets();
        tmapSetting();

    }

    private void initWidgets() {
        btnBack = findViewById(R.id.btnBack);
        linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        layoutBtnSearch = findViewById(R.id.layoutBtnSearch);
        layoutBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, PlaceSearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void tmapSetting() {
        // T Map
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(API_Key);

        // Initial Setting
        tMapView.setZoomLevel(16);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // T Map View Using Linear Layout
        linearLayoutTmap.addView(tMapView);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(10);
        tMapGPS.setMinDistance(1);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);

        // 화면중심을 단말의 현재위치로 이동
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);

        tMapGPS.OpenGps();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapActivity.this, "현재 위치 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        alTMapPoint.add( new TMapPoint(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        alTMapPoint.add( new TMapPoint(location.getLatitude(), location.getLongitude()));
    }
}