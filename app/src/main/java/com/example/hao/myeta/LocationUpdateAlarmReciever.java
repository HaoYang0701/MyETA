package com.example.hao.myeta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class LocationUpdateAlarmReciever extends BroadcastReceiver {
  public static int REQUEST_CODE = 1;

  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle extras = intent.getExtras();
    String recievedSessionString = extras.getString("IntentSessionID");
    String recievedDatabaseBaseString = extras.getString("IntentdatabaseBaseId");
    Intent i = new Intent(context, LocationUpdateService.class);

    Bundle newExtra = new Bundle();
    newExtra.putString("IntentSessionID", recievedSessionString);
    newExtra.putString("IntentdatabaseBaseId", recievedDatabaseBaseString);
    i.putExtras(extras);
    context.startService(i);
  }
}
