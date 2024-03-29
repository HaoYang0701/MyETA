package com.myeta;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsUtil {

  public static void correctZoom(GoogleMap googleMap, List<Marker> listOfLMarkers) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Marker marker : listOfLMarkers) {
      builder.include(marker.getPosition());
    }
    LatLngBounds bounds = builder.build();
    int padding = 0;
    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    googleMap.moveCamera(cu);
    googleMap.moveCamera(CameraUpdateFactory.zoomOut());
  }

  public static void createMarkers(GoogleMap googleMap, ArrayList<Marker> listOfMarkers,
                                  Session savedSession, boolean iscurrentUser) {
    final LatLng currentLocation = new LatLng(savedSession.getLocation().getLatitude(),
        savedSession.getLocation().getLongitude());
    Marker newMarker;
    if (iscurrentUser) {
      newMarker = googleMap.addMarker(new MarkerOptions()
          .position(currentLocation)
          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Current Location"));
      newMarker.showInfoWindow();
    } else {
      newMarker = googleMap.addMarker(new MarkerOptions().position(currentLocation));
    }
    listOfMarkers.add(newMarker);
  }
}
