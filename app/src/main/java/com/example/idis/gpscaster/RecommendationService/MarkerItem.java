package com.example.idis.gpscaster.RecommendationService;

/**
 * Created by IDIS on 2016-12-02.
 */

public class MarkerItem {


    double lat;
    double lng;
    //int price;

    public MarkerItem(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
       // this.price = price;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lon) {
        this.lng = lon;
    }


}