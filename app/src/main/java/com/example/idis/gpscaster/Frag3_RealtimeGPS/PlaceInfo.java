package com.example.idis.gpscaster.Frag3_RealtimeGPS;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by IDIS on 2016-11-30.
 */

public class PlaceInfo implements Serializable{

    /*public PlaceInfo(Parcel in){
        readFromParcel(in);
    }*/
    public PlaceInfo(){

    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpen_now() {
        return open_now;
    }

    public void setOpen_now(String open_now) {
        this.open_now = open_now;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public int getPrice_level() {
        return price_level;
    }

    public void setPrice_level(int price_level) {
        this.price_level = price_level;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    Double lng;//longitude
    Double lat;//latitude
    String name;//name
    String open_now;
    String place_id;
    Double rating;
    String types[];
    String vicinity;
    int price_level; //Exist or not
    String url;
  /*  @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lng);
        dest.writeDouble(lat);
        dest.writeString(name);
        dest.writeString(open_now);
        dest.writeString(place_id);
        dest.writeDouble(rating);
        dest.writeStringArray(types);
        dest.writeString(vicinity);
        dest.writeInt(price_level);
        dest.writeString(url);
    }

    public void readFromParcel(Parcel in){
        lng = in.readDouble();
        lat = in.readDouble();
        name = in.readString();
        open_now = in.readString();
        place_id = in.readString();
        rating = in.readDouble();
        //in.readStringArray(types);
        vicinity = in.readString();
        price_level = in.readInt();
        url = in.readString();
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<PlaceInfo> CREATOR = new Parcelable.Creator(){

        @Override
        public Object createFromParcel(Parcel source) {
            return new PlaceInfo(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new PlaceInfo[size];
        }
    };
*/
}
