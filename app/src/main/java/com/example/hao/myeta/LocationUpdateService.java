package com.example.hao.myeta;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.hao.myeta.MainActivity.DESTINATION_LATITUDE;
import static com.example.hao.myeta.MainActivity.DESTINATION_LONGITUDE;
import static com.example.hao.myeta.MainActivity.USERNAME;

public class LocationUpdateService extends Service {
  private volatile HandlerThread mHandlerThread;
  private ServiceHandler mServiceHandler;
  private SharedPreferences prefs;
  private DatabaseReference mfirebaseDatabase;

  private final class ServiceHandler extends Handler {
    public ServiceHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message message) {
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    prefs = this.getSharedPreferences(
        "com.example.hao.myeta", Context.MODE_PRIVATE);
    mHandlerThread = new HandlerThread("MyCustomService.HandlerThread");
    mHandlerThread.start();
    mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Bundle extras = intent.getExtras();
    final String recievedSessionString = extras.getString("IntentSessionID");
    final String recievedDatabaseBaseString = extras.getString("IntentdatabaseBaseId");
    mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();

    mServiceHandler.post(new Runnable() {
      @Override
      public void run() {
        LocationUtils.LocationResult locationResult = new LocationUtils.LocationResult() {
          @Override
          public void gotLocation(android.location.Location location) {
            final Session session = new Session();
            String tempusername = prefs.getString(USERNAME, null);
            if (tempusername == null){
              return;
            }
            session.setUser(tempusername);
            com.example.hao.myeta.Location locationObject = new com.example.hao.myeta.Location();
            locationObject.setLatitude(location.getLatitude());
            locationObject.setLongitude(location.getLongitude());
            session.setLocation(locationObject);

            mfirebaseDatabase.child(getString(R.string.session) + recievedDatabaseBaseString)
                .child(recievedSessionString)
                .setValue(session);

            double endLat = Double.longBitsToDouble(
                prefs.getLong(DESTINATION_LATITUDE, Double.doubleToLongBits(0)));
            double endLong = Double.longBitsToDouble(
                prefs.getLong(DESTINATION_LONGITUDE, Double.doubleToLongBits(0)));

            GoogleDirection.withServerKey("AIzaSyDaMLLRbHXqa1UB7U_dLXYnr6DuvTvaQYk")
                .from(new LatLng(location.getLatitude(), location.getLongitude()))
                .to(new LatLng(endLat, endLong))
                .avoid(AvoidType.FERRIES)
                .execute(new DirectionCallback() {
                  @Override
                  public void onDirectionSuccess(Direction direction, String rawBody) {
                    if(direction.isOK()) {
                      Map<String, Object> childUpdates = new HashMap<>();

                      TripInfo tripInfo = new TripInfo();
                      ArrayList<CustomLatLng> customDirectionList = new ArrayList<>();
                      Leg directionLeg = direction.getRouteList().get(0).getLegList().get(0);
                      String distance = directionLeg.getDistance().getText();
                      String duration = directionLeg.getDuration().getText();
                      String startAddress = directionLeg.getStartAddress().toString();
                      ArrayList<LatLng> originalDirections = directionLeg.getDirectionPoint();
                      int directionSize = directionLeg.getDirectionPoint().size();
                      if ( directionSize > 0){
                        for (LatLng L : originalDirections){
                          customDirectionList.add(new CustomLatLng(L.latitude, L.longitude));
                        }
                      }
                      tripInfo.setCustomDirectionList(customDirectionList);
                      tripInfo.setDistance(distance);
                      tripInfo.setDuration(duration);
                      tripInfo.setStartAddress(startAddress);
                      session.setTripInfo(tripInfo);
                      childUpdates.put("/tripInfo/", tripInfo);
                      mfirebaseDatabase.child(getString(R.string.session) + recievedDatabaseBaseString)
                          .child(recievedSessionString).updateChildren(childUpdates);

                      stopSelf();
                    } else {
                      stopSelf();
                    }
                  }

                  @Override
                  public void onDirectionFailure(Throwable t) {
                    stopSelf();
                  }
                });
          }
        };
        LocationUtils myLocation = new LocationUtils();
        myLocation.getLocation(getApplicationContext(), locationResult);
      }
    });
  return START_STICKY;
  };
}
