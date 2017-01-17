package com.example.hao.myeta;

public class Session {
  private String password;
  private String user;
  private Location location;

  public Session(){};

  public void setUser(String user){
    this.user = user;
  }

  public void setLocation(Location location){
    this.location = location;
  }

  public String getUser(){
    return user;
  }

  public Location getLocation(){
    return location;
  }
}
