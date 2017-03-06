package com.myeta;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

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
      PermissionPopup(context);
    }
    else {
      CharSequence text = context.getString(R.string.location_already_enabled);
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(context, text, duration);
      toast.show();
    }
  }

  public static void PermissionPopup(Context context){
    ActivityCompat.requestPermissions((Activity)context,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        MY_PERMISSIONS_REQUEST_FINE_LOCATIONS);
  }
}
