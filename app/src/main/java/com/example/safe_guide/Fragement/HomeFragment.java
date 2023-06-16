package com.example.safe_guide.Fragement;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.safe_guide.R;
import com.example.safe_guide.activity.MapActivity;
import com.example.safe_guide.activity.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HomeFragment extends Fragment {
    private View view;

    // 날씨
    public static String weather121313 = "현재 날씨는 맑은 상태입니다.";
    private double longitude = 37.4481;    // 인하공전 경도
    private double latitude = 126.6585;    // 인하공전 위도
    private static String weatherResult = "";  // 날씨 정보
    private ImageView ivWeather;
    private TextView tvTemperatures, tvWeather;
    private String baseDate;            // 조회하고 싶은 날짜
    private String baseTime;            // 조회하고 싶은 시간
    private String weather;             // 날씨 결과
    private String tmperature;

    // gif
    private ImageView ivRun;

    private Button btnMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        initWidgets();
        weatherSetting();
        return view;
    }

    private void initWidgets() {
        // Weather
        ivWeather = view.findViewById(R.id.ivWeather);
        tvTemperatures = view.findViewById(R.id.tvTemperatures);
        tvWeather = view.findViewById(R.id.tvWeather);
        Glide.with(this).load(R.drawable.sun).into(ivWeather);

        // GIF
        ivRun = view.findViewById(R.id.ivRun);
        Glide.with(this).load(R.drawable.run2).into(ivRun);
        ivRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HomeFragment.class);
                startActivity(intent);
            }
        });

        btnMap = view.findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(), MapActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    private void weatherSetting() {
        new Thread(() -> {
            try {
                weatherResult = lookUpWeather(longitude, latitude);
                Log.d("날씨정보",weatherResult);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        // 날씨
        int beginIndex = weatherResult.lastIndexOf(",") + 1;
        int endIndex = weatherResult.length();
        // 혹시 모를 에러 처리하기!!
        if (beginIndex != 0) {
            Log.d("정보", String.valueOf(beginIndex));
            String temperatures = weatherResult.substring(beginIndex, endIndex);    // 기온
            String weather = weatherResult.substring(0, (beginIndex - 1));    // 날씨
            tvTemperatures.setText(temperatures);
            tvWeather.setText(weather);
            if(!weather121313.equals(weather)) {
                // 날씨에 따라 이미지 변경
                if (weather.equals("현재 날씨는 맑은 상태입니다.")) {
                    Glide.with(ivWeather).load(R.drawable.sun).into(ivWeather);
                    weather121313 = "현재 날씨는 맑은 상태입니다.";
                    ivWeather.setImageResource(R.drawable.sun);
                } else if (weather.equals("현재 날씨는 비가 오는 상태입니다.")) {
                    Glide.with(ivWeather).load(R.drawable.rain).into(ivWeather);
                    ivWeather.setImageResource(R.drawable.rain);
                    weather121313 = "현재 날씨는 비가 오는 상태입니다.";
                } else if (weather.equals("현재 날씨는 구름이 많은 상태입니다.")) {
                    Glide.with(ivWeather).load(R.drawable.cloudy).into(ivWeather);
                    ivWeather.setImageResource(R.drawable.cloudy);
                    weather121313 = "현재 날씨는 구름이 많은 상태입니다.";
                } else if (weather.equals("현재 날씨는 흐린 상태입니다.")) {
                    Glide.with(ivWeather).load(R.drawable.clouds).into(ivWeather);
                    ivWeather.setImageResource(R.drawable.clouds);
                    weather121313 = "현재 날씨는 흐린 상태입니다.";
                }
            }
        }
    }

    private String lookUpWeather(double longitude, double latitude) throws JSONException, IOException {
        int ix = (int) longitude;
        int iy = (int) latitude;
        String nx = String.valueOf(ix);
        String ny = String.valueOf(iy);
        Log.i("날씨: 위도!!", nx);
        Log.i("날씨: 경도!!", ny);

        // 현재 날짜 구하기 (시스템 시계, 시스템 타임존)
        LocalDate date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDate.now();
        }
        LocalTime time = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = LocalTime.now();
        }
        baseDate = String.valueOf(date).replaceAll("-", "");
        int correctionDate = Integer.parseInt(baseDate) - 1;     // 날씨 API : 매 시각 45분 이후 호출 // 오전 12시인 경우 사용

        // 시간(30분 단위로 맞추기)
        DateTimeFormatter formatter1 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter1 = DateTimeFormatter.ofPattern("HHmm");
        }
        DateTimeFormatter formatter2 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter2 = DateTimeFormatter.ofPattern("HH");
        }

        int itime1 = 0; // 실제 시간
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            itime1 = Integer.parseInt(time.format(formatter1));
        }
        int itime2 = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            itime2 = Integer.parseInt(time.format(formatter2)) - 1;
        }

        //  /*06시30분 발표(30분 단위)*/
        if (itime2 <= 7) {
            itime2 = 23;
            baseDate = String.valueOf(correctionDate);
            baseTime = "2100";
        } else {
            // api가 30분 단위로 업데이트
            if (itime1 % 100 >= 30) baseTime = itime2 + "30";
            else baseTime = itime2 + "00";
        }
        // 오전에는 시간이 3자리로 나옴...
        if (baseTime.length() == 3) {
            baseTime = "0" + baseTime;
        }

        String weatherResult = "현재 날씨를 확인할 수가 없어요.";

        Log.i("날씨: 입력일자!!", baseDate);
        Log.i("날씨: 입력시간!!", baseTime);

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=eWD3WU%2B78w6UiyRQFINsKmuNGrDvg3JnKDnefyrBx1jEAGOxNI%2FuFwXB5W7LgsBunL2cQz6OqBLIuJQWDES1SQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); /*‘21년 6월 28일 발표*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); /*06시30분 발표(30분 단위)*/
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); /*예보지점 X 좌표값*/
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); /*예보지점 Y 좌표값*/

        /*
         * GET방식으로 전송해서 파라미터 받아오기
         */
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();

        Log.d("정보", result);
        //=======이 밑에 부터는 json에서 데이터 파싱해 오는 부분이다=====//

        // response 키를 가지고 데이터를 파싱
        JSONObject jsonObj_1 = new JSONObject(result);
        String response = jsonObj_1.getString("response");

        // response 로 부터 body 찾기
        JSONObject jsonObj_2 = new JSONObject(response);
        String body = jsonObj_2.getString("body");

        // body 로 부터 items 찾기
        JSONObject jsonObj_3 = new JSONObject(body);
        String items = jsonObj_3.getString("items");
        Log.i("ITEMS", items);

        // items로 부터 itemlist 를 받기
        JSONObject jsonObj_4 = new JSONObject(items);
        JSONArray jsonArray = jsonObj_4.getJSONArray("item");

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObj_4 = jsonArray.getJSONObject(i);
            String fcstValue = jsonObj_4.getString("fcstValue");
            String category = jsonObj_4.getString("category");

            if (category.equals("SKY")) {
                weather = "현재 날씨는 ";
                if (fcstValue.equals("1")) {
                    weather += "맑은 상태입니다.";
                } else if (fcstValue.equals("2")) {
                    weather += "비가 오는 상태입니다.";
                } else if (fcstValue.equals("3")) {
                    weather += "구름이 많은 상태입니다.";
                } else if (fcstValue.equals("4")) {
                    weather += "흐린 상태입니다.";
                }
            }

            if (category.equals("T3H") || category.equals("T1H")) {
                if(fcstValue.equals("-99")) {
                    fcstValue="10";
                }
                tmperature = fcstValue + " ℃";
            }
            weatherResult = weather + "," + tmperature;
        }
        Log.i("리턴!!", weatherResult);
        return weatherResult;
    }
}
