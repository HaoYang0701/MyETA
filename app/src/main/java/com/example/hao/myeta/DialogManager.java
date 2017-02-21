package com.example.hao.myeta;

import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;

public class DialogManager {

  public void expandWindow(Dialog dialog) {
    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    Window window = dialog.getWindow();
    layoutParams.copyFrom(window.getAttributes());
    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    window.setAttributes(layoutParams);
  }
}
