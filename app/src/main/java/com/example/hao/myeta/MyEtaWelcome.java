package com.myeta;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class MyEtaWelcome extends WelcomeActivity{
  @Override
  protected WelcomeConfiguration configuration() {
    return new WelcomeConfiguration.Builder(this)
        .defaultTitleTypefacePath("Montserrat-Bold.ttf")
        .defaultHeaderTypefacePath("Montserrat-Bold.ttf")

        .page(new BasicPage(R.drawable.applaunchicon,
            "Welcome",
            "MyEta is an app that lets you share your location and estimated time of arrival with friends")
            .background(R.color.colorLight)
        )

        .page(new BasicPage(R.drawable.welcomecreatesession,
            "Quick and simple to use",
            "Create your own session or join an existing session")
            .background(R.color.colorGreenLight)
        )

        .page(new BasicPage(R.drawable.welcomescreenshare,
            "Easy to share",
            "Share your session Id with friends so they can join you in one click")
            .background(R.color.colorAccentLight)
        )

        .swipeToDismiss(true)
        .exitAnimation(android.R.anim.fade_out)
        .build();
  }

  public static String welcomeKey() {
    return "WelcomeScreen";
  }

}
