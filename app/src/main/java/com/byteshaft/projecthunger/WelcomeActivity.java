package com.byteshaft.projecthunger;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.byteshaft.projecthunger.utils.Helpers;

import java.text.ParseException;

import static com.byteshaft.projecthunger.MainActivity.selectedProjectType;

/**
 * Created by fi8er1 on 05/02/2017.
 */

public class WelcomeActivity extends FragmentActivity implements ImageButton.OnClickListener {

    ImageButton ibWelcomePizza;
    ImageButton ibWelcomeWings;
    ImageButton ibWelcomeBurger;
    ImageButton ibWelcomeTaco;
    ImageButton ibWelcomeLateNight;
    Animation fadeInAnimation;
    public static boolean isWelcomeActivityRunning;
    private static WelcomeActivity welcomeInstance;
    boolean visibility;
    public static WelcomeActivity getInstance() {
        return welcomeInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcomeInstance = this;
        ibWelcomePizza = (ImageButton) findViewById(R.id.ib_welcome_pizza);
        ibWelcomePizza.setOnClickListener(this);
        ibWelcomeWings = (ImageButton) findViewById(R.id.ib_welcome_wings);
        ibWelcomeWings.setOnClickListener(this);
        ibWelcomeBurger = (ImageButton) findViewById(R.id.ib_welcome_burger);
        ibWelcomeBurger.setOnClickListener(this);
        ibWelcomeTaco = (ImageButton) findViewById(R.id.ib_welcome_taco);
        ibWelcomeTaco.setOnClickListener(this);
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                ibWelcomeLateNight.setVisibility(View.VISIBLE);
                visibility = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        ibWelcomeLateNight = (ImageButton) findViewById(R.id.ib_welcome_late_night);
        ibWelcomeLateNight.setOnClickListener(this);

//        Helpers.isDeviceReadyForLocationAcquisition(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_welcome_pizza:
                selectedProjectType = 0;
                break;
            case R.id.ib_welcome_wings:
                selectedProjectType = 1;
                break;
            case R.id.ib_welcome_burger:
                selectedProjectType = 2;
                break;
            case R.id.ib_welcome_taco:
                selectedProjectType = 3;
                break;
            case R.id.ib_welcome_late_night:
                selectedProjectType = 4;
                break;
        }
        Helpers.loadActivity(WelcomeActivity.this, MainActivity.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isWelcomeActivityRunning = false;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isWelcomeActivityRunning = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isWelcomeActivityRunning) {
                    try {
                        setLateNightProjectButtonVisibility(Helpers.isCurrentTimeBetween("21:00:00", "05:00:00"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1500);
    }

    private void setLateNightProjectButtonVisibility(boolean lateNightActive) {
        if (lateNightActive) {
            ibWelcomeLateNight.startAnimation(fadeInAnimation);
        }
    }


}