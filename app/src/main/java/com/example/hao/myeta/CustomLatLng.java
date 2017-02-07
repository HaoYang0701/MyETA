package com.example.hao.myeta;

public class CustomLatLng {
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
}
