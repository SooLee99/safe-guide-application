package com.example.safe_guide.activity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_guide.R;
import com.example.safe_guide.adapter.PlaceAdapter;
import com.example.safe_guide.model.PlaceListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TMapGpsManager.onLocationChangedCallback
public class PlaceSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageButton btnBack;

    private ArrayList<PlaceListModel> placeListModels;
    private PlaceAdapter placeAdapter;
    private RecyclerView rvPlacesList;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onBackPressed() {
        finish();
    }

    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);
        initWidgets();

    }

    private void initWidgets() {
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvPlacesList = findViewById(R.id.rvPlacesList);
        linearLayoutManager = new LinearLayoutManager(this);
        rvPlacesList.setLayoutManager(linearLayoutManager);
        placeListModels = new ArrayList<>();
        placeAdapter = new PlaceAdapter(placeListModels);
        rvPlacesList.setAdapter(placeAdapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = etSearch.getText().toString();

                if(!search.equals("")) {
                    new Thread(() -> {
                        try {
                            searchPlace(search);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    placeAdapter = new PlaceAdapter(placeListModels);
                    rvPlacesList.setAdapter(placeAdapter);
                }
                else {
                    Toast.makeText(PlaceSearchActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void searchPlace(String searchKeyword) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://apis.openapi.sk.com/tmap/pois";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("version", "1")
                .addQueryParameter("searchKeyword", searchKeyword)
                .addQueryParameter("searchType", "all")
                .addQueryParameter("searchtypCd", "A")          // A : 정확도
                .addQueryParameter("reqCoordType", "WGS84GEO")  // WGS84 경위도 좌표계
                .addQueryParameter("resCoordType", "WGS84GEO")  // WGS84 경위도 좌표계
                .addQueryParameter("page", "1")
                .addQueryParameter("count", "20")               // 20개 리스트 정보만 제공.
                .addQueryParameter("multiPoint", "N")           // 검색할 관심 장소가 정문, 후문 등 여러 개의 건물인 경우 -> 모든 결괏값 반환
                .addQueryParameter("poiGroupYn", "N");          // 관심 장소(POI)의 부속 시설물 정보 미반환

        String finalUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("appKey", "e8wHh2tya84M88aReEpXCa5XTQf3xgo01aZG39k5")
                .build();

        Response response = client.newCall(request).execute();

        if(response.isSuccessful()) {
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            parseJsonResponse(String.valueOf(jsonObject));
        }
        else {
            Toast.makeText(PlaceSearchActivity.this, "위치 검색에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    private void parseJsonResponse(String jsonResponse) {
        placeListModels = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject searchPoiInfo = jsonObject.getJSONObject("searchPoiInfo");
            JSONObject pois = searchPoiInfo.getJSONObject("pois");
            JSONArray poiArray = pois.getJSONArray("poi");

            for (int i = 0; i < poiArray.length(); i++) {
                JSONObject poiObject = poiArray.getJSONObject(i);
                String id = poiObject.getString("id");                          // 장소 아이디
                String name = poiObject.getString("name");                      // 장소 이름
                String telNo = poiObject.getString("telNo");                    // 장소 전화번호
                String frontLat = poiObject.getString("frontLat");              // 위도 y 좌표
                String frontLon = poiObject.getString("frontLon");              // 경도 x 좌표
                String upperBizName = poiObject.getString("upperBizName");      // 장소 대분류
                String middleBizName = poiObject.getString("middleBizName");    // 장소 중분류
                String lowerBizName = poiObject.getString("lowerBizName");      // 장소 소분류
                String detailBizName = poiObject.getString("detailBizName");    // 장소 디테일 분류
                String desc = poiObject.getString("desc");                      // 장소 설명
                String fullAddressRoad = poiObject.getJSONObject("newAddressList")    // 상세 주소
                        .getJSONArray("newAddress")
                        .getJSONObject(0)
                        .getString("fullAddressRoad");

                try {
                    float lat = Float.parseFloat(frontLat);
                    float lon = Float.parseFloat(frontLon);
                    int pid = Integer.parseInt(id);

                    PlaceListModel placeData = new PlaceListModel(name, fullAddressRoad, lon, lat, pid);
                    placeListModels.add(placeData);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    placeAdapter = new PlaceAdapter(placeListModels);
                    rvPlacesList.setAdapter(placeAdapter);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            String text = "검색에 실패하였습니다.";
            Toast.makeText(getApplicationContext(), text,Toast.LENGTH_SHORT).show();
        }
    }
}