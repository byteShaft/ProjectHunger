package com.byteshaft.projecthunger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.byteshaft.projecthunger.utils.Helpers;

import static com.byteshaft.projecthunger.MainActivity.selectedProjectType;

/**
 * Created by fi8er1 on 05/02/2017.
 */

public class WelcomeActivity extends Activity implements ImageButton.OnClickListener {

    ImageButton ibWelcomePizza;
    ImageButton ibWelcomeWings;
    ImageButton ibWelcomeBurger;
    ImageButton ibWelcomeTaco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ibWelcomePizza = (ImageButton) findViewById(R.id.ib_welcome_pizza);
        ibWelcomePizza.setOnClickListener(this);
        ibWelcomeWings = (ImageButton) findViewById(R.id.ib_welcome_wings);
        ibWelcomeWings.setOnClickListener(this);
        ibWelcomeBurger = (ImageButton) findViewById(R.id.ib_welcome_burger);
        ibWelcomeBurger.setOnClickListener(this);
        ibWelcomeTaco = (ImageButton) findViewById(R.id.ib_welcome_taco);
        ibWelcomeTaco.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_welcome_pizza:
                selectedProjectType = 0;
                Helpers.loadActivity(WelcomeActivity.this, MainActivity.class);
                break;
            case R.id.ib_welcome_wings:
                selectedProjectType = 1;
                Helpers.loadActivity(WelcomeActivity.this, MainActivity.class);
                break;
            case R.id.ib_welcome_burger:
                selectedProjectType = 2;
                Helpers.loadActivity(WelcomeActivity.this, MainActivity.class);
                break;
            case R.id.ib_welcome_taco:
                selectedProjectType = 3;
                Helpers.loadActivity(WelcomeActivity.this, MainActivity.class);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
