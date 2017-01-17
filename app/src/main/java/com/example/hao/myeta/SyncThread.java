package com.example.hao.myeta;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class SyncThread extends HandlerThread {
  Handler handler;

  public SyncThread(String myHandler) {
    super(myHandler);
  }

  @Override
  protected void onLooperPrepared() {
    super.onLooperPrepared();
    handler = new syncHandler(Looper.getMainLooper());
  }

  public void postTask(Runnable runnable){
    handler.post(runnable);
  }
}
