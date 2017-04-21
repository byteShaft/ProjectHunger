package com.byteshaft.projecthunger.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.byteshaft.projecthunger.MainActivity;
import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.utils.DatabaseHelpers;
import com.byteshaft.projecthunger.utils.Helpers;

import static com.byteshaft.projecthunger.MainActivity.selectedProjectType;
import static com.byteshaft.projecthunger.utils.Helpers.loadFragment;

/**
 * Created by fi8er1 on 17/11/2016.
 */

public class ProjectFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    RelativeLayout rlFragmentProject;
    View baseViewWelcomeFragment;
    DatabaseHelpers mDatabaseHelpers;
    ImageView ivProjectTitle;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewWelcomeFragment = inflater.inflate(R.layout.fragment_project, container, false);

        rlFragmentProject = (RelativeLayout) baseViewWelcomeFragment.findViewById(R.id.fragment_project);
        rlFragmentProject.setBackgroundResource(getAppropriateBackground());

        ivProjectTitle = (ImageView) baseViewWelcomeFragment.findViewById(R.id.iv_project_title);
        ivProjectTitle.setBackgroundResource(getAppropriateTitleImage());

        btnMap = (Button) baseViewWelcomeFragment.findViewById(R.id.btn_main_map);
        btnFavorites = (Button) baseViewWelcomeFragment.findViewById(R.id.btn_main_favorites);
        btnRegister = (Button) baseViewWelcomeFragment.findViewById(R.id.btn_main_register);

        mDatabaseHelpers = new DatabaseHelpers(getActivity());

        btnMap.setOnClickListener(this);
        btnFavorites.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        MainActivity.getInstance().setTitle(Helpers.getAppropriateProjectName(MainActivity.selectedProjectType));
        return baseViewWelcomeFragment;
    }

    Button btnMap;
    Button btnFavorites;
    Button btnRegister;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_main_map:
                Helpers.onRecheckLocationAvailableTaskType = 0;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else if (!Helpers.checkPlayServicesAvailability(getActivity())) {
                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(), "Location components missing",
                            "You need to install GooglePlayServices to continue", "Install",
                            "Dismiss", Helpers.openPlayServicesInstallation);
                } else if (!Helpers.isAnyLocationServiceAvailable()) {
                    Helpers.AlertDialogWithPositiveNegativeNeutralFunctions(getActivity(), "Location Service disabled",
                            "Enable device GPS to continue", "Settings", "ReCheck", "Dismiss",
                            Helpers.openLocationServiceSettings, Helpers.recheckLocationServiceStatus);
                } else {
                    loadFragment(MainActivity.fragmentManager, new MapsFragment(), "MapsFragment");
                }
                break;
            case R.id.btn_main_favorites:
                if (!mDatabaseHelpers.isEmpty()) {
                    loadFragment(MainActivity.fragmentManager, new FavoritesFragment(), "FavoritesFragment");
                } else {
                    Helpers.AlertDialogMessage(getActivity(), "Favorites", "List is empty", "Ok");
                }
                break;
            case R.id.btn_main_register:
                loadFragment(MainActivity.fragmentManager, new RegisterFragment(), "RegisterFragment");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnMap.callOnClick();
                } else {
                    Toast.makeText(getActivity(), "Location access permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private int getAppropriateBackground() {
        int backgroundResourceId = 0;
        if (selectedProjectType == 0) {
            backgroundResourceId = R.mipmap.iv_background_pizza;
        } else if (selectedProjectType == 1) {
            backgroundResourceId = R.mipmap.iv_background_wings;
        } else if (selectedProjectType == 2) {
            backgroundResourceId = R.mipmap.iv_background_burger;
        } else if (selectedProjectType == 3) {
            backgroundResourceId = R.mipmap.iv_background_taco;
        }
        return backgroundResourceId;
    }

    private int getAppropriateTitleImage() {
        int titleResourceId = 0;
        if (selectedProjectType == 0) {
            titleResourceId = R.mipmap.iv_title_pizza;
        } else if (selectedProjectType == 1) {
            titleResourceId = R.mipmap.iv_title_wings;
        } else if (selectedProjectType == 2) {
            titleResourceId = R.mipmap.iv_title_burger;
        } else if (selectedProjectType == 3) {
            titleResourceId = R.mipmap.iv_title_taco;
        }
        return titleResourceId;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setTitle("");
    }
}
