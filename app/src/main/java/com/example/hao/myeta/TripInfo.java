package com.example.hao.myeta;

import java.util.ArrayList;

public class TripInfo {
  ArrayList<CustomLatLng> customDirectionList;
  String distance;
  String duration;
  String startAddress;

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
}
