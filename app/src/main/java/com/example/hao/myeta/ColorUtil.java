package com.myeta;

import java.util.Random;

public class ColorUtil {
  private static int[] colorArray = {0xFF000000,0xFF888888,0xFFF44336,0xFF64FFDA,0xFF03A9F4,0xFFE65100, 0xFF00838F, 0xFFE040FB, 0xFF6200EA};

  public static int generateRandomColor(){
    Random rand = new Random();
    int  n = rand.nextInt(8);
    return colorArray[n];
  }
}
