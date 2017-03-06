package com.myeta;

import android.os.Handler;
import android.os.Looper;

public class MapsHandler extends Handler {
  private static MapsHandler mapsHandler;

  private MapsHandler(Looper mainLooper){
  }

  public static MapsHandler getInstance() {
    if (mapsHandler == null) {
      mapsHandler = new MapsHandler(Looper.getMainLooper());
    }
    return mapsHandler;
  }
}
