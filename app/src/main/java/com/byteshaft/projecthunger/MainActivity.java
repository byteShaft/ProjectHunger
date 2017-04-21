package com.byteshaft.projecthunger;

import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.byteshaft.projecthunger.fragments.ProjectFragment;
import com.byteshaft.projecthunger.utils.AppGlobals;
import com.byteshaft.projecthunger.utils.Helpers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static MainActivity sInstance;
    public static boolean isMainActivityRunning;
    public static FragmentManager fragmentManager;
    public static int selectedProjectType = 0;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        sInstance = this;
        fragmentManager = getSupportFragmentManager();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        RelativeLayout header = (RelativeLayout) headerView.findViewById(R.id.nav_header);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Helpers.loadActivity(MainActivity.this, WelcomeActivity.class);
                    }
                }, 250);
            }
        });

        navigationView.getMenu().getItem(selectedProjectType).setChecked(true);
        navigationView.setItemIconTintList(null);
        onNavigationItemSelected(navigationView.getMenu().getItem(selectedProjectType));
        setTitle(Helpers.getAppropriateProjectName(selectedProjectType));

        if (AppGlobals.getUniqueDeviceId() == null) {
            AppGlobals.putUniqueDeviceID(Settings.Secure.getString(this.getContentResolver(),
                            Settings.Secure.ANDROID_ID));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pepperoni) {
            selectedProjectType = 0;
        } else if (id == R.id.nav_wings) {
            selectedProjectType = 1;
        } else if (id == R.id.nav_burger) {
            selectedProjectType = 2;
        } else if (id == R.id.nav_taco) {
            selectedProjectType = 3;
        }

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Helpers.loadFragment(fragmentManager, new ProjectFragment(), null);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        setTitle(Helpers.getAppropriateProjectName(MainActivity.selectedProjectType));
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMainActivityRunning = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isMainActivityRunning = true;
    }

}
