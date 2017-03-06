package com.myeta;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {
  private double latitude;
  private double longitude;

  public Location(){
  }

  public Location(double latitude, double longitude){
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
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

  protected Location(Parcel in) {
    this.latitude = in.readDouble();
    this.longitude = in.readDouble();
  }

  public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
    @Override
    public Location createFromParcel(Parcel source) {
      return new Location(source);
    }

    @Override
    public Location[] newArray(int size) {
      return new Location[size];
    }
  };
}
