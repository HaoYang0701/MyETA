package com.example.hao.myeta;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class LocationAlarmManager {
  public static final long INTERVAL_FIVE_MINUTES = 5 * 60 * 1000;

  public void scheduleAlarm(Application application, String databaseBaseId, String sessionId) {
    Intent intent = new Intent(application, LocationUpdateAlarmReciever.class);
    Bundle extras = new Bundle();
    extras.putString("IntentSessionID", sessionId);
    extras.putString("IntentdatabaseBaseId", databaseBaseId);
    intent.putExtras(extras);
    final PendingIntent pIntent = PendingIntent.getBroadcast(application,
        LocationUpdateAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    long firstMillis = System.currentTimeMillis(); // alarm is set right away
    android.app.AlarmManager alarm = (android.app.AlarmManager) application.getSystemService(Context.ALARM_SERVICE);

    alarm.setInexactRepeating(android.app.AlarmManager.RTC_WAKEUP, firstMillis,
        INTERVAL_FIVE_MINUTES, pIntent);
  }

  public void cancelAlarm(Application application) {
    Intent intent = new Intent(application, LocationUpdateAlarmReciever.class);
    final PendingIntent pIntent = PendingIntent.getBroadcast(application,
        LocationUpdateAlarmReciever.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
    alarm.cancel(pIntent);
  }
}
