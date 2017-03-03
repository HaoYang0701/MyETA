package com.example.hao.myeta;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import static com.example.hao.myeta.MainActivity.MAP_UPDATE_TIMER;

public class LocationAlarmManager {
  public static final long INTERVAL_FIVE_MINUTES = 5 * 60 * 1000;
  public static final long TURN_OFF_TIMER = 60 * 60 * 1000;
  private static SharedPreferences prefs;

  public void scheduleAlarm(Application application, String databaseBaseId, String sessionId) {
    prefs = application.getSharedPreferences("com.example.hao.myeta", Context.MODE_PRIVATE);
    Intent intent = new Intent(application, LocationUpdateAlarmReciever.class);
    Bundle extras = new Bundle();
    extras.putString("IntentSessionID", sessionId);
    extras.putString("IntentdatabaseBaseId", databaseBaseId);
    intent.putExtras(extras);
    final PendingIntent pIntent = PendingIntent.getBroadcast(application,
        LocationUpdateAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    long firstMillis = System.currentTimeMillis(); // alarm is set right away
    android.app.AlarmManager alarm = (android.app.AlarmManager) application.getSystemService(Context.ALARM_SERVICE);

    final long updateInterval = prefs.getLong(MAP_UPDATE_TIMER, INTERVAL_FIVE_MINUTES);

    alarm.setInexactRepeating(android.app.AlarmManager.RTC_WAKEUP, firstMillis,
        updateInterval, pIntent);
  }

  public void setAlarmTurnOffTimer(){

  }

  public void cancelAlarm(Application application) {
    Intent intent = new Intent(application, LocationUpdateAlarmReciever.class);
    final PendingIntent pIntent = PendingIntent.getBroadcast(application,
        LocationUpdateAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
    alarm.cancel(pIntent);
  }
}
