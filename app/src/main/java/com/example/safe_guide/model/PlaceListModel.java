package com.example.safe_guide.model;

public class PlaceListModel {
    private String placeName;
    private String placeAddress;
    private float endX;
    private float endY;
    private int endPoiId;

    public PlaceListModel(String tvPlaceName, String tvAddress, float endX, float endY, int endPoiId) {
        this.placeName = tvPlaceName;
        this.placeAddress = tvAddress;
        this.endX = endX;
        this.endY = endY;
        this.endPoiId = endPoiId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public float getEndY() {
        return endY;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }

    public int getEndPoiId() {
        return endPoiId;
    }

    public void setEndPoiId(int endPoiId) {
        this.endPoiId = endPoiId;
    }
}
