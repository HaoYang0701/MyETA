package com.example.hao.myeta;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class LocationUtils {
  private static final String EXCEPTIONLOG = "EXCEPTIONLOG";
  Context context;
  Timer locationTimer;
  LocationManager locationManager;
  LocationResult locationResult;
  boolean isGpsEnabled = false;
  boolean isNetworkEnabled = false;

  public boolean getLocation(Context context, LocationResult result) {
    //I use LocationResult callback class to pass location value from MyLocation to user code.
    this.context = context;
    locationResult = result;

    if (locationManager == null) {
      locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    //exceptions will be thrown if provider is not permitted.
    try {
      isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception ex) {
      Log.d(EXCEPTIONLOG, ex.getMessage());
    }
    try {
      isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception ex) {
      Log.d(EXCEPTIONLOG, ex.getMessage());
    }

    //don't start listeners if no provider is enabled
    if (!isGpsEnabled && !isNetworkEnabled) {
      return false;
    }

    if (ActivityCompat.checkSelfPermission(context,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    }

    if (isGpsEnabled) {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
          locationListenerGps);
    }

    if (isNetworkEnabled) {
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
          locationListenerNetwork);
    }

    locationTimer = new Timer();
    locationTimer.schedule(new GetLastLocation(), 20000);
    return true;
  }

  LocationListener locationListenerGps = new LocationListener() {
    public void onLocationChanged(Location location) {
      locationTimer.cancel();
      locationResult.gotLocation(location);
      if (ActivityCompat.checkSelfPermission(context,
          android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      locationManager.removeUpdates(this);
      locationManager.removeUpdates(locationListenerNetwork);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  LocationListener locationListenerNetwork = new LocationListener() {
    public void onLocationChanged(Location location) {
      locationTimer.cancel();
      locationResult.gotLocation(location);
      if (ActivityCompat.checkSelfPermission(context,
          android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      locationManager.removeUpdates(this);
      locationManager.removeUpdates(locationListenerGps);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  class GetLastLocation extends TimerTask {
    @Override
    public void run() {
      if (ActivityCompat.checkSelfPermission(context,
          android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      locationManager.removeUpdates(locationListenerGps);
      locationManager.removeUpdates(locationListenerNetwork);

      Location networkLocation = null;
      Location gpsLocation = null;

      if (isGpsEnabled) {
        gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      }
      if (isNetworkEnabled) {
        networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      }
      //if there are both values use the latest one
      if (gpsLocation != null && networkLocation != null) {
        if (gpsLocation.getTime() > networkLocation.getTime()) {
          locationResult.gotLocation(gpsLocation);
        } else {
          locationResult.gotLocation(networkLocation);
        }
        return;
      }

      if (gpsLocation != null) {
        locationResult.gotLocation(gpsLocation);
        return;
      }
      if (networkLocation != null) {
        locationResult.gotLocation(networkLocation);
        return;
      }
      locationResult.gotLocation(null);
    }
  }

  public abstract static class LocationResult {
    public abstract void gotLocation(Location location);
  }
}