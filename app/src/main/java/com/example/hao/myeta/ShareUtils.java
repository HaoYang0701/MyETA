package com.example.hao.myeta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import static com.example.hao.myeta.MainActivity.DATABASE_ID;

public class ShareUtils {
  public static String intentType = "text/plain";

  public static void createShareSessionIntent(Context context, SharedPreferences prefs) {
    String shareableSessionId = prefs.getString(DATABASE_ID, context.getString(R.string.nullValue));
    if (shareableSessionId.equals(context.getString(R.string.nullValue))){
      CharSequence text = context.getString(R.string.you_cannot_share);
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(context, text, duration);
      toast.show();
      return;
    }
    Intent sendIntent = new Intent();
    sendIntent.setType(intentType);
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT,
        String.format(context.getString(R.string.share_session), shareableSessionId));
    context.startActivity(sendIntent);
  }

  public static void createPlaystoreIntent(Context context) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType(intentType);
    i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.my_application_name));
    i.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_session_playstore));
    context.startActivity(i);
  }
}
