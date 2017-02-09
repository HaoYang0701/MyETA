package com.example.hao.myeta;

import com.akexorcist.googledirection.model.Step;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Session {
  private String user;
  private Location location;
  private TripInfo tripInfo;

  public Session(){};

  public void setUser(String user){
    this.user = user;
  }

  public void setLocation(Location location){
    this.location = location;
  }

  public void setTripInfo (TripInfo tripInfo){
    this.tripInfo = tripInfo;
  }

  public String getUser(){
    return user;
  }

  public Location getLocation(){
    return location;
  }

  public TripInfo getTripInfo(){
    return tripInfo;
  }
}
