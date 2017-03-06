package com.myeta;

import android.os.Parcel;
import android.os.Parcelable;

public class Session implements Parcelable {
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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.user);
    dest.writeParcelable(this.tripInfo, flags);
    dest.writeParcelable(this.location, flags);
  }

  protected Session(Parcel in) {
    this.user = in.readString();
    this.tripInfo = in.readParcelable(TripInfo.class.getClassLoader());
    this.location = in.readParcelable(Location.class.getClassLoader());
  }

  public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
    @Override
    public Session createFromParcel(Parcel source) {
      return new Session(source);
    }

    @Override
    public Session[] newArray(int size) {
      return new Session[size];
    }
  };
}
