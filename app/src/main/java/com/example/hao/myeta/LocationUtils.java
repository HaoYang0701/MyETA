package com.myeta;

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
  //Taken from
  //http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-on-a
  private static final String EXCEPTIONLOG = "EXCEPTIONLOG";
  private Context context;
  private Timer locationTimer;
  private LocationManager locationManager;
  private LocationResult locationResult;
  private boolean isGpsEnabled = false;
  private boolean isNetworkEnabled = false;

  public boolean getLocation(Context context, LocationResult result) {
    this.context = context;
    locationResult = result;

    if (locationManager == null) {
      locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

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