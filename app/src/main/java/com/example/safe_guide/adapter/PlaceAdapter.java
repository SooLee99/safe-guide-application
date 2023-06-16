package com.example.safe_guide.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_guide.R;
import com.example.safe_guide.activity.RouteGuideActivity;
import com.example.safe_guide.model.PlaceListModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.CustomViewHolder> {
    private ArrayList<PlaceListModel> placeListModels;

    public PlaceAdapter(ArrayList<PlaceListModel> locationListModels) {
        this.placeListModels = locationListModels;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_list, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.tvPlaceName.setText(placeListModels.get(position).getPlaceName());
        holder.tvAddress.setText(placeListModels.get(position).getPlaceAddress());
        holder.tvLatitude.setText(String.valueOf(placeListModels.get(position).getEndX()));
        holder.tvLongitude.setText(String.valueOf(placeListModels.get(position).getEndY()));

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startPlaceName;
                String endPlaceName = holder.tvPlaceName.getText().toString();
                String getEndY = holder.tvLongitude.getText().toString();
                String getEndX = holder.tvLatitude.getText().toString();
                String endPoiId = holder.tvEndPoiId.getText().toString();

                String latitude = "0";
                String longitude = "0";

                Toast.makeText(v.getContext(), endPlaceName + " 경로안내를 시작하겠습니다.", Toast.LENGTH_SHORT).show();

                LocationManager locationManager = (LocationManager) v.getContext().getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 위치 권한이 없는 경우 권한 요청
                    ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());
                    startPlaceName = getCurrentLocation(v, Double.parseDouble(latitude), Double.parseDouble(longitude));
                } else {
                    // 인하공업전문대학 학교위치
                    latitude = "37.4480158";
                    longitude = "126.6575041";
                    startPlaceName = "인하공업전문대학";
                }

                Intent intent = new Intent(v.getContext(), RouteGuideActivity.class);
                intent.putExtra("startPlaceName", startPlaceName);
                intent.putExtra("endPlaceName", endPlaceName);
                intent.putExtra("state", 1);
                intent.putExtra("endX", getEndX);
                intent.putExtra("endY", getEndY);
                intent.putExtra("endPoiId", endPoiId);

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // 액티비티 이동 시 애니메이션 제거.
                v.getContext().startActivity(intent);
                ((Activity) v.getContext()).finish();
            }

            private String getCurrentLocation(View v, double latitude, double longitude) {
                Geocoder geocoder = new Geocoder(v.getContext(), Locale.getDefault());
                String detailedAddress = "인하공업전문대학";
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        detailedAddress = address.getAddressLine(0); // 상세 주소
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return detailedAddress;
            }

        });
    }

    @Override
    public int getItemCount() {
        return (null != placeListModels ? placeListModels.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvPlaceName;
        protected TextView tvAddress;
        protected TextView tvLatitude;
        protected TextView tvLongitude;
        protected TextView tvEndPoiId;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            this.tvAddress = itemView.findViewById(R.id.tvAddress);
            this.tvLatitude = itemView.findViewById(R.id.tvLatitude);
            this.tvLongitude = itemView.findViewById(R.id.tvLongitude);
            this.tvEndPoiId = itemView.findViewById(R.id.tvEndPoiId);
        }
    }
}
