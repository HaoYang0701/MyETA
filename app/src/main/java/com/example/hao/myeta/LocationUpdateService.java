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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.hao.myeta.MainActivity.destinationLat;
import static com.example.hao.myeta.MainActivity.destinationLong;
import static com.example.hao.myeta.MainActivity.username;

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
            Session session = new Session();
            session.setUser(prefs.getString(username, "Placeholder"));
            com.example.hao.myeta.Location locationObject = new com.example.hao.myeta.Location();
            locationObject.setLatitude(location.getLatitude());
            locationObject.setLongitude(location.getLongitude());
            session.setLocation(locationObject);

            mfirebaseDatabase.child(getString(R.string.session) + recievedDatabaseBaseString)
                .child(recievedSessionString)
                .setValue(session);

            double endLat = Double.longBitsToDouble(
                prefs.getLong(destinationLat, Double.doubleToLongBits(0)));
            double endLong = Double.longBitsToDouble(
                prefs.getLong(destinationLong, Double.doubleToLongBits(0)));

            GoogleDirection.withServerKey("AIzaSyDaMLLRbHXqa1UB7U_dLXYnr6DuvTvaQYk")
                .from(new LatLng(location.getLatitude(), location.getLongitude()))
                .to(new LatLng(endLat, endLong))
                .avoid(AvoidType.FERRIES)
                .execute(new DirectionCallback() {
                  @Override
                  public void onDirectionSuccess(Direction direction, String rawBody) {
                    if(direction.isOK()) {
                      Map<String, Object> childUpdates = new HashMap<>();
                      ArrayList<CustomLatLng> customDirectionList = new ArrayList<>();

                      ArrayList<LatLng> originalDirections = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                      int directionSize = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint().size();
                      if ( directionSize > 0){
                        for (LatLng L : originalDirections){
                          customDirectionList.add(new CustomLatLng(L.latitude, L.longitude));
                        }
                      }
                      childUpdates.put("/stepArrayList/", customDirectionList);
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
