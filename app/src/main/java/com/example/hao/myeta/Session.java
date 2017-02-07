package com.example.hao.myeta;

import com.akexorcist.googledirection.model.Step;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Session {
  private String password;
  private String user;
  private Location location;
  private ArrayList<CustomLatLng> stepArrayList;

  public Session(){};

  public void setUser(String user){
    this.user = user;
  }

  public void setLocation(Location location){
    this.location = location;
  }

  public void setStepArrayList (ArrayList<CustomLatLng> steps){
    this.stepArrayList = steps;
  }

  public String getUser(){
    return user;
  }

  public Location getLocation(){
    return location;
  }

  public ArrayList<CustomLatLng> getStepArrayList(){
    return stepArrayList;
  }
}
