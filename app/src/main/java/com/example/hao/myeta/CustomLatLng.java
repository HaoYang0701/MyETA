package com.myeta;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomLatLng implements Parcelable {
  private double latitude;
  private double longitude;

  public CustomLatLng(){
  };

  public CustomLatLng(double Latitude, double Longitude){
    this.latitude = Latitude;
    this.longitude = Longitude;
  }
  public void setLat(double latitude){
    this.latitude = latitude;
  }

  public void setLong(double longitude){
    this.longitude = longitude;
  }

  public double getLong(){
    return longitude;
  }

  public double getLat(){
    return latitude;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeDouble(this.latitude);
    dest.writeDouble(this.longitude);
  }

  protected CustomLatLng(Parcel in) {
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
  }

  public static final Parcelable.Creator<CustomLatLng> CREATOR = new Parcelable.Creator<CustomLatLng>() {
    @Override
    public CustomLatLng createFromParcel(Parcel source) {
      return new CustomLatLng(source);
    }

    @Override
    public CustomLatLng[] newArray(int size) {
      return new CustomLatLng[size];
    }
  };
}
