package com.example.hao.myeta;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class syncHandler extends Handler {
  Looper looper;

  public syncHandler(Looper looper) {
    super(looper);
    this.looper = looper;
  }

  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);

  }
}
