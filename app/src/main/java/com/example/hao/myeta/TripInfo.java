package com.example.hao.myeta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class TripInfo implements Parcelable {
  ArrayList<CustomLatLng> customDirectionList;
  String distance;
  String duration;String startAddress;

  public TripInfo(){
  }

  public String getStartAddress() {
    return startAddress;
  }

  public void setStartAddress(String startAddress) {
    this.startAddress = startAddress;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getDistance() {
    return distance;
  }

  public void setDistance(String distance) {
    this.distance = distance;
  }

  public ArrayList<CustomLatLng> getCustomDirectionList() {
    return customDirectionList;
  }

  public void setCustomDirectionList(ArrayList<CustomLatLng> customDirectionList) {
    this.customDirectionList = customDirectionList;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeList(this.customDirectionList);
    dest.writeString(this.distance);
    dest.writeString(this.duration);
    dest.writeString(this.startAddress);
  }

  protected TripInfo(Parcel in) {
    this.customDirectionList = new ArrayList<CustomLatLng>();
    in.readList(this.customDirectionList, CustomLatLng.class.getClassLoader());
    this.distance = in.readString();
    this.duration = in.readString();
    this.startAddress = in.readString();
  }

  public static final Parcelable.Creator<TripInfo> CREATOR = new Parcelable.Creator<TripInfo>() {
    @Override
    public TripInfo createFromParcel(Parcel source) {
      return new TripInfo(source);
    }

    @Override
    public TripInfo[] newArray(int size) {
      return new TripInfo[size];
    }
  };
}
