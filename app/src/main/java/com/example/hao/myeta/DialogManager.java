package com.example.hao.myeta;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
