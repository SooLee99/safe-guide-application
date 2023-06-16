package com.example.safe_guide.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safe_guide.R;
import com.example.safe_guide.object_detector.ObjectDetectorClass;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RouteGuideActivity extends Activity implements TMapGpsManager.onLocationChangedCallback,
        CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG="MainActivity";
    
    // Open CV
    private Mat mRgba;
    private CameraBridgeViewBase mOpenCvCameraView;
    private ObjectDetectorClass objectDetectorClass;

    // T Map
    private String API_Key = "API 키 작성하기";
    private TMapPolyLine polyline;

    @SuppressLint("WrongViewCast")
    private TMapView tMapView;

    private TMapGpsManager tMapGPS = null;
    private ArrayList<TMapPoint> alTMapPoint = new ArrayList<TMapPoint>();
    private LinearLayout linearLayoutTmap;
    private LocationManager locationManager;

    private String startPlaceName;
    private String endPlaceName;
    private double endX;
    private double endY;
    private int markerCount = -1;
    private String description;
    private TextView tvRouteGuide;

    // 마커와의 거리 임계값 (단위: 미터)
    private double markerProximityThreshold = 0.01;
    
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public RouteGuideActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWidgets();
        tMapSetting();
        new Thread(() -> {
            try {
                receiveIntent();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void receiveIntent() throws JSONException, IOException {
        Intent intent = getIntent();
        if (intent != null) {
            startPlaceName = intent.getStringExtra("startPlaceName");
            endPlaceName = intent.getStringExtra("endPlaceName");
            endX = Float.parseFloat(intent.getStringExtra("endX"));
            endY = Float.parseFloat(intent.getStringExtra("endY"));

        } else {
            runOnUiThread(() -> {
                Toast.makeText(RouteGuideActivity.this, "목적지 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(RouteGuideActivity.this, "현재 위치 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        Log.d("위치1", Double.toString(location.getLongitude()) + "  ,  " + Double.toString(location.getLatitude()));
        alTMapPoint.add(new TMapPoint(location.getLatitude(), location.getLongitude()));
    }

    private void tMapSetting() {
        tMapView = new TMapView(RouteGuideActivity.this);
        tMapView.setSKTMapApiKey(API_Key);

        // Initial Setting
        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // 현재 방향으로 지도를 보기 위해 추가
        tMapView.setCompassMode(true);
        tMapView.setTrackingMode(true);

        // T Map View Using Linear Layout
        linearLayoutTmap.addView(tMapView);

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // Initial Setting
        tMapGPS.setMinTime(1);
        tMapGPS.setMinDistance(1);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);

        // 화면중심을 단말의 현재위치로 이동
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        tMapGPS.OpenGps();

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                onLocationChange(location);
            }
        };
    }

    @Override
    public void onLocationChange(Location location) {
        // 현재 방향으로 지도를 보기 위해 추가
        tMapView.setCompassMode(true);
        tMapView.setTrackingMode(true);

        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        alTMapPoint.add(new TMapPoint(location.getLatitude(), location.getLongitude()));

        Log.d("마커 개수1", String.valueOf(markerCount));

        if (markerCount == 0) {
            // 마커가 없는 경우
            checkDestinationReached(location.getLatitude(), location.getLongitude());
        } else if (markerCount == -1) {

        } else {
            // 마커가 있는 경우
            checkMarkerProximity(location.getLatitude(), location.getLongitude());
        }

        try {
            // TODO: 경로 라인 초기화
            tMapView.removeTMapPolyLine(polyline.getID());
            sendLocationInformation(startPlaceName, endPlaceName, location.getLongitude(), location.getLatitude(), endX, endY);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendLocationInformation(String startPlaceName, String endPlaceName, double startX, double startY, double endX, double endY) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        int angle = 20;
        int speed = 30;
        String reqCoordType = "WGS84GEO";
        String startName = URLEncoder.encode(startPlaceName, "UTF-8");
        String endName = URLEncoder.encode(endPlaceName, "UTF-8");
        String searchOption = "30"; // 최단거리+계단제외
        String resCoordType = "WGS84GEO";
        String sort = "index";

        RequestBody body = RequestBody.create(mediaType,
                String.format("{\"startX\":%f,\"startY\":%f,\"angle\":%d,\"speed\":%d," +
                                "\"endX\":%f,\"endY\":%f,\"reqCoordType\":\"%s\",\"startName\":\"%s\"," +
                                "\"endName\":\"%s\",\"searchOption\":\"%s\",\"resCoordType\":\"%s\",\"sort\":\"%s\"}",
                        startX, startY, angle, speed,
                        endX, endY, reqCoordType, startName,
                        endName, searchOption, resCoordType, sort));

        Request request = new Request.Builder()
                .url("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("appKey", "e8wHh2tya84M88aReEpXCa5XTQf3xgo01aZG39k5")
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("Json 결과", responseBody);
                    JSONObject jsonObject = new JSONObject(responseBody);

                    runOnUiThread(() -> parseAndDrawPath(String.valueOf(jsonObject))); // UI 스레드에서 경로 그리기 메소드 호출
                } else {
                    runOnUiThread(() -> {
                        Log.d("Json 결과", "실패");
                        Toast.makeText(RouteGuideActivity.this, "위치 검색에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseAndDrawPath(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray features = json.getJSONArray("features");

            boolean isFirstDescription = true; // 첫 번째 description 메시지인지 확인하기 위한 변수
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                String type = geometry.getString("type");

                if (type.equals("Point")) {
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);

                    TMapPoint tMapPoint = new TMapPoint(lat, lon);
                    TMapMarkerItem marker = new TMapMarkerItem();
                    marker.setTMapPoint(tMapPoint);
                    tMapView.addMarkerItem("marker_" + i, marker);

                    if (feature.has("properties")) {
                        if (isFirstDescription) {
                            JSONObject properties = feature.getJSONObject("properties");
                            description = properties.getString("description");
                            showToast(description);
                            isFirstDescription = false; // 다음에는 출력하지 않도록 변수 변경
                        }
                    }
                    markerCount++;
                } else if (type.equals("LineString")) {
                    // 라인(LineString)인 경우
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    ArrayList<TMapPoint> points = new ArrayList<>();

                    for (int j = 0; j < coordinates.length(); j++) {
                        JSONArray coord = coordinates.getJSONArray(j);
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);

                        TMapPoint tMapPoint = new TMapPoint(lat, lon);
                        points.add(tMapPoint);
                    }

                    // 경로 라인 초기화
                    tMapView.removeTMapPolyLine(polyline.getID());

                    // 경로 라인 추가
                    polyline.setLineColor(Color.YELLOW);
                    polyline.setLineWidth(10);
                    polyline.setOutLineWidth(20);
                    for (TMapPoint point : points) {
                        polyline.addLinePoint(point);
                    }
                    tMapView.addTMapPolyLine("line_" + i, polyline);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkMarkerProximity(double latitude, double longitude) {
        // 현재 위치와 마커들과의 거리 확인
        for (TMapPoint markerPoint : alTMapPoint) {
            float[] results = new float[1];
            Location.distanceBetween(latitude, longitude, markerPoint.getLatitude(), markerPoint.getLongitude(), results);
            float distance = results[0];

            Log.d("마커와의 거리 확인", "현재 위치와 마커 사이의 거리: " + distance);

            if (distance <= markerProximityThreshold) {
                // 마커에 대한 description 메시지 출력
                TMapMarkerItem marker = tMapView.getMarkerItemFromID(markerPoint.toString());
                if (marker != null) {
                    showToast(marker.getCalloutTitle());
                }
            }
        }
    }

    private void checkDestinationReached(double latitude, double longitude) {
        // 현재 위치와 마커들과의 거리 확인
        for (TMapPoint markerPoint : alTMapPoint) {
            float[] results = new float[1];
            Location.distanceBetween(latitude, longitude, markerPoint.getLatitude(), markerPoint.getLongitude(), results);
            float distance = results[0];

            Log.d("마커와의 거리 확인", "현재 위치와 마커 사이의 거리: " + distance);

            if (distance <= markerProximityThreshold) {
                // 마커에 대한 description 메시지 출력
                TMapMarkerItem marker = tMapView.getMarkerItemFromID(markerPoint.toString());
                if (marker != null) {
                    showToast(marker.getCalloutTitle());
                }
            }
        }
    }

    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RouteGuideActivity.this, "전방에 자동차를 감지했습니다.", Toast.LENGTH_SHORT).show();
                tvRouteGuide.setText(message);
                return;
            }
        });
    }

    private void initWidgets() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(RouteGuideActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(RouteGuideActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_route_guide);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        try{
            // input size is 300 for this model
            objectDetectorClass = new ObjectDetectorClass(getAssets(),"ssd_mobilenet.tflite","labelmap.txt",300);
            Log.d("MainActivity","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("MainActivity","Getting some error");
            e.printStackTrace();
        }

        // Add this after initializing mOpenCvCameraView
        mOpenCvCameraView.post(new Runnable() {
            @Override
            public void run() {
                // Get the layout parameters of the CameraView
                ViewGroup.LayoutParams params = mOpenCvCameraView.getLayoutParams();

                // Set the size of the CameraView to match the frame size
                params.width = mOpenCvCameraView.getWidth(); // This needs to be the width of the camera frame
                params.height = mOpenCvCameraView.getHeight(); // This needs to be the height of the camera frame

                // Apply the changed layout parameters to the CameraView
                mOpenCvCameraView.setLayoutParams(params);
            }
        });

        linearLayoutTmap = findViewById(R.id.linearLayoutTmap);
        tvRouteGuide = findViewById(R.id.tvRouteGuide);
        polyline = new TMapPolyLine();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
        mOpenCvCameraView.setMaxFrameSize(650, 490); // 이 예제에서는 480x360으로 조절합니다.
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped(){
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        // recognizeImage 함수를 호출하기 전에 이미지의 크기를 조정합니다.
        Mat resizeBefore = new Mat();
        Size szBefore = new Size(640, 480); // 이 크기는 recognizeImage 함수가 기대하는 크기로 설정해야 합니다.
        Imgproc.resize(mRgba, resizeBefore, szBefore);

        // recognizeImage 함수를 호출합니다.
        Mat out = objectDetectorClass.recognizeImage(resizeBefore);

        // recognizeImage 함수를 호출한 후에 이미지의 크기를 원래대로 조정합니다.
        Mat resizeAfter = new Mat();
        Core.rotate(out, resizeAfter, Core.ROTATE_90_CLOCKWISE);
        Size szAfter = new Size(mRgba.cols(), mRgba.rows()); // 이 크기는 mRgba의 원래 크기로 설정해야 합니다.
        Imgproc.resize(resizeAfter, out, szAfter);

        return out;
    }
}