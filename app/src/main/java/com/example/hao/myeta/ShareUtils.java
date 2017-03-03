package com.example.hao.myeta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static com.example.hao.myeta.MainActivity.DATABASE_ID;

public class ShareUtils {

  public static void createShareSessionIntent(Context context, SharedPreferences prefs) {
    String shareableSessionId = prefs.getString(DATABASE_ID, context.getString(R.string.nullValue));
    Intent sendIntent = new Intent();
    sendIntent.setType("text/plain");
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.share_session), shareableSessionId));
    context.startActivity(sendIntent);
  }

  public static void createPlaystoreIntent(Context context) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("text/plain");
    i.putExtra(Intent.EXTRA_SUBJECT, "My application name");
    String sAux = "\nLet me recommend you this application\n\n";
    sAux = sAux + "https://play.google.com/store/apps/details?id=Orion.Soft \n\n";
    i.putExtra(Intent.EXTRA_TEXT, sAux);
    context.startActivity(i);
  }
}
