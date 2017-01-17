package com.example.hao.myeta;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {
  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS = 1;

  public PermissionUtil() {
  }

  public static boolean isLocationPermissionsOn(Context context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      return true;
    } else {
      return false;
    }
  }

  public static void checkLocationPermissions(Context context){
    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions((Activity)context,
          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          MY_PERMISSIONS_REQUEST_FINE_LOCATIONS);
    }
  }
}
